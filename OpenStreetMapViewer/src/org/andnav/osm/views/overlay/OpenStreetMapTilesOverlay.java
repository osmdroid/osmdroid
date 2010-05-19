package org.andnav.osm.views.overlay;

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.MyMath;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class OpenStreetMapTilesOverlay extends OpenStreetMapViewOverlay {

	protected OpenStreetMapView mOsmv;
	protected OpenStreetMapRendererInfo mRendererInfo;

	/** Current renderer */
	protected final OpenStreetMapTileProvider mTileProvider;
	protected final Paint mPaint = new Paint();

	public OpenStreetMapTilesOverlay(final OpenStreetMapView aOsmv,
			final OpenStreetMapRendererInfo aRendererInfo,
			final OpenStreetMapTileProvider aTileProvider) {
		this.mOsmv = aOsmv;
		this.mRendererInfo = aRendererInfo;
		if(aTileProvider == null)
			mTileProvider = OpenStreetMapTileProvider.getInstance(mOsmv.getContext(), new SimpleInvalidationHandler());
		else
			this.mTileProvider = aTileProvider;
	}
	
	public void detach()
	{
		this.mTileProvider.detach();		
	}
	
	public OpenStreetMapRendererInfo getRendererInfo() {
		return mRendererInfo;
	}
	
	public void setRendererInfo(final OpenStreetMapRendererInfo aRendererInfo) {
		this.mRendererInfo = aRendererInfo;
		// XXX perhaps we should set the cache capacity back to default here
	}

	public void setAlpha(int a) {
		this.mPaint.setAlpha(a);
	}
	
	@Override
	protected void onDraw(Canvas c, OpenStreetMapView osmv) {

		if(DEBUGMODE)
			Log.v(DEBUGTAG, "onDraw");

		/*
		 * Do some calculations and drag attributes to local variables to save
		 * some performance.
		 */
		final OpenStreetMapViewProjection pj = osmv.getProjection();
		final int zoomLevel = osmv.getZoomLevel();
		final Rect viewPort = c.getClipBounds();
		final int tileSizePx = this.mRendererInfo.MAPTILE_SIZEPX;
		final int tileZoom = this.mRendererInfo.MAPTILE_ZOOM;
		final int worldSize_2 = 1 << (zoomLevel + this.mRendererInfo.MAPTILE_ZOOM - 1);
		final int rendererId = this.mRendererInfo.ordinal();	// TODO get from service
		
		/*
		 * Calculate the amount of tiles needed for each side around the center 
		 * one.
		 */
		viewPort.offset(worldSize_2, worldSize_2);
		final int tileNeededToLeftOfCenter = (viewPort.left >> tileZoom) - 1;
		final int tileNeededToRightOfCenter = viewPort.right >> tileZoom;
		final int tileNeededToTopOfCenter = (viewPort.top >> tileZoom) - 1;
		final int tileNeededToBottomOfCenter = viewPort.bottom >> tileZoom;

		final int mapTileUpperBound = 1 << zoomLevel;
		final Point tilePos = new Point();

		// make sure the cache is big enough for all the tiles
		final int numNeeded = (tileNeededToBottomOfCenter - tileNeededToTopOfCenter + 1)
							* (tileNeededToRightOfCenter - tileNeededToLeftOfCenter + 1);
		mTileProvider.ensureCapacity(numNeeded);
		
		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = tileNeededToTopOfCenter; y <= tileNeededToBottomOfCenter; y++) {
			for (int x = tileNeededToLeftOfCenter; x <= tileNeededToRightOfCenter; x++) {
				/* Construct a URLString, which represents the MapTile. */
				final int tileY = MyMath.mod(y, mapTileUpperBound);
				final int tileX = MyMath.mod(x, mapTileUpperBound);
				final OpenStreetMapTile tile = new OpenStreetMapTile(rendererId, zoomLevel, tileX, tileY);

				pj.toPixels(x, y, tilePos);
				final Bitmap currentMapTile = mTileProvider.getMapTile(tile);
				if (currentMapTile != null) {
					c.drawBitmap(currentMapTile, tilePos.x, tilePos.y, mPaint);
				}

				if (DEBUGMODE) {
					c.drawText(tile.toString(), tilePos.x + 1, tilePos.y + mPaint.getTextSize(), mPaint);
					c.drawLine(tilePos.x, tilePos.y, tilePos.x + tileSizePx, tilePos.y, mPaint);
					c.drawLine(tilePos.x, tilePos.y, tilePos.x, tilePos.y + tileSizePx, mPaint);
				}
			}
		}
		
		// draw a cross at center in debug mode
		if (DEBUGMODE) {
			final GeoPoint center = osmv.getMapCenter();
			final Point centerPoint = pj.toMapPixels(center, null);
			c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9, mPaint);
			c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y, mPaint);
		}

	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
	}

	private class SimpleInvalidationHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case OpenStreetMapTile.MAPTILE_SUCCESS_ID:
					mOsmv.invalidate();
					break;
			}
		}
	}
}

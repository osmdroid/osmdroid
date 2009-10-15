package org.andnav.osm.views.overlay;

import org.andnav.osm.util.MyMath;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileDownloader;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

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
			mTileProvider = new OpenStreetMapTileProvider(mOsmv.getContext(), new SimpleInvalidationHandler());
		else
			this.mTileProvider = aTileProvider;
	}
	
	public OpenStreetMapRendererInfo getRendererInfo() {
		return mRendererInfo;
	}
	
	public void setRendererInfo(final OpenStreetMapRendererInfo aRendererInfo) {
		this.mRendererInfo = aRendererInfo;
	}

	public void setAlpha(int a) {
		this.mPaint.setAlpha(a);
	}
	
	@Override
	protected void onDraw(Canvas c, OpenStreetMapView osmv) {
		/*
		 * Do some calculations and drag attributes to local variables to save
		 * some performance.
		 */
		final int zoomLevel = osmv.getZoomLevel();
		final Rect viewPort = c.getClipBounds();
		final int viewWidth_2 = viewPort.width()/2;
		final int viewHeight_2 = viewPort.height()/2;
		final int tileSizePx = this.mRendererInfo.MAPTILE_SIZEPX;
		final int tileZoom = this.mRendererInfo.MAPTILE_ZOOM;
		final int worldTiles_2 = 1 << (zoomLevel-1);	// TODO make compatible for zoomLevel == 0
		final int worldSize_2 = 1 << (zoomLevel + this.mRendererInfo.MAPTILE_ZOOM - 1);
		
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
		final int[] mapTileCoords = new int[2];

		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = tileNeededToTopOfCenter; y <= tileNeededToBottomOfCenter; y++) {
			for (int x = tileNeededToLeftOfCenter; x <= tileNeededToRightOfCenter; x++) {
				/*
				 * Add/substract the difference of the tile-position to the one
				 * of the center.
				 */
				mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] = MyMath.mod(y, mapTileUpperBound);
				mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] = MyMath.mod(x, mapTileUpperBound);
				/* Construct a URLString, which represents the MapTile. */
				final String tileURLString = this.mRendererInfo.getTileURLString(mapTileCoords,
						zoomLevel);

				/* Draw the MapTile 'i tileSizePx' above of the centerMapTile */
				final Bitmap currentMapTile = this.mTileProvider.getMapTile(tileURLString);
				final int tileLeft = viewWidth_2 + (x - worldTiles_2) * tileSizePx;
				final int tileTop = viewHeight_2 + (y - worldTiles_2) * tileSizePx;
				c.drawBitmap(currentMapTile, tileLeft, tileTop, this.mPaint);

				if (OpenStreetMapViewConstants.DEBUGMODE) {
					c.drawLine(tileLeft, tileTop, tileLeft + tileSizePx, tileTop, this.mPaint);
					c.drawLine(tileLeft, tileTop, tileLeft, tileTop + tileSizePx, this.mPaint);
				}
			}
		}
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
	}

	private class SimpleInvalidationHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID:
				case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
					mOsmv.invalidate();
					break;
			}
		}
	}
}

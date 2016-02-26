package org.osmdroid.google.overlay;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.MyMath;
import org.osmdroid.views.util.Mercator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import org.osmdroid.thirdparty.Constants;

/**
 * This class represents an OSM Tiles Overlay at a Google Map. It is a copy from
 * the class org.osmdroid.views.overlay.TilesOverlay (r789) (formerly known as
 * org.andnav.osm.views.overlay.OpenStreetMapTilesOverlay) and modified to make
 * it working.
 *
 */
public class GoogleTilesOverlay extends Overlay {

	private static final boolean DEBUGMODE = false;

	/** Current tile source */
	protected final MapTileProviderBase mTileProvider;

	/* to avoid allocations during draw */
	protected final Paint mPaint = new Paint();
	private final Rect mTileRect = new Rect();
	private final Point mTilePos = new Point();

	public GoogleTilesOverlay(final MapTileProviderBase aTileProvider, final Context aContext) {
		this(aTileProvider);

	}

	public GoogleTilesOverlay(final MapTileProviderBase aTileProvider) {
		// Original line in org.osmdroid.views.overlay.TilesOverlay.java
		// super(pResourceProxy);
		if (aTileProvider == null) {
			throw new IllegalArgumentException("You must pass a valid tile provider to the tiles overlay.");
		}

		this.mTileProvider = aTileProvider;
	}

	public void setAlpha(final int a) {
		this.mPaint.setAlpha(a);
	}

	public int getMinimumZoomLevel() {
		return mTileProvider.getMinimumZoomLevel();
	}

	public int getMaximumZoomLevel() {
		return mTileProvider.getMaximumZoomLevel();
	}

	/**
	 * Whether to use the network connection if it's available.
	 */
	public boolean useDataConnection() {
		return mTileProvider.useDataConnection();
	}

	/**
	 * Set whether to use the network connection if it's available.
	 *
	 * @param aMode
	 *            if true use the network connection if it's available. if false
	 *            don't use the network connection even if it's available.
	 */
	public void setUseDataConnection(final boolean aMode) {
		mTileProvider.setUseDataConnection(aMode);
	}

	@Override
	public void draw(final Canvas c, final MapView osmv, final boolean shadow) {
		if (DEBUGMODE) {
               Log.d(Constants.LOGTAG, "draw");
		}

		// Calculate the half-world size
		final Projection pj = osmv.getProjection();
		final int zoomLevel = osmv.getZoomLevel() - 1;
		final int tileSizePx = this.mTileProvider.getTileSource().getTileSizePixels();

		// Calculate the tiles needed for each side around the center one.
		final int latSpan = osmv.getLatitudeSpan();
		final int longSpan = osmv.getLongitudeSpan();
		final int topLatitude = osmv.getMapCenter().getLatitudeE6() + latSpan/2;
		final int leftLongitude = osmv.getMapCenter().getLongitudeE6() - longSpan/2;
		final int bottomLatitude = osmv.getMapCenter().getLatitudeE6() - latSpan/2;
		final int rightLongitude = osmv.getMapCenter().getLongitudeE6() + longSpan/2;
		final Point leftTopXY = Mercator.projectGeoPoint(topLatitude*1E-6, leftLongitude*1E-6, zoomLevel, new Point(0,0));
		final Point rightBottomXY = Mercator.projectGeoPoint(bottomLatitude*1E-6, rightLongitude*1E-6, zoomLevel, new Point(0,0));
		final int tileNeededAtLeft = leftTopXY.x;
		final int tileNeededAtRight = rightBottomXY.x;
		final int tileNeededAtTop = leftTopXY.y;
		final int tileNeededAtBottom = rightBottomXY.y;

		final int mapTileUpperBound = 1 << zoomLevel;
		// make sure the cache is big enough for all the tiles
		final int numNeeded = (tileNeededAtBottom - tileNeededAtTop + 1)
				* (tileNeededAtRight - tileNeededAtLeft + 1);
		mTileProvider.ensureCapacity(numNeeded);
		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = tileNeededAtTop; y <= tileNeededAtBottom; y++) {
			for (int x = tileNeededAtLeft; x <= tileNeededAtRight; x++) {
				// Construct a MapTile to request from the tile provider.
				final int tileY = MyMath.mod(y, mapTileUpperBound);
				final int tileX = MyMath.mod(x, mapTileUpperBound);
				final MapTile tile = new MapTile(zoomLevel, tileX, tileY);
				final Drawable currentMapTile = mTileProvider.getMapTile(tile);
				if (currentMapTile != null) {
					final GeoPoint gp = new GeoPoint(
							(int) (Mercator.tile2lat(y, zoomLevel) * 1E6),
							(int) (Mercator.tile2lon(x, zoomLevel) * 1E6));
					pj.toPixels(gp, mTilePos);
					mTileRect.set(mTilePos.x, mTilePos.y, mTilePos.x + tileSizePx, mTilePos.y + tileSizePx);
					currentMapTile.setBounds(mTileRect);
					currentMapTile.draw(c);
				}

				if (DEBUGMODE) {
					c.drawText(tile.toString(), mTileRect.left + 1, mTileRect.top + mPaint.getTextSize(), mPaint);
					c.drawLine(mTileRect.left, mTileRect.top, mTileRect.right, mTileRect.top, mPaint);
					c.drawLine(mTileRect.left, mTileRect.top, mTileRect.left, mTileRect.bottom, mPaint);
				}
			}
		}

		// draw a cross at center in debug mode
		if (DEBUGMODE) {
			final GeoPoint center = osmv.getMapCenter();
			final Point centerPoint = pj.toPixels(center, null);
			c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9, mPaint);
			c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y, mPaint);
		}
	}
}

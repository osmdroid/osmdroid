package org.osmdroid.views.overlay;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.MyMath;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * These objects are the principle consumer of map tiles.
 * 
 * see {@link MapTile} for an overview of how tiles are acquired by this overlay.
 * 
 */

public class TilesOverlay extends Overlay {

	private static final Logger logger = LoggerFactory.getLogger(TilesOverlay.class);

	/** Current tile source */
	protected final MapTileProviderBase mTileProvider;
	protected final Paint mPaint = new Paint();

	/* to avoid allocations during draw */
	private final Rect mTileRect = new Rect();
	private final Rect mViewPort = new Rect();

	private int mWorldSize_2;

	public TilesOverlay(final MapTileProviderBase aTileProvider, final Context aContext) {
		this(aTileProvider, new DefaultResourceProxyImpl(aContext));
	}

	public TilesOverlay(final MapTileProviderBase aTileProvider, final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
		this.mTileProvider = aTileProvider; // TODO check for null
	}

	public void detach() {
		this.mTileProvider.detach();
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
	 *            if true use the network connection if it's available. if false don't use the
	 *            network connection even if it's available.
	 */
	public void setUseDataConnection(final boolean aMode) {
		mTileProvider.setUseDataConnection(aMode);
	}

	@Override
	protected void onDraw(final Canvas c, final MapView osmv) {

		if (DEBUGMODE)
			logger.trace("onDraw");

		// Calculate the half-world size
		final Projection pj = osmv.getProjection();
		final int zoomLevel = pj.getZoomLevel();
		final int tileZoom = pj.getTileMapZoom();
		mWorldSize_2 = 1 << (zoomLevel + tileZoom - 1);

		// Get the area we are drawing to
		c.getClipBounds(mViewPort);

		// Translate the Canvas coordinates into Mercator coordinates
		mViewPort.offset(mWorldSize_2, mWorldSize_2);

		// Draw the tiles!
		drawTiles(c, pj.getZoomLevel(), pj.getTileSizePixels(), mViewPort);
	}

	/**
	 * This is meant to be a "pure" tile drawing function that doesn't take into account
	 * platform-specific characteristics (like Android's canvas's having 0,0 as the center rather
	 * than the upper-left corner).
	 */
	public void drawTiles(final Canvas c, final int zoomLevel, final int tileSizePx,
			final Rect viewPort) {

		final int tileZoom = MapView.getMapTileZoom(tileSizePx);

		/*
		 * Calculate the amount of tiles needed for each side around the center one.
		 */
		final int tileNeededToLeftOfCenter = (viewPort.left >> tileZoom) - 1;
		final int tileNeededToRightOfCenter = viewPort.right >> tileZoom;
		final int tileNeededToTopOfCenter = (viewPort.top >> tileZoom) - 1;
		final int tileNeededToBottomOfCenter = viewPort.bottom >> tileZoom;

		final int mapTileUpperBound = 1 << zoomLevel;

		// make sure the cache is big enough for all the tiles
		final int numNeeded = (tileNeededToBottomOfCenter - tileNeededToTopOfCenter + 1)
				* (tileNeededToRightOfCenter - tileNeededToLeftOfCenter + 1);
		mTileProvider.ensureCapacity(numNeeded);

		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = tileNeededToTopOfCenter; y <= tileNeededToBottomOfCenter; y++) {
			for (int x = tileNeededToLeftOfCenter; x <= tileNeededToRightOfCenter; x++) {
				// Construct a MapTile to request from the tile provider.
				final int tileY = MyMath.mod(y, mapTileUpperBound);
				final int tileX = MyMath.mod(x, mapTileUpperBound);
				final MapTile tile = new MapTile(zoomLevel, tileX, tileY);

				final Drawable currentMapTile = mTileProvider.getMapTile(tile);
				if (currentMapTile != null) {
					mTileRect.set(x * tileSizePx, y * tileSizePx, x * tileSizePx + tileSizePx, y
							* tileSizePx + tileSizePx);
					onTileReadyToDraw(c, currentMapTile, mTileRect);
				}

				if (DEBUGMODE) {
					mTileRect.set(x * tileSizePx, y * tileSizePx, x * tileSizePx + tileSizePx, y
							* tileSizePx + tileSizePx);
					c.drawText(tile.toString(), mTileRect.left + 1,
							mTileRect.top + mPaint.getTextSize(), mPaint);
					c.drawLine(mTileRect.left, mTileRect.top, mTileRect.right, mTileRect.top,
							mPaint);
					c.drawLine(mTileRect.left, mTileRect.top, mTileRect.left, mTileRect.bottom,
							mPaint);
				}
			}
		}

		// draw a cross at center in debug mode
		if (DEBUGMODE) {
			// final GeoPoint center = osmv.getMapCenter();
			final Point centerPoint = new Point(viewPort.centerX() - mWorldSize_2,
					viewPort.centerY() - mWorldSize_2);
			c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9, mPaint);
			c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y, mPaint);
		}

	}

	protected void onTileReadyToDraw(final Canvas c, final Drawable currentMapTile,
			final Rect tileRect) {
		tileRect.offset(-mWorldSize_2, -mWorldSize_2);
		currentMapTile.setBounds(tileRect);
		currentMapTile.draw(c);
	}

	@Override
	protected void onDrawFinished(final Canvas c, final MapView osmv) {
	}
}

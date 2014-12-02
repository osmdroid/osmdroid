package org.osmdroid.views.overlay;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;

/**
 * Draws a mini-map as an overlay layer. It currently uses its own MapTileProviderBasic or a tile
 * provider supplied to it. Do NOT share a tile provider amongst multiple tile drawing overlays - it
 * will create an under-sized cache.
 * 
 * @author Marc Kurtz
 * 
 */
public class MinimapOverlay extends TilesOverlay {

	private int mWidth = 100;
	private int mHeight = 100;
	private int mPadding = 10;
	private int mZoomDifference;
	private final Paint mPaint;

	// The Mercator coordinates of the map tile area we are interested in the target zoom level
	final private Rect mTileArea = new Rect();

	// The Canvas coordinates where the minimap should be drawn
	final private Rect mMiniMapCanvasRect = new Rect();

	// Stores the intersection of the minimap and the Canvas clipping area
	final private Rect mIntersectionRect = new Rect();
	private Projection mProjection;

	// Prevent allocations during draw
	private Point mTopLeftMercator = new Point();
	private Point mBottomRightMercator = new Point();

	/**
	 * Creates a {@link MinimapOverlay} with the supplied tile provider. The {@link Handler} passed
	 * in is typically the same handler being used by the main map. The {@link MapTileProviderBase}
	 * passed in cannot be the same tile provider used in the {@link TilesOverlay}, it must be a new
	 * instance.
	 * 
	 * @param pContext
	 *            a context
	 * @param pTileRequestCompleteHandler
	 *            a handler for the tile request complete notifications
	 * @param pTileProvider
	 *            a tile provider
	 */
	public MinimapOverlay(final Context pContext, final Handler pTileRequestCompleteHandler,
			final MapTileProviderBase pTileProvider, final int pZoomDifference) {
		super(pTileProvider, pContext);
		setZoomDifference(pZoomDifference);

		mTileProvider.setTileRequestCompleteHandler(pTileRequestCompleteHandler);

		// Don't draw loading lines in the minimap
		setLoadingLineColor(getLoadingBackgroundColor());

		// Scale the default size
		final float density = pContext.getResources().getDisplayMetrics().density;
		mWidth *= density;
		mHeight *= density;

		mPaint = new Paint();
		mPaint.setColor(Color.GRAY);
		mPaint.setStyle(Style.FILL);
		mPaint.setStrokeWidth(2);
	}

	/**
	 * Creates a {@link MinimapOverlay} with the supplied tile provider. The {@link Handler} passed
	 * in is typically the same handler being used by the main map. The {@link MapTileProviderBase}
	 * passed in cannot be the same tile provider used in the {@link TilesOverlay}, it must be a new
	 * instance.
	 * 
	 * @param pContext
	 *            a context
	 * @param pTileRequestCompleteHandler
	 *            a handler for the tile request complete notifications
	 * @param pTileProvider
	 *            a tile provider
	 */
	public MinimapOverlay(final Context pContext, final Handler pTileRequestCompleteHandler,
			final MapTileProviderBase pTileProvider) {
		this(pContext, pTileRequestCompleteHandler, pTileProvider,
				DEFAULT_ZOOMLEVEL_MINIMAP_DIFFERENCE);
	}

	/**
	 * Creates a {@link MinimapOverlay} that uses its own {@link MapTileProviderBasic}. The
	 * {@link Handler} passed in is typically the same handler being used by the main map.
	 * 
	 * @param pContext
	 *            a context
	 * @param pTileRequestCompleteHandler
	 *            a handler for tile request complete notifications
	 */
	public MinimapOverlay(final Context pContext, final Handler pTileRequestCompleteHandler) {
		this(pContext, pTileRequestCompleteHandler, new MapTileProviderBasic(pContext));
	}

	public void setTileSource(final ITileSource pTileSource) {
		mTileProvider.setTileSource(pTileSource);
	}

	public int getZoomDifference() {
		return mZoomDifference;
	}

	public void setZoomDifference(final int zoomDifference) {
		mZoomDifference = zoomDifference;
	}

	@Override
	protected void draw(Canvas c, MapView osmv, boolean shadow) {
		if (shadow) {
			return;
		}

		// Don't draw if we are animating
		if (osmv.isAnimating()) {
			return;
		}

		mProjection = osmv.getProjection();
		final int zoomLevel = mProjection.getZoomLevel();

		// Save the Mercator coordinates of what is on the screen
		Rect screenRect = mProjection.getScreenRect();

		mProjection.toMercatorPixels(screenRect.left, screenRect.top, mTopLeftMercator);
		mProjection.toMercatorPixels(screenRect.right, screenRect.bottom, mBottomRightMercator);
		mTileArea.set(mTopLeftMercator.x, mTopLeftMercator.y, mBottomRightMercator.x,
				mBottomRightMercator.y);

		// Get the target zoom level difference.
		int miniMapZoomLevelDifference = getZoomDifference();

		// Make sure the zoom level difference isn't below the minimum zoom level
		if (zoomLevel - getZoomDifference() < mTileProvider.getMinimumZoomLevel()) {
			miniMapZoomLevelDifference += zoomLevel - getZoomDifference()
					- mTileProvider.getMinimumZoomLevel();
		}

		// Shift the screen coordinates into the target zoom level
		mTileArea.set(mTileArea.left >> miniMapZoomLevelDifference,
				mTileArea.top >> miniMapZoomLevelDifference,
				mTileArea.right >> miniMapZoomLevelDifference,
				mTileArea.bottom >> miniMapZoomLevelDifference);

		// Limit the area we are interested in for tiles to be the MAP_WIDTH by MAP_HEIGHT and
		// centered on the center of the screen
		mTileArea.set(mTileArea.centerX() - (getWidth() / 2), mTileArea.centerY()
				- (getHeight() / 2), mTileArea.centerX() + (getWidth() / 2), mTileArea.centerY()
				+ (getHeight() / 2));

		// Get the area where we will draw the minimap in screen coordinates
		mMiniMapCanvasRect.set(screenRect.right - getPadding() - getWidth(), screenRect.bottom
				- getPadding() - getHeight(), screenRect.right - getPadding(), screenRect.bottom
				- getPadding());

		// Draw a solid background where the minimap will be drawn with a 2 pixel inset
		c.drawRect(mMiniMapCanvasRect.left - 2, mMiniMapCanvasRect.top - 2,
				mMiniMapCanvasRect.right + 2, mMiniMapCanvasRect.bottom + 2, mPaint);

		super.drawTiles(c, mProjection, mProjection.getZoomLevel() - miniMapZoomLevelDifference,
				TileSystem.getTileSize(), mTileArea);
	}

	@Override
	protected void onTileReadyToDraw(final Canvas c, final Drawable currentMapTile,
			final Rect tileRect) {

		// Get the offsets for where to draw the tiles relative to where the minimap is located
		final int xOffset = (tileRect.left - mTileArea.left) + (mMiniMapCanvasRect.left);
		final int yOffset = (tileRect.top - mTileArea.top) + (mMiniMapCanvasRect.top);

		// Set the drawable's location
		currentMapTile.setBounds(xOffset, yOffset, xOffset + tileRect.width(),
				yOffset + tileRect.height());

		// Save the current clipping bounds
		c.save();
		// Check to see if the drawing area intersects with the minimap area
		if (mIntersectionRect.setIntersect(c.getClipBounds(), mMiniMapCanvasRect)) {
			// If so, then clip that area
			c.clipRect(mIntersectionRect);

			// Draw the tile, which will be appropriately clipped
			currentMapTile.draw(c);
		}
		c.restore();
	}

	@Override
	public boolean onSingleTapUp(final MotionEvent pEvent, final MapView pMapView) {
		// Consume event so layers underneath don't receive
		if (mMiniMapCanvasRect.contains((int) pEvent.getX(), (int) pEvent.getY())) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onDoubleTap(final MotionEvent pEvent, final MapView pMapView) {
		// Consume event so layers underneath don't receive
		if (mMiniMapCanvasRect.contains((int) pEvent.getX(), (int) pEvent.getY())) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onLongPress(final MotionEvent pEvent, final MapView pMapView) {
		// Consume event so layers underneath don't receive
		if (mMiniMapCanvasRect.contains((int) pEvent.getX(), (int) pEvent.getY())) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isOptionsMenuEnabled() {
		// Don't provide menu items from TilesOverlay.
		return false;
	}

	/**
	 * Sets the width of the mini-map in pixels
	 * 
	 * @param width
	 *            the width to set in pixels
	 */
	public void setWidth(final int width) {
		mWidth = width;
	}

	/**
	 * Gets the width of the mini-map in pixels
	 * 
	 * @return the width in pixels
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * Sets the height of the mini-map in pixels
	 * 
	 * @param height
	 *            the height to set in pixels
	 */
	public void setHeight(final int height) {
		mHeight = height;
	}

	/**
	 * Gets the height of the mini-map in pixels
	 * 
	 * @return the height in pixels
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * Sets the number of pixels from the lower-right corner to offset the mini-map
	 * 
	 * @param padding
	 *            the padding to set in pixels
	 */
	public void setPadding(final int padding) {
		mPadding = padding;
	}

	/**
	 * Gets the number of pixels from the lower-right corner to offset the mini-map
	 * 
	 * @return the padding in pixels
	 */
	public int getPadding() {
		return mPadding;
	}
}

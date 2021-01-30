package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

/**
 * Draws a mini-map as an overlay layer. It currently uses its own MapTileProviderBasic or a tile
 * provider supplied to it. Do NOT share a tile provider amongst multiple tile drawing overlays - it
 * will create an under-sized cache.
 * <p>
 * Notice, this class has some problems when the parent map view is rotation enabled.
 * See https://github.com/osmdroid/osmdroid/issues/98 for a work around
 *
 * @author Marc Kurtz
 */
public class MinimapOverlay extends TilesOverlay {

    private int mWidth = 100;
    private int mHeight = 100;
    private int mPadding = 10;
    private int mZoomDifference;
    private final Paint mPaint;

    /**
     * Creates a {@link MinimapOverlay} with the supplied tile provider. The {@link Handler} passed
     * in is typically the same handler being used by the main map. The {@link MapTileProviderBase}
     * passed in cannot be the same tile provider used in the {@link TilesOverlay}, it must be a new
     * instance.
     *
     * @param pContext                    a context
     * @param pTileRequestCompleteHandler a handler for the tile request complete notifications
     * @param pTileProvider               a tile provider
     */
    public MinimapOverlay(final Context pContext, final Handler pTileRequestCompleteHandler,
                          final MapTileProviderBase pTileProvider, final int pZoomDifference) {
        super(pTileProvider, pContext);
        setZoomDifference(pZoomDifference);

        mTileProvider.getTileRequestCompleteHandlers().add(pTileRequestCompleteHandler);

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
     * @param pContext                    a context
     * @param pTileRequestCompleteHandler a handler for the tile request complete notifications
     * @param pTileProvider               a tile provider
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
     * @param pContext                    a context
     * @param pTileRequestCompleteHandler a handler for tile request complete notifications
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
    public void draw(Canvas c, Projection pProjection) {
        if (!setViewPort(c, pProjection)) {
            return;
        }

        // Draw a solid background where the minimap will be drawn with a 2 pixel inset
        pProjection.save(c, false, true);
        c.drawRect(
                getCanvasRect().left - 2, getCanvasRect().top - 2,
                getCanvasRect().right + 2, getCanvasRect().bottom + 2, mPaint);

        super.drawTiles(c, getProjection(), getProjection().getZoomLevel(), mViewPort);
        pProjection.restore(c, true);
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent pEvent, final MapView pMapView) {
        // Consume event so layers underneath don't receive
        return contains(pEvent);
    }

    @Override
    public boolean onDoubleTap(final MotionEvent pEvent, final MapView pMapView) {
        // Consume event so layers underneath don't receive
        return contains(pEvent);
    }

    @Override
    public boolean onLongPress(final MotionEvent pEvent, final MapView pMapView) {
        // Consume event so layers underneath don't receive
        return contains(pEvent);
    }

    @Override
    public boolean isOptionsMenuEnabled() {
        // Don't provide menu items from TilesOverlay.
        return false;
    }

    /**
     * Sets the width of the mini-map in pixels
     *
     * @param width the width to set in pixels
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
     * @param height the height to set in pixels
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
     * @param padding the padding to set in pixels
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

    private boolean contains(final MotionEvent pEvent) {
        final Rect canvasRect = getCanvasRect();
        return canvasRect != null && canvasRect.contains((int) pEvent.getX(), (int) pEvent.getY());
    }

    @Override
    protected boolean setViewPort(final Canvas pCanvas, final Projection pProjection) {
        final double zoomLevel = pProjection.getZoomLevel() - getZoomDifference();
        if (zoomLevel < mTileProvider.getMinimumZoomLevel()) {
            return false;
        }

        final int left = pCanvas.getWidth() - getPadding() - getWidth();
        final int top = pCanvas.getHeight() - getPadding() - getHeight();
        setCanvasRect(new Rect(left, top, left + getWidth(), top + getHeight()));
        setProjection(pProjection.getOffspring(zoomLevel, getCanvasRect()));
        getProjection().getMercatorViewPort(mViewPort);
        return true;
    }
}

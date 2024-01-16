package org.osmdroid.util;

import android.graphics.Rect;

import androidx.annotation.NonNull;

import org.osmdroid.views.overlay.IViewBoundingBoxChangedListener;

import java.util.Objects;

/**
 * A class that will loop around all the map tiles in the given viewport.
 */
public abstract class TileLooper {

    protected final Rect mTiles = new Rect();
    protected int mTileZoomLevel;
    private boolean horizontalWrapEnabled;
    private boolean verticalWrapEnabled;
    private final Rect mLastBoundingBox = new Rect();
    private int mLastZoom = -1;
    private final Rect mNewBoundingBox = new Rect();
    private final IViewBoundingBoxChangedListener mViewBoundingBoxChangedListener;

    public TileLooper() {
        this(false, false);
    }

    public TileLooper(final boolean horizontalWrapEnabled, final boolean verticalWrapEnabled) {
        this.horizontalWrapEnabled = horizontalWrapEnabled;
        this.verticalWrapEnabled = verticalWrapEnabled;
        mViewBoundingBoxChangedListener = getViewBoundingBoxChangedListener();
    }

    protected void loop(final double pZoomLevel, @NonNull final RectL pMercatorViewPort) {
        TileSystem.getTileFromMercator(pMercatorViewPort, TileSystem.getTileSize(pZoomLevel), mTiles);
        mTileZoomLevel = TileSystem.getInputTileZoomLevel(pZoomLevel);
        if (!Objects.equals(mTiles, mLastBoundingBox) &&
                ((pZoomLevel % 1) == 0)   //<-- this limits event at only when Zoom double is a whole avoiding triggering while zoom animation is running
        ) {
            mNewBoundingBox.set(mTiles);
            mViewBoundingBoxChangedListener.onViewBoundingBoxChanged(mLastBoundingBox, mLastZoom, mNewBoundingBox, mTileZoomLevel);
            mLastZoom = mTileZoomLevel;
            mLastBoundingBox.set(mTiles);
        }

        initialiseLoop();

        final int mapTileUpperBound = 1 << mTileZoomLevel;

        /* Draw all the MapTiles (from the upper left to the lower right). */
        int tileX;
        int tileY;
        long tile;
        for (int i = mTiles.left; i <= mTiles.right; i++) {
            for (int j = mTiles.top; j <= mTiles.bottom; j++) {
                if ((horizontalWrapEnabled || (i >= 0 && i < mapTileUpperBound)) && (verticalWrapEnabled
                        || (j >= 0 && j < mapTileUpperBound))) {
                    tileX = MyMath.mod(i, mapTileUpperBound);
                    tileY = MyMath.mod(j, mapTileUpperBound);
                    tile = MapTileIndex.getTileIndex(mTileZoomLevel, tileX, tileY);
                    handleTile(tile, i, j, tileX, tileY, mTileZoomLevel);
                }
            }
        }

        finaliseLoop();
    }

    public void initialiseLoop() {
    }

    public abstract void handleTile(long pMapTileIndex, int pX, int pY, int tX, int tY, int tZ);

    public void finaliseLoop() {
    }

    public boolean isHorizontalWrapEnabled() {
        return horizontalWrapEnabled;
    }

    public void setHorizontalWrapEnabled(boolean horizontalWrapEnabled) {
        this.horizontalWrapEnabled = horizontalWrapEnabled;
    }

    public boolean isVerticalWrapEnabled() {
        return verticalWrapEnabled;
    }

    public void setVerticalWrapEnabled(boolean verticalWrapEnabled) {
        this.verticalWrapEnabled = verticalWrapEnabled;
    }

    protected abstract IViewBoundingBoxChangedListener getViewBoundingBoxChangedListener();

}

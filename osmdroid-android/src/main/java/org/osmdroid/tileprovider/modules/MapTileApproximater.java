package org.osmdroid.tileprovider.modules;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The MapTileApproximater computes approximation of tiles.
 * The approximation is based on the tiles of the same region, but on lower zoom level tiles.
 * An obvious use is in offline mode: it's better to display an approximation than an empty grey square.
 *
 * @since 5.6.5
 * @author Fabrice Fontaine
 */
public class MapTileApproximater extends MapTileModuleProviderBase {

    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<>();
    private final IFilesystemCache mReader;

    public MapTileApproximater(final ITileSource pTileSource, final IFilesystemCache pFilesystemCache) {
        this(pTileSource, pFilesystemCache,
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());
    }

    public MapTileApproximater(final ITileSource pTileSource, final IFilesystemCache pFilesystemCache,
                               final int pThreadPoolSize, final int pPendingQueueSize) {
        super(pThreadPoolSize, pPendingQueueSize);
        setTileSource(pTileSource);
        mReader = pFilesystemCache;
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    protected String getName() {
        return "Offline Tile Approximation Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "approximater";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public int getMinimumZoomLevel() {
        final ITileSource tileSource = mTileSource.get();
        return tileSource.getMinimumZoomLevel();
    }

    @Override
    public int getMaximumZoomLevel() {
        final ITileSource tileSource = mTileSource.get();
        return tileSource.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState pState) {
            final ITileSource tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }
            final Bitmap bitmap = approximateTileFromLowerZoom(tileSource, mReader, pState.getMapTile());
            if (bitmap == null) {
                return null;
            }
            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            ExpirableBitmapDrawable.setState(drawable, ExpirableBitmapDrawable.SCALED);
            return drawable;
        }
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @param pTileSource Tile source to use in order to get the source tile
     * @param pReader Cache where source tiles are stored
     * @param pMapTile Destination tile
     * @return
     */
    public static Bitmap approximateTileFromLowerZoom(
            final ITileSource pTileSource, final IFilesystemCache pReader,
            final MapTile pMapTile) {
        for (int zoomDiff = 1 ; pMapTile.getZoomLevel() - zoomDiff >= pTileSource.getMinimumZoomLevel() ; zoomDiff ++) {
            final Bitmap result = approximateTileFromLowerZoom(pTileSource, pReader, pMapTile, zoomDiff);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @param pTileSource Tile source to use in order to get the source tile
     * @param pReader Cache where source tiles are stored
     * @param pMapTile Destination tile
     * @param pZoomDiff Zoom level difference between the destination and the source; strictly positive
     * @return
     */
    public static Bitmap approximateTileFromLowerZoom(
            final ITileSource pTileSource, final IFilesystemCache pReader,
            final MapTile pMapTile, final int pZoomDiff) {
        if (pZoomDiff <= 0) {
            return null;
        }
        final int srcZoomLevel = pMapTile.getZoomLevel() - pZoomDiff;
        if (srcZoomLevel < pTileSource.getMinimumZoomLevel()) {
            return null;
        }

        final MapTile srcTile = new MapTile(srcZoomLevel, pMapTile.getX() >> pZoomDiff, pMapTile.getY() >> pZoomDiff);
        try {
            final Drawable srcDrawable = pReader.loadTile(pTileSource, srcTile);
            if (!(srcDrawable instanceof BitmapDrawable)) {
                return null;
            }
            return MapTileApproximater.approximateTileFromLowerZoom(
                    pTileSource.getTileSizePixels(), (BitmapDrawable) srcDrawable,
                    pMapTile, pZoomDiff);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @since 5.6.5
     * @param pTileSizePixels Pixel tile size, for source and destination
     * @param pSrcDrawable Source tile bitmap
     * @param pMapTile Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @param pZoomDiff Zoom level difference between the destination and the source; strictly positive
     * @return
     */
    public static Bitmap approximateTileFromLowerZoom(
            final int pTileSizePixels, final BitmapDrawable pSrcDrawable,
            final MapTile pMapTile, final int pZoomDiff) {
        if (pZoomDiff <= 0) {
            return null;
        }
        final Bitmap bitmap = getTileBitmap(pTileSizePixels);
        final Canvas canvas = new Canvas(bitmap);
        final boolean isReusable = pSrcDrawable instanceof ReusableBitmapDrawable;
        final ReusableBitmapDrawable reusableBitmapDrawable = isReusable ?
                (ReusableBitmapDrawable) pSrcDrawable : null;
        boolean success = false;
        if (isReusable) {
            reusableBitmapDrawable.beginUsingDrawable();
        }
        try {
            if (!isReusable || reusableBitmapDrawable.isBitmapValid()) {
                final int srcSize = pTileSizePixels >> pZoomDiff;
                final int srcX = (pMapTile.getX() % (1 << pZoomDiff)) * srcSize;
                final int srcY = (pMapTile.getY() % (1 << pZoomDiff)) * srcSize;
                final Rect srcRect = new Rect(srcX, srcY, srcX + srcSize, srcY + srcSize);
                final Rect dstRect = new Rect(0, 0, pTileSizePixels, pTileSizePixels);
                canvas.drawBitmap(pSrcDrawable.getBitmap(), srcRect, dstRect, null);
                success = true;
            }
        } finally {
            if (isReusable)
                reusableBitmapDrawable.finishUsingDrawable();
        }
        if (!success) {
            return null;
        }
        return bitmap;
    }

    /**
     * Try to get a tile bitmap from the pool, otherwise allocate a new one
     *
     * @param pTileSizePx
     * @return
     */
    public static Bitmap getTileBitmap(final int pTileSizePx) {
        final Bitmap bitmap = BitmapPool.getInstance().obtainSizedBitmapFromPool(pTileSizePx, pTileSizePx);
        if (bitmap != null) {
            return bitmap;
        }
        return Bitmap.createBitmap(pTileSizePx, pTileSizePx, Bitmap.Config.ARGB_8888);
    }
}
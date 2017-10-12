package org.osmdroid.tileprovider.modules;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.util.ArrayList;
import java.util.List;

/**
 * The MapTileApproximater computes approximation of tiles.
 * The approximation is based on the tiles of the same region, but on lower zoom level tiles.
 * An obvious use is in offline mode: it's better to display an approximation than an empty grey square.
 *
 * @since 5.6.5
 * @author Fabrice Fontaine
 */
public class MapTileApproximater extends MapTileModuleProviderBase {

    private final List<MapTileModuleProviderBase> mProviders = new ArrayList<>();
    private int minZoomLevel;

    /**
     * @since 6.0.0
     */
    public MapTileApproximater() {
        this(
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());
    }

    /**
     * @since 6.0.0
     */
    public MapTileApproximater(final int pThreadPoolSize, final int pPendingQueueSize) {
        super(pThreadPoolSize, pPendingQueueSize);
    }

    /**
     * @since 6.0.0
     */
    public void addProvider(final MapTileModuleProviderBase pProvider) {
        mProviders.add(pProvider);
        computeZoomLevels();
    }

    private void computeZoomLevels() {
        boolean first = true;
        minZoomLevel = OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL;
        for (final MapTileModuleProviderBase provider : mProviders) {
            final int otherMin = provider.getMinimumZoomLevel();;
            if (first) {
                first = false;
                minZoomLevel = otherMin;
            } else {
                minZoomLevel = Math.min(minZoomLevel, otherMin);
            }
        }
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
    public TileLoader getTileLoader() {
        return new TileLoader();
    }

    @Override
    public int getMinimumZoomLevel() {
        return minZoomLevel;
    }

    @Override
    public int getMaximumZoomLevel() {
        return microsoft.mappoint.TileSystem.getMaximumZoomLevel();
    }

    @Deprecated
    @Override
    public void setTileSource(final ITileSource pTileSource) {
        // not relevant
    }

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTile pMapTile) {
            final Bitmap bitmap = approximateTileFromLowerZoom(pMapTile);
            if (bitmap != null) {
                final BitmapDrawable drawable = new BitmapDrawable(bitmap);
                ExpirableBitmapDrawable.setState(drawable, ExpirableBitmapDrawable.SCALED);
                return drawable;
            }
            return null;
        }
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @since 6.0.0
     * @param pMapTile Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @return
     */
    public Bitmap approximateTileFromLowerZoom(final MapTile pMapTile) {
        for (int zoomDiff = 1; pMapTile.getZoomLevel() - zoomDiff >= OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL ; zoomDiff ++) {
            final Bitmap bitmap = approximateTileFromLowerZoom(pMapTile, zoomDiff);
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @since 6.0.0
     * @param pMapTile Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @param pZoomDiff Zoom level difference between the destination and the source; strictly positive
     * @return
     */
    public Bitmap approximateTileFromLowerZoom(final MapTile pMapTile, final int pZoomDiff) {
        for (final MapTileModuleProviderBase provider : mProviders) {
            final Bitmap bitmap = approximateTileFromLowerZoom(provider, pMapTile, pZoomDiff);
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @since 6.0.0
     * @param pProvider Source tile provider
     * @param pMapTile Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @param pZoomDiff Zoom level difference between the destination and the source; strictly positive
     * @return
     */
    public static Bitmap approximateTileFromLowerZoom(
            final MapTileModuleProviderBase pProvider,
            final MapTile pMapTile, final int pZoomDiff) {
        if (pZoomDiff <= 0) {
            return null;
        }
        final int srcZoomLevel = pMapTile.getZoomLevel() - pZoomDiff;
        if (srcZoomLevel < pProvider.getMinimumZoomLevel()) {
            return null;
        }
        if (srcZoomLevel > pProvider.getMaximumZoomLevel()) {
            return null;
        }
        final MapTile srcTile = new MapTile(srcZoomLevel, pMapTile.getX() >> pZoomDiff, pMapTile.getY() >> pZoomDiff);
        try {
            final Drawable srcDrawable = pProvider.getTileLoader().loadTile(srcTile);
            if (!(srcDrawable instanceof BitmapDrawable)) {
                return null;
            }
            return approximateTileFromLowerZoom((BitmapDrawable) srcDrawable, pMapTile, pZoomDiff);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @since 5.6.5
     * @param pSrcDrawable Source tile bitmap
     * @param pMapTile Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @param pZoomDiff Zoom level difference between the destination and the source; strictly positive
     * @return
     */
    public static Bitmap approximateTileFromLowerZoom(
            final BitmapDrawable pSrcDrawable,
            final MapTile pMapTile, final int pZoomDiff) {
        if (pZoomDiff <= 0) {
            return null;
        }
        final int tileSizePixels = pSrcDrawable.getBitmap().getWidth();
        final Bitmap bitmap = getTileBitmap(tileSizePixels);
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
                final int srcSize = tileSizePixels >> pZoomDiff;
                if (srcSize == 0) {
                    success = false;
                } else {
                    final int srcX = (pMapTile.getX() % (1 << pZoomDiff)) * srcSize;
                    final int srcY = (pMapTile.getY() % (1 << pZoomDiff)) * srcSize;
                    final Rect srcRect = new Rect(srcX, srcY, srcX + srcSize, srcY + srcSize);
                    final Rect dstRect = new Rect(0, 0, tileSizePixels, tileSizePixels);
                    canvas.drawBitmap(pSrcDrawable.getBitmap(), srcRect, dstRect, null);
                    success = true;
                }
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

    /**
     * @since 6.0.0
     */
    @Override
    public void detach() {
        super.detach();
        mProviders.clear();
    }
}

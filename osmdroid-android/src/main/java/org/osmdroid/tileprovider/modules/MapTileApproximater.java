package org.osmdroid.tileprovider.modules;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The MapTileApproximater computes approximation of tiles.
 * The approximation is based on the tiles of the same region, but on lower zoom level tiles.
 * An obvious use is in offline mode: it's better to display an approximation than an empty grey square.
 *
 * @author Fabrice Fontaine
 * @since 5.6.5
 */
public class MapTileApproximater extends MapTileModuleProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String CONST_MAPTILEPROVIDER_APPROXIMATER = "approximater";
    private static final int CONST_THREAD_CANVAS_CACHE_SIZE = 32;

    // ===========================================================
    // Fields
    // ===========================================================

    private final List<MapTileModuleProviderBase> mProviders = new CopyOnWriteArrayList<>();
    private int minZoomLevel;
    private final TileLoader mTileLoader = new TileLoader();
    private static final LinkedHashMap<Long,Canvas> mCanvasCache = new LinkedHashMap<Long,Canvas>(10, 0.1f, true) {
        @Override
        protected boolean removeEldestEntry(@NonNull final Entry eldest) {
            return (mCanvasCache.size() >= CONST_THREAD_CANVAS_CACHE_SIZE);
        }
    };
    private static final LinkedHashMap<Long, Pair<Rect,Rect>> mRectsCache = new LinkedHashMap<Long, Pair<Rect,Rect>>(10, 0.1f, true) {
        @Override
        protected boolean removeEldestEntry(@NonNull final Entry eldest) {
            return (mRectsCache.size() >= CONST_THREAD_CANVAS_CACHE_SIZE);
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================

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
            final int otherMin = provider.getMinimumZoomLevel();
            ;
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
        return CONST_MAPTILEPROVIDER_APPROXIMATER;
    }

    @Override
    public TileLoader getTileLoader() {
        return mTileLoader;
    }

    @Override
    public int getMinimumZoomLevel() {
        return minZoomLevel;
    }

    @Override
    public int getMaximumZoomLevel() {
        return org.osmdroid.util.TileSystem.getMaximumZoomLevel();
    }

    @Deprecated
    @Override
    public void setTileSource(final ITileSource pTileSource) {
        // not relevant
    }

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final long pMapTileIndex) {
            final Bitmap bitmap = approximateTileFromLowerZoom(pMapTileIndex);
            if (bitmap != null) {
                final BitmapDrawable drawable = new BitmapDrawable(bitmap);
                ExpirableBitmapDrawable.setState(drawable, ExpirableBitmapDrawable.SCALED);
                return drawable;
            }
            return null;
        }

        @IMapTileProviderCallback.TILEPROVIDERTYPE
        @Override
        public final int getProviderType() { return IMapTileProviderCallback.TILEPROVIDERTYPE_APPROXIMATER; }
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @param pMapTileIndex Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @return
     * @since 6.0.0
     */
    public Bitmap approximateTileFromLowerZoom(final long pMapTileIndex) {
        for (int zoomDiff = 1; MapTileIndex.getZoom(pMapTileIndex) - zoomDiff >= OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL; zoomDiff++) {
            final Bitmap bitmap = approximateTileFromLowerZoom(pMapTileIndex, zoomDiff);
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @param pMapTileIndex Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @param pZoomDiff     Zoom level difference between the destination and the source; strictly positive
     * @return
     * @since 6.0.0
     */
    public Bitmap approximateTileFromLowerZoom(final long pMapTileIndex, final int pZoomDiff) {
        for (final MapTileModuleProviderBase provider : mProviders) {
            final Bitmap bitmap = approximateTileFromLowerZoom(provider, pMapTileIndex, pZoomDiff);
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @param pProvider     Source tile provider
     * @param pMapTileIndex Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @param pZoomDiff     Zoom level difference between the destination and the source; strictly positive
     * @return
     * @since 6.0.0
     */
    public static Bitmap approximateTileFromLowerZoom(
            final MapTileModuleProviderBase pProvider,
            final long pMapTileIndex, final int pZoomDiff) {
        if (pZoomDiff <= 0) {
            return null;
        }
        final int srcZoomLevel = MapTileIndex.getZoom(pMapTileIndex) - pZoomDiff;
        if (srcZoomLevel < pProvider.getMinimumZoomLevel()) {
            return null;
        }
        if (srcZoomLevel > pProvider.getMaximumZoomLevel()) {
            return null;
        }
        final long srcTile = MapTileIndex.getTileIndex(srcZoomLevel,
                MapTileIndex.getX(pMapTileIndex) >> pZoomDiff,
                MapTileIndex.getY(pMapTileIndex) >> pZoomDiff);
        try {
            final Drawable srcDrawable = pProvider.getTileLoader().loadTileIfReachable(srcTile);
            if (!(srcDrawable instanceof BitmapDrawable)) {
                return null;
            }
            return approximateTileFromLowerZoom((BitmapDrawable) srcDrawable, pMapTileIndex, pZoomDiff);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Approximate a tile from a lower zoom level
     *
     * @param pSrcDrawable  Source tile bitmap
     * @param pMapTileIndex Destination tile, for the same place on the planet as the source, but on a higher zoom
     * @param pZoomDiff     Zoom level difference between the destination and the source; strictly positive
     * @return
     * @since 5.6.5
     */
    public static Bitmap approximateTileFromLowerZoom(
            final BitmapDrawable pSrcDrawable,
            final long pMapTileIndex, final int pZoomDiff) {
        if (pZoomDiff <= 0) {
            return null;
        }
        final int tileSizePixels = pSrcDrawable.getBitmap().getWidth();
        final Bitmap bitmap = getTileBitmap(tileSizePixels);
        final long cThreadId = Thread.currentThread().getId();
        final Canvas cCanvas;
        synchronized (mCanvasCache) {
            if (mCanvasCache.containsKey(cThreadId)) cCanvas = mCanvasCache.get(cThreadId);
            else mCanvasCache.put(cThreadId, (cCanvas = new Canvas()));
        }
        //noinspection DataFlowIssue
        cCanvas.setBitmap(bitmap);
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
                    final int srcX = (MapTileIndex.getX(pMapTileIndex) % (1 << pZoomDiff)) * srcSize;
                    final int srcY = (MapTileIndex.getY(pMapTileIndex) % (1 << pZoomDiff)) * srcSize;
                    final Pair<Rect,Rect> cPair;
                    synchronized (mRectsCache) {
                        if (mRectsCache.containsKey(cThreadId)) cPair = mRectsCache.get(cThreadId);
                        else mRectsCache.put(cThreadId, (cPair = new Pair<>(new Rect(), new Rect())));
                    }
                    //noinspection DataFlowIssue
                    final Rect srcRect = cPair.first;
                    final Rect dstRect = cPair.second;
                    srcRect.set(srcX, srcY, srcX + srcSize, srcY + srcSize);
                    dstRect.set(0, 0, tileSizePixels, tileSizePixels);
                    cCanvas.drawBitmap(pSrcDrawable.getBitmap(), srcRect, dstRect, null);
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
     */
    public static Bitmap getTileBitmap(final int pTileSizePx) {
        final Bitmap bitmap = BitmapPool.getInstance().obtainSizedBitmapFromPool(pTileSizePx, pTileSizePx);
        if (bitmap != null) {
            // without that, the retrieved bitmap forgets it allowed transparency
            bitmap.setHasAlpha(true);
            // without that, the bitmap keeps its previous contents when transparent content is copied on it
            bitmap.eraseColor(Color.TRANSPARENT);
            return bitmap;
        }
        return Bitmap.createBitmap(pTileSizePx, pTileSizePx, Bitmap.Config.ARGB_8888);
    }

    /** {@inheritDoc} */
    @Override
    public void detach() {
        super.detach();
        mProviders.clear();
    }
}

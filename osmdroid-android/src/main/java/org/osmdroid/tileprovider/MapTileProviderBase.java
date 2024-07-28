package org.osmdroid.tileprovider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.TileLooper;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IViewBoundingBoxChangedListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This is an abstract class. The tile provider is responsible for:
 * <ul>
 * <li>determining if a map tile is available,</li>
 * <li>notifying the client, via a callback handler</li>
 * </ul>
 * see {@link MapTileIndex} for an overview of how tiles are served by this provider.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author plusminus on 21:46:22 - 25.09.2008
 * @author and many other contributors
 */
public abstract class MapTileProviderBase implements IMapTileProviderCallback {
    private static final String TAG = "MapTileProviderBase";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag=true, value={ MAPTYPERESULT_LOADING, MAPTYPERESULT_SUCCESS, MAPTYPERESULT_FAIL, MAPTYPERESULT_DONE_BUT_UNKNOWN, MAPTYPERESULT_DISCARTED_OUT_OF_BOUNDS })
    public @interface MAPTYPERESULT {}
    /** @noinspection PointlessBitwiseExpression*/
    public static final int MAPTYPERESULT_LOADING                   = 1 << 0;
    public static final int MAPTYPERESULT_SUCCESS                   = 1 << 1;
    /** Used to mark a Tile request as Failed because there wasn't possibile to find an already cached data nor anything else for it neither old/expired */
    public static final int MAPTYPERESULT_FAIL                      = 1 << 2;
    /** Used to mark a Tile request as <i>Done but without done nothing</i><br>
     * <br>
     * <i>(mostly used when a Network connection is not present but a Tile is marked as <i>Expired</i> and already well queued for download)</i>
     */
    public static final int MAPTYPERESULT_DONE_BUT_UNKNOWN          = 1 << 3;
    public static final int MAPTYPERESULT_DISCARTED_OUT_OF_BOUNDS   = 1 << 4;
    private static int maskMapTileResult(@MAPTYPERESULT final int mapTileResult, @TILEPROVIDERTYPE final int providerType) { return (mapTileResult | providerType); }
    @SuppressLint("WrongConstant")
    @MAPTYPERESULT
    public static int unmaskMapTypeResult(final int messageWhat) { return (messageWhat & 0xFF); }
    @SuppressLint("WrongConstant")
    @TILEPROVIDERTYPE
    public static int unmaskTileProviderType(final int messageWhat) { return (messageWhat & 0xFFFFFF00); }

    private static int sApproximationBackgroundColor = Color.LTGRAY;

    protected final MapTileCache mTileCache;
    private final Collection<Handler> mTileRequestCompleteHandlers = new LinkedHashSet<>();
    protected boolean mUseDataConnection = true;
    protected Drawable mTileNotFoundImage = null;
    private ITileSource mTileSource;
    private final RectL mViewPortMercator = new RectL();
    private final ZoomInTileLooper mZoomInTileLooper = new ZoomInTileLooper();
    private final ZoomOutTileLooper mZoomOutTileLooper = new ZoomOutTileLooper();

    public MapTileProviderBase(@NonNull final Context context, @NonNull final ITileSource pTileSource) { this(context, pTileSource, null); }
    public MapTileProviderBase(@NonNull final Context context, @NonNull final ITileSource pTileSource, @Nullable final Handler pDownloadFinishedListener) {
        mTileCache = this.createTileCache();
        if (pDownloadFinishedListener != null) mTileRequestCompleteHandlers.add(pDownloadFinishedListener);
        mTileSource = pTileSource;
    }

    /**
     * Attempts to get a Drawable that represents a {@link MapTileIndex}. If the tile is not immediately
     * available this will return null and attempt to get the tile from known tile sources for
     * subsequent future requests. Note that this may return a {@link ReusableBitmapDrawable} in
     * which case you should follow proper handling procedures for using that Drawable or it may
     * reused while you are working with it.
     *
     * @see ReusableBitmapDrawable
     */
    public abstract Drawable getMapTile(final long pMapTileIndex);

    /**
     * classes that extend MapTileProviderBase must call this method to prevent memory leaks.
     */
    public final void detach(@Nullable final Context context) {
        this.onDetach(context);
    }
    @CallSuper
    protected void onDetach(@Nullable final Context context) {
        if (mTileNotFoundImage != null) {
            if (mTileNotFoundImage instanceof ReusableBitmapDrawable)
                BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) mTileNotFoundImage);
        }
        mTileNotFoundImage = null;
        clearTileCache();
    }

    /**
     * Sets the default color for approximated tiles.
     *
     * @param pColor the default color that will be shown for approximated tiles
     */
    public static void setApproximationBackgroundColor(final int pColor) {
        sApproximationBackgroundColor = pColor;
    }

    /**
     * Gets the minimum zoom level this tile provider can provide
     *
     * @return the minimum zoom level
     */
    public abstract int getMinimumZoomLevel();

    /**
     * Gets the maximum zoom level this tile provider can provide
     *
     * @return the maximum zoom level
     */
    public abstract int getMaximumZoomLevel();

    /**
     * Sets the tile source for this tile provider.
     *
     * @param pTileSource the tile source
     */
    @CallSuper
    public void setTileSource(@NonNull final ITileSource pTileSource) {
        mTileSource = pTileSource;
        clearTileCache();
    }

    /**
     * Gets the tile source for this tile provider.
     *
     * @return the tile source
     */
    public final ITileSource getTileSource() {
        return mTileSource;
    }

    /**
     * Creates a {@link MapTileCache} to be used to cache tiles in memory.
     */
    private MapTileCache createTileCache() {
        return new MapTileCache();
    }

    /**
     * Sets the "sorry we can't load a tile for this location" image. If it's null, the default view
     * is shown, which is the standard grey grid controlled by the tiles overlay
     * {@link org.osmdroid.views.overlay.TilesOverlay#setLoadingLineColor(Context, int)} and
     * {@link org.osmdroid.views.overlay.TilesOverlay#setLoadingBackgroundColor(Context, int)}
     *
     * @since 5.2+
     */
    public void setTileLoadFailureImage(final Drawable drawable) {
        this.mTileNotFoundImage = drawable;
    }

    @CallSuper
    @Override
    public void mapTileRequestStarted(@NonNull final MapTileRequestState pState, final int pending, final int working) {
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        final MapTileModuleProviderBase cMapTileModuleProviderBase;
        @TILEPROVIDERTYPE
        final int cProviderType = (((cMapTileModuleProviderBase = pState.getCurrentProvider()) != null) ? cMapTileModuleProviderBase.getTileLoader().getProviderType() : TILEPROVIDERTYPE_NONE);
        sendMessage(maskMapTileResult(MAPTYPERESULT_LOADING, cProviderType), cMapTileIndex, null);
    }

    /**
     * Called by implementation class methods indicating that they have completed the request as
     * best it can. The tile is added to the cache, and a MAPTILE_SUCCESS_ID message is sent.
     *
     * @param pState    the map tile request state object
     * @param pDrawable the Drawable of the map tile
     */
    @CallSuper
    @Override
    public void mapTileRequestCompleted(@NonNull final MapTileRequestState pState, final Drawable pDrawable) {
        // put the tile in the cache
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        putTileIntoCache(cMapTileIndex, pDrawable, ExpirableBitmapDrawable.UP_TO_DATE);

        // tell our caller we've finished and it should update its view
        final MapTileModuleProviderBase cMapTileModuleProviderBase;
        @TILEPROVIDERTYPE
        final int cProviderType = (((cMapTileModuleProviderBase = pState.getCurrentProvider()) != null) ? cMapTileModuleProviderBase.getTileLoader().getProviderType() : TILEPROVIDERTYPE_NONE);
        sendMessage(maskMapTileResult(MAPTYPERESULT_SUCCESS, cProviderType), cMapTileIndex, pState.getLoadingTimeMillis());

        if (Configuration.getInstance().isDebugTileProviders()) {
            Log.d(IMapView.LOGTAG, TAG+".mapTileRequestCompleted(): " + MapTileIndex.toString(pState));
        }
    }

    /**
     * Called by implementation class methods indicating that they have failed to retrieve the
     * requested map tile. a MAPTILE_FAIL_ID message is sent.
     *
     * @param pState the map tile request state object
     */
    @CallSuper
    @Override
    public void mapTileRequestFailed(@NonNull final MapTileRequestState pState) {
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        if (mTileNotFoundImage != null) {
            putTileIntoCache(cMapTileIndex, mTileNotFoundImage, ExpirableBitmapDrawable.NOT_FOUND);
            final MapTileModuleProviderBase cMapTileModuleProviderBase;
            @TILEPROVIDERTYPE
            final int cProviderType = (((cMapTileModuleProviderBase = pState.getCurrentProvider()) != null) ? cMapTileModuleProviderBase.getTileLoader().getProviderType() : TILEPROVIDERTYPE_NONE);
            sendMessage(maskMapTileResult(MAPTYPERESULT_SUCCESS, cProviderType), cMapTileIndex, pState.getLoadingTimeMillis());
        } else {
            sendMessage(maskMapTileResult(MAPTYPERESULT_FAIL, TILEPROVIDERTYPE_NONE), cMapTileIndex, pState.getLoadingTimeMillis());
        }
        if (Configuration.getInstance().isDebugTileProviders()) {
            Log.d(IMapView.LOGTAG, TAG+".mapTileRequestFailed(): " + MapTileIndex.toString(cMapTileIndex));
        }
    }

    @CallSuper
    @Override
    public void mapTileRequestDoneButUnknown(@NonNull final MapTileRequestState pState) {
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        final MapTileModuleProviderBase cMapTileModuleProviderBase;
        @TILEPROVIDERTYPE
        final int cProviderType = (((cMapTileModuleProviderBase = pState.getCurrentProvider()) != null) ? cMapTileModuleProviderBase.getTileLoader().getProviderType() : TILEPROVIDERTYPE_NONE);
        sendMessage(maskMapTileResult(MAPTYPERESULT_DONE_BUT_UNKNOWN, cProviderType), cMapTileIndex, pState.getLoadingTimeMillis());
    }

    /**
     * Called by implementation class methods indicating that they have failed to retrieve the
     * requested map tile, because the max queue size has been reached
     *
     * @param pState the map tile request state object
     */
    @CallSuper
    @Override
    public void mapTileRequestFailedExceedsMaxQueueSize(@NonNull final MapTileRequestState pState) {
        mapTileRequestFailed(pState);
    }

    /**
     * Called by implementation class methods indicating that they have produced an expired result
     * that can be used but better results may be delivered later. The tile is added to the cache,
     * and a MAPTILE_SUCCESS_ID message is sent.
     *
     * @param pState    the map tile request state object
     * @param pDrawable the Drawable of the map tile
     */
    @CallSuper
    @Override
    public void mapTileRequestExpiredTile(@NonNull final MapTileRequestState pState, Drawable pDrawable) {
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        putTileIntoCache(cMapTileIndex, pDrawable, ExpirableBitmapDrawable.getState(pDrawable));

        // tell our caller we've finished and it should update its view
        final MapTileModuleProviderBase cMapTileModuleProviderBase;
        @TILEPROVIDERTYPE
        final int cProviderType = (((cMapTileModuleProviderBase = pState.getCurrentProvider()) != null) ? cMapTileModuleProviderBase.getTileLoader().getProviderType() : TILEPROVIDERTYPE_NONE);
        sendMessage(maskMapTileResult(MAPTYPERESULT_SUCCESS, cProviderType), cMapTileIndex, pState.getLoadingTimeMillis());

        if (Configuration.getInstance().isDebugTileProviders()) {
            Log.d(IMapView.LOGTAG, TAG+".mapTileRequestExpiredTile(): " + MapTileIndex.toString(cMapTileIndex));
        }
    }

    /** {@inheritDoc} */
    @CallSuper
    @Override
    public void mapTileRequestDiscartedDueToOutOfViewBounds(@NonNull final MapTileRequestState pState) {
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;

        final MapTileModuleProviderBase cMapTileModuleProviderBase;
        @TILEPROVIDERTYPE
        final int cProviderType = (((cMapTileModuleProviderBase = pState.getCurrentProvider()) != null) ? cMapTileModuleProviderBase.getTileLoader().getProviderType() : TILEPROVIDERTYPE_NONE);
        sendMessage(maskMapTileResult(MAPTYPERESULT_DISCARTED_OUT_OF_BOUNDS, cProviderType), cMapTileIndex, pState.getLoadingTimeMillis());

        if (Configuration.getInstance().isDebugTileProviders()) {
            Log.d(IMapView.LOGTAG, TAG+".mapTileRequestDiscartedDueToOutOfViewBounds(): " + MapTileIndex.toString(cMapTileIndex));
        }
    }

    /**
     * @since 5.6.5
     */
    protected void putTileIntoCache(final long pMapTileIndex, final Drawable pDrawable, final int pState) {
        if (pDrawable == null) {
            return;
        }
        final Drawable before = mTileCache.getMapTile(pMapTileIndex);
        if (before != null) {
            final int stateBefore = ExpirableBitmapDrawable.getState(before);
            if (stateBefore > pState) {
                return;
            }
        }
        ExpirableBitmapDrawable.setState(pDrawable, pState);
        mTileCache.putTile(pMapTileIndex, pDrawable);
    }

    /**
     * @deprecated Use {@link #putTileIntoCache(long, Drawable, int)}} instead
     */
    @Deprecated
    protected void putExpiredTileIntoCache(MapTileRequestState pState, Drawable pDrawable) {
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        putTileIntoCache(cMapTileIndex, pDrawable, ExpirableBitmapDrawable.EXPIRED);
    }

    /**
     * @deprecated Use {@link #getTileRequestCompleteHandlers()} instead
     */
    @Deprecated
    public void setTileRequestCompleteHandler(@Nullable final Handler handler) {
        if (handler == null) return;
        mTileRequestCompleteHandlers.clear();
        mTileRequestCompleteHandlers.add(handler);
    }

    /**
     * @since 6.1.0
     */
    public Collection<Handler> getTileRequestCompleteHandlers() {
        return mTileRequestCompleteHandlers;
    }

    public void ensureCapacity(final int pCapacity) {
        mTileCache.ensureCapacity(pCapacity);
    }

    /**
     * @since 6.0.0
     */
    public MapTileCache getTileCache() {
        return mTileCache;
    }

    /**
     * purges the cache of all tiles (default is the in memory cache)
     */
    public void clearTileCache() {
        mTileCache.clear();
    }

    /**
     * Whether to use the network connection if it's available.
     */
    @Override
    public boolean useDataConnection() {
        return mUseDataConnection;
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param pMode if true use the network connection if it's available. if false don't use the
     *              network connection even if it's available.
     */
    public void setUseDataConnection(final boolean pMode) {
        mUseDataConnection = pMode;
    }

    /**
     * Recreate the cache using scaled versions of the tiles currently in it
     *
     * @param pNewZoomLevel the zoom level that we need now
     * @param pOldZoomLevel the previous zoom level that we should get the tiles to rescale
     * @param pViewPort     the view port we need tiles for
     */
    public void rescaleCache(final Projection pProjection, final double pNewZoomLevel,
                             final double pOldZoomLevel, final Rect pViewPort) {

        if (TileSystem.getInputTileZoomLevel(pNewZoomLevel) == TileSystem.getInputTileZoomLevel(pOldZoomLevel)) {
            return;
        }

        long startMs = 0;
        if (Configuration.getInstance().isDebugTileProviders()) {
            startMs = System.currentTimeMillis();
            Log.i(IMapView.LOGTAG, "rescale tile cache from " + pOldZoomLevel + " to " + pNewZoomLevel);
        }

        final PointL topLeftMercator = pProjection.toMercatorPixels(pViewPort.left, pViewPort.top, null);
        final PointL bottomRightMercator = pProjection.toMercatorPixels(pViewPort.right, pViewPort.bottom,
                null);
        mViewPortMercator.set(topLeftMercator.x, topLeftMercator.y, bottomRightMercator.x, bottomRightMercator.y);

        final ScaleTileLooper tileLooper = pNewZoomLevel > pOldZoomLevel
                ? mZoomInTileLooper
                : mZoomOutTileLooper;
        tileLooper.loop(pNewZoomLevel, mViewPortMercator, pOldZoomLevel, mTileSource.getTileSizePixels());

        if (Configuration.getInstance().isDebugTileProviders()) {
            final long endMs = System.currentTimeMillis();
            Log.i(IMapView.LOGTAG, "Finished rescale in " + (endMs - startMs) + "ms");
        }
    }

    private abstract class ScaleTileLooper extends TileLooper {

        /**
         * new (scaled) tiles to add to cache
         * NB first generate all and then put all in cache,
         * otherwise the ones we need will be pushed out
         */
        protected final HashMap<Long, Bitmap> mNewTiles = new HashMap<>();

        protected int mOldTileZoomLevel;
        protected int mTileSize;
        protected int mDiff;
        protected int mTileSize_2;
        protected final Rect mDestRect = new Rect();
        protected final Paint mDebugPaint = new Paint();
        private boolean isWorth;
        private final Canvas mCanvas = new Canvas();

        public ScaleTileLooper() {
            super();
        }

        public void loop(final double pZoomLevel, final RectL pViewPortMercator, final double pOldZoomLevel, final int pTileSize) {
            mOldTileZoomLevel = TileSystem.getInputTileZoomLevel(pOldZoomLevel);
            mTileSize = pTileSize;
            loop(pZoomLevel, pViewPortMercator);
        }

        @Override
        public void initialiseLoop() {
            super.initialiseLoop();
            mDiff = Math.abs(mTileZoomLevel - mOldTileZoomLevel);
            mTileSize_2 = mTileSize >> mDiff;
            isWorth = mDiff != 0;
        }

        @Override
        public void handleTile(final long pMapTileIndex, final int pX, final int pY, int tX, int tY, final int tZ) {
            if (!isWorth) {
                return;
            }

            // Get tile from cache.
            // If it's found then no need to created scaled version.
            // If not found (null) them we've initiated a new request for it,
            // and now we'll create a scaled version until the request completes.
            final Drawable requestedTile = getMapTile(pMapTileIndex);
            if (requestedTile == null) {
                try {
                    computeTile(pMapTileIndex, pX, pY);
                } catch (final OutOfMemoryError e) {
                    Log.e(IMapView.LOGTAG, "OutOfMemoryError rescaling cache");
                }
            }
        }

        @Override
        public void finaliseLoop() {
            // now add the new ones, pushing out the old ones
            final Iterator<Long> cIterator = mNewTiles.keySet().iterator();
            while (cIterator.hasNext()) {
                final long index = cIterator.next();
                final Bitmap bitmap = mNewTiles.get(index);
                cIterator.remove();
                putScaledTileIntoCache(index, bitmap);
            }
        }

        protected abstract void computeTile(final long pMapTileIndex, final int pX, final int pY);

        /**
         * @since 5.6.5
         */
        protected void putScaledTileIntoCache(final long pMapTileIndex, final Bitmap pBitmap) {
            final ReusableBitmapDrawable drawable = new ReusableBitmapDrawable(pBitmap);
            putTileIntoCache(pMapTileIndex, drawable, ExpirableBitmapDrawable.SCALED);
            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "Created scaled tile: " + MapTileIndex.toString(pMapTileIndex));
                mDebugPaint.setTextSize(40);
                mCanvas.setBitmap(pBitmap);
                mCanvas.drawText("scaled", 50, 50, mDebugPaint);
            }
        }

        @Override
        protected IViewBoundingBoxChangedListener getViewBoundingBoxChangedListener() {
            return MapTileProviderBase.this;
        }
    }

    private class ZoomInTileLooper extends ScaleTileLooper {

        public ZoomInTileLooper() {
            super();
        }

        @Override
        public void computeTile(final long pMapTileIndex, final int pX, final int pY) {
            // get the correct fraction of the tile from cache and scale up

            final long oldTile = MapTileIndex.getTileIndex(mOldTileZoomLevel,
                    MapTileIndex.getX(pMapTileIndex) >> mDiff, MapTileIndex.getY(pMapTileIndex) >> mDiff);
            final Drawable oldDrawable = mTileCache.getMapTile(oldTile);

            if (oldDrawable instanceof BitmapDrawable) {
                final Bitmap bitmap = MapTileApproximater.approximateTileFromLowerZoom(
                        (BitmapDrawable) oldDrawable, pMapTileIndex, mDiff);
                if (bitmap != null) {
                    mNewTiles.put(pMapTileIndex, bitmap);
                }
            }
        }
    }

    private class ZoomOutTileLooper extends ScaleTileLooper {
        private static final int MAX_ZOOM_OUT_DIFF = 4;
        private final Canvas mCanvas = new Canvas();

        public ZoomOutTileLooper() {
            super();
        }

        @Override
        protected void computeTile(final long pMapTileIndex, final int pX, final int pY) {

            if (mDiff >= MAX_ZOOM_OUT_DIFF) {
                return;
            }

            // get many tiles from cache and make one tile from them
            final int xx = MapTileIndex.getX(pMapTileIndex) << mDiff;
            final int yy = MapTileIndex.getY(pMapTileIndex) << mDiff;
            final int numTiles = 1 << mDiff;
            Bitmap bitmap = null;
            for (int x = 0; x < numTiles; x++) {
                for (int y = 0; y < numTiles; y++) {
                    final long oldTile = MapTileIndex.getTileIndex(mOldTileZoomLevel, xx + x, yy + y);
                    final Drawable oldDrawable = mTileCache.getMapTile(oldTile);
                    if (oldDrawable instanceof BitmapDrawable) {
                        final Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
                        if (oldBitmap != null) {
                            if (bitmap == null) {
                                bitmap = MapTileApproximater.getTileBitmap(mTileSize);
                                mCanvas.setBitmap(bitmap);
                                mCanvas.drawColor(sApproximationBackgroundColor);
                            }
                            mDestRect.set(
                                    x * mTileSize_2, y * mTileSize_2,
                                    (x + 1) * mTileSize_2, (y + 1) * mTileSize_2);
                            mCanvas.drawBitmap(oldBitmap, null, mDestRect, null);
                        }
                    }
                }
            }

            if (bitmap != null) {
                mNewTiles.put(pMapTileIndex, bitmap);
            }
        }
    }


    public abstract IFilesystemCache getTileWriter();

    /**
     * @return the number of tile requests currently in the queue
     * @since 5.6
     */
    public abstract long getQueueSize();

    /**
     * Expire a tile that is in the memory cache
     * Typical use is for mapsforge, where the contents of the tile can evolve,
     * depending on the neighboring tiles that have been displayed so far.
     *
     * @since 6.0.3
     */
    public void expireInMemoryCache(final long pMapTileIndex) {
        final Drawable drawable = mTileCache.getMapTile(pMapTileIndex);
        if (drawable != null) {
            ExpirableBitmapDrawable.setState(drawable, ExpirableBitmapDrawable.EXPIRED);
        }
    }

    /**
     * Concurrency exception management (cf. <a href="https://github.com/osmdroid/osmdroid/issues/1446">...</a>)
     * Given the likelihood of consecutive ConcurrentModificationException's,
     * we just try again and 3 attempts are supposedly enough.
     *
     * @since 6.2.0
     */
    private void sendMessage(final int pMessageId, final long mapTileIndex, @Nullable final Long loadingTime_ms) {
        for (int attempt = 0; attempt < 3; attempt++) {
            if (sendMessageFailFast(pMessageId, mapTileIndex, loadingTime_ms)) return;
        }
    }

    /**
     * Concurrency exception management (cf. <a href="https://github.com/osmdroid/osmdroid/issues/1446">...</a>)
     * Of course a for-each loop would make sense, but it's prone to concurrency issues.
     *
     * @return false if a ConcurrentModificationException was thrown
     * @since 6.2.0
     */
    private boolean sendMessageFailFast(final int pMessageId, final long mapTileIndex, @Nullable final Long loadingTime_ms) {
        boolean res = true;
        for (final Iterator<Handler> iterator = mTileRequestCompleteHandlers.iterator(); iterator.hasNext(); ) {
            final Handler handler;
            try {
                handler = iterator.next();
            } catch (final ConcurrentModificationException cme) {
                return false;
            }
            if (handler != null) {
                if (!handler.sendMessage(Message.obtain(handler, pMessageId, (int)(mapTileIndex >> 32), (int)mapTileIndex, loadingTime_ms))) {
                    Log.e(IMapView.LOGTAG, "Handler is not ready to accept Messages (what: #" + pMessageId + " | mapTileIndex: " + mapTileIndex + ")");
                    res = false;
                }
            }
        }
        return res;
    }

    /** {@inheritDoc} */
    @Override
    public void onViewBoundingBoxChanged(@NonNull final Rect fromBounds, final int fromZoom, @NonNull final Rect toBounds, final int toZoom) { /*nothing*/ }

}

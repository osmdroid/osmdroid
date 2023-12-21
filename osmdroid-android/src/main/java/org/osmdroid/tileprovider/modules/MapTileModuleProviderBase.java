package org.osmdroid.tileprovider.modules;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.overlay.IViewBoundingBoxChangedListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * An abstract base class for modular tile providers
 *
 * @author Marc Kurtz
 * @author Neil Boyd
 */
public abstract class MapTileModuleProviderBase implements IViewBoundingBoxChangedListener {
    private static final String TAG = "MapTileModuleProviderBase";

    /**
     * Gets the human-friendly name assigned to this tile provider.
     *
     * @return the thread name
     */
    protected abstract String getName();

    /**
     * Gets the name assigned to the thread for this provider.
     *
     * @return the thread name
     */
    protected abstract String getThreadGroupName();

    /**
     * It is expected that the implementation will construct an internal member which internally
     * implements a {@link TileLoader}. This method is expected to return a that internal member to
     * methods of the parent methods.
     *
     * @return the internal member of this tile provider.
     */
    public abstract TileLoader getTileLoader();

    /**
     * Returns true if implementation uses a data connection, false otherwise. This value is used to
     * determine if this provider should be skipped if there is no data connection.
     *
     * @return true if implementation uses a data connection, false otherwise
     */
    public abstract boolean getUsesDataConnection();

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
     * @since 6.1.3
     */
    public boolean isTileReachable(final long pMapTileIndex) {
        final int zoom = MapTileIndex.getZoom(pMapTileIndex);
        return zoom >= getMinimumZoomLevel() && zoom <= getMaximumZoomLevel();
    }

    /**
     * Sets the tile source for this tile provider.
     *
     * @param tileSource the tile source
     */
    public abstract void setTileSource(ITileSource tileSource);

    private final ExecutorService mExecutor;
    private final Object mQueueLockObject = new Object();
    private final HashMap<Long, MapTileRequestState> mWorking;
    private final LinkedHashMap<Long, MapTileRequestState> mPending;
    @Nullable
    private IMapTileProviderCallback mCallback = null;
    private final Rect mLastViewBoundingBox = new Rect(0,0,0,0);
    private final HashMap<Long,Long> mTilesOutOfViewBounds = new HashMap<>();

    public MapTileModuleProviderBase(int pThreadPoolSize, final int pPendingQueueSize) {
        if (pPendingQueueSize < pThreadPoolSize) {
            Log.w(IMapView.LOGTAG, "The pending queue size is smaller than the thread pool size. Automatically reducing the thread pool size.");
            pThreadPoolSize = pPendingQueueSize;
        }
        mExecutor = Executors.newFixedThreadPool(pThreadPoolSize, new ConfigurablePriorityThreadFactory(Thread.MIN_PRIORITY, getThreadGroupName()));

        mWorking = new HashMap<>();
        mPending = new LinkedHashMap<Long, MapTileRequestState>(pPendingQueueSize + 2, 0.1f, true) {
            /* commented out because this Pending queue is reduced when pending tiles goes OUTSIDE View boundaries
            @Override
            protected boolean removeEldestEntry(final Map.Entry<Long, MapTileRequestState> pEldest) {
                if (size() <= pPendingQueueSize) {
                    return false;
                }
                // get the oldest tile that isn't in the mWorking queue
                final Iterator<Long> iterator = mPending.keySet().iterator();
                while (iterator.hasNext()) {
                    final long mapTileIndex = iterator.next();
                    if (!mWorking.containsKey(mapTileIndex)) {
                        final MapTileRequestState state = mPending.get(mapTileIndex);
                        if (state != null) { // check for concurrency reasons
                            removeTileFromQueues(mapTileIndex);
                            if (mCallback != null) mCallback.mapTileRequestFailedExceedsMaxQueueSize(state);
                            return false;
                        }
                    }
                }
                return false;
            }
            */
        };
    }

    public void loadMapTileAsync(final MapTileRequestState pState, final IMapTileProviderCallback callback) {
        mCallback = callback;
        // Make sure we're not detached
        if (mExecutor.isShutdown())
            return;

        synchronized (mQueueLockObject) {
            final Long cMapTileIndex = pState.getMapTileIndex();
            if (cMapTileIndex == null) return;
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".loadMaptileAsync() on provider: " + getName() + " for tile: " + MapTileIndex.toString(pState));
                if (mPending.containsKey(cMapTileIndex))
                    Log.d(IMapView.LOGTAG, TAG+".loadMaptileAsync() tile already exists in request queue for modular provider. Moving to front of queue.");
                else
                    Log.d(IMapView.LOGTAG, TAG+".loadMaptileAsync() adding tile to request queue for modular provider.");
            }

            // this will put the tile in the queue, or move it to the front of the queue if it's already present
            mPending.put(cMapTileIndex, pState);
        }
        try {
            mExecutor.execute(getTileLoader());
        } catch (final RejectedExecutionException e) {
            Log.w(IMapView.LOGTAG, "RejectedExecutionException", e);
        }
    }

    private void clearQueue() {
        synchronized (mQueueLockObject) {
            mPending.clear();
            mWorking.clear();
        }
    }

    /**
     * Detach, we're shutting down - Stops all workers.
     */
    public void detach() {
        this.clearQueue();
        this.mExecutor.shutdown();

    }

    public int getPendingCount() {
        synchronized (mQueueLockObject) {
            return mPending.size();
        }
    }

    protected void removeTileFromQueues(final long pMapTileIndex) {
        synchronized (mQueueLockObject) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, "MapTileModuleProviderBase.removeTileFromQueues() on provider: " + getName() + " for tile: " + MapTileIndex.toString(pMapTileIndex));
            }
            mPending.remove(pMapTileIndex);
            mWorking.remove(pMapTileIndex);
        }
    }

    /**
     * Load the requested tile. An abstract internal class whose objects are used by worker threads
     * to acquire tiles from servers. It processes tiles from the 'pending' set to the 'working' set
     * as they become available. The key unimplemented method is 'loadTile'.
     */
    public abstract class TileLoader implements Runnable {
        private static final String TAG = "TileLoader";
        private final IConfigurationProvider mConfiguration = Configuration.getInstance();
        public TileLoader() {

        }

        /**
         * Actual load of the requested tile.
         * Do implement this method, but call {@link #loadTileIfReachable(long)} instead
         *
         * @return the tile if it was loaded successfully, or null if failed to
         * load and other tile providers need to be called
         * @throw CantContinueException
         * @since 6.0.0
         */
        public abstract Drawable loadTile(final long pMapTileIndex)
                throws CantContinueException;

        /**
         * @since 6.1.3
         */
        @Nullable
        public Drawable loadTileIfReachable(final long pMapTileIndex)
                throws CantContinueException {
            if (!isTileReachable(pMapTileIndex)) {
                return null;
            }
            return loadTile(pMapTileIndex);
        }

        @IMapTileProviderCallback.TILEPROVIDERTYPE
        public abstract int getProviderType();

        @Deprecated
        @Nullable
        protected Drawable loadTile(MapTileRequestState pState)
                throws CantContinueException {
            final Long cMapTileIndex = pState.getMapTileIndex();
            if (cMapTileIndex == null) return null;
            return loadTileIfReachable(cMapTileIndex);
        }

        @CallSuper
        protected void onTileLoaderInit() {
            // Do nothing by default
        }

        @CallSuper
        protected void onTileLoaderShutdown() {
            // Do nothing by default
        }

        @Nullable
        protected MapTileRequestState nextTile() {

            synchronized (mQueueLockObject) {
                Long result = null;

                // get the most recently accessed tile
                // - the last item in the iterator that's not already being processed
                final Iterator<Long> iterator = mPending.keySet().iterator();

                // TODO this iterates the whole list, make this faster...
                Long cMapTileIndex;
                while (iterator.hasNext()) {
                    cMapTileIndex = iterator.next();
                    if (!mWorking.containsKey(cMapTileIndex)) {
                        if (this.mConfiguration.isDebugTileProviders()) {
                            Log.d(IMapView.LOGTAG, TAG+".nextTile() on provider: " + getName()
                                    + " found tile in working queue: " + MapTileIndex.toString(cMapTileIndex));
                        }
                        result = cMapTileIndex;
                    }
                }

                if (result != null) {
                    if (this.mConfiguration.isDebugTileProviders()) {
                        Log.d(IMapView.LOGTAG, TAG+".nextTile() on provider: " + getName()
                                + " adding tile to working queue: " + result);
                    }
                    mWorking.put(result, mPending.get(result));
                }

                return (result != null ? mPending.get(result) : null);
            }
        }

        protected void onTileLoaderStart(@NonNull final MapTileRequestState pState, final int pending, final int working) {
            if (this.mConfiguration.isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".tileLoaded() on provider: " + getName() + " starting loading tile: "
                        + MapTileIndex.toString(pState));
            }
            if (mCallback != null) mCallback.mapTileRequestStarted(pState, pending, working);
        }
        /**
         * A tile has loaded.
         */
        protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable, final IMapTileProviderCallback callback) {
            if (this.mConfiguration.isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".tileLoaded() on provider: " + getName() + " with tile: "
                        + MapTileIndex.toString(pState));
            }
            final Long cMapTileIndex = pState.getMapTileIndex();
            if (cMapTileIndex == null) return;
            removeTileFromQueues(cMapTileIndex);
            ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.UP_TO_DATE);
            if (mCallback != null) mCallback.mapTileRequestCompleted(pState, pDrawable);
        }

        /**
         * A tile has loaded but it's expired.
         * Return it <b>and</b> send request to next provider.
         */
        protected void tileLoadedExpired(final MapTileRequestState pState, final Drawable pDrawable) {
            if (this.mConfiguration.isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".tileLoadedExpired() on provider: " + getName()
                        + " with tile: " + MapTileIndex.toString(pState));
            }
            final Long cMapTileIndex = pState.getMapTileIndex();
            if (cMapTileIndex == null) return;
            removeTileFromQueues(cMapTileIndex);
            ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.EXPIRED);
            if (mCallback != null) mCallback.mapTileRequestExpiredTile(pState, pDrawable);
        }

        protected void tileLoadedScaled(final MapTileRequestState pState, final Drawable pDrawable) {
            if (this.mConfiguration.isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".tileLoadedScaled() on provider: " + getName()
                        + " with tile: " + MapTileIndex.toString(pState));
            }
            final Long cMapTileIndex = pState.getMapTileIndex();
            if (cMapTileIndex == null) return;
            removeTileFromQueues(cMapTileIndex);
            ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.SCALED);
            if (mCallback != null) mCallback.mapTileRequestExpiredTile(pState, pDrawable);
        }


        protected void tileLoadedFailed(final MapTileRequestState pState) {
            if (this.mConfiguration.isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".tileLoadedFailed() on provider: " + getName()
                        + " with tile: " + MapTileIndex.toString(pState));
            }
            final Long cMapTileIndex = pState.getMapTileIndex();
            if (cMapTileIndex == null) return;
            removeTileFromQueues(cMapTileIndex);
            if (mCallback != null) mCallback.mapTileRequestFailed(pState);
        }

        protected void tileDiscartedDueToOutOfViewBounds(final MapTileRequestState pState) {
            if (this.mConfiguration.isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".tileDiscartedDueToOutOfViewBounds() on provider: " + getName()
                        + " with tile: " + MapTileIndex.toString(pState));
            }
            final Long cMapTileIndex = pState.getMapTileIndex();
            if (cMapTileIndex == null) return;
            removeTileFromQueues(cMapTileIndex);
            if (mCallback != null) mCallback.mapTileRequestDiscartedDueToOutOfViewBounds(pState);
        }

        /**
         * This is a functor class of type Runnable. The run method is the encapsulated function.
         */
        @Override
        final public void run() {

            onTileLoaderInit();

            MapTileRequestState state;
            Drawable result = null;
            @Nullable
            Long cMapTileIndex;
            while ((state = nextTile()) != null) {
                cMapTileIndex = state.getMapTileIndex();
                if (mConfiguration.isDebugTileProviders()) {
                    Log.d(IMapView.LOGTAG, TAG+".run() processing next tile: " + MapTileIndex.toString(cMapTileIndex));
                }
                if (cMapTileIndex == null) continue;
                try {
                    result = null;

                    synchronized (mQueueLockObject) {
                        onTileLoaderStart(state, mPending.size(), mWorking.size());

                        if (mTilesOutOfViewBounds.containsKey(cMapTileIndex)) {
                            if (mConfiguration.isDebugTileProviders()) {
                                Log.d(IMapView.LOGTAG, TAG+".run() discarted tile "
                                        + MapTileIndex.toString(cMapTileIndex)
                                        + " because out of current view boundaries"
                                );
                            }
                            tileDiscartedDueToOutOfViewBounds(state);
                            mTilesOutOfViewBounds.remove(cMapTileIndex);
                            continue;
                        }
                    }

                    result = loadTileIfReachable(cMapTileIndex);
                } catch (final CantContinueException e) {
                    Log.i(IMapView.LOGTAG, "Tile loader can't continue: " + MapTileIndex.toString(cMapTileIndex), e);
                    clearQueue();
                } catch (final Throwable e) {
                    Log.i(IMapView.LOGTAG, "Error downloading tile: " + MapTileIndex.toString(cMapTileIndex), e);
                }

                if (result == null) {
                    tileLoadedFailed(state);
                } else if (ExpirableBitmapDrawable.getState(result) == ExpirableBitmapDrawable.EXPIRED) {
                    tileLoadedExpired(state, result);
                } else if (ExpirableBitmapDrawable.getState(result) == ExpirableBitmapDrawable.SCALED) {
                    tileLoadedScaled(state, result);
                } else {
                    tileLoaded(state, result, mCallback);
                }
            }

            onTileLoaderShutdown();
        }
    }

    /** {@inheritDoc} */
    @Override
    @UiThread @MainThread
    public void onViewBoundingBoxChanged(@NonNull final Rect fromBounds, final int fromZoom, @NonNull final Rect toBounds, final int toZoom) {
        if (!mLastViewBoundingBox.equals(toBounds)) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, TAG+".onViewBoundingBoxChanged() on provider: " + getName());
            }

            synchronized (mQueueLockObject) {
                final Iterator<Long> iterator = mPending.keySet().iterator();
                Long cMapTileIndex;
                int cX, cY, cZ;
                while (iterator.hasNext()) {
                    cMapTileIndex = iterator.next();
                    cX = MapTileIndex.getX(cMapTileIndex);
                    cY = MapTileIndex.getY(cMapTileIndex);
                    cZ = MapTileIndex.getZoom(cMapTileIndex);
                    if ((!toBounds.contains(cX, cY) && (cZ == fromZoom)) || (cZ != toZoom)) {
                        mTilesOutOfViewBounds.put(cMapTileIndex, cMapTileIndex);
                    }
                }
            }

            mLastViewBoundingBox.set(toBounds);
        }
    }

}

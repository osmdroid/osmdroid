package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * An abstract base class for modular tile providers
 *
 * @author Marc Kurtz
 * @author Neil Boyd
 */
public abstract class MapTileModuleProviderBase {

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

    protected final Object mQueueLockObject = new Object();
    protected final HashMap<Long, MapTileRequestState> mWorking;
    protected final LinkedHashMap<Long, MapTileRequestState> mPending;

    public MapTileModuleProviderBase(int pThreadPoolSize, final int pPendingQueueSize) {
        if (pPendingQueueSize < pThreadPoolSize) {
            Log.w(IMapView.LOGTAG, "The pending queue size is smaller than the thread pool size. Automatically reducing the thread pool size.");
            pThreadPoolSize = pPendingQueueSize;
        }
        mExecutor = Executors.newFixedThreadPool(pThreadPoolSize,
                new ConfigurablePriorityThreadFactory(Thread.NORM_PRIORITY, getThreadGroupName()));

        mWorking = new HashMap<>();
        mPending = new LinkedHashMap<Long, MapTileRequestState>(pPendingQueueSize + 2, 0.1f,
                true) {

            private static final long serialVersionUID = 6455337315681858866L;

            @Override
            protected boolean removeEldestEntry(
                    final Map.Entry<Long, MapTileRequestState> pEldest) {
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
                            state.getCallback().mapTileRequestFailedExceedsMaxQueueSize(state);
                            return false;
                        }
                    }
                }
                return false;
            }
        };
    }

    public void loadMapTileAsync(final MapTileRequestState pState) {
        // Make sure we're not detached
        if (mExecutor.isShutdown())
            return;

        synchronized (mQueueLockObject) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, "MapTileModuleProviderBase.loadMaptileAsync() on provider: "
                        + getName() + " for tile: " + MapTileIndex.toString(pState.getMapTile()));
                if (mPending.containsKey(pState.getMapTile()))
                    Log.d(IMapView.LOGTAG, "MapTileModuleProviderBase.loadMaptileAsync() tile already exists in request queue for modular provider. Moving to front of queue.");
                else
                    Log.d(IMapView.LOGTAG, "MapTileModuleProviderBase.loadMaptileAsync() adding tile to request queue for modular provider.");
            }

            // this will put the tile in the queue, or move it to the front of
            // the queue if it's already present
            mPending.put(pState.getMapTile(), pState);
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

    protected void removeTileFromQueues(final long pMapTileIndex) {
        synchronized (mQueueLockObject) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, "MapTileModuleProviderBase.removeTileFromQueues() on provider: "
                        + getName() + " for tile: " + MapTileIndex.toString(pMapTileIndex));
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

        /**
         * Actual load of the requested tile.
         * Do implement this method, but call {@link #loadTileIfReachable(long)} instead
         *
         * @return the tile if it was loaded successfully, or null if failed to
         * load and other tile providers need to be called
         * @throws CantContinueException
         * @since 6.0.0
         */
        public abstract Drawable loadTile(final long pMapTileIndex)
                throws CantContinueException;

        /**
         * @since 6.1.3
         */
        public Drawable loadTileIfReachable(final long pMapTileIndex)
                throws CantContinueException {
            if (!isTileReachable(pMapTileIndex)) {
                return null;
            }
            return loadTile(pMapTileIndex);
        }

        @Deprecated
        protected Drawable loadTile(MapTileRequestState pState)
                throws CantContinueException {
            return loadTileIfReachable(pState.getMapTile());
        }

        protected void onTileLoaderInit() {
            // Do nothing by default
        }

        protected void onTileLoaderShutdown() {
            // Do nothing by default
        }

        protected MapTileRequestState nextTile() {

            synchronized (mQueueLockObject) {
                Long result = null;

                // get the most recently accessed tile
                // - the last item in the iterator that's not already being
                // processed
                Iterator<Long> iterator = mPending.keySet().iterator();

                // TODO this iterates the whole list, make this faster...
                while (iterator.hasNext()) {
                    final Long mapTileIndex = iterator.next();
                    if (!mWorking.containsKey(mapTileIndex)) {
                        if (Configuration.getInstance().isDebugTileProviders()) {
                            Log.d(IMapView.LOGTAG, "TileLoader.nextTile() on provider: " + getName()
                                    + " found tile in working queue: " + MapTileIndex.toString(mapTileIndex));
                        }
                        result = mapTileIndex;
                    }
                }

                if (result != null) {
                    if (Configuration.getInstance().isDebugTileProviders()) {
                        Log.d(IMapView.LOGTAG, "TileLoader.nextTile() on provider: " + getName()
                                + " adding tile to working queue: " + result);
                    }
                    mWorking.put(result, mPending.get(result));
                }

                return (result != null ? mPending.get(result) : null);
            }
        }

        /**
         * A tile has loaded.
         */
        protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, "TileLoader.tileLoaded() on provider: " + getName() + " with tile: "
                        + MapTileIndex.toString(pState.getMapTile()));
            }
            removeTileFromQueues(pState.getMapTile());
            ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.UP_TO_DATE);
            pState.getCallback().mapTileRequestCompleted(pState, pDrawable);
        }

        /**
         * A tile has loaded but it's expired.
         * Return it <b>and</b> send request to next provider.
         */
        protected void tileLoadedExpired(final MapTileRequestState pState, final Drawable pDrawable) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, "TileLoader.tileLoadedExpired() on provider: " + getName()
                        + " with tile: " + MapTileIndex.toString(pState.getMapTile()));
            }
            removeTileFromQueues(pState.getMapTile());
            ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.EXPIRED);
            pState.getCallback().mapTileRequestExpiredTile(pState, pDrawable);
        }

        protected void tileLoadedScaled(final MapTileRequestState pState, final Drawable pDrawable) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, "TileLoader.tileLoadedScaled() on provider: " + getName()
                        + " with tile: " + MapTileIndex.toString(pState.getMapTile()));
            }
            removeTileFromQueues(pState.getMapTile());
            ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.SCALED);
            pState.getCallback().mapTileRequestExpiredTile(pState, pDrawable);
        }


        protected void tileLoadedFailed(final MapTileRequestState pState) {
            if (Configuration.getInstance().isDebugTileProviders()) {
                Log.d(IMapView.LOGTAG, "TileLoader.tileLoadedFailed() on provider: " + getName()
                        + " with tile: " + MapTileIndex.toString(pState.getMapTile()));
            }
            removeTileFromQueues(pState.getMapTile());
            pState.getCallback().mapTileRequestFailed(pState);
        }

        /**
         * This is a functor class of type Runnable. The run method is the encapsulated function.
         */
        @Override
        final public void run() {

            onTileLoaderInit();

            MapTileRequestState state;
            Drawable result = null;
            while ((state = nextTile()) != null) {
                if (Configuration.getInstance().isDebugTileProviders()) {
                    Log.d(IMapView.LOGTAG, "TileLoader.run() processing next tile: "
                            + MapTileIndex.toString(state.getMapTile())
                            + ", pending:" + mPending.size()
                            + ", working:" + mWorking.size()
                    );
                }
                try {
                    result = null;
                    result = loadTileIfReachable(state.getMapTile());
                } catch (final CantContinueException e) {
                    Log.i(IMapView.LOGTAG, "Tile loader can't continue: " + MapTileIndex.toString(state.getMapTile()), e);
                    clearQueue();
                } catch (final Throwable e) {
                    Log.i(IMapView.LOGTAG, "Error downloading tile: " + MapTileIndex.toString(state.getMapTile()), e);
                }

                if (result == null) {
                    tileLoadedFailed(state);
                } else if (ExpirableBitmapDrawable.getState(result) == ExpirableBitmapDrawable.EXPIRED) {
                    tileLoadedExpired(state, result);
                } else if (ExpirableBitmapDrawable.getState(result) == ExpirableBitmapDrawable.SCALED) {
                    tileLoadedScaled(state, result);
                } else {
                    tileLoaded(state, result);
                }
            }

            onTileLoaderShutdown();
        }
    }
}

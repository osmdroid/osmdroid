package org.osmdroid.tileprovider.modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.drawable.Drawable;
import android.util.Log;
import org.osmdroid.api.IMapView;

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
	 * Sets the tile source for this tile provider.
	 *
	 * @param tileSource
	 *            the tile source
	 */
	public abstract void setTileSource(ITileSource tileSource);

	private final ExecutorService mExecutor;

	protected final Object mQueueLockObject = new Object();
	protected final HashMap<MapTile, MapTileRequestState> mWorking;
	protected final LinkedHashMap<MapTile, MapTileRequestState> mPending;

	public MapTileModuleProviderBase(int pThreadPoolSize, final int pPendingQueueSize) {
		if (pPendingQueueSize < pThreadPoolSize) {
               Log.w(IMapView.LOGTAG,"The pending queue size is smaller than the thread pool size. Automatically reducing the thread pool size.");
			pThreadPoolSize = pPendingQueueSize;
		}
		mExecutor = Executors.newFixedThreadPool(pThreadPoolSize,
				new ConfigurablePriorityThreadFactory(Thread.NORM_PRIORITY, getThreadGroupName()));

		mWorking = new HashMap<MapTile, MapTileRequestState>();
		mPending = new LinkedHashMap<MapTile, MapTileRequestState>(pPendingQueueSize + 2, 0.1f,
				true) {

			private static final long serialVersionUID = 6455337315681858866L;

			@Override
			protected boolean removeEldestEntry(
					final Map.Entry<MapTile, MapTileRequestState> pEldest) {
				if (size() > pPendingQueueSize) {
					MapTile result = null;

					// get the oldest tile that isn't in the mWorking queue
					Iterator<MapTile> iterator = mPending.keySet().iterator();

					while (result == null && iterator.hasNext()) {
						final MapTile tile = iterator.next();
						if (!mWorking.containsKey(tile)) {
							result = tile;
						}
					}

					if (result != null) {
						MapTileRequestState state = mPending.get(result);
						removeTileFromQueues(result);
						state.getCallback().mapTileRequestFailedExceedsMaxQueueSize(state);
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
				Log.d(IMapView.LOGTAG,"MapTileModuleProviderBase.loadMaptileAsync() on provider: "
						+ getName() + " for tile: " + pState.getMapTile());
				if (mPending.containsKey(pState.getMapTile()))
					Log.d(IMapView.LOGTAG,"MapTileModuleProviderBase.loadMaptileAsync() tile already exists in request queue for modular provider. Moving to front of queue.");
				else
					Log.d(IMapView.LOGTAG,"MapTileModuleProviderBase.loadMaptileAsync() adding tile to request queue for modular provider.");
			}

			// this will put the tile in the queue, or move it to the front of
			// the queue if it's already present
			mPending.put(pState.getMapTile(), pState);
		}
		try {
			mExecutor.execute(getTileLoader());
		} catch (final RejectedExecutionException e) {
			Log.w(IMapView.LOGTAG,"RejectedExecutionException", e);
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

	protected void removeTileFromQueues(final MapTile mapTile) {
		synchronized (mQueueLockObject) {
			if (Configuration.getInstance().isDebugTileProviders()) {
				Log.d(IMapView.LOGTAG,"MapTileModuleProviderBase.removeTileFromQueues() on provider: "
						+ getName() + " for tile: " + mapTile);
			}
			mPending.remove(mapTile);
			mWorking.remove(mapTile);
		}
	}

	/**
	 * Load the requested tile. An abstract internal class whose objects are used by worker threads
	 * to acquire tiles from servers. It processes tiles from the 'pending' set to the 'working' set
	 * as they become available. The key unimplemented method is 'loadTile'.
	 */
	public abstract class TileLoader implements Runnable {

		/**
		 * Load the requested tile.
		 *
		 * @since 6.0.0
		 * @return the tile if it was loaded successfully, or null if failed to
		 *         load and other tile providers need to be called
		 * @param pTile
		 * @throws CantContinueException
		 */
		public abstract Drawable loadTile(final MapTile pTile)
				throws CantContinueException;

		@Deprecated
		protected Drawable loadTile(MapTileRequestState pState)
				throws CantContinueException {
			return loadTile(pState.getMapTile());
		}

		protected void onTileLoaderInit() {
			// Do nothing by default
		}

		protected void onTileLoaderShutdown() {
			// Do nothing by default
		}

		protected MapTileRequestState nextTile() {

			synchronized (mQueueLockObject) {
				MapTile result = null;

				// get the most recently accessed tile
				// - the last item in the iterator that's not already being
				// processed
				Iterator<MapTile> iterator = mPending.keySet().iterator();

				// TODO this iterates the whole list, make this faster...
				while (iterator.hasNext()) {
					final MapTile tile = iterator.next();
					if (!mWorking.containsKey(tile)) {
						if (Configuration.getInstance().isDebugTileProviders()) {
							Log.d(IMapView.LOGTAG,"TileLoader.nextTile() on provider: " + getName()
									+ " found tile in working queue: " + tile);
						}
						result = tile;
					}
				}

				if (result != null) {
					if (Configuration.getInstance().isDebugTileProviders()) {
						Log.d(IMapView.LOGTAG,"TileLoader.nextTile() on provider: " + getName()
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
				Log.d(IMapView.LOGTAG,"TileLoader.tileLoaded() on provider: " + getName() + " with tile: "
						+ pState.getMapTile());
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
				Log.d(IMapView.LOGTAG,"TileLoader.tileLoadedExpired() on provider: " + getName()
						+ " with tile: " + pState.getMapTile());
			}
			removeTileFromQueues(pState.getMapTile());
			ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.EXPIRED);
			pState.getCallback().mapTileRequestExpiredTile(pState, pDrawable);
		}

		protected void tileLoadedScaled(final MapTileRequestState pState, final Drawable pDrawable) {
			if (Configuration.getInstance().isDebugTileProviders()) {
				Log.d(IMapView.LOGTAG,"TileLoader.tileLoadedScaled() on provider: " + getName()
						+ " with tile: " + pState.getMapTile());
			}
			removeTileFromQueues(pState.getMapTile());
			ExpirableBitmapDrawable.setState(pDrawable, ExpirableBitmapDrawable.SCALED);
			pState.getCallback().mapTileRequestExpiredTile(pState, pDrawable);
		}


		protected void tileLoadedFailed(final MapTileRequestState pState) {
			if (Configuration.getInstance().isDebugTileProviders()) {
				Log.d(IMapView.LOGTAG,"TileLoader.tileLoadedFailed() on provider: " + getName()
						+ " with tile: " + pState.getMapTile());
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
					Log.d(IMapView.LOGTAG,"TileLoader.run() processing next tile: "
							+ state.getMapTile()
							+ ", pending:" + mPending.size()
							+ ", working:" + mWorking.size()
					);
				}
				try {
					result = null;
					result = loadTile(state.getMapTile());
				} catch (final CantContinueException e) {
					Log.i(IMapView.LOGTAG,"Tile loader can't continue: " + state.getMapTile(), e);
					clearQueue();
				} catch (final Throwable e) {
					Log.i(IMapView.LOGTAG,"Error downloading tile: " + state.getMapTile(), e);
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

	/**
	 * Thrown by a tile provider module in TileLoader.loadTile() to signal that it can no longer
	 * function properly. This will typically clear the pending queue.
	 */
	public class CantContinueException extends Exception {
		private static final long serialVersionUID = 146526524087765133L;

		public CantContinueException(final String pDetailMessage) {
			super(pDetailMessage);
		}

		public CantContinueException(final Throwable pThrowable) {
			super(pThrowable);
		}
	}
}

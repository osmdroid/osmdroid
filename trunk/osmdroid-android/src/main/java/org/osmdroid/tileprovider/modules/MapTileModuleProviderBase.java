package org.osmdroid.tileprovider.modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;

/**
 * An abstract base class for modular tile providers
 *
 * @author Marc Kurtz
 * @author Neil Boyd
 */
public abstract class MapTileModuleProviderBase implements OpenStreetMapTileProviderConstants {

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
	protected abstract Runnable getTileLoader();

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

	private static final Logger logger = LoggerFactory.getLogger(MapTileModuleProviderBase.class);

	protected final Object mQueueLockObject = new Object();
	protected final HashMap<MapTile, MapTileRequestState> mWorking;
	protected final LinkedHashMap<MapTile, MapTileRequestState> mPending;

	public MapTileModuleProviderBase(int pThreadPoolSize, final int pPendingQueueSize) {
		if (pPendingQueueSize < pThreadPoolSize) {
			logger.warn("The pending queue size is smaller than the thread pool size. Automatically reducing the thread pool size.");
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
						state.getCallback().mapTileRequestFailed(state);
					}
				}
				return false;
			}
		};
	}

	public void loadMapTileAsync(final MapTileRequestState pState) {
		synchronized (mQueueLockObject) {
			// this will put the tile in the queue, or move it to the front of
			// the queue if it's already present
			mPending.put(pState.getMapTile(), pState);
		}
		try {
			mExecutor.execute(getTileLoader());
		} catch (final RejectedExecutionException e) {
			logger.warn("RejectedExecutionException", e);
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

	void removeTileFromQueues(final MapTile mapTile) {
		synchronized (mQueueLockObject) {
			mPending.remove(mapTile);
			mWorking.remove(mapTile);
		}
	}

	/**
	 * Load the requested tile. An abstract internal class whose objects are used by worker threads
	 * to acquire tiles from servers. It processes tiles from the 'pending' set to the 'working' set
	 * as they become available. The key unimplemented method is 'loadTile'.
	 */
	protected abstract class TileLoader implements Runnable {

		/**
		 * Load the requested tile.
		 *
		 * @return the tile if it was loaded successfully, or null if failed to
		 *         load and other tile providers need to be called
		 * @param pState
		 * @throws {@link CantContinueException}
		 */
		protected abstract Drawable loadTile(MapTileRequestState pState)
				throws CantContinueException;

		protected void onTileLoaderInit() {
			// Do nothing by default
		}

		protected void onTileLoaderShutdown() {
			// Do nothing by default
		}

		private MapTileRequestState nextTile() {

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
						result = tile;
					}
				}

				if (result != null) {
					mWorking.put(result, mPending.get(result));
				}

				return (result != null ? mPending.get(result) : null);
			}
		}

		/**
		 * A tile has loaded.
		 */
		protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
			removeTileFromQueues(pState.getMapTile());
			pState.getCallback().mapTileRequestCompleted(pState, pDrawable);
		}

		/**
		 * A tile has loaded but it's expired.
		 * Return it <b>and</b> send request to next provider.
		 */
		protected void tileLoadedExpired(final MapTileRequestState pState, final Drawable pDrawable) {
			removeTileFromQueues(pState.getMapTile());
			pState.getCallback().mapTileRequestExpiredTile(pState, pDrawable);
		}

		protected void tileLoadedFailed(final MapTileRequestState pState) {
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
				if (DEBUGMODE) {
					logger.debug("Next tile: " + state.getMapTile());
				}
				try {
					result = null;
					result = loadTile(state);
				} catch (final CantContinueException e) {
					logger.info("Tile loader can't continue: " + state.getMapTile(), e);
					clearQueue();
				} catch (final Throwable e) {
					logger.error("Error downloading tile: " + state.getMapTile(), e);
				}

				if (result == null) {
					tileLoadedFailed(state);
				} else if (ExpirableBitmapDrawable.isDrawableExpired(result)) {
					tileLoadedExpired(state, result);
				} else {
					tileLoaded(state, result);
				}

				if (DEBUGMODE) {
					logger.debug("No more tiles");
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

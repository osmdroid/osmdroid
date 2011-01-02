package org.osmdroid.tileprovider;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.osmdroid.tileprovider.OpenStreetMapAsyncTileProvider.CantContinueException;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.views.util.IOpenStreetMapRendererInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;

/**
 * An abstract base class for modular tile providers
 * 
 * @author Marc Kurtz
 * @author Neil Boyd
 */
public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapTileProviderConstants {

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
	public abstract void setTileSource(IOpenStreetMapRendererInfo tileSource);

	private static final Logger logger = LoggerFactory
			.getLogger(OpenStreetMapAsyncTileProvider.class);

	private final int mThreadPoolSize;
	private final ThreadGroup mThreadPool = new ThreadGroup(getThreadGroupName());
	private final ConcurrentHashMap<OpenStreetMapTile, OpenStreetMapTileRequestState> mWorking;
	final LinkedHashMap<OpenStreetMapTile, OpenStreetMapTileRequestState> mPending;

	public OpenStreetMapAsyncTileProvider(final int aThreadPoolSize, final int aPendingQueueSize) {
		mThreadPoolSize = aThreadPoolSize;
		mWorking = new ConcurrentHashMap<OpenStreetMapTile, OpenStreetMapTileRequestState>();
		mPending = new LinkedHashMap<OpenStreetMapTile, OpenStreetMapTileRequestState>(
				aPendingQueueSize + 2, 0.1f, true) {
			private static final long serialVersionUID = 6455337315681858866L;
		};
	}

	public void loadMapTileAsync(final OpenStreetMapTileRequestState aState) {

		final int activeCount = mThreadPool.activeCount();

		synchronized (mPending) {
			// this will put the tile in the queue, or move it to the front of
			// the queue if it's already present
			mPending.put(aState.getMapTile(), aState);
		}

		if (DEBUGMODE)
			logger.debug(activeCount + " active threads");
		if (activeCount < mThreadPoolSize) {
			final Thread t = new Thread(mThreadPool, getTileLoader());
			t.start();
		}
	}

	private void clearQueue() {
		synchronized (mPending) {
			mPending.clear();
		}
		mWorking.clear();
	}

	/**
	 * Detach, we're shutting down - Stops all workers.
	 */
	public void detach() {
		this.clearQueue();
		this.mThreadPool.interrupt();
	}

	private void removeTileFromQueues(OpenStreetMapTile mapTile) {
		synchronized (mPending) {
			mPending.remove(mapTile);
		}
		mWorking.remove(mapTile);
	}

	/**
	 * Load the requested tile. An abstract internal class whose objects are used by worker threads
	 * to acquire tiles from servers. It processes tiles from the 'pending' set to the 'working' set
	 * as they become available. The key unimplemented method is 'loadTile'.
	 * 
	 * @param aTile
	 *            the tile to load
	 * @throws CantContinueException
	 *             if it is not possible to continue with processing the queue
	 */
	protected abstract class TileLoader implements Runnable {

		/**
		 * The key unimplemented method.
		 * 
		 * @return true if the tile was loaded successfully and other tile providers need not be
		 *         called, false otherwise
		 * @param aTile
		 * @throws {@link CantContinueException}
		 */
		protected abstract Drawable loadTile(OpenStreetMapTileRequestState aState)
				throws CantContinueException;

		private OpenStreetMapTileRequestState nextTile() {

			synchronized (mPending) {
				OpenStreetMapTile result = null;

				// get the most recently accessed tile
				// - the last item in the iterator that's not already being
				// processed
				Iterator<OpenStreetMapTile> iterator = mPending.keySet().iterator();

				// TODO this iterates the whole list, make this faster...
				while (iterator.hasNext()) {
					try {
						final OpenStreetMapTile tile = iterator.next();
						if (!mWorking.containsKey(tile)) {
							result = tile;
						}
					} catch (final ConcurrentModificationException e) {
						if (DEBUGMODE)
							logger.warn("ConcurrentModificationException break: "
									+ (result != null));

						// if we've got a result return it, otherwise try again
						if (result != null) {
							break;
						} else {
							iterator = mPending.keySet().iterator();
						}
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
		 * 
		 * @param aTile
		 *            the tile that has loaded
		 * @param aTileInputStream
		 *            the input stream of the file.
		 */
		private void tileLoaded(final OpenStreetMapTileRequestState aState, final Drawable aDrawable) {
			removeTileFromQueues(aState.getMapTile());

			aState.getCallback().mapTileRequestCompleted(aState, aDrawable);
		}

		protected void tileCandidateLoaded(final OpenStreetMapTileRequestState aState,
				final Drawable aDrawable) {
			aState.getCallback().mapTileRequestCandidate(aState, aDrawable);
		}

		private void tileLoadedFailed(final OpenStreetMapTileRequestState aState) {
			removeTileFromQueues(aState.getMapTile());

			aState.getCallback().mapTileRequestFailed(aState);
		}

		/**
		 * This is a functor class of type Runnable. The run method is the encapsulated function.
		 */
		@Override
		final public void run() {

			OpenStreetMapTileRequestState state;
			Drawable result = null;
			while ((state = nextTile()) != null) {
				if (DEBUGMODE)
					logger.debug("Next tile: " + state);
				try {
					result = null;
					result = loadTile(state);
				} catch (final CantContinueException e) {
					logger.info("Tile loader can't continue", e);
					clearQueue();
				} catch (final Throwable e) {
					logger.error("Error downloading tile: " + state, e);
				}

				if (result != null)
					tileLoaded(state, result);
				else
					tileLoadedFailed(state);

				if (DEBUGMODE)
					logger.debug("No more tiles");
			}
		}
	}

	class CantContinueException extends Exception {
		private static final long serialVersionUID = 146526524087765133L;

		public CantContinueException(final String aDetailMessage) {
			super(aDetailMessage);
		}

		public CantContinueException(final Throwable aThrowable) {
			super(aThrowable);
		}
	}
}

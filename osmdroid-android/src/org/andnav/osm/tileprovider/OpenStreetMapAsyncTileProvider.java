package org.andnav.osm.tileprovider;

import java.util.HashSet;
import java.util.TreeSet;

import org.andnav.osm.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapTileProviderConstants {

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapAsyncTileProvider.class);
	
	/** max threads */
	private final int mThreadPoolSize;
	/** max pending */
	private final int mPendingQueueSize;
	/** my group of workers */
	private final ThreadGroup mThreadPool = new ThreadGroup(threadGroupName());
	/** these guys are beeing loaded right now  */
	private final HashSet<OpenStreetMapTile> mWorking;
	/** The guys we're still working on.  Left package for testing */
	final TreeSet<PendingEntry> mPending;
	
	protected final IOpenStreetMapTileProviderCallback mCallback;
	
	public OpenStreetMapAsyncTileProvider(final IOpenStreetMapTileProviderCallback pCallback, final int aThreadPoolSize, final int aPendingQueueSize) {
		mCallback = pCallback;
		mThreadPoolSize = aThreadPoolSize;
		mPendingQueueSize = aPendingQueueSize;
		mPending = new TreeSet<PendingEntry>();
		mWorking = new HashSet<OpenStreetMapTile>();		
	}
	
	public void loadMapTileAsync(final OpenStreetMapTile aTile) {

		final int activeCount = mThreadPool.activeCount();
		
		// we're already loading this guy
		synchronized (mWorking) {
			if( mWorking.contains(aTile) ) {
				return;
			}
		}

		// this will put the tile in the queue, or move it to the front of the
		// queue if it's already present
		synchronized (mPending) {
			PendingEntry ent = new PendingEntry(aTile);
			mPending.remove(ent);
			mPending.add(ent);
			
			if( mPending.size() > mPendingQueueSize ) {
				mPending.remove(mPending.last());
			}
		}

		if (DEBUGMODE)
			logger.debug(activeCount + " active threads");
		if (activeCount < mThreadPoolSize) {
			final Thread t = new Thread(mThreadPool, getTileLoader());
			t.start();
		}
	}

	private void clearQueue() {
		synchronized (mWorking) {
			mWorking.clear();
		}
		synchronized (mPending) {
			mPending.clear();	
		}
	}
	
	/**
	 * Stops all workers, the service is shutting down.
	 */
	public void stopWorkers()
	{
		this.clearQueue();
		this.mThreadPool.interrupt();
	}
	
	protected abstract String threadGroupName();
	
	protected abstract Runnable getTileLoader();
	
	protected interface TileLoaderCallback {
		/**
		 * A tile has loaded.
		 * @param aTile the tile that has loaded
		 * @param aTilePath the path of the file. May be null.
		 * @param aRefresh whether to redraw the screen so that new tiles will be used
		 */
		void tileLoaded(OpenStreetMapTile aTile, String aTilePath, boolean aRefresh);
	}

	protected abstract class TileLoader implements Runnable, TileLoaderCallback {
		/**
		 * Load the requested tile.
		 * @param aTile the tile to load
		 * @throws CantContinueException if it is not possible to continue with processing the queue
		 * @throws CloudmadeException if there's an error authorizing for Cloudmade tiles
		 */
		protected abstract void loadTile(OpenStreetMapTile aTile) throws CantContinueException;

		private OpenStreetMapTile nextTile() {
			OpenStreetMapTile nextTile = null;
			synchronized (mPending) {
				if( mPending.size() > 0 ) {
					PendingEntry ent = mPending.first();
					mPending.remove(ent);
					nextTile = ent.tile;
				}
			}
			if( nextTile != null ) {
				synchronized (mWorking) {
					mWorking.add(nextTile);
				}
			}
			return nextTile;
		}

		@Override
		public void tileLoaded(final OpenStreetMapTile aTile, final String aTilePath, final boolean aRefresh) {
			synchronized (mWorking) {
				mWorking.remove(aTile);
			}
			if (aRefresh) {
				mCallback.mapTileRequestCompleted(aTile, aTilePath);
			}
		}

		@Override
		final public void run() {

			OpenStreetMapTile tile;
			while ((tile = nextTile()) != null) {
				if (DEBUGMODE)
					logger.debug("Next tile: " + tile);
				try {
					loadTile(tile);
				} catch (final CantContinueException e) {
					logger.info("Tile loader can't continue", e);
					clearQueue();
				} catch (final Exception e) {
					logger.error("Error downloading tile: " + tile, e);
				}
			}
			if (DEBUGMODE)
				logger.debug("No more tiles");
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
	
	private static class PendingEntry implements Comparable<PendingEntry> {
		
		private final long insertTime;
		private final OpenStreetMapTile tile;
	
		public PendingEntry(OpenStreetMapTile tile) {
			this.insertTime = System.currentTimeMillis();
			this.tile = tile;
		}
		
		@Override
		public int compareTo(PendingEntry entry) {
			if( tile.equals(entry.tile) )
				return 0;
			return insertTime > entry.insertTime ? -1 : insertTime < entry.insertTime ? 1 : 0;
		}
	}
}

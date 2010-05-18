package org.andnav.osm.tileprovider;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.andnav.osm.tileprovider.constants.OpenStreetMapTileProviderConstants;

import android.util.Log;

public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapTileProviderConstants {

	private final int mThreadPoolSize;
	private final int mPendingQueueSize;
	private final ThreadGroup mThreadPool = new ThreadGroup(debugtag());
	private final HashMap<OpenStreetMapTile, Object> mWorking;
	final LinkedHashMap<OpenStreetMapTile, Object> mPending;
	private static final Object PRESENT = new Object();
	
	protected final IOpenStreetMapTileProviderCallback mCallback;
	
	public OpenStreetMapAsyncTileProvider(final IOpenStreetMapTileProviderCallback pCallback, final int aThreadPoolSize, final int aPendingQueueSize) {
		mCallback = pCallback;
		mThreadPoolSize = aThreadPoolSize;
		mPendingQueueSize = aPendingQueueSize;
		mWorking = new HashMap<OpenStreetMapTile, Object>();
		mPending = new LinkedHashMap<OpenStreetMapTile, Object>(aPendingQueueSize + 2, 0.1f, true) {
			private static final long serialVersionUID = 6455337315681858866L;
			@Override
			protected boolean removeEldestEntry(Entry<OpenStreetMapTile, Object> pEldest) {
				return size() > mPendingQueueSize;
			}
		};
	}
	
	public void loadMapTileAsync(final OpenStreetMapTile aTile) {

		final int activeCount = mThreadPool.activeCount();

		// sanity check
		if (activeCount == 0 && !mPending.isEmpty()) {
			Log.w(debugtag(), "Unexpected - no active threads but pending queue not empty");
			clearQueue();
		}

		// this will put the tile in the queue, or move it to the front of the
		// queue if it's already present
		// FIXME it sometimes puts duplicates in the list
		mPending.put(aTile, PRESENT);

		if (DEBUGMODE)
			Log.d(debugtag(), activeCount + " active threads");
		if (activeCount < mThreadPoolSize) {
			final Thread t = new Thread(mThreadPool, getTileLoader());
			t.start();
		}
	}
	
	private void clearQueue() {
		mPending.clear();
		mWorking.clear();
	}
	
	/**
	 * Stops all workers, the service is shutting down.
	 */
	public void stopWorkers()
	{
		this.clearQueue();
		this.mThreadPool.interrupt();
	}
	
	/**
	 * The debug tag.
	 * Because the tag of the abstract class is not so interesting.
	 * @return
	 */
	protected abstract String debugtag();
	
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
		 */
		protected abstract void loadTile(OpenStreetMapTile aTile) throws CantContinueException;

		private OpenStreetMapTile nextTile() {
			
			synchronized (mPending) {
				OpenStreetMapTile result = null;

				// get the most recently accessed tile
				// - the last item in the iterator that's not already being processed
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
							Log.d(debugtag(), "ConcurrentModificationException break: " + (result != null));

						// if we've got a result return it, otherwise try again
						if (result != null) {
							break;
						} else {
							iterator = mPending.keySet().iterator();
						}
					}
				}
				
				if (result != null)
				{
					mWorking.put(result, PRESENT);					
				}
				
				return result;					
			}
		}

		@Override
		public void tileLoaded(final OpenStreetMapTile aTile, final String aTilePath, final boolean aRefresh) {

			mPending.remove(aTile);
			mWorking.remove(aTile);

			if (aRefresh) {
				mCallback.mapTileRequestCompleted(aTile, aTilePath);
			}
		}

		@Override
		final public void run() {

			OpenStreetMapTile tile;
			while ((tile = nextTile()) != null) {
				if (DEBUGMODE)
					Log.d(debugtag(), "Next tile: " + tile);
				try {
					loadTile(tile);
				} catch (final CantContinueException e) {
					Log.i(debugtag(), "Tile loader can't continue");
					clearQueue();
				} catch (final Throwable e) {
					Log.e(debugtag(), "Error downloading tile: " + tile, e);
				}
			}
			if (DEBUGMODE)
				Log.d(debugtag(), "No more tiles");
		}
	}
	
	protected class CantContinueException extends Exception {
		private static final long serialVersionUID = 146526524087765133L;
	}
}

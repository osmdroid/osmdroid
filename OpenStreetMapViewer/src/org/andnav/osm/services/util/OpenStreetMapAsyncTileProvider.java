package org.andnav.osm.services.util;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.services.util.constants.OpenStreetMapServiceConstants;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;

public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapServiceConstants {

	private final int mThreadPoolSize;
	private final int mPendingQueueSize;
	private final ThreadGroup mThreadPool = new ThreadGroup(debugtag());
	private final HashMap<OpenStreetMapTile, Object> mWorking;
	private final LinkedHashMap<OpenStreetMapTile, Object> mPending;
	private static final Object PRESENT = new Object();
	
	public OpenStreetMapAsyncTileProvider(final int aThreadPoolSize, final int aPendingQueueSize) {
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
	
	public void loadMapTileAsync(final OpenStreetMapTile aTile,
			final IOpenStreetMapTileProviderCallback aCallback) {

		final int activeCount = mThreadPool.activeCount();

		// sanity check
		if (activeCount == 0 && !mPending.isEmpty()) {
			Log.w(debugtag(), "Unexpected - no active threads but pending queue not empty");
			mPending.clear();
		}

		// this will put the tile in the queue, or move it to the front of the
		// queue if it's already present
		mPending.put(aTile, PRESENT);

		if (DEBUGMODE)
			Log.d(debugtag(), activeCount + " active threads");
		if (activeCount < mThreadPoolSize) {
			final Thread t = new Thread(mThreadPool, getTileLoader(aCallback));
			t.start();
		}
	}
	
	/**
	 * The debug tag.
	 * Because the tag of the abstract class is not so interesting.
	 * @return
	 */
	protected abstract String debugtag();
	
	protected abstract Runnable getTileLoader(final IOpenStreetMapTileProviderCallback aTileProviderCallback);
	
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
		final IOpenStreetMapTileProviderCallback mTileProviderCallback;

		public TileLoader(final IOpenStreetMapTileProviderCallback aTileProviderCallback) {
			mTileProviderCallback = aTileProviderCallback;
		}
		
		/**
		 * Load the requested tile.
		 * @param aTile the tile to load
		 * @param aTileLoaderCallback the callback to notify when the tile has loaded
		 * @throws CantContinueException if it is not possible to continue with processing the queue
		 */
		protected abstract void loadTile(OpenStreetMapTile aTile, TileLoaderCallback aTileLoaderCallback) throws CantContinueException;

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
				try {
					mTileProviderCallback.mapTileRequestCompleted(aTile.rendererID, aTile.zoomLevel, aTile.x, aTile.y, aTilePath);
				} catch (final DeadObjectException e) {
					// our caller has died so there's not much point carrying on
					Log.e(debugtag(), "Caller has died");
					clearQueue();
				} catch (final RemoteException e) {
					Log.e(debugtag(), "Service failed", e);
				}
			}
		}

		private void clearQueue() {
			mPending.clear();
			mWorking.clear();
		}
		
		@Override
		final public void run() {

			OpenStreetMapTile tile;
			while ((tile = nextTile()) != null) {
				if (DEBUGMODE)
					Log.d(debugtag(), "Next tile: " + tile);
				try {
					loadTile(tile, this);
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

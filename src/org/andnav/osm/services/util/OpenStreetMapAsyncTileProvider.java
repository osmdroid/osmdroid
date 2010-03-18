package org.andnav.osm.services.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.services.util.constants.OpenStreetMapServiceConstants;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;

public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapServiceConstants {

	private final int mThreadPoolSize;
	private final int mPendingQueueSize;
	private final ThreadGroup mThreadPool = new ThreadGroup(debugtag());
	private final LinkedHashMap<OpenStreetMapTile, Object> mPending;
    private static final Object PRESENT = new Object();
	
	public OpenStreetMapAsyncTileProvider(final int aThreadPoolSize, final int aPendingQueueSize) {
		mThreadPoolSize = aThreadPoolSize;
		mPendingQueueSize = aPendingQueueSize;
		mPending = new LinkedHashMap<OpenStreetMapTile, Object>(aPendingQueueSize + 2, 0.1f, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry(Entry<OpenStreetMapTile, Object> pEldest) {
				final boolean max = size() > mPendingQueueSize;
				return max;
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
	
	protected abstract Runnable getTileLoader(final IOpenStreetMapTileProviderCallback aCallback);

	protected abstract class TileLoader implements Runnable {
		final IOpenStreetMapTileProviderCallback mCallback;
		private Iterator<OpenStreetMapTile> mIterator;

		public TileLoader(final IOpenStreetMapTileProviderCallback aCallback) {
			mCallback = aCallback;
		}
		
		/**
		 * Load the requested tile.
		 * @param aTile the tile to load
		 * @return the path of the requested tile
		 * @throws CantContinueException if it is not possible to continue with processing the queue
		 */
		protected abstract String loadTile(OpenStreetMapTile aTile) throws CantContinueException;

		private OpenStreetMapTile nextTile() {
			while(true) {
				if (mIterator == null) {
					mIterator = mPending.keySet().iterator();
				}
				if (!mIterator.hasNext()) {
					return null;
				}
				try {
					synchronized (mPending) {
						final OpenStreetMapTile tile = mIterator.next();
						try {
							mIterator.remove();
						} catch(ConcurrentModificationException e) {
							// couldn't remove this request
							// never mind, we'll process it again
						}
						catch(NoSuchElementException e) {
							// we shouldn't get this, but just in case
							return null;
						}
						return tile;
					}
				} catch(ConcurrentModificationException e) {
					// get a new iterator and try again
					mIterator = null;
				}
			}
		}
		
		@Override
		final public void run() {

			OpenStreetMapTile tile;
			while ((tile = nextTile()) != null) {
				if (DEBUGMODE)
					Log.d(debugtag(), "Next tile: " + tile);
				String path = null;
				try {
					path = loadTile(tile);
				} catch (final CantContinueException e) {
					Log.i(debugtag(), "Tile loader can't continue");
					mPending.clear();
				} catch (final Throwable e) {
					Log.e(debugtag(), "Error downloading tile: " + tile, e);
				} finally {
					// Tell the callback we've finished.
					try {
						mCallback.mapTileRequestCompleted(tile.rendererID, tile.zoomLevel, tile.x, tile.y, path);
					} catch (DeadObjectException e) {
						// our caller has died so there's not much point
						// carrying on
						Log.e(debugtag(), "Caller has died");
						break;
					} catch (RemoteException e) {
						Log.e(debugtag(), "Service failed", e);
					}
				}
			}
			if (DEBUGMODE)
				Log.d(debugtag(), "No more tiles");
			mPending.clear();
		}
	}
	
	protected class CantContinueException extends Exception {
		private static final long serialVersionUID = 146526524087765133L;
	}
}

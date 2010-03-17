package org.andnav.osm.services.util;

import java.util.Stack;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.services.util.constants.OpenStreetMapServiceConstants;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;

public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapServiceConstants {

	private final int mPoolSize;
	private final ThreadGroup mThreadPool = new ThreadGroup(debugtag());
	private final Stack<OpenStreetMapTile> mPending = new Stack<OpenStreetMapTile>();
	
	public OpenStreetMapAsyncTileProvider(final int aPoolSize) {
		mPoolSize = aPoolSize;
	}
	
	public void loadMapTileAsync(final OpenStreetMapTile aTile,
			final IOpenStreetMapTileProviderCallback aCallback) {

		final int activeCount = mThreadPool.activeCount();

		// sanity check
		if (activeCount == 0 && !mPending.isEmpty()) {
			Log.w(debugtag(), "Unexpected - no active threads but pending queue not empty");
			mPending.clear();
		}

		if(mPending.contains(aTile)) {
			if (DEBUGMODE)
				Log.d(debugtag(), "Pending, ignore: " + aTile);
			return;
		}
		
		mPending.push(aTile);

		if (DEBUGMODE)
			Log.d(debugtag(), activeCount + " active threads");
		if (activeCount < mPoolSize) {
			final Thread t = new Thread(mThreadPool, getTileLoader(aCallback));
			t.start();
		}
	}
	
	/**
	 * Get the next tile.
	 * @return the tile, or null if there are no more requested
	 */
	protected OpenStreetMapTile getTile() {
		if (mPending.empty()) {
			return null;
		} else {
			return mPending.pop();
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
		
		@Override
		final public void run() {

			while(!mPending.empty()) {
				final OpenStreetMapTile tile = mPending.pop();
				if(DEBUGMODE)
					Log.d(debugtag(), "Next tile: " + tile);
				String path = null;
				try {
					path = loadTile(tile);
				} catch(final CantContinueException e) {
					Log.i(debugtag(), "Tile loader can't continue");
					mPending.clear();
				} catch(final Throwable e) {
					Log.e(debugtag(), "Error downloading tile: " + tile, e);
				} finally {
					// Tell the callback we've finished.
					try {
						mCallback.mapTileRequestCompleted(tile.rendererID, tile.zoomLevel, tile.x, tile.y, path);
					} catch(DeadObjectException e) {
						// our caller has died so there's not much point carrying on
						Log.e(debugtag(), "Caller has died");
						break;
					} catch (RemoteException e) {
						Log.e(debugtag(), "Service failed", e);
					}
				}
			}
			if(DEBUGMODE)
				Log.d(debugtag(), "No more tiles");
		}
	}
	
	protected class CantContinueException extends Exception {
		private static final long serialVersionUID = 146526524087765133L;
	}
}

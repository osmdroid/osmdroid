package org.andnav.osm.services.util;

import java.util.Stack;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.services.util.constants.OpenStreetMapServiceConstants;

import android.os.RemoteException;
import android.util.Log;

public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapServiceConstants {

	private static final String DEBUGTAG = "OSM_ASYNC_PROVIDER";
	
	private final int mPoolSize;
	private final ThreadGroup mThreadPool = new ThreadGroup(DEBUGTAG);
	private final Stack<OpenStreetMapTile> mPending = new Stack<OpenStreetMapTile>();
	
	public OpenStreetMapAsyncTileProvider(final int aPoolSize) {
		mPoolSize = aPoolSize;
	}
	
	public void loadMapTileAsync(final OpenStreetMapTile aTile,
			final IOpenStreetMapTileProviderCallback aCallback) {

		final int activeCount = mThreadPool.activeCount();

		// sanity check
		if (activeCount == 0 && !mPending.isEmpty()) {
			Log.w(DEBUGTAG, "Unexpected - no active threads but pending queue not empty");
			mPending.clear();
		}

		if(mPending.contains(aTile)) {
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "Pending, ignore: " + aTile);
			return;
		}
		
		mPending.push(aTile);

		if (DEBUGMODE)
			Log.d(DEBUGTAG, "mPoolSize=" + mPoolSize + " activeCount=" + activeCount);
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
		 */
		protected abstract String loadTile(OpenStreetMapTile aTile);
		
		@Override
		final public void run() {

			while(!mPending.empty()) {
				final OpenStreetMapTile tile = mPending.pop();
				if(DEBUGMODE)
					Log.d(DEBUGTAG, "Next tile: " + tile);
				String path = null;
				try {
					path = loadTile(tile);
				} catch(final Throwable e) {
					Log.e(DEBUGTAG, "Error downloading tile: " + tile, e);
				} finally {
					// Tell the callback we've finished.
					try {
						mCallback.mapTileRequestCompleted(tile.rendererID, tile.zoomLevel, tile.x, tile.y, path);
					} catch (RemoteException e) {
						Log.e(DEBUGTAG, "Service failed", e);
					}
				}
			}
			if(DEBUGMODE)
				Log.d(DEBUGTAG, "No more tiles");
		}
	}
}

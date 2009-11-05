package org.andnav.osm.services.util;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;

public abstract class OpenStreetMapAsyncTileProvider {

	protected ExecutorService mThreadPool;
	private final HashSet<String> mPending = new HashSet<String>();
	
	public void loadMapTileAsync(final OpenStreetMapTile aTile,
			final IOpenStreetMapTileProviderCallback aCallback) {
		final String tileID = aTile.toString();
		
		if(this.mPending.contains(tileID))
			return;
		
		this.mPending.add(tileID);

		this.mThreadPool.execute(getTileLoader(aTile, aCallback));
	}
	
	protected abstract Runnable getTileLoader(final OpenStreetMapTile aTile,
			final IOpenStreetMapTileProviderCallback aCallback);

	protected abstract class TileLoader implements Runnable {
		final OpenStreetMapTile mTile;
		final IOpenStreetMapTileProviderCallback mCallback;
		
		public TileLoader(final OpenStreetMapTile aTile, final IOpenStreetMapTileProviderCallback aCallback) {
			mTile = aTile;
			mCallback = aCallback;
		}
		
		protected void finished() {
			mPending.remove(mTile.toString());
		}
	}
	
}

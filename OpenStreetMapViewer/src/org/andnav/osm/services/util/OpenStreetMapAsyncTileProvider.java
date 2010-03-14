package org.andnav.osm.services.util;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.services.util.constants.OpenStreetMapServiceConstants;

import android.util.Log;

public abstract class OpenStreetMapAsyncTileProvider implements OpenStreetMapServiceConstants {

	private static final String DEBUGTAG = "OSM_ASYNC_PROVIDER";
	
	private final ExecutorService mThreadPool;
	private final HashSet<String> mPending = new HashSet<String>();
	
	public void loadMapTileAsync(final OpenStreetMapTile aTile,
			final IOpenStreetMapTileProviderCallback aCallback) {
		final String tileID = aTile.toString();
		
		if(this.mPending.contains(tileID)) {
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "Pending, ignore: " + tileID);
			return;
		}
		
		this.mPending.add(tileID);

		this.mThreadPool.execute(getTileLoader(aTile, aCallback));
	}
	
	public OpenStreetMapAsyncTileProvider(final int aPoolSize) {
		mThreadPool = Executors.newFixedThreadPool(aPoolSize);
	}
	
	protected abstract Runnable getTileLoader(final OpenStreetMapTile aTile,
			final IOpenStreetMapTileProviderCallback aCallback);

	protected abstract class TileLoader implements Runnable {
		OpenStreetMapTile mTile;
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

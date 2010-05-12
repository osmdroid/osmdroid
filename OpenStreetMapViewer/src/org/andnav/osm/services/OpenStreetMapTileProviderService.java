package org.andnav.osm.services;

import org.andnav.osm.services.util.OpenStreetMapTile;
import org.andnav.osm.services.util.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.services.util.constants.OpenStreetMapServiceConstants;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * The OpenStreetMapTileProviderService can download map tiles from a server
 * and stores them in a file system cache.
 * @author Manuel Stahl
 */
public class OpenStreetMapTileProviderService extends Service implements OpenStreetMapServiceConstants {

	private static final String DEBUGTAG = "OSM_TILE_PROVIDER_SERVICE";

	private OpenStreetMapTileFilesystemProvider mFileSystemProvider;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mFileSystemProvider = new OpenStreetMapTileFilesystemProvider(getBaseContext());
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onConfigurationChanged(Configuration pNewConfig) {
		if(DEBUGMODE)
			Log.d(DEBUGTAG, "onConfigurationChanged");
		super.onConfigurationChanged(pNewConfig);
	}

	@Override
	public void onDestroy() {
		if(DEBUGMODE)
			Log.d(DEBUGTAG, "onDestroy");
		mFileSystemProvider.stopWorkers();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		if(DEBUGMODE)
			Log.d(DEBUGTAG, "onLowMemory");
		super.onLowMemory();
	}

	@Override
	public void onRebind(Intent pIntent) {
		if(DEBUGMODE)
			Log.d(DEBUGTAG, "onRebind");
		super.onRebind(pIntent);
	}

	@Override
	public void onStart(Intent pIntent, int pStartId) {
		if(DEBUGMODE)
			Log.d(DEBUGTAG, "onStart");
		super.onStart(pIntent, pStartId);
	}

	@Override
	public boolean onUnbind(Intent pIntent) {
		if(DEBUGMODE)
			Log.d(DEBUGTAG, "onUnbind");
		return super.onUnbind(pIntent);
	}

	/**
	 * The IRemoteInterface is defined through IDL
	 */
	private final IOpenStreetMapTileProviderService.Stub mBinder = new IOpenStreetMapTileProviderService.Stub() {
		@Override
		public void requestMapTile(int rendererID, int zoomLevel, int tileX,
				int tileY, IOpenStreetMapTileProviderCallback callback)
				throws RemoteException {

			OpenStreetMapTile tile = new OpenStreetMapTile(rendererID, zoomLevel, tileX, tileY);
			mFileSystemProvider.loadMapTileAsync(tile, callback);
		}
	};

}

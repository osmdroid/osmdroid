package org.andnav.osm.services;

import org.andnav.osm.services.util.OpenStreetMapTileFilesystemProvider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * The OpenStreetMapTileProviderService can download map tiles from a server
 * and stores them in a file system cache.
 * @author Manuel Stahl
 */
public class OpenStreetMapTileProviderService extends Service {

	public static final int MAPTILE_SUCCESS_ID = 0;
	public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;
	
	private OpenStreetMapTileFilesystemProvider mFileSystemProvider;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mFileSystemProvider = new OpenStreetMapTileFilesystemProvider(
				this.getBaseContext(), 4 * 1024 * 1024); // 4MB FSCache
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * The IRemoteInterface is defined through IDL
	 */
	private final IOpenStreetMapTileProviderService.Stub mBinder = new IOpenStreetMapTileProviderService.Stub() {
		@Override
		public void getMapTile(String aTileURLString, IOpenStreetMapTileProviderCallback callback)
				throws RemoteException {
			mFileSystemProvider.loadMapTileAsync(aTileURLString, callback);
		}
	};

}

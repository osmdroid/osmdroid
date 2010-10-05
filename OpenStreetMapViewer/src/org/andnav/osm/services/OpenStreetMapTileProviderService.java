package org.andnav.osm.services;

import java.io.InputStream;

import org.andnav.osm.services.constants.OpenStreetMapServiceConstants;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.IRegisterReceiver;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.tileprovider.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.tileprovider.util.CloudmadeUtil;
import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * The OpenStreetMapTileProviderService can download map tiles from a server
 * and stores them in a file system cache.
 * @author Manuel Stahl
 */
public class OpenStreetMapTileProviderService extends Service implements OpenStreetMapServiceConstants, IOpenStreetMapTileProviderCallback {

	private static final String DEBUGTAG = "OSM_TILE_PROVIDER_SERVICE";

	private OpenStreetMapTileFilesystemProvider mFileSystemProvider;
	private IOpenStreetMapTileProviderServiceCallback mCallback;

	@Override
	public void onCreate() {
		super.onCreate();
		final Context applicationContext = this.getApplicationContext();
		final IRegisterReceiver registerReceiver = new IRegisterReceiver() {
			@Override
			public Intent registerReceiver(final BroadcastReceiver aReceiver, final IntentFilter aFilter) {
				return applicationContext.registerReceiver(aReceiver, aFilter);
			}
			@Override
			public void unregisterReceiver(final BroadcastReceiver aReceiver) {
				applicationContext.unregisterReceiver(aReceiver);
			}
		};
		mFileSystemProvider = new OpenStreetMapTileFilesystemProvider(this, registerReceiver);
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

	@Override
	public void mapTileRequestCompleted(final OpenStreetMapTile pTile, final String pTilePath) {
		try {
			mCallback.mapTileRequestCompleted(pTile.getRenderer().name(), pTile.getZoomLevel(), pTile.getX(), pTile.getY(), pTilePath);
		} catch (final RemoteException e) {
			Log.e(DEBUGTAG, "Error invoking callback", e);
		}
	}

	@Override
	public void mapTileRequestCompleted(final OpenStreetMapTile pTile, final InputStream pTileInputStream) {
		// TODO implementation
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void mapTileRequestCompleted(final OpenStreetMapTile pTile) {
		// TODO implementation
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public String getCloudmadeKey() {
		return CloudmadeUtil.getCloudmadeKey(this);
	}

	@Override
	public boolean useDataConnection() {
		// TODO implementation
		return true;
	}

	/**
	 * The IRemoteInterface is defined through IDL
	 */
	private final IOpenStreetMapTileProviderService.Stub mBinder = new IOpenStreetMapTileProviderService.Stub() {
		@Override
		public void setCallback(final IOpenStreetMapTileProviderServiceCallback pCallback) throws RemoteException {
			mCallback = pCallback;
		}
		@Override
		public void requestMapTile(String rendererName, int zoomLevel, int tileX, int tileY) throws RemoteException {
			final IOpenStreetMapRendererInfo renderer = OpenStreetMapRendererFactory.getRenderer(rendererName);
			OpenStreetMapTile tile = new OpenStreetMapTile(renderer, zoomLevel, tileX, tileY);
			mFileSystemProvider.loadMapTileAsync(tile);
		}
	};

}

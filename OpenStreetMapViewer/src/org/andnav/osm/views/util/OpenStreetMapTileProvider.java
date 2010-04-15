// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import java.io.File;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.services.IOpenStreetMapTileProviderService;
import org.andnav.osm.services.util.OpenStreetMapTile;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class OpenStreetMapTileProvider implements ServiceConnection, OpenStreetMapConstants,
		OpenStreetMapViewConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	/** cache provider */
	protected OpenStreetMapTileCache mTileCache;

	
	/**
	 * Service is bound, but maybe not still connected.
	 */
	private boolean mServiceBound;
	private IOpenStreetMapTileProviderService mTileService;
	private Handler mDownloadFinishedHandler;

	private Context mContext;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileProvider(final Context ctx,
			final Handler aDownloadFinishedListener) {
		this.mContext = ctx;
		this.mTileCache = new OpenStreetMapTileCache();
		
		this.bindToService();
		
		this.mDownloadFinishedHandler = aDownloadFinishedListener;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	public void onServiceConnected(final ComponentName name, final IBinder service) {
		mTileService = IOpenStreetMapTileProviderService.Stub.asInterface(service);
		try {
			mDownloadFinishedHandler.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);
		} catch(Exception e) {
			Log.e(DEBUGTAG, "Error sending success message on connect", e);
		}
		Log.d(DEBUGTAG, "connected");
	};
	
	@Override
	public void onServiceDisconnected(final ComponentName name) {
		this.onDisconnect();
		Log.d(DEBUGTAG, "disconnected");
	}
	
	// ===========================================================
	// Methods
	// ===========================================================


	public void ensureCapacity(final int aCapacity) {
		mTileCache.ensureCapacity(aCapacity);
	}
	
	private boolean bindToService()
	{
		if (this.mServiceBound)
			return true;
		
		boolean success = this.mContext.bindService(new Intent(IOpenStreetMapTileProviderService.class.getName()), this, Context.BIND_AUTO_CREATE);
		
		if (!success)
			Log.e(DEBUGTAG, "Could not bind to " + IOpenStreetMapTileProviderService.class.getName());
		
		this.mServiceBound = success;
		
		return success;
	}

	/***
	 * Disconnects from the tile downloader service.
	 */
	public void disconnectService()
	{
		if (this.mServiceBound)
		{
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "Unbinding service");		
			this.mContext.unbindService(this);
			this.onDisconnect();
		}
	}

	private void onDisconnect()
	{
		this.mServiceBound = false;
		this.mTileService = null;
	}
	
	/**
	 * Get the tile from the cache.
	 * If it's in the cache then it will be returned.
	 * If not it will return null and request it from the service.
	 * In turn, the service will request it from the file system.
	 * If it's found in the file system it will notify the callback.
	 * If not it will initiate a download.
	 * When the download has finished it will notify the callback.
	 * @param aTile the tile being requested
	 * @return the tile bitmap if found in the cache, null otherwise
	 */
	public Bitmap getMapTile(final OpenStreetMapTile aTile) {
		if (this.mTileCache.containsTile(aTile)) { 							// from cache
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "MapTileCache succeeded for: " + aTile);
			return mTileCache.getMapTile(aTile);
		} else { 															// from service
			if (mTileService != null) {
				if (DEBUGMODE)
					Log.d(DEBUGTAG, "Cache failed, trying from FS: " + aTile);
				try {
					mTileService.requestMapTile(aTile.rendererID, aTile.zoomLevel, aTile.x, aTile.y, mServiceCallback);
				} catch (Throwable e) {
					Log.e(DEBUGTAG, "Error getting map tile from tile service: " + aTile, e);
				}
			} else {
				// try to reconnect, but the connection will take time.
				if (!this.bindToService()) {
					if (DEBUGMODE)
						Log.d(DEBUGTAG, "Cache failed, can't get from FS because no tile service: " + aTile);
				} else {
					if (DEBUGMODE)
						Log.d(DEBUGTAG, "Cache failed, tile service still not woken up: " + aTile);
				}
			}

			return null;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	IOpenStreetMapTileProviderCallback mServiceCallback = new IOpenStreetMapTileProviderCallback.Stub() {

		@Override
		public void mapTileRequestCompleted(final int aRendererID, final int aZoomLevel, final int aTileX, final int aTileY, final String aTilePath) throws RemoteException {

			final OpenStreetMapTile tile = new OpenStreetMapTile(aRendererID, aZoomLevel, aTileX, aTileY);
			
			// if the tile path has been returned, add the tile to the cache
			if (aTilePath != null) {
				try {
					final Bitmap bitmap = BitmapFactory.decodeFile(aTilePath);
					if (bitmap != null) {
						mTileCache.putTile(tile, bitmap);
					} else {
						// if we couldn't load it then it's invalid - delete it
						try {
							new File(aTilePath).delete();
						} catch (Throwable e) {
							Log.e(DEBUGTAG, "Error deleting invalid file: " + aTilePath, e);
						}
					}
				} catch (final OutOfMemoryError e) {
					Log.e(DEBUGTAG, "OutOfMemoryError putting tile in cache: " + tile);
					mTileCache.clear();
					System.gc();
				}
			}
			
			// tell our caller we've finished and it should update its view
			mDownloadFinishedHandler.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);

			if (DEBUGMODE)
				Log.d(DEBUGTAG, "MapTile request complete: " + tile);
		}
	};

}

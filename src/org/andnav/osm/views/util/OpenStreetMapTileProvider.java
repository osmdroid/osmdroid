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

	private IOpenStreetMapTileProviderService mTileService;
	private Handler mDownloadFinishedHandler;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileProvider(final Context ctx,
			final Handler aDownloadFinishedListener) {
		this.mTileCache = new OpenStreetMapTileCache();
		
		if(!ctx.bindService(new Intent(IOpenStreetMapTileProviderService.class.getName()), this, Context.BIND_AUTO_CREATE))
			Log.e(DEBUGTAG, "Could not bind to " + IOpenStreetMapTileProviderService.class.getName());
		
		this.mDownloadFinishedHandler = aDownloadFinishedListener;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
		mTileService = IOpenStreetMapTileProviderService.Stub.asInterface(service);
		mDownloadFinishedHandler.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);
		Log.d("Service", "connected");
	};
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		mTileService = null;
		Log.d("Service", "disconnected");
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

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
		if (this.mTileCache.containsTile(aTile)) {							// from cache
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "MapTileCache succeeded for: " + aTile);
			return this.mTileCache.getMapTile(aTile);			
		} else {															// from service
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "Cache failed, trying from FS: " + aTile);
			try {
				this.mTileService.requestMapTile(aTile.rendererID, aTile.zoomLevel, aTile.x, aTile.y, this.mServiceCallback);
			} catch (RemoteException e) {
				Log.e(DEBUGTAG, "Error getting map tile from tile service: " + aTile, e);
			}
			return null;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private IOpenStreetMapTileProviderCallback mServiceCallback = new IOpenStreetMapTileProviderCallback.Stub() {
		
		@Override
		public void mapTileRequestCompleted(int rendererID, int zoomLevel, int tileX, int tileY, String aTilePath) throws RemoteException {
		    if (aTilePath != null) {
    			try {
    				final Bitmap tile = BitmapFactory.decodeFile(aTilePath);
    				if (tile != null) {
    					mTileCache.putTile(new OpenStreetMapTile(rendererID, zoomLevel, tileX, tileY), tile);
    				} else {
    					// if we couldn't load it then it's invalid - delete it
    					try {
    						new File(aTilePath).delete();
    					} catch(Throwable e) {
    						Log.e(DEBUGTAG, "Error deleting invalid file", e);
    					}
    				}
    			} catch(OutOfMemoryError e) {
    				Log.e(DEBUGTAG, "OutOfMemoryError putting tile in cache");
    			}
		    }
			mDownloadFinishedHandler.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "MapTile request complete.");
		}
	};

}

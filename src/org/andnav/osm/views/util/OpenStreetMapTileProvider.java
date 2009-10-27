// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.R;
import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.services.IOpenStreetMapTileProviderService;
import org.andnav.osm.services.OpenStreetMapTileProviderService;
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

	/** place holder if tile not available */
	protected final Bitmap mLoadingMapTile;
	// protected Context mCtx;
	/** cache provider */
	protected OpenStreetMapTileCache mTileCache;

	private IOpenStreetMapTileProviderService mTileService;
	private Handler mDownloadFinishedHandler;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileProvider(final Context ctx,
			final Handler aDownloadFinishedListener) {
		// this.mCtx = ctx;
		this.mLoadingMapTile = BitmapFactory.decodeResource(ctx.getResources(),
				R.drawable.maptile_loading);
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

	public boolean isTileAvailable(final String aTileURLString) {
		return this.mTileCache.containsTile(aTileURLString);
	}

	public Bitmap getMapTile(final String aTileURLString) {
		if (this.mTileCache.containsTile(aTileURLString)) {							// from cache
			if (DEBUGMODE)
				Log.i(DEBUGTAG, "MapTileCache succeded for: " + aTileURLString);
			return this.mTileCache.getMapTile(aTileURLString);
			
		} else {																	// from service
			if (DEBUGMODE)
				Log.i(DEBUGTAG, "Cache failed, trying from FS.");
			try {
				this.mTileService.getMapTile(aTileURLString, this.mServiceCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public void preCacheTile(String aTileURLString) {
		if (!this.mTileCache.containsTile(aTileURLString)) {
			try {
				this.mTileService.getMapTile(aTileURLString, this.mServiceCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private IOpenStreetMapTileProviderCallback mServiceCallback = new IOpenStreetMapTileProviderCallback.Stub() {
		
		@Override
		public void mapTileLoaded(String aTileURLString, Bitmap aTile) throws RemoteException {
			mTileCache.putTile(aTileURLString, aTile);
			mDownloadFinishedHandler
					.sendEmptyMessage(OpenStreetMapTileProviderService.MAPTILE_SUCCESS_ID);
			if (DEBUGMODE)
				Log.i(DEBUGTAG, "MapTile download success.");
		}
		
		@Override
		public void mapTileFailed(String aTileURLString) throws RemoteException {
			if (DEBUGMODE)
				Log.e(DEBUGTAG, "MapTile download error.");
		}
	};

}

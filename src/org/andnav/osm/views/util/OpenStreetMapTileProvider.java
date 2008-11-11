// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.R;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapTileProvider implements OpenStreetMapConstants, OpenStreetMapViewConstants{
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	
	protected final Bitmap mLoadingMapTile;
	protected Context mCtx;
	protected OpenStreetMapTileCache mTileCache;
	protected OpenStreetMapTileFilesystemProvider mFSTileProvider;
	protected OpenStreetMapTileDownloader mTileDownloader;
	private Handler mLoadCallbackHandler = new LoadCallbackHandler();
	private Handler mDownloadFinishedListenerHander;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public OpenStreetMapTileProvider(final Context ctx, final Handler aDownloadFinishedListener) {
		this.mCtx = ctx;
		this.mLoadingMapTile = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.maptile_loading);
		this.mTileCache = new OpenStreetMapTileCache();
		this.mFSTileProvider = new OpenStreetMapTileFilesystemProvider(ctx, 4 * 1024 * 1024, this.mTileCache); // 4MB FSCache
		this.mTileDownloader = new OpenStreetMapTileDownloader(ctx, this.mFSTileProvider);
		this.mDownloadFinishedListenerHander = aDownloadFinishedListener;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public Bitmap getMapTile(final String aTileURLString){
		Bitmap ret = this.mTileCache.getMapTile(aTileURLString);
		if(ret != null){
			if(DEBUGMODE)
				Log.i(DEBUGTAG, "MapTileCache succeded for: " + aTileURLString);
		}else{
			if(DEBUGMODE)
				Log.i(DEBUGTAG, "Cache failed, trying from FS.");
			try {
				this.mFSTileProvider.loadMapTileToMemCacheAsync(aTileURLString, this.mLoadCallbackHandler);
				ret = this.mLoadingMapTile;
			} catch (Exception e) {
				if(DEBUGMODE)
					Log.d(DEBUGTAG, "Error(" + e.getClass().getSimpleName() + ") loading MapTile from Filesystem: " + OpenStreetMapTileNameFormatter.format(aTileURLString));
			}
			if(ret == null){ /* FS did not contain the MapTile, we need to download it asynchronous. */
				if(DEBUGMODE)
					Log.i(DEBUGTAG, "Requesting Maptile for download.");
				ret = this.mLoadingMapTile;
							
				this.mTileDownloader.requestMapTileAsync(aTileURLString, this.mLoadCallbackHandler);
			}
		}
		return ret;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private class LoadCallbackHandler extends Handler{
		@Override
		public void handleMessage(final Message msg) {
			final int what = msg.what;
			switch(what){
				case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID:
					OpenStreetMapTileProvider.this.mDownloadFinishedListenerHander.sendEmptyMessage(OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID);
					if(DEBUGMODE)
						Log.i(DEBUGTAG, "MapTile download success.");
					break;
				case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_FAIL_ID:
					if(DEBUGMODE)
						Log.e(DEBUGTAG, "MapTile download error.");
					break;
					
				case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
					OpenStreetMapTileProvider.this.mDownloadFinishedListenerHander.sendEmptyMessage(OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID);
					if(DEBUGMODE)
						Log.i(DEBUGTAG, "MapTile fs->cache success.");
					break;
				case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_FAIL_ID:
					if(DEBUGMODE)
						Log.e(DEBUGTAG, "MapTile download error.");
					break;
			}
		}
	}

	public void preCacheTile(String aTileURLString) {
		getMapTile(aTileURLString);
	}
}

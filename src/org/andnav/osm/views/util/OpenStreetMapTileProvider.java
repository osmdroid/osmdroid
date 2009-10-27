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
public class OpenStreetMapTileProvider implements OpenStreetMapConstants,
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
	/** file system provider */
	protected OpenStreetMapTileFilesystemProvider mFSTileProvider;
	private Handler mLoadCallbackHandler = new LoadCallbackHandler();
	private Handler mDownloadFinishedListenerHander;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileProvider(final Context ctx,
			final Handler aDownloadFinishedListener) {
		// this.mCtx = ctx;
		this.mLoadingMapTile = BitmapFactory.decodeResource(ctx.getResources(),
				R.drawable.maptile_loading);
		this.mTileCache = new OpenStreetMapTileCache();
		this.mFSTileProvider = new OpenStreetMapTileFilesystemProvider(ctx,
				4 * 1024 * 1024, this.mTileCache); // 4MB FSCache
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

	public boolean isTileAvailable(final String aTileURLString) {
		return this.mTileCache.containsTile(aTileURLString);
	}

	public Bitmap getMapTile(final String aTileURLString) {
		if (this.mTileCache.containsTile(aTileURLString)) {							// cache
			if (DEBUGMODE)
				Log.i(DEBUGTAG, "MapTileCache succeded for: " + aTileURLString);
			return this.mTileCache.getMapTile(aTileURLString);
			
		} else { //  if(this.mFSTileProvider.containsTile(aTileURLString)) {				// file system
			if (DEBUGMODE)
				Log.i(DEBUGTAG, "Cache failed, trying from FS.");
			this.mFSTileProvider.loadMapTileToMemCacheAsync(aTileURLString, this.mLoadCallbackHandler);
		}
		return null;
	}

	public void preCacheTile(String aTileURLString) {
		if (!this.mTileCache.containsTile(aTileURLString)) {
			this.mFSTileProvider.loadMapTileToMemCacheAsync(aTileURLString, this.mLoadCallbackHandler);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private class LoadCallbackHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			final int what = msg.what;
			switch (what) {
			case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID:
				OpenStreetMapTileProvider.this.mDownloadFinishedListenerHander
						.sendEmptyMessage(OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID);
				if (DEBUGMODE)
					Log.i(DEBUGTAG, "MapTile download success.");
				break;
			case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_FAIL_ID:
				if (DEBUGMODE)
					Log.e(DEBUGTAG, "MapTile download error.");
				break;

			case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
				OpenStreetMapTileProvider.this.mDownloadFinishedListenerHander
						.sendEmptyMessage(OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID);
				if (DEBUGMODE)
					Log.i(DEBUGTAG, "MapTile fs->cache success.");
				break;
			case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_FAIL_ID:
				if (DEBUGMODE)
					Log.e(DEBUGTAG, "MapTile download error.");
				break;
			}
		}
	}

}

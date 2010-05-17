// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import java.io.File;

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public abstract class OpenStreetMapTileProvider implements OpenStreetMapViewConstants {
	
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected Context mContext;
	protected OpenStreetMapTileCache mTileCache;
	protected Handler mDownloadFinishedHandler;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileProvider(final Context pContext,
			final Handler pDownloadFinishedListener) {
		mContext = pContext;
		mTileCache = new OpenStreetMapTileCache();
		mDownloadFinishedHandler = pDownloadFinishedListener;
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

	public void mapTileRequestCompleted(final OpenStreetMapTile pTile, final String pTilePath) {

		// if the tile path has been returned, add the tile to the cache
		if (pTilePath != null) {
			try {
				final Bitmap bitmap = BitmapFactory.decodeFile(pTilePath);
				if (bitmap != null) {
					mTileCache.putTile(pTile, bitmap);
				} else {
					// if we couldn't load it then it's invalid - delete it
					try {
						new File(pTilePath).delete();
					} catch (Throwable e) {
						Log.e(DEBUGTAG, "Error deleting invalid file: " + pTilePath, e);
					}
				}
			} catch (final OutOfMemoryError e) {
				Log.e(DEBUGTAG, "OutOfMemoryError putting tile in cache: " + pTile);
				mTileCache.clear();
				System.gc();
			}
		}
		
		// tell our caller we've finished and it should update its view
		mDownloadFinishedHandler.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);

		if (DEBUGMODE)
			Log.d(DEBUGTAG, "MapTile request complete: " + pTile);
	}	
	
	public void ensureCapacity(final int aCapacity) {
		mTileCache.ensureCapacity(aCapacity);
	}

	public abstract Bitmap getMapTile(OpenStreetMapTile pTile);

	public abstract void detach();
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
}

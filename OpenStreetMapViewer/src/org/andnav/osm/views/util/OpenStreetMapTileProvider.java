// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

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

	public OpenStreetMapTileProvider(final Context ctx,
			final Handler aDownloadFinishedListener) {
		mContext = ctx;
		mTileCache = new OpenStreetMapTileCache();
		mDownloadFinishedHandler = aDownloadFinishedListener;
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

	public void ensureCapacity(final int aCapacity) {
		mTileCache.ensureCapacity(aCapacity);
	}

	public abstract Bitmap getMapTile(OpenStreetMapTile pTile);

	public abstract void detach();
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
}

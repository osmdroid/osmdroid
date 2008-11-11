// Created by plusminus on 17:58:57 - 25.09.2008
package org.andnav.osm.views.util;

import java.util.HashMap;

import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.graphics.Bitmap;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapTileCache implements OpenStreetMapViewConstants{
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	
	protected HashMap<String, Bitmap> mCachedTiles;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public OpenStreetMapTileCache(){
		this(CACHE_MAPTILECOUNT_DEFAULT);
	}
	
	/**
	 * @param aMaximumCacheSize Maximum amount of MapTiles to be hold within.
	 */
	public OpenStreetMapTileCache(final int aMaximumCacheSize){
		this.mCachedTiles = new LRUMapTileCache(aMaximumCacheSize);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public synchronized Bitmap getMapTile(final String aTileURLString) {
		return this.mCachedTiles.get(aTileURLString);
	}

	public synchronized void putTile(final String aTileURLString, final Bitmap aTile) {
		this.mCachedTiles.put(aTileURLString, aTile);
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

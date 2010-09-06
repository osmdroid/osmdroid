// Created by plusminus on 17:58:57 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.graphics.drawable.Drawable;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public final class OpenStreetMapTileCache implements OpenStreetMapViewConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected LRUMapTileCache mCachedTiles;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileCache() {
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

	public synchronized void ensureCapacity(final int aCapacity) {
		mCachedTiles.ensureCapacity(aCapacity);
	}

	public synchronized Drawable getMapTile(final OpenStreetMapTile aTile) {
		return this.mCachedTiles.get(aTile);
	}

	public synchronized void putTile(final OpenStreetMapTile aTile, final Drawable aDrawable) {
		if (aDrawable != null) {
			this.mCachedTiles.put(aTile, aDrawable);
		}
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public synchronized boolean containsTile(final OpenStreetMapTile aTile) {
		return this.mCachedTiles.containsKey(aTile);
	}

	public synchronized void clear() {
		this.mCachedTiles.clear();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

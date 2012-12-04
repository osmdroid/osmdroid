// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import android.graphics.drawable.Drawable;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public final class MapTileCache implements OpenStreetMapTileProviderConstants {
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

	public MapTileCache() {
		this(CACHE_MAPTILECOUNT_DEFAULT);
	}

	/**
	 * @param aMaximumCacheSize
	 *            Maximum amount of MapTiles to be hold within.
	 */
	public MapTileCache(final int aMaximumCacheSize) {
		this.mCachedTiles = new LRUMapTileCache(aMaximumCacheSize);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void ensureCapacity(final int aCapacity) {
		synchronized (mCachedTiles) {
			mCachedTiles.ensureCapacity(aCapacity);
		}
	}

	public Drawable getMapTile(final MapTile aTile) {
		synchronized (mCachedTiles) {
			return this.mCachedTiles.get(aTile);
		}
	}

	public void putTile(final MapTile aTile, final Drawable aDrawable) {
		if (aDrawable != null) {
			synchronized (mCachedTiles) {
				this.mCachedTiles.put(aTile, aDrawable);
			}
		}
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public boolean containsTile(final MapTile aTile) {
		synchronized (mCachedTiles) {
			return this.mCachedTiles.containsKey(aTile);
		}
	}

	public void clear() {
		synchronized (mCachedTiles) {
			this.mCachedTiles.clear();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

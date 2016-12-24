// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import android.graphics.drawable.Drawable;

/**
 * In memory cache of tiles
 * @author Nicolas Gramlich
 * 
 */
public class MapTileCache {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Object mCachedTilesLockObject = new Object();
	protected LRUMapTileCache mCachedTiles;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileCache() {
		this(Configuration.getInstance().getCacheMapTileCount());
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
		synchronized (mCachedTilesLockObject) {
			mCachedTiles.ensureCapacity(aCapacity);
		}
	}

	public Drawable getMapTile(final MapTile aTile) {
		synchronized (mCachedTilesLockObject) {
			return this.mCachedTiles.get(aTile);
		}
	}

	public void putTile(final MapTile aTile, final Drawable aDrawable) {
		if (aDrawable != null) {
			synchronized (mCachedTilesLockObject) {
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
		synchronized (mCachedTilesLockObject) {
			return this.mCachedTiles.containsKey(aTile);
		}
	}

	public void clear() {
		synchronized (mCachedTilesLockObject) {
			this.mCachedTiles.clear();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

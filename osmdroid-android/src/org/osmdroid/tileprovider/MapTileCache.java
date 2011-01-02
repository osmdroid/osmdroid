// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

	private final ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

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
		mReadWriteLock.readLock().lock();
		mCachedTiles.ensureCapacity(aCapacity);
		mReadWriteLock.readLock().unlock();
	}

	public Drawable getMapTile(final MapTile aTile) {
		mReadWriteLock.readLock().lock();
		final Drawable result = this.mCachedTiles.get(aTile);
		mReadWriteLock.readLock().unlock();
		return result;
	}

	public void putTile(final MapTile aTile, final Drawable aDrawable) {
		if (aDrawable != null) {
			mReadWriteLock.writeLock().lock();
			this.mCachedTiles.put(aTile, aDrawable);
			mReadWriteLock.writeLock().unlock();
		}
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public boolean containsTile(final MapTile aTile) {
		mReadWriteLock.readLock().lock();
		final boolean result = this.mCachedTiles.containsKey(aTile);
		mReadWriteLock.readLock().unlock();
		return result;
	}

	public void clear() {
		mReadWriteLock.writeLock().lock();
		this.mCachedTiles.clear();
		mReadWriteLock.writeLock().unlock();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

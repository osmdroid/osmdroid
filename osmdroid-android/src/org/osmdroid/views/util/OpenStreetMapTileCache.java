// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.views.util;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.osmdroid.tileprovider.OpenStreetMapTile;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import android.graphics.drawable.Drawable;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public final class OpenStreetMapTileCache implements OpenStreetMapTileProviderConstants {
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

	public OpenStreetMapTileCache() {
		this(CACHE_MAPTILECOUNT_DEFAULT);
	}

	/**
	 * @param aMaximumCacheSize
	 *            Maximum amount of MapTiles to be hold within.
	 */
	public OpenStreetMapTileCache(final int aMaximumCacheSize) {
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

	public Drawable getMapTile(final OpenStreetMapTile aTile) {
		mReadWriteLock.readLock().lock();
		Drawable result = this.mCachedTiles.get(aTile);
		mReadWriteLock.readLock().unlock();
		return result;
	}

	public void putTile(final OpenStreetMapTile aTile, final Drawable aDrawable) {
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

	public boolean containsTile(final OpenStreetMapTile aTile) {
		mReadWriteLock.readLock().lock();
		boolean result = this.mCachedTiles.containsKey(aTile);
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

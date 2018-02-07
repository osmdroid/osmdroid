// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import org.osmdroid.util.MapTileList;

import android.graphics.drawable.Drawable;

import java.util.HashMap;

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

	/**
	 * @since 6.0.0
	 * Was in LRUMapTileCache
	 */
	public interface TileRemovedListener {
		void onTileRemoved(final long pMapTileIndex);
	}

	private TileRemovedListener mTileRemovedListener;
	private final HashMap<Long, Drawable> mCachedTiles = new HashMap<>();
	/**
	 * Tiles currently displayed
	 */
	private final MapTileList mMapTileList = new MapTileList();
	/**
	 * Tiles neighbouring the tiles currently displayed (borders, zoom +-1, ...)
	 */
	private final MapTileList mAdditionalMapTileList = new MapTileList();
	/**
	 * Tiles currently in the cache, without the concurrency side effects
	 */
	private final MapTileList mGC = new MapTileList();

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileCache() {}

	/**
	 * @param aMaximumCacheSize
	 *            Maximum amount of MapTiles to be hold within.
	 */
	@Deprecated
	public MapTileCache(final int aMaximumCacheSize) {}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	@Deprecated
	public void ensureCapacity(final int aCapacity) {}

	public Drawable getMapTile(final long pMapTileIndex) {
		synchronized (mCachedTiles) {
			return this.mCachedTiles.get(pMapTileIndex);
		}
	}

	public void putTile(final long pMapTileIndex, final Drawable aDrawable) {
		if (aDrawable != null) {
			synchronized (mCachedTiles) {
				this.mCachedTiles.put(pMapTileIndex, aDrawable);
			}
		}
	}

	/**
	 * Removes from the memory cache all the tiles that should no longer be there
	 * @since 6.0.0
	 */
	public void garbageCollection() {
		mAdditionalMapTileList.clear();
		mAdditionalMapTileList.populateFrom(mMapTileList, -1);
		mAdditionalMapTileList.populateFrom(mMapTileList, 1);
		populateSyncCachedTiles(mGC);
		for (int i = 0; i < mGC.getSize() ; i ++) {
			final long index = mGC.get(i);
			if (mMapTileList.contains(index)) {
				continue;
			}
			if (mAdditionalMapTileList.contains(index)) {
				continue;
			}
			remove(index);
		}
	}

	/**
	 * @since 6.0.0
	 */
	public MapTileList getMapTileList() {
		return  mMapTileList;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public boolean containsTile(final long pMapTileIndex) {
		synchronized (mCachedTiles) {
			return this.mCachedTiles.containsKey(pMapTileIndex);
		}
	}

	/**
	 * @since 6.0.0
	 * Was in LRUMapTileCache
	 */
	public void clear() {
		// remove them all individually so that they get recycled
		final MapTileList list = new MapTileList(mCachedTiles.size());
		populateSyncCachedTiles(list);
		for (int i = 0; i < list.getSize() ; i ++) {
			final long index = list.get(i);
			remove(index);
		}

		// and then clear
		mCachedTiles.clear();
	}

	/**
	 * @since 6.0.0
	 * Was in LRUMapTileCache
	 */
	public void remove(final long pMapTileIndex) {
		final Drawable drawable = mCachedTiles.remove(pMapTileIndex);
		if (getTileRemovedListener() != null)
			getTileRemovedListener().onTileRemoved(pMapTileIndex);
		BitmapPool.getInstance().asyncRecycle(drawable);
	}

	/**
	 * @since 6.0.0
	 * Was in LRUMapTileCache
	 */
	public TileRemovedListener getTileRemovedListener() {
		return mTileRemovedListener;
	}

	/**
	 * @since 6.0.0
	 * Was in LRUMapTileCache
	 */
	public void setTileRemovedListener(TileRemovedListener tileRemovedListener) {
		mTileRemovedListener = tileRemovedListener;
	}

	/**
	 * Just a helper method in order to parse all indices without concurrency side effects
	 * @since 6.0.0
	 */
	private void populateSyncCachedTiles(final MapTileList pList) {
		synchronized (mCachedTiles) {
			pList.ensureCapacity(mCachedTiles.size());
			pList.clear();
			for (final long index : mCachedTiles.keySet()) {
				pList.put(index);
			}
		}
	}
}

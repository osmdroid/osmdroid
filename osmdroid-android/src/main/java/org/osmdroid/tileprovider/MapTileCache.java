// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.MapTileList;
import org.osmdroid.util.MapTileListComputer;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	private final List<MapTileListComputer> mComputers = new ArrayList<>();

	private int mCapacity;

	private final MapTilePreCache mPreCache;

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
		ensureCapacity(aMaximumCacheSize);
		mPreCache = new MapTilePreCache(this);
	}

	/**
	 * @since 6.0.2
	 */
	public List<MapTileListComputer> getProtectedTileComputers() {
		return mComputers;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void ensureCapacity(final int pCapacity) {
		if (mCapacity < pCapacity) {
			Log.i(IMapView.LOGTAG, "Tile cache increased from " + mCapacity + " to " + pCapacity);
			mCapacity = pCapacity;
		}
	}

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
		final int size = mCachedTiles.size();
		int toBeRemoved = size - mCapacity;
		if (toBeRemoved <= 0) {
			return;
		}
		mAdditionalMapTileList.clear();
		for (final MapTileListComputer computer : mComputers) {
			computer.computeFromSource(mMapTileList, mAdditionalMapTileList);
		}
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
			if (-- toBeRemoved == 0) {
				break;
			};
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
		final MapTileList list = new MapTileList();
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

	/**
	 * @since 6.0.0
	 */
	public int getSize() {
		return mCachedTiles.size();
	}

	/**
	 * Maintenance operations
	 * @since 6.0.2
	 */
	public void maintenance() {
		garbageCollection();
		mPreCache.fill();
	}

	/**
	 * @since 6.0.2
	 */
	public MapTilePreCache getPreCache() {
		return mPreCache;
	}
}

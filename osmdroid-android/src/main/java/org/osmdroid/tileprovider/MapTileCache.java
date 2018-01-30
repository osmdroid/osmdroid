// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import org.osmdroid.util.MapTileList;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.util.HashMap;
import java.util.NoSuchElementException;

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
	private final MapTileList mMapTileList = new MapTileList();
	private final MapTileList mAdditionalMapTileList = new MapTileList();
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
		mGC.clear();
		for (final Long index : mCachedTiles.keySet()) {
			if (mMapTileList.contains(index)) {
				continue;
			}
			if (mAdditionalMapTileList.contains(index)) {
				continue;
			}
			mGC.put(index);
		}
		for (int i = 0 ; i < mGC.getSize() ; i ++) {
			mCachedTiles.remove(mGC.get(i));
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
		while (!mCachedTiles.isEmpty()) {
			try {
				remove(mCachedTiles.keySet().iterator().next());
			} catch (NoSuchElementException nse) {
				// as a protection
				//https://github.com/osmdroid/osmdroid/issues/776
			}
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
		// Only recycle if we are running on a project less than 2.3.3 Gingerbread.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			if (drawable instanceof BitmapDrawable) {
				final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
				if (bitmap != null) {
					bitmap.recycle();
				}
			}
		}
		if (getTileRemovedListener() != null)
			getTileRemovedListener().onTileRemoved(pMapTileIndex);
		if (drawable instanceof ReusableBitmapDrawable)
			BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) drawable);
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
}

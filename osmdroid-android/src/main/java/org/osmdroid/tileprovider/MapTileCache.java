// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.MapTileArea;
import org.osmdroid.util.MapTileAreaComputer;
import org.osmdroid.util.MapTileAreaList;
import org.osmdroid.util.MapTileContainer;
import org.osmdroid.util.MapTileList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * In memory cache of tiles
 *
 * @author Nicolas Gramlich
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
    private final MapTileArea mMapTileArea = new MapTileArea();
    /**
     * Tiles neighbouring the tiles currently displayed (borders, zoom +-1, ...)
     */
    private final MapTileAreaList mAdditionalMapTileList = new MapTileAreaList();
    /**
     * Tiles currently in the cache, without the concurrency side effects
     */
    private final MapTileList mGC = new MapTileList();

    private final List<MapTileAreaComputer> mComputers = new ArrayList<>();

    private int mCapacity;

    private final MapTilePreCache mPreCache;

    /**
     * @since 6.0.2
     */
    private final List<MapTileContainer> mProtectors = new ArrayList<>();

    /**
     * @since 6.0.3
     */
    private boolean mAutoEnsureCapacity;

    /**
     * @since 6.0.4
     */
    private boolean mStressedMemory;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileCache() {
        this(Configuration.getInstance().getCacheMapTileCount());
    }

    /**
     * @param aMaximumCacheSize Maximum amount of MapTiles to be hold within.
     */
    public MapTileCache(final int aMaximumCacheSize) {
        ensureCapacity(aMaximumCacheSize);
        mPreCache = new MapTilePreCache(this);
    }

    /**
     * @since 6.0.2
     */
    public List<MapTileAreaComputer> getProtectedTileComputers() {
        return mComputers;
    }

    /**
     * @since 6.0.2
     */
    public List<MapTileContainer> getProtectedTileContainers() {
        return mProtectors;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * @since 6.0.3
     */
    public void setAutoEnsureCapacity(final boolean pAutoEnsureCapacity) {
        mAutoEnsureCapacity = pAutoEnsureCapacity;
    }

    /**
     * @since 6.0.4
     * When true, all the tiles in the cache that eventually don't belong here are removed asap.
     * When false, we will still remove tiles that do not belong in the cache anymore,
     * but not necessarily all of them: only the amount we need in order to fit the cache size.
     * Should be set to true when you have small memory and big tiles in order to
     * avoid OutOfMemoryException.
     * Should be set to false for better performances.
     */
    public void setStressedMemory(final boolean pStressedMemory) {
        mStressedMemory = pStressedMemory;
    }

    public boolean ensureCapacity(final int pCapacity) {
        if (mCapacity < pCapacity) {
            Log.i(IMapView.LOGTAG, "Tile cache increased from " + mCapacity + " to " + pCapacity);
            mCapacity = pCapacity;
            return true;
        }
        return false;
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
     *
     * @since 6.0.0
     */
    public void garbageCollection() {
        // number of tiles to remove from cache
        int toBeRemoved = Integer.MAX_VALUE; // MAX_VALUE for stressed memory case
        final int size = mCachedTiles.size();
        if (!mStressedMemory) {
            toBeRemoved = size - mCapacity;
            if (toBeRemoved <= 0) {
                return;
            }
        }

        refreshAdditionalLists();

        if (mAutoEnsureCapacity) {
            final int target = mMapTileArea.size() + mAdditionalMapTileList.size();
            if (ensureCapacity(target)) {
                if (!mStressedMemory) {
                    toBeRemoved = size - mCapacity;
                    if (toBeRemoved <= 0) {
                        return;
                    }
                }
            }
        }
        populateSyncCachedTiles(mGC);
        for (int i = 0; i < mGC.getSize(); i++) {
            final long index = mGC.get(i);
            if (shouldKeepTile(index)) {
                continue;
            }
            remove(index);
            if (--toBeRemoved == 0) {
                break;
            }
            ;
        }
    }

    /**
     * @since 6.0.3
     */
    private void refreshAdditionalLists() {
        int index = 0;
        for (final MapTileAreaComputer computer : mComputers) {
            final MapTileArea area;
            if (index < mAdditionalMapTileList.getList().size()) {
                area = mAdditionalMapTileList.getList().get(index);
            } else {
                area = new MapTileArea();
                mAdditionalMapTileList.getList().add(area);
            }
            computer.computeFromSource(mMapTileArea, area);
            index++;
        }
        while (index < mAdditionalMapTileList.getList().size()) {
            mAdditionalMapTileList.getList().remove(mAdditionalMapTileList.getList().size() - 1);
        }
    }

    /**
     * @since 6.0.2
     */
    private boolean shouldKeepTile(final long pMapTileIndex) {
        if (mMapTileArea.contains(pMapTileIndex)) {
            return true;
        }
        if (mAdditionalMapTileList.contains(pMapTileIndex)) {
            return true;
        }
        for (final MapTileContainer container : mProtectors) {
            if (container.contains(pMapTileIndex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @since 6.0.3
     */
    public MapTileArea getMapTileArea() {
        return mMapTileArea;
    }

    /**
     * @since 6.0.3
     */
    public MapTileAreaList getAdditionalMapTileList() {
        return mAdditionalMapTileList;
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
        for (int i = 0; i < list.getSize(); i++) {
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
    protected void remove(final long pMapTileIndex) {
        final Drawable drawable;
        synchronized (mCachedTiles) {
            drawable = mCachedTiles.remove(pMapTileIndex);
        }
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
     *
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
     *
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

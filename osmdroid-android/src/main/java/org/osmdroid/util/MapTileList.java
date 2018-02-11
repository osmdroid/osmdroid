package org.osmdroid.util;

/**
 * An optimized list of map tile indices
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class MapTileList {

    private long[] mTileIndices;
    private int mSize;

    /**
     * @since 6.0.0
     */
    public MapTileList() {
        this(1);
    }

    /**
     * @since 6.0.0
     */
    public MapTileList(final int initialCapacity) {
        mTileIndices = new long[initialCapacity];
    }

    public void clear() {
        mSize = 0;
    }

    public int getSize() {
        return mSize;
    }

    public long get(final int pIndex) {
        return mTileIndices[pIndex];
    }

    public void put(final long pTileIndex) {
        ensureCapacity(mSize + 1);
        mTileIndices[mSize ++] = pTileIndex;
    }

    public void ensureCapacity(final int pCapacity) {
        if (mTileIndices.length >= pCapacity) {
            return;
        }
        synchronized(this) {
            final long[] tmp = new long[pCapacity];
            System.arraycopy(mTileIndices, 0, tmp, 0, mTileIndices.length);
            mTileIndices = tmp;
        }
    }

    public boolean contains(final long pTileIndex) {
        for (int i = 0 ; i < mSize ; i ++) {
            if (mTileIndices[i] == pTileIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compute the map tile list corresponding to a map tile list source, but on another zoom level
     * @param pSource Map tile list to convert data from
     * @param pZoomDelta Zoom delta to apply to the source data
     */
    public void populateFrom(final MapTileList pSource, final int pZoomDelta) {
        for (int i = 0 ; i < pSource.mSize ; i ++) {
            final long sourceIndex = pSource.mTileIndices[i];
            final int sourceZoom = MapTileIndex.getZoom(sourceIndex);
            final int destZoom = sourceZoom + pZoomDelta;
            if (destZoom < 0 || destZoom > MapTileIndex.mMaxZoomLevel) {
                continue;
            }
            final int sourceX = MapTileIndex.getX(sourceIndex);
            final int sourceY = MapTileIndex.getY(sourceIndex);
            if (pZoomDelta <= 0) {
                put(MapTileIndex.getTileIndex(destZoom, sourceX >> -pZoomDelta, sourceY >> -pZoomDelta));
                continue;
            }
            final int power = 1 << pZoomDelta;
            final int destX = sourceX << pZoomDelta;
            final int destY = sourceY << pZoomDelta;
            for (int j = 0 ; j < power ; j ++) {
                for (int k = 0 ; k < power ; k ++) {
                    put(MapTileIndex.getTileIndex(destZoom, destX + j, destY + k));
                }
            }
        }
    }

    /**
     * @since 6.0.0
     */
    public long[] toArray() {
        final long[] result = new long[mSize];
        System.arraycopy(mTileIndices, 0, result, 0, mSize);
        return result;
    }
}

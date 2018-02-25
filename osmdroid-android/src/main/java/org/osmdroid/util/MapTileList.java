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
     * @since 6.0.0
     */
    public long[] toArray() {
        final long[] result = new long[mSize];
        System.arraycopy(mTileIndices, 0, result, 0, mSize);
        return result;
    }
}

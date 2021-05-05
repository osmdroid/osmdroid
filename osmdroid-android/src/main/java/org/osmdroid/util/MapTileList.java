package org.osmdroid.util;

/**
 * An optimized list of map tile indices
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class MapTileList implements MapTileContainer {

    private long[] mTileIndices;
    private int mSize;

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
        mTileIndices[mSize++] = pTileIndex;
    }

    /**
     * @since 6.0.2
     */
    public void put(final int pZoom, final int pLeft, final int pTop, final int pRight, final int pBottom) {
        final int max = 1 << pZoom;
        final int spanX = (pRight - pLeft + 1) + (pRight < pLeft ? max : 0);
        final int spanY = (pBottom - pTop + 1) + (pBottom < pTop ? max : 0);
        ensureCapacity(getSize() + spanX * spanY);
        for (int i = 0; i < spanX; i++) {
            for (int j = 0; j < spanY; j++) {
                final int x = (pLeft + i) % max;
                final int y = (pTop + j) % max;
                put(MapTileIndex.getTileIndex(pZoom, x, y));
            }
        }
    }

    /**
     * @since 6.0.2
     */
    public void put(final int pZoom) {
        final int max = 1 << pZoom;
        put(pZoom, 0, 0, max - 1, max - 1);
    }

    public void ensureCapacity(final int pCapacity) {
        if (pCapacity == 0) {
            return;
        }
        if (mTileIndices != null && mTileIndices.length >= pCapacity) {
            return;
        }
        synchronized (this) {
            final long[] tmp = new long[pCapacity];
            if (mTileIndices != null) {
                System.arraycopy(mTileIndices, 0, tmp, 0, mTileIndices.length);
            }
            mTileIndices = tmp;
        }
    }

    @Override
    public boolean contains(final long pTileIndex) {
        if (mTileIndices == null) {
            return false;
        }
        for (int i = 0; i < mSize; i++) {
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
        if (mTileIndices != null) {
            System.arraycopy(mTileIndices, 0, result, 0, mSize);
        }
        return result;
    }
}

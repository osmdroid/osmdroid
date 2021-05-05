package org.osmdroid.util;

import android.graphics.Rect;

import java.util.Iterator;

/**
 * An area of map tiles.
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MapTileArea implements MapTileContainer, IterableWithSize<Long> {

    private int mZoom;
    private int mLeft;
    private int mTop;
    private int mWidth;
    private int mHeight;
    private int mMapTileUpperBound;

    public MapTileArea set(final int pZoom, final int pLeft, final int pTop, final int pRight, final int pBottom) {
        mZoom = pZoom;
        mMapTileUpperBound = 1 << mZoom;
        mWidth = computeSize(pLeft, pRight);
        mHeight = computeSize(pTop, pBottom);
        mLeft = cleanValue(pLeft);
        mTop = cleanValue(pTop);
        return this;
    }

    public MapTileArea set(final int pZoom, final Rect pRect) {
        return set(pZoom, pRect.left, pRect.top, pRect.right, pRect.bottom);
    }

    public MapTileArea set(final MapTileArea pArea) {
        if (pArea.size() == 0) {
            return reset();
        } else {
            return set(pArea.mZoom, pArea.mLeft, pArea.mTop, pArea.getRight(), pArea.getBottom());
        }
    }

    /**
     * Set the area as an empty area
     */
    public MapTileArea reset() {
        mWidth = 0;
        return this;
    }

    public int getZoom() {
        return mZoom;
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getRight() {
        return (mLeft + mWidth) % mMapTileUpperBound;
    }

    public int getBottom() {
        return (mTop + mHeight) % mMapTileUpperBound;
    }

    @Override
    public int size() {
        return mWidth * mHeight;
    }

    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {

            private int mIndex;

            @Override
            public boolean hasNext() {
                return mIndex < size();
            }

            @Override
            public Long next() {
                if (!hasNext()) {
                    return null;
                }
                int x = mLeft + mIndex % mWidth;
                int y = mTop + mIndex / mWidth;
                mIndex++;
                while (x >= mMapTileUpperBound) {
                    x -= mMapTileUpperBound;
                }
                while (y >= mMapTileUpperBound) {
                    y -= mMapTileUpperBound;
                }
                return MapTileIndex.getTileIndex(mZoom, x, y);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean contains(long pTileIndex) {
        if (MapTileIndex.getZoom(pTileIndex) != mZoom) {
            return false;
        }
        if (!contains(MapTileIndex.getX(pTileIndex), mLeft, mWidth)) {
            return false;
        }
        return contains(MapTileIndex.getY(pTileIndex), mTop, mHeight);
    }

    private boolean contains(int pValue, final int pFirst, final int pSize) {
        while (pValue < pFirst) {
            pValue += mMapTileUpperBound;
        }
        return pValue < pFirst + pSize;
    }

    private int cleanValue(int pValue) {
        while (pValue < 0) {
            pValue += mMapTileUpperBound;
        }
        while (pValue >= mMapTileUpperBound) {
            pValue -= mMapTileUpperBound;
        }
        return pValue;
    }

    private int computeSize(final int pTopLeft, int pBottomRight) {
        while (pTopLeft > pBottomRight) {
            pBottomRight += mMapTileUpperBound;
        }
        return Math.min(mMapTileUpperBound, pBottomRight - pTopLeft + 1);
    }

    @Override
    public String toString() {
        if (mWidth == 0) {
            return "MapTileArea:empty";
        }
        return "MapTileArea:zoom=" + mZoom + ",left=" + mLeft + ",top=" + mTop + ",width=" + mWidth + ",height=" + mHeight;
    }
}

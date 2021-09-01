package org.osmdroid.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of areas of map tiles
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MapTileAreaList implements MapTileContainer, IterableWithSize<Long> {

    private final List<MapTileArea> mList = new ArrayList<>();

    public List<MapTileArea> getList() {
        return mList;
    }

    @Override
    public int size() {
        int size = 0;
        for (final MapTileArea area : mList) {
            size += area.size();
        }
        return size;
    }

    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {

            private int mIndex;
            private Iterator<Long> mCurrent;

            @Override
            public boolean hasNext() {
                final Iterator<Long> current = getCurrent();
                return current != null && current.hasNext();
            }

            @Override
            public Long next() {
                final long result = getCurrent().next();
                if (!getCurrent().hasNext()) {
                    mCurrent = null; // in order to force the next item
                }
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            private Iterator<Long> getCurrent() {
                if (mCurrent != null) {
                    return mCurrent;
                }
                if (mIndex < mList.size()) {
                    return mCurrent = mList.get(mIndex++).iterator();
                }
                return null;
            }
        };
    }

    @Override
    public boolean contains(final long pTileIndex) {
        for (final MapTileArea area : mList) {
            if (area.contains(pTileIndex)) {
                return true;
            }
        }
        return false;
    }
}

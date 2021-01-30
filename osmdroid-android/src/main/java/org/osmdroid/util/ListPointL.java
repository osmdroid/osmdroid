package org.osmdroid.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Optimized version of List&lt;PointL&gt;
 * Created by Fabrice on 31/12/2017.
 *
 * @since 6.0.0
 */

public class ListPointL implements Iterable<PointL> {

    private final List<PointL> mList = new ArrayList<>();
    private int mSize;

    public void clear() {
        mSize = 0;
    }

    public int size() {
        return mSize;
    }

    public PointL get(final int pIndex) {
        return mList.get(pIndex);
    }

    public void add(final long pX, final long pY) {
        final PointL point;
        if (mSize >= mList.size()) {
            point = new PointL();
            mList.add(point);
        } else {
            point = mList.get(mSize);
        }
        mSize++;
        point.set(pX, pY);
    }

    @Override
    public Iterator<PointL> iterator() {
        return new Iterator<PointL>() {

            private int mIndex;

            @Override
            public boolean hasNext() {
                return mIndex < mSize;
            }

            @Override
            public PointL next() {
                return get(mIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

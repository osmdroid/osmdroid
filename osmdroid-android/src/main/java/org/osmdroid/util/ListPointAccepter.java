package org.osmdroid.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PointAccepter} that builds a {@link List} of {@link PointL} as a list of long, long
 *
 * @author Fabrice Fontaine
 * @since 6.2.0
 */

public class ListPointAccepter implements PointAccepter {

    private final List<Long> mList = new ArrayList<>();
    private final PointL mLatestPoint = new PointL();
    private final boolean mRemoveConsecutiveDuplicates;
    private boolean mFirst;

    public ListPointAccepter(final boolean pRemoveConsecutiveDuplicates) {
        mRemoveConsecutiveDuplicates = pRemoveConsecutiveDuplicates;
    }

    public List<Long> getList() {
        return mList;
    }

    @Override
    public void init() {
        mList.clear();
        mFirst = true;
    }

    @Override
    public void add(long pX, long pY) {
        if (!mRemoveConsecutiveDuplicates) {
            mList.add(pX);
            mList.add(pY);
            return;
        }
        if (mFirst) {
            mFirst = false;
            mList.add(pX);
            mList.add(pY);
            mLatestPoint.set(pX, pY);
        } else if (mLatestPoint.x != pX || mLatestPoint.y != pY) {
            mList.add(pX);
            mList.add(pY);
            mLatestPoint.set(pX, pY);
        }
    }

    @Override
    public void end() {
    }
}

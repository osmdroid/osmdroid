package org.osmdroid.util;

import android.graphics.Path;

/**
 * Created by Fabrice on 24/12/2017.
 * @since 6.0.0
 */

public class PathBuilder implements PointAccepter{

    private final Path mPath;
    private final PointL mLatestPoint = new PointL();
    private final ListPointL mPoints = new ListPointL();
    private boolean mFirst;

    public PathBuilder(final Path pPath) {
        mPath = pPath;
    }

    @Override
    public void init() {
        mFirst = true;
        mPoints.clear();
    }

    @Override
    public void add(final long pX, final long pY) {
        if (mFirst) {
            mFirst = false;
            if (mPath != null) {
                mPath.moveTo(pX, pY);
            } else {
                mPoints.add(pX, pY);
            }
            mLatestPoint.set(pX, pY);
        } else {
            if (mLatestPoint.x != pX || mLatestPoint.y != pY) {
                if (mPath != null) {
                    mPath.lineTo(pX, pY);
                } else {
                    mPoints.add(pX, pY);
                }
                mLatestPoint.set(pX, pY);
            }
        }
    }

    @Override
    public void end() {}

    /**
     * @since 6.0.0
     */
    public ListPointL getPoints() {
        return mPoints;
    }
}

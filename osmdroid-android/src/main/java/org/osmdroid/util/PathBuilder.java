package org.osmdroid.util;

import android.graphics.Path;

/**
 * Created by Fabrice on 24/12/2017.
 *
 * @since 6.0.0
 */

public class PathBuilder implements PointAccepter {

    private final Path mPath;
    private final PointL mLatestPoint = new PointL();
    private boolean mFirst;

    public PathBuilder(final Path pPath) {
        mPath = pPath;
    }

    @Override
    public void init() {
        mFirst = true;
    }

    @Override
    public void add(final long pX, final long pY) {
        if (mFirst) {
            mFirst = false;
            mPath.moveTo(pX, pY);
            mLatestPoint.set(pX, pY);
        } else if (mLatestPoint.x != pX || mLatestPoint.y != pY) {
            mPath.lineTo(pX, pY);
            mLatestPoint.set(pX, pY);
        }
    }

    @Override
    public void end() {
    }
}

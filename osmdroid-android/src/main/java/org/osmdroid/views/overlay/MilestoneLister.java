package org.osmdroid.views.overlay;

import org.osmdroid.util.PointL;
import org.osmdroid.util.PointAccepter;
import org.osmdroid.views.util.constants.MathConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabrice on 22/12/2017.
 * @since 6.0.0
 */

public abstract class MilestoneLister implements PointAccepter{

    private final List<MilestoneStep> mMilestones = new ArrayList<>();
    private final PointL mLatestPoint = new PointL();
    private boolean mFirst;

    public List<MilestoneStep> getMilestones() {
        return mMilestones;
    }

    protected PointL getLatestPoint() {
        return mLatestPoint;
    }

    @Override
    public void init() {
        mMilestones.clear();
        mFirst = true;
    }

    @Override
    public void add(long pX, long pY) {
        if (mFirst) {
            mFirst = false;
            mLatestPoint.set(pX, pY);
        } else {
            add(mLatestPoint.x, mLatestPoint.y, pX, pY);
            mLatestPoint.set(pX, pY);
        }
    }

    @Override
    public void end() {}

    protected void add(final MilestoneStep pMilestoneStep) {
        mMilestones.add(pMilestoneStep);
    }

    protected abstract void add(final long x0, final long y0, final long x1, final long y1);

    // TODO 0000 test + static
    /**
     * @return the orientation (in degrees) of the slope between point p0 and p1, or 0 if same point
     * @since 6.0.0
     */
    public static double getOrientation(final long x0, final long y0, final long x1, final long y1) {
        if (x0 == x1) {
            if (y0 == y1) {
                return 0;
            }
            if (y0 > y1) {
                return -90;
            }
            return 90;
        }
        final double slope = ((double)(y1 - y0)) / (x1 - x0);
        final boolean isBeyondHalfPI = x1 < x0;
        return MathConstants.RAD2DEG * Math.atan(slope) + (isBeyondHalfPI ? 180 : 0);
    }
}

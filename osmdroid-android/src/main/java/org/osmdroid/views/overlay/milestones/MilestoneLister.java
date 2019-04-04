package org.osmdroid.views.overlay.milestones;

import org.osmdroid.util.PointL;
import org.osmdroid.util.PointAccepter;
import org.osmdroid.util.MyMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Creating a list of `MilestoneStep`s from a list of `PointL`
 * Created by Fabrice on 22/12/2017.
 * @since 6.0.0
 */

public abstract class MilestoneLister implements PointAccepter{

    private final List<MilestoneStep> mMilestones = new ArrayList<>();
    private final PointL mLatestPoint = new PointL();
    private boolean mFirst;
    private double[] mDistances;

    public List<MilestoneStep> getMilestones() {
        return mMilestones;
    }

    public void setDistances(final double[] pDistances) {
        mDistances = pDistances;
    }

    protected double getDistance(final int pIndex) {
        return mDistances[pIndex];
    }

    @Override
    public void init() {
        mMilestones.clear();
        mFirst = true;
    }

    @Override
    public void add(final long pX, final long pY) {
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

    /**
     * @return the orientation (in degrees) of the slope between point p0 and p1, or 0 if same point
     * @since 6.0.0
     * @deprecated Use {@link MyMath#getOrientation(long, long, long, long)} instead
     */
    @Deprecated
    public static double getOrientation(final long x0, final long y0, final long x1, final long y1) {
        return MyMath.getOrientation(x0, y0, x1, y1);
    }
}

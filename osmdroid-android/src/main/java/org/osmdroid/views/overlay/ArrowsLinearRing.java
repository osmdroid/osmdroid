package org.osmdroid.views.overlay;

import android.graphics.Path;

import org.osmdroid.util.Distance;
import org.osmdroid.util.PointL;
import org.osmdroid.views.Projection;
import org.osmdroid.views.util.constants.MathConstants;

import java.util.ArrayList;

/**
 * Created by Jason Marks on 12/2/2017.
 */

class ArrowsLinearRing extends LinearRing {

    private double mDistance;
    private final ArrayList<MilestoneStep> mMilestones = new ArrayList<>();
    private static final float DEFAULT_ARROW_LENGTH = 20f;
    private float mDirectionalArrowLength = DEFAULT_ARROW_LENGTH;

    public ArrowsLinearRing(final Path pPath) {
        super(pPath);
    }

    @Override
    public void lineTo(final long pX, final long pY) {
        if (!getIsNextMove()) {
            PointL mPreviousPoint = new PointL(getLatestPathPoint());
//                addMilestoneInTheMiddle(mPreviousPoint.x, mPreviousPoint.y, pX, pY);
            addMilestoneAsDistance(mPreviousPoint.x, mPreviousPoint.y, pX, pY);
        }
        super.lineTo(pX, pY);
    }

    @Override
    void clearPath() {
        super.clearPath();
        mMilestones.clear();
        mDistance = 0;
    }

    @Override
    PointL buildPathPortion(final Projection pProjection,
                            final boolean pClosePath, final PointL pOffset){
        if (!mMilestones.isEmpty()) {
            mMilestones.clear();
        }
        mDistance = 0;

        return super.buildPathPortion(pProjection, pClosePath, pOffset);
    }

    /**
     * Update the stroke width
     *
     * @param strokeWidth
     */
    public void setStrokeWidth(final float strokeWidth) {
        this.mDirectionalArrowLength = DEFAULT_ARROW_LENGTH + strokeWidth;
    }

    public ArrayList<MilestoneStep> getMilestones() {
        return mMilestones;
    }

    private void addMilestoneInTheMiddle(final long x0, final long y0, final long x1, final long y1) {
        // if the points are really close don't draw an arrow
        if (Distance.getSquaredDistanceToPoint(x0, y0, x1, y1) <= mDirectionalArrowLength) { // TODO 000 sqrt?
            return;
        }

        final long centerX = (x0 + x1) / 2;
        final long centerY = (y0 + y1) / 2;
        final double orientation = getOrientation(x0, y0, x1, y1);
        final MilestoneStep step = new MilestoneStep(centerX, centerY, orientation);
        mMilestones.add(step);
    }

    private final static double nbPixels = 100;

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

    private void addMilestoneAsDistance(final long x0, final long y0, final long x1, final long y1) {
        double currentDistance = Math.sqrt(Distance.getSquaredDistanceToPoint(x0, y0, x1, y1));
        if (currentDistance == 0) {
            return;
        }
        final double orientation = getOrientation(x0, y0, x1, y1);
        double x = x0;
        double y = y0;
        while(true) {
            final double latestMilestone = Math.floor(mDistance / nbPixels) * nbPixels;
            final double neededForNext = latestMilestone + nbPixels - mDistance;
            if (currentDistance < neededForNext) {
                mDistance += currentDistance;
                return;
            }
            mDistance += neededForNext;
            currentDistance -= neededForNext;
            x += neededForNext * Math.cos(MathConstants.DEG2RAD * orientation);
            y += neededForNext * Math.sin(MathConstants.DEG2RAD * orientation);
            final MilestoneStep step = new MilestoneStep((long)x, (long)y, orientation);
            mMilestones.add(step);
        }
    }
}

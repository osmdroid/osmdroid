package org.osmdroid.views.overlay.milestones;

import org.osmdroid.util.Distance;
import org.osmdroid.views.util.constants.MathConstants;

/**
 * Listing every x meters of the `Path`
 * Created by Fabrice on 28/12/2017.
 * @since 6.0.0
 */

public class MilestoneMeterDistanceLister extends MilestoneLister{

    private final double mNbMetersRecurrence;
    private double mDistance;
    private int mIndex;

    public MilestoneMeterDistanceLister(final double pNbMetersRecurrence) {
        mNbMetersRecurrence = pNbMetersRecurrence;
    }

    @Override
    public void init() {
        super.init();
        mDistance = 0;
        mIndex = 0;
    }

    @Override
    protected void add(final long x0, final long y0, final long x1, final long y1) {
        mIndex ++;
        double currentDistance = getDistance(mIndex);
        if (currentDistance == 0) {
            return;
        }
        final double pixelDistance = Math.sqrt(Distance.getSquaredDistanceToPoint(x0, y0, x1, y1));
        final double metersToPixels = pixelDistance / currentDistance;
        final double orientation = getOrientation(x0, y0, x1, y1);
        double x = x0;
        double y = y0;
        while(true) {
            final double latestMilestone = Math.floor(mDistance / mNbMetersRecurrence) * mNbMetersRecurrence;
            final double neededForNext = latestMilestone + mNbMetersRecurrence - mDistance;
            if (currentDistance < neededForNext) {
                mDistance += currentDistance;
                return;
            }
            mDistance += neededForNext;
            currentDistance -= neededForNext;
            x += neededForNext * Math.cos(MathConstants.DEG2RAD * orientation) * metersToPixels;
            y += neededForNext * Math.sin(MathConstants.DEG2RAD * orientation) * metersToPixels;
            add(new MilestoneStep((long)x, (long)y, orientation, mDistance));
        }
    }
}

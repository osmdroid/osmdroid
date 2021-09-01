package org.osmdroid.views.overlay.milestones;

import org.osmdroid.util.Distance;
import org.osmdroid.views.util.constants.MathConstants;

/**
 * Listing every x pixels of the `Path`, with an initial value
 * Created by Fabrice on 23/12/2017.
 *
 * @since 6.0.0
 */

public class MilestonePixelDistanceLister extends MilestoneLister {

    private final double mNbPixelsInit;
    private final double mNbPixelsRecurrence;
    private double mDistance;

    public MilestonePixelDistanceLister(final double pNbPixelsInit, final double pNbPixelsRecurrence) {
        mNbPixelsInit = pNbPixelsInit;
        mNbPixelsRecurrence = pNbPixelsRecurrence;
    }

    @Override
    public void init() {
        super.init();
        mDistance = mNbPixelsRecurrence - mNbPixelsInit; // might be tricky if negative
    }

    @Override
    protected void add(final long x0, final long y0, final long x1, final long y1) {
        double currentDistance = Math.sqrt(Distance.getSquaredDistanceToPoint(x0, y0, x1, y1));
        if (currentDistance == 0) {
            return;
        }
        final double orientation = getOrientation(x0, y0, x1, y1);
        double x = x0;
        double y = y0;
        while (true) {
            final double latestMilestone = Math.floor(mDistance / mNbPixelsRecurrence) * mNbPixelsRecurrence;
            final double neededForNext = latestMilestone + mNbPixelsRecurrence - mDistance;
            if (currentDistance < neededForNext) {
                mDistance += currentDistance;
                return;
            }
            mDistance += neededForNext;
            currentDistance -= neededForNext;
            x += neededForNext * Math.cos(MathConstants.DEG2RAD * orientation);
            y += neededForNext * Math.sin(MathConstants.DEG2RAD * orientation);
            add(new MilestoneStep((long) x, (long) y, orientation, mDistance));
        }
    }
}

package org.osmdroid.views.overlay.milestones;

import org.osmdroid.util.Distance;
import org.osmdroid.views.util.constants.MathConstants;

/**
 * Listing the vertices for a slice of the path between two distances
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 */

public class MilestoneMeterDistanceSliceLister extends MilestoneLister {

    private enum Step {
        STEP_INIT,
        STEP_STARTED,
        STEP_ENDED
    }

    private double mNbMetersStart;
    private double mNbMetersEnd;
    private double mDistance;
    private int mIndex;
    private Step mStep;

    public void setMeterDistanceSlice(final double pNbMetersStart, final double pNbMetersEnd) {
        mNbMetersStart = pNbMetersStart;
        mNbMetersEnd = pNbMetersEnd;
    }

    @Override
    public void init() {
        super.init();
        mDistance = 0;
        mIndex = 0;
        mStep = Step.STEP_INIT;
    }

    @Override
    protected void add(final long x0, final long y0, final long x1, final long y1) {
        if (mStep == Step.STEP_ENDED) {
            return;
        }
        double currentDistance = getDistance(++mIndex);
        if (currentDistance == 0) {
            return;
        }
        final double pixelDistance = Math.sqrt(Distance.getSquaredDistanceToPoint(x0, y0, x1, y1));
        final double metersToPixels = pixelDistance / currentDistance;
        final double orientation = getOrientation(x0, y0, x1, y1);
        double x = x0;
        double y = y0;
        if (mStep == Step.STEP_INIT) { // looking for the first distance
            final double neededForNext = mNbMetersStart - mDistance;
            if (neededForNext > currentDistance) { // not reached yet
                mDistance += currentDistance;
                return;
            }
            mStep = Step.STEP_STARTED;
            mDistance += neededForNext;
            currentDistance -= neededForNext;
            x += neededForNext * Math.cos(MathConstants.DEG2RAD * orientation) * metersToPixels;
            y += neededForNext * Math.sin(MathConstants.DEG2RAD * orientation) * metersToPixels;
            add(new MilestoneStep((long) x, (long) y, orientation, null));
            if (mNbMetersStart == mNbMetersEnd) {
                mStep = Step.STEP_ENDED;
                return;
            }
        }
        if (mStep == Step.STEP_STARTED) { // looking for the second/last distance
            final double neededForNext = mNbMetersEnd - mDistance;
            if (neededForNext > currentDistance) { // not reached yet
                mDistance += currentDistance;
                add(new MilestoneStep(x1, y1, orientation, null));
                return;
            }
            mStep = Step.STEP_ENDED;
            x += neededForNext * Math.cos(MathConstants.DEG2RAD * orientation) * metersToPixels;
            y += neededForNext * Math.sin(MathConstants.DEG2RAD * orientation) * metersToPixels;
            add(new MilestoneStep((long) x, (long) y, orientation, null));
        }
    }
}

package org.osmdroid.views.overlay.milestones;

import org.osmdroid.util.Distance;
import org.osmdroid.views.util.constants.MathConstants;

/**
 * Listing every given meters of the `Path`
 * Created by Fabrice on 28/12/2017.
 *
 * @since 6.0.0
 */

public class MilestoneMeterDistanceLister extends MilestoneLister {

    private final double mNbMetersRecurrence;
    private double mDistance;
    private int mIndex;

    /**
     * @since 6.0.3
     */
    private final double[] mMilestoneMeters;
    private int mMilestoneMetersIndex;
    private double mNeededForNext;

    // handling last milestone's side effect (with all the roundings and double/float conversions)
    private boolean mSideEffectLastFlag;
    private double mSideEffectLastEpsilon = 1E-5;
    private long mSideEffectLastX;
    private long mSideEffectLastY;
    private double mSideEffectLastOrientation;

    /**
     * Use it if you want a milestone every x meters
     */
    public MilestoneMeterDistanceLister(final double pNbMetersRecurrence) {
        mNbMetersRecurrence = pNbMetersRecurrence;
        mMilestoneMeters = null;
    }

    /**
     * @since 6.0.3
     * Use it if you want milestones separated by different length (in meters)
     * All the distances are from the origin and must be increasing.
     * E.g for a marathon: [0, 10000, 20000, 21097, 30000, 40000, 42195]
     */
    public MilestoneMeterDistanceLister(final double[] pMilestoneMeters) {
        mNbMetersRecurrence = 0;
        mMilestoneMeters = pMilestoneMeters;
    }

    @Override
    public void init() {
        super.init();
        mDistance = 0;
        mIndex = 0;
        if (mMilestoneMeters != null) {
            mMilestoneMetersIndex = 0;
        }
        mNeededForNext = getNewNeededForNext();
        mSideEffectLastFlag = false;
    }

    @Override
    protected void add(final long x0, final long y0, final long x1, final long y1) {
        mSideEffectLastFlag = false;
        if (mNeededForNext == -1) {
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
        while (true) {
            if (currentDistance < mNeededForNext) {
                mDistance += currentDistance;
                mNeededForNext -= currentDistance;
                mSideEffectLastFlag = true;
                mSideEffectLastX = x1;
                mSideEffectLastY = y1;
                mSideEffectLastOrientation = orientation;
                return;
            }
            mDistance += mNeededForNext;
            currentDistance -= mNeededForNext;
            x += mNeededForNext * Math.cos(MathConstants.DEG2RAD * orientation) * metersToPixels;
            y += mNeededForNext * Math.sin(MathConstants.DEG2RAD * orientation) * metersToPixels;
            add((long) x, (long) y, orientation);
            mNeededForNext = getNewNeededForNext();
            if (mNeededForNext == -1) {
                return;
            }
        }
    }

    /**
     * @since 6.0.3
     */
    private double getNewNeededForNext() {
        if (mMilestoneMeters == null) {
            return mNbMetersRecurrence;
        }
        if (mMilestoneMetersIndex >= mMilestoneMeters.length) {
            return -1;
        }
        final double before = mMilestoneMetersIndex == 0 ? 0 : mMilestoneMeters[mMilestoneMetersIndex - 1];
        final double needed = mMilestoneMeters[mMilestoneMetersIndex++] - before;
        if (needed < 0) {
            throw new IllegalArgumentException();
        }
        return needed;
    }

    /**
     * @since 6.0.3
     */
    @Override
    public void end() {
        if (mSideEffectLastFlag && mNeededForNext < mSideEffectLastEpsilon) {
            add(mSideEffectLastX, mSideEffectLastY, mSideEffectLastOrientation);
        }
        super.end();
    }

    /**
     * @since 6.0.3
     */
    public void setSideEffectLastEpsilon(final double pSideEffectLastEpsilon) {
        mSideEffectLastEpsilon = pSideEffectLastEpsilon;
    }

    /**
     * @since 6.0.3
     */
    private void add(final long pX, final long pY, final double pOrientation) {
        add(new MilestoneStep(pX, pY, pOrientation, mDistance));
    }
}

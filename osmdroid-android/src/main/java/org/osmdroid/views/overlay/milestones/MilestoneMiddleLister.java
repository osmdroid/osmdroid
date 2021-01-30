package org.osmdroid.views.overlay.milestones;

import org.osmdroid.util.Distance;

/**
 * Listing all the vertices' middle, provided that there are enough pixels between them
 * Created by Fabrice on 23/12/2017.
 *
 * @since 6.0.0
 */

public class MilestoneMiddleLister extends MilestoneLister {

    private final double mMinimumSquaredPixelDistance;

    public MilestoneMiddleLister(final double pMinimumPixelDistance) {
        mMinimumSquaredPixelDistance = pMinimumPixelDistance * pMinimumPixelDistance;
    }

    @Override
    protected void add(final long x0, final long y0, final long x1, final long y1) {
        if (Distance.getSquaredDistanceToPoint(x0, y0, x1, y1) <= mMinimumSquaredPixelDistance) {
            return;
        }

        final long centerX = (x0 + x1) / 2;
        final long centerY = (y0 + y1) / 2;
        final double orientation = getOrientation(x0, y0, x1, y1);
        add(new MilestoneStep(centerX, centerY, orientation));
    }
}

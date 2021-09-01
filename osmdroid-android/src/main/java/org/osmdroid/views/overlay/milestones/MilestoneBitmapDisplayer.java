package org.osmdroid.views.overlay.milestones;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Displayer of `MilestoneStep`s as `Bitmap`s
 * Created by Fabrice Fontaine on 22/12/2017.
 *
 * @since 6.0.0
 */

public class MilestoneBitmapDisplayer extends MilestoneDisplayer {

    private final Bitmap mBitmap;
    private final int mOffsetX;
    private final int mOffsetY;

    public MilestoneBitmapDisplayer(
            final double pInitialOrientation, final boolean pFollowTrajectory,
            final Bitmap pBitmap, final int pOffsetX, final int pOffsetY) {
        super(pInitialOrientation, pFollowTrajectory);
        mBitmap = pBitmap;
        mOffsetX = pOffsetX;
        mOffsetY = pOffsetY;
    }

    @Override
    protected void draw(final Canvas pCanvas, final Object pParameter) {
        pCanvas.drawBitmap(mBitmap, -mOffsetX, -mOffsetY, null);
    }
}

package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Displayer of `MilestoneStep`s as `Path`s
 * Created by Fabrice Fontaine on 22/12/2017.
 * @since 6.0.0
 */

public class MilestonePathDisplayer extends MilestoneDisplayer{

    private final Path mPath;
    private final Paint mPaint;

    public MilestonePathDisplayer(
            final double pInitialOrientation, final boolean pFollowTrajectory,
            final Path pPath, final Paint pPaint) {
        super(pInitialOrientation, pFollowTrajectory);
        mPath = pPath;
        mPaint = pPaint;
    }

    @Override
    protected void draw(final Canvas pCanvas) {
        pCanvas.drawPath(mPath, mPaint);
    }
}

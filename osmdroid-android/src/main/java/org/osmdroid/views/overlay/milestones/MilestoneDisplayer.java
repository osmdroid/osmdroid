package org.osmdroid.views.overlay.milestones;

import android.graphics.Canvas;

/**
 * Displayer of `MilestoneStep`s
 * Created by Fabrice Fontaine on 22/12/2017.
 *
 * @since 6.0.0
 */

public abstract class MilestoneDisplayer {

    /**
     * Initial orientation (in degrees) of the milestone display.
     * For instance, if we're talking about bitmaps,
     * a "up arrow" would use 90 and a "right arrow" would use 0.
     */
    private final double mInitialOrientation;

    /**
     * "Should we follow the trajectory?"
     * For instance, if we're talking about bitmaps,
     * an arrow would use true - in order to follow the polyline's trajectory,
     * and a work-in-progress logo would use false - in order to display always the same orientation
     * of logo, regardless of the polyline's trajectory
     */
    private final boolean mFollowTrajectory;

    public MilestoneDisplayer(final double pInitialOrientation, final boolean pFollowTrajectory) {
        mInitialOrientation = pInitialOrientation;
        mFollowTrajectory = pFollowTrajectory;
    }

    public void draw(final Canvas pCanvas, final MilestoneStep pStep) {
        final double orientation = mInitialOrientation + (mFollowTrajectory ? pStep.getOrientation() : 0);
        pCanvas.save();
        pCanvas.rotate((float) orientation, pStep.getX(), pStep.getY());
        pCanvas.translate(pStep.getX(), pStep.getY());
        draw(pCanvas, pStep.getObject());
        pCanvas.restore();
    }

    /**
     * Draw on pixel (0,0) with no rotation
     */
    protected abstract void draw(final Canvas pCanvas, final Object pParameter);

    /**
     * @since 6.0.2
     */
    public void drawBegin(final Canvas pCanvas) {
    }

    /**
     * @since 6.0.2
     */
    public void drawEnd(final Canvas pCanvas) {
    }
}

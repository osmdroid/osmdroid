package org.osmdroid.views.overlay.milestones;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.osmdroid.views.overlay.LineDrawer;

/**
 * Display lines between milestone steps
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MilestoneLineDisplayer extends MilestoneDisplayer {

    private boolean mFirst = true;

    /**
     * @since 6.2.0
     */
    private long mPreviousX;
    private long mPreviousY;

    private final LineDrawer mLineDrawer = new LineDrawer(256) {
        @Override
        public void flush() {
            super.flush();
            mFirst = true;
        }
    };

    public MilestoneLineDisplayer(final Paint pPaint) {
        super(0, false);
        mLineDrawer.setPaint(pPaint);
    }

    @Override
    public void drawBegin(Canvas pCanvas) {
        mLineDrawer.init();
        mLineDrawer.setCanvas(pCanvas);
        mFirst = true;
    }

    /**
     * Overriding the "standard" milestone behavior (where we display something at each milestone)
     * Instead, we populate a line drawer that will connect the steps
     */
    @Override
    public void draw(Canvas pCanvas, MilestoneStep pStep) {
        final long nextX = pStep.getX();
        final long nextY = pStep.getY();
        if (mFirst) {
            mFirst = false;
        } else if (mPreviousX != nextX || mPreviousY != nextY) {
            mLineDrawer.add(mPreviousX, mPreviousY);
            mLineDrawer.add(nextX, nextY);
        }
        mPreviousX = nextX;
        mPreviousY = nextY;
    }

    @Override
    public void drawEnd(Canvas pCanvas) {
        mLineDrawer.end();
    }

    @Override
    protected void draw(final Canvas pCanvas, final Object pParameter) {
        // do nothing as we override the draw method that calls this one
    }
}

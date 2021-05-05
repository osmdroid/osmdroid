package org.osmdroid.views.overlay.milestones;

import android.graphics.Canvas;

import org.osmdroid.util.PointAccepter;

/**
 * Created by Fabrice on 24/12/2017.
 *
 * @since 6.0.0
 */

public class MilestoneManager implements PointAccepter {

    private final MilestoneLister mLister;
    private final MilestoneDisplayer mDisplayer;

    public MilestoneManager(final MilestoneLister pLister, final MilestoneDisplayer pDisplayer) {
        mLister = pLister;
        mDisplayer = pDisplayer;
    }

    public void draw(final Canvas pCanvas) {
        mDisplayer.drawBegin(pCanvas);
        for (final MilestoneStep step : mLister.getMilestones()) {
            mDisplayer.draw(pCanvas, step);
        }
        mDisplayer.drawEnd(pCanvas);
    }

    @Override
    public void init() {
        mLister.init();
    }

    @Override
    public void add(final long pX, final long pY) {
        mLister.add(pX, pY);
    }

    @Override
    public void end() {
        mLister.end();
    }

    public void setDistances(final double[] pDistances) {
        mLister.setDistances(pDistances);
    }
}

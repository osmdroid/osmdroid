package org.osmdroid.views.overlay;

/**
 * Created by Fabrice on 23/12/2017.
 * @since 6.0.0
 */

public class MilestoneVertexLister extends MilestoneLister{

    private double mLatestOrientation;
    private long mLatestX;
    private long mLatestY;

    @Override
    protected void add(final long x0, final long y0, final long x1, final long y1) {
        mLatestOrientation = getOrientation(x0, y0, x1, y1);
        add(new MilestoneStep(x0, y0, mLatestOrientation));
        mLatestX = x1;
        mLatestY = y1;
    }

    @Override
    public void end() {
        super.end();
        add(new MilestoneStep(mLatestX, mLatestY, mLatestOrientation));
    }
}
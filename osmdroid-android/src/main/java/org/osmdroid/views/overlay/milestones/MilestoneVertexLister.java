package org.osmdroid.views.overlay.milestones;

/**
 * Listing every vertex
 * Created by Fabrice on 23/12/2017.
 *
 * @since 6.0.0
 */

public class MilestoneVertexLister extends MilestoneLister {

    private double mLatestOrientation;
    private long mLatestX;
    private long mLatestY;
    private int mIndex;

    @Override
    public void init() {
        super.init();
        mIndex = 0;
    }

    @Override
    protected void add(final long x0, final long y0, final long x1, final long y1) {
        mLatestOrientation = getOrientation(x0, y0, x1, y1);
        innerAdd(x0, y0, mIndex++);
        mLatestX = x1;
        mLatestY = y1;
    }

    @Override
    public void end() {
        super.end();
        innerAdd(mLatestX, mLatestY, -mIndex); // how do we know if it's the last vertex? If it's negative!
    }

    private void innerAdd(final long pX, final long pY, final int pIndex) {
        add(new MilestoneStep(pX, pY, mLatestOrientation, pIndex));
    }
}

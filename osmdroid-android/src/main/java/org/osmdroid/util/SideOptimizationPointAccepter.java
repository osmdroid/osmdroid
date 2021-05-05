package org.osmdroid.util;

/**
 * {@link PointAccepter} that simplifies the list of consecutive points with the same X or Y.
 * One goal is to have faster Path rendering.
 * As we clip the Path with a rectangle, additional segments are created by {@link SegmentClipper}.
 * When most of the Polygon is out of the screen, many consecutive segments are on the same side
 * of the clip rectangle (e.g. same X or same Y). Do we need to render all those segments? No.
 * We can simplify this list of consecutive segments into a tiny list of maximum 3 segments.
 * And that makes the Path rendering much faster.
 *
 * @author Fabrice Fontaine
 * @since 6.2.0
 */

public class SideOptimizationPointAccepter implements PointAccepter {

    private static final int STATUS_DIFFERENT = 0;
    private static final int STATUS_SAME_X = 1;
    private static final int STATUS_SAME_Y = 2;

    private final PointL mLatestPoint = new PointL();
    private final PointL mStartPoint = new PointL();
    private final PointAccepter mPointAccepter;
    private boolean mFirst;
    private long mMin;
    private long mMax;
    private int mStatus;

    /**
     * We optimize on top of another {@link PointAccepter}
     */
    public SideOptimizationPointAccepter(final PointAccepter pPointAccepter) {
        mPointAccepter = pPointAccepter;
    }

    @Override
    public void init() {
        mFirst = true;
        mStatus = STATUS_DIFFERENT;
        mPointAccepter.init();
    }

    @Override
    public void add(final long pX, final long pY) {
        if (mFirst) {
            mFirst = false;
            addToAccepter(pX, pY);
            mLatestPoint.set(pX, pY);
            return;
        }
        if (mLatestPoint.x == pX && mLatestPoint.y == pY) {
            return;
        }
        if (mLatestPoint.x == pX) {
            if (mStatus == STATUS_SAME_X) {
                if (mMin > pY) {
                    mMin = pY;
                }
                if (mMax < pY) {
                    mMax = pY;
                }
            } else {
                flushSides();
                mStatus = STATUS_SAME_X;
                mStartPoint.set(mLatestPoint);
                mMin = Math.min(pY, mLatestPoint.y);
                mMax = Math.max(pY, mLatestPoint.y);
            }
        } else if (mLatestPoint.y == pY) {
            if (mStatus == STATUS_SAME_Y) {
                if (mMin > pX) {
                    mMin = pX;
                }
                if (mMax < pX) {
                    mMax = pX;
                }
            } else {
                flushSides();
                mStatus = STATUS_SAME_Y;
                mStartPoint.set(mLatestPoint);
                mMin = Math.min(pX, mLatestPoint.x);
                mMax = Math.max(pX, mLatestPoint.x);
            }
        } else {
            flushSides();
            addToAccepter(pX, pY);
        }
        mLatestPoint.set(pX, pY);
    }

    @Override
    public void end() {
        flushSides();
        mPointAccepter.end();
    }

    /**
     * Flushing the side (same X or same Y) computed so far
     */
    private void flushSides() {
        final long segmentMin;
        final long segmentMax;
        switch (mStatus) {
            case STATUS_DIFFERENT:
                break;
            case STATUS_SAME_X:
                final long x = mStartPoint.x;
                if (mStartPoint.y <= mLatestPoint.y) {
                    segmentMin = mStartPoint.y;
                    segmentMax = mLatestPoint.y;
                } else {
                    segmentMin = mLatestPoint.y;
                    segmentMax = mStartPoint.y;
                }
                if (mMin < segmentMin) {
                    addToAccepter(x, mMin);
                }
                if (mMax > segmentMax) {
                    addToAccepter(x, mMax);
                }
                addToAccepter(x, mLatestPoint.y);
                break;
            case STATUS_SAME_Y:
                final long y = mStartPoint.y;
                if (mStartPoint.x <= mLatestPoint.x) {
                    segmentMin = mStartPoint.x;
                    segmentMax = mLatestPoint.x;
                } else {
                    segmentMin = mLatestPoint.x;
                    segmentMax = mStartPoint.x;
                }
                if (mMin < segmentMin) {
                    addToAccepter(mMin, y);
                }
                if (mMax > segmentMax) {
                    addToAccepter(mMax, y);
                }
                addToAccepter(mLatestPoint.x, y);
                break;
        }
        mStatus = STATUS_DIFFERENT;
    }

    /**
     * Actually adding the point to the embedded {@link PointAccepter}
     */
    private void addToAccepter(final long pX, final long pY) {
        mPointAccepter.add(pX, pY);
    }
}

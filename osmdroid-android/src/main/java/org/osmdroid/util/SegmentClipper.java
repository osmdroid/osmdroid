package org.osmdroid.util;

/**
 * A tool to clip segments
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class SegmentClipper implements PointAccepter {

    // for optimization reasons: avoiding to create objects all the time
    private final PointL mOptimIntersection = new PointL();
    private final PointL mOptimIntersection1 = new PointL();
    private final PointL mOptimIntersection2 = new PointL();

    private long mXMin;
    private long mYMin;
    private long mXMax;
    private long mYMax;
    private PointAccepter mPointAccepter;
    private IntegerAccepter mIntegerAccepter;
    private final long[] cornerX = new long[4];
    private final long[] cornerY = new long[4];
    private final PointL mPoint0 = new PointL();
    private final PointL mPoint1 = new PointL();
    private boolean mFirstPoint;
    /**
     * If true we keep the invisible segments: they have an impact on Path inner area
     */
    private boolean mPathMode;

    private int mCurrentSegmentIndex;

    public void set(final long pXMin, final long pYMin, final long pXMax, final long pYMax,
                    final PointAccepter pPointAccepter, final IntegerAccepter pIntegerAccepter, final boolean pPathMode) {
        mXMin = pXMin;
        mYMin = pYMin;
        mXMax = pXMax;
        mYMax = pYMax;
        cornerX[0] = cornerX[1] = mXMin;
        cornerX[2] = cornerX[3] = mXMax;
        cornerY[0] = cornerY[2] = mYMin;
        cornerY[1] = cornerY[3] = mYMax;
        mPointAccepter = pPointAccepter;
        mIntegerAccepter = pIntegerAccepter;
        mPathMode = pPathMode;
    }

    public void set(final long pXMin, final long pYMin, final long pXMax, final long pYMax,
                    final PointAccepter pPointAccepter, final boolean pPathMode) {
        set(pXMin, pYMin, pXMax, pYMax, pPointAccepter, null, pPathMode);
    }

    @Override
    public void init() {
        mFirstPoint = true;
        if (mIntegerAccepter != null) {
            mIntegerAccepter.init();
        }
        mPointAccepter.init();
    }

    @Override
    public void add(final long pX, final long pY) {
        mPoint1.set(pX, pY);
        if (mFirstPoint) {
            mFirstPoint = false;
            mCurrentSegmentIndex = 0;
        } else {
            clip(mPoint0.x, mPoint0.y, mPoint1.x, mPoint1.y);
            mCurrentSegmentIndex++;
        }
        mPoint0.set(mPoint1);
    }

    @Override
    public void end() {
        if (mIntegerAccepter != null) {
            mIntegerAccepter.end();
        }
        mPointAccepter.end();
    }

    /**
     * Clip a segment into the clip area
     */
    public void clip(final long pX0, final long pY0, final long pX1, final long pY1) {
        if (!mPathMode) {
            if (isOnTheSameSideOut(pX0, pY0, pX1, pY1)) {
                return;
            }
        }
        if (isInClipArea(pX0, pY0)) {
            if (isInClipArea(pX1, pY1)) {
                nextVertex(pX0, pY0);
                nextVertex(pX1, pY1);
                return;
            }
            if (intersection(pX0, pY0, pX1, pY1)) {
                nextVertex(pX0, pY0);
                nextVertex(mOptimIntersection.x, mOptimIntersection.y);
                if (mPathMode) {
                    nextVertex(clipX(pX1), clipY(pY1));
                }
                return;
            }
            throw new RuntimeException("Cannot find expected mOptimIntersection for " + new RectL(pX0, pY0, pX1, pY1));
        }
        if (isInClipArea(pX1, pY1)) {
            if (intersection(pX0, pY0, pX1, pY1)) {
                if (mPathMode) {
                    nextVertex(clipX(pX0), clipY(pY0));
                }
                nextVertex(mOptimIntersection.x, mOptimIntersection.y);
                nextVertex(pX1, pY1);
                return;
            }
            throw new RuntimeException("Cannot find expected mOptimIntersection for " + new RectL(pX0, pY0, pX1, pY1));
        }
        // no point is on the screen
        int count = 0;
        if (intersection(pX0, pY0, pX1, pY1, mXMin, mYMin, mXMin, mYMax)) { // x mClipMin segment
            final PointL point = count++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (intersection(pX0, pY0, pX1, pY1, mXMax, mYMin, mXMax, mYMax)) { // x mClipMax segment
            final PointL point = count++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (intersection(pX0, pY0, pX1, pY1, mXMin, mYMin, mXMax, mYMin)) { // y mClipMin segment
            final PointL point = count++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (intersection(pX0, pY0, pX1, pY1, mXMin, mYMax, mXMax, mYMax)) { // y mClipMax segment
            final PointL point = count++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (count == 2) {
            final double distance1 = Distance.getSquaredDistanceToPoint(
                    mOptimIntersection1.x, mOptimIntersection1.y, pX0, pY0);
            final double distance2 = Distance.getSquaredDistanceToPoint(
                    mOptimIntersection2.x, mOptimIntersection2.y, pX0, pY0);
            final PointL start = distance1 < distance2 ? mOptimIntersection1 : mOptimIntersection2;
            final PointL end = distance1 < distance2 ? mOptimIntersection2 : mOptimIntersection1;
            if (mPathMode) {
                nextVertex(clipX(pX0), clipY(pY0));
            }
            nextVertex(start.x, start.y);
            nextVertex(end.x, end.y);
            if (mPathMode) {
                nextVertex(clipX(pX1), clipY(pY1));
            }
            return;
        }
        if (count == 1) {
            if (mPathMode) {
                nextVertex(clipX(pX0), clipY(pY0));
                nextVertex(mOptimIntersection1.x, mOptimIntersection1.y);
                nextVertex(clipX(pX1), clipY(pY1));
            }
            return;
        }
        if (count == 0) {
            if (mPathMode) {
                nextVertex(clipX(pX0), clipY(pY0));
                final int corner = getClosestCorner(pX0, pY0, pX1, pY1);
                nextVertex(cornerX[corner], cornerY[corner]);
                nextVertex(clipX(pX1), clipY(pY1));
            }
            return;
        }
        throw new RuntimeException("Impossible mOptimIntersection count (" + count + ")");
    }

    /**
     * Check if a point is in the clip area
     */
    public boolean isInClipArea(final long pX, final long pY) {
        return pX > mXMin && pX < mXMax && pY > mYMin && pY < mYMax;
    }

    /**
     * Clip a value into the clip area min/max
     */
    private static long clip(final long pValue, final long pMin, final long pMax) {
        return pValue <= pMin ? pMin : pValue >= pMax ? pMax : pValue;
    }

    private long clipX(final long pX) {
        return clip(pX, mXMin, mXMax);
    }

    private long clipY(final long pY) {
        return clip(pY, mYMin, mYMax);
    }

    private void nextVertex(final long pX, final long pY) {
        if (mIntegerAccepter != null) {
            mIntegerAccepter.add(mCurrentSegmentIndex);
        }
        mPointAccepter.add(pX, pY);
    }

    /**
     * Intersection of two segments
     */
    private boolean intersection(
            final long pX0, final long pY0, final long pX1, final long pY1,
            final long pX2, final long pY2, final long pX3, final long pY3
    ) {
        return SegmentIntersection.intersection(
                pX0, pY0, pX1, pY1,
                pX2, pY2, pX3, pY3, mOptimIntersection);
    }

    /**
     * Intersection of a segment with the 4 segments of the clip area
     */
    private boolean intersection(final long pX0, final long pY0, final long pX1, final long pY1) {
        return intersection(pX0, pY0, pX1, pY1, mXMin, mYMin, mXMin, mYMax) // x min segment
                || intersection(pX0, pY0, pX1, pY1, mXMax, mYMin, mXMax, mYMax) // x max segment
                || intersection(pX0, pY0, pX1, pY1, mXMin, mYMin, mXMax, mYMin) // y min segment
                || intersection(pX0, pY0, pX1, pY1, mXMin, mYMax, mXMax, mYMax); // y max segment
    }

    /**
     * Gets the clip area corner which is the closest to the given segment
     *
     * @since 6.0.0
     * We have a clip area and we have a segment with no intersection with this clip area.
     * The question is: how do we clip this segment?
     * If we only clip both segment ends, we may end up with a (min,min) x (max,max)
     * clip approximation that displays a backslash on the screen.
     * The idea is to compute the clip area corner which is the closest to the segment,
     * and to use it as a clip step.
     * Which will do something like:
     * (min,min)[first segment point] x (min,max)[closest corner] x (max,max)[second segment point]
     * or
     * (min,min)[first segment point] x (max,min)[closest corner] x (max,max)[second segment point]
     */
    private int getClosestCorner(final long pX0, final long pY0, final long pX1, final long pY1) {
        double min = Double.MAX_VALUE;
        int corner = 0;
        for (int i = 0; i < cornerX.length; i++) {
            final double distance = Distance.getSquaredDistanceToSegment(
                    cornerX[i], cornerY[i],
                    pX0, pY0, pX1, pY1);
            if (min > distance) {
                min = distance;
                corner = i;
            }
        }
        return corner;
    }

    /**
     * Optimization for lines (as opposed to Path)
     * If both points are outside of the clip area and "on the same side of the outside" (sic)
     * we don't need to compute anything anymore as it won't draw a line in the end
     *
     * @since 6.0.0
     */
    private boolean isOnTheSameSideOut(final long pX0, final long pY0, final long pX1, final long pY1) {
        return (pX0 < mXMin && pX1 < mXMin)
                || (pX0 > mXMax && pX1 > mXMax)
                || (pY0 < mYMin && pY1 < mYMin)
                || (pY0 > mYMax && pY1 > mYMax);
    }

}

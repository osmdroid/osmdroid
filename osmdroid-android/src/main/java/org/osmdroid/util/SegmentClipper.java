package org.osmdroid.util;

/**
 * A tool to clip segments
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class SegmentClipper {

    public interface SegmentClippable {
        void init();
        void lineTo(final long pX, final long pY);
    }

    // for optimization reasons: avoiding to create objects all the time
    private final PointL mOptimIntersection = new PointL();
    private final PointL mOptimIntersection1 = new PointL();
    private final PointL mOptimIntersection2 = new PointL();

    private final long mXMin;
    private final long mYMin;
    private final long mXMax;
    private final long mYMax;
    private final SegmentClippable mSegmentClippable;
    private final long[] cornerX = new long[4];
    private final long[] cornerY = new long[4];

    public SegmentClipper(final long pXMin, final long pYMin, final long pXMax, final long pYMax,
                          final SegmentClippable pSegmentClippable) {
        mXMin = pXMin;
        mYMin = pYMin;
        mXMax = pXMax;
        mYMax = pYMax;
        cornerX[0] = cornerX[1] = mXMin;
        cornerX[2] = cornerX[3] = mXMax;
        cornerY[0] = cornerY[2] = mYMin;
        cornerY[1] = cornerY[3] = mYMax;
        mSegmentClippable = pSegmentClippable;
    }

    /**
     * Clip a segment into the clip area
     */
    public void clip(final RectL pSegment) {
        if (isInClipArea(pSegment.left, pSegment.top)) {
            if (isInClipArea(pSegment.right, pSegment.bottom)) {
                add(pSegment.left, pSegment.top);
                add(pSegment.right, pSegment.bottom);
                return;
            }
            if (intersection(pSegment)) {
                add(pSegment.left, pSegment.top);
                add(mOptimIntersection.x, mOptimIntersection.y);
                add(clipX(pSegment.right), clipY(pSegment.bottom));
                return;
            }
            throw new RuntimeException("Cannot find expected mOptimIntersection for " + pSegment);
        }
        if (isInClipArea(pSegment.right, pSegment.bottom)) {
            if (intersection(pSegment)) {
                add(clipX(pSegment.left), clipY(pSegment.top));
                add(mOptimIntersection.x, mOptimIntersection.y);
                add(pSegment.right, pSegment.bottom);
                return;
            }
            throw new RuntimeException("Cannot find expected mOptimIntersection for " + pSegment);
        }
        // no point is on the screen
        int count = 0;
        if (intersection(pSegment, mXMin, mYMin, mXMin, mYMax)) { // x mClipMin segment
            final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (intersection(pSegment, mXMax, mYMin, mXMax, mYMax)) { // x mClipMax segment
            final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (intersection(pSegment, mXMin, mYMin, mXMax, mYMin)) { // y mClipMin segment
            final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (intersection(pSegment, mXMin, mYMax, mXMax, mYMax)) { // y mClipMax segment
            final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
            point.set(mOptimIntersection);
        }
        if (count == 2) {
            final double distance1 = Distance.getSquaredDistanceToPoint(
                    mOptimIntersection1.x, mOptimIntersection1.y, pSegment.left, pSegment.top);
            final double distance2 = Distance.getSquaredDistanceToPoint(
                    mOptimIntersection2.x, mOptimIntersection2.y, pSegment.left, pSegment.top);
            final PointL start = distance1 < distance2 ? mOptimIntersection1 : mOptimIntersection2;
            final PointL end =  distance1 < distance2 ? mOptimIntersection2 : mOptimIntersection1;
            add(clipX(pSegment.left), clipY(pSegment.top));
            add(start.x, start.y);
            add(end.x, end.y);
            add(clipX(pSegment.right), clipY(pSegment.bottom));
            return;
        }
        if (count == 1) {
            add(clipX(pSegment.left), clipY(pSegment.top));
            add(mOptimIntersection1.x, mOptimIntersection1.y);
            add(clipX(pSegment.right), clipY(pSegment.bottom));
            return;
        }
        if (count == 0) {
            add(clipX(pSegment.left), clipY(pSegment.top));
            final int corner = getClosestCorner(pSegment);
            add(cornerX[corner], cornerY[corner]);
            add(clipX(pSegment.right), clipY(pSegment.bottom));
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

    private void add(final long pX, final long pY) {
        mSegmentClippable.lineTo(pX, pY);
    }

    /**
     * Intersection of two segments
     */
    private boolean intersection(
            final RectL segment, final long x3, final long y3, final long x4, final long y4
    ) {
        return SegmentIntersection.intersection(
                segment.left, segment.top, segment.right, segment.bottom,
                x3, y3, x4, y4, mOptimIntersection);
    }

    /**
     * Intersection of a segment with the 4 segments of the clip area
     */
    private boolean intersection(final RectL pSegment) {
        return intersection(pSegment, mXMin, mYMin, mXMin, mYMax) // x min segment
                || intersection(pSegment, mXMax, mYMin, mXMax, mYMax) // x max segment
                || intersection(pSegment, mXMin, mYMin, mXMax, mYMin) // y min segment
                || intersection(pSegment, mXMin, mYMax, mXMax, mYMax); // y max segment
    }

    /**
     * Gets the clip area corner which is the closest to the given segment
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
    private int getClosestCorner(final RectL pSegment) {
        double min = Double.MAX_VALUE;
        int corner = 0;
        for (int i = 0 ; i < cornerX.length ; i ++) {
            final double distance = Distance.getSquaredDistanceToSegment(
                    cornerX[i], cornerY[i],
                    pSegment.left, pSegment.top, pSegment.right, pSegment.bottom);
            if (min > distance) {
                min = distance;
                corner = i;
            }
        }
        return corner;
    }
}

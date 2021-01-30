package org.osmdroid.util;

import org.osmdroid.views.overlay.SpeechBalloonOverlay;

/**
 * Helper class for {@link SpeechBalloonOverlay}
 * whose main interest is method {@link #compute(RectL, PointL, double, PointL, PointL)}
 *
 * @author Fabrice Fontaine
 * @since 6.1.1
 */
public class SpeechBalloonHelper {

    public static final int CORNER_INSIDE = -1;
    public static final int CORNER_NONE = 0;
    public static final int CORNER_LEFT = 1;
    public static final int CORNER_RIGHT = 2;
    public static final int CORNER_TOP = 4;
    public static final int CORNER_BOTTOM = 8;

    private final PointL mTrianglePoint = new PointL();
    private RectL mRect;
    private PointL mPoint;

    /**
     * Computes the intersection between a rectangle and the triangle that starts from a Point
     * and goes to a circle centered on the rectangle's center
     *
     * @return CORNER_INSIDE if the Point is within the rectangle, CORNER_NONE if both intersections
     * are on the same border, or a combination of CORNER_% that tells which rectangle's corner is
     * included between both intersections
     */
    public int compute(final RectL pInputRect, final PointL pInputPoint, final double pInputRadius,
                       final PointL pOutputIntersection1, final PointL pOutputIntersection2) {
        mRect = pInputRect;
        mPoint = pInputPoint;

        if (pInputRect.contains(mPoint.x, mPoint.y)) {
            return CORNER_INSIDE;
        }

        final double angle = MyMath.computeAngle(mRect.centerX(), mRect.centerY(), mPoint.x, mPoint.y);

        computeCirclePoint(mTrianglePoint, pInputRadius, angle, false);
        final int corner1 = checkIntersection(pOutputIntersection1);
        computeCirclePoint(mTrianglePoint, pInputRadius, angle, true);
        final int corner2 = checkIntersection(pOutputIntersection2);
        if (corner1 == corner2) {
            return CORNER_NONE;
        }
        return corner1 | corner2;
    }

    private int checkIntersection(final PointL pIntersection) {
        if (mPoint.y <= mRect.top && checkIntersectionY(mRect.top, pIntersection)) {
            return CORNER_TOP;
        }
        if (mPoint.y >= mRect.bottom && checkIntersectionY(mRect.bottom, pIntersection)) {
            return CORNER_BOTTOM;
        }
        if (mPoint.x <= mRect.left && checkIntersectionX(mRect.left, pIntersection)) {
            return CORNER_LEFT;
        }
        if (mPoint.x >= mRect.right && checkIntersectionX(mRect.right, pIntersection)) {
            return CORNER_RIGHT;
        }
        throw new IllegalArgumentException();
    }

    private boolean checkIntersectionX(final long pX, final PointL pIntersection) {
        return SegmentIntersection.intersection(
                mPoint.x, mPoint.y, mTrianglePoint.x, mTrianglePoint.y,
                pX, mRect.top, pX, mRect.bottom,
                pIntersection);
    }

    private boolean checkIntersectionY(final long pY, final PointL pIntersection) {
        return SegmentIntersection.intersection(
                mPoint.x, mPoint.y, mTrianglePoint.x, mTrianglePoint.y,
                mRect.left, pY, mRect.right, pY,
                pIntersection);
    }

    private void computeCirclePoint(final PointL pDestination, final double pRadius,
                                    final double pAngle, final boolean pFirst) {
        MyMath.computeCirclePoint(mRect.centerX(), mRect.centerY(), pRadius,
                pAngle + Math.PI / 2 * (pFirst ? 1 : -1), pDestination);
    }
}
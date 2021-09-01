package org.osmdroid.util;

/**
 * Tools about 2D distance computation
 * Optimized code: we compute the square of the distance.
 * If you really want to know the distance, apply Math.sqrt
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class Distance {

    /**
     * Square of the distance between two points
     */
    public static double getSquaredDistanceToPoint(
            final double pFromX, final double pFromY, final double pToX, final double pToY) {
        final double dX = pFromX - pToX;
        final double dY = pFromY - pToY;
        return dX * dX + dY * dY;
    }

    /**
     * Square of the distance between a point and line AB
     */
    public static double getSquaredDistanceToLine(
            final double pFromX, final double pFromY,
            final double pAX, final double pAY, final double pBX, final double pBY
    ) {
        return getSquaredDistanceToProjection(pFromX, pFromY, pAX, pAY, pBX, pBY,
                getProjectionFactorToLine(pFromX, pFromY, pAX, pAY, pBX, pBY));
    }

    /**
     * Square of the distance between a point and segment AB
     */
    public static double getSquaredDistanceToSegment(
            final double pFromX, final double pFromY,
            final double pAX, final double pAY, final double pBX, final double pBY
    ) {
        return getSquaredDistanceToProjection(pFromX, pFromY, pAX, pAY, pBX, pBY,
                getProjectionFactorToSegment(pFromX, pFromY, pAX, pAY, pBX, pBY));
    }

    /**
     * @since 6.0.3
     * Square of the distance between a point and its projection on line AB
     */
    public static double getSquaredDistanceToProjection(
            final double pFromX, final double pFromY,
            final double pAX, final double pAY, final double pBX, final double pBY,
            final double pProjectionFactor
    ) {
        final double projectedX = pAX + (pBX - pAX) * pProjectionFactor;
        final double projectedY = pAY + (pBY - pAY) * pProjectionFactor;
        return getSquaredDistanceToPoint(pFromX, pFromY, projectedX, projectedY);
    }

    /**
     * @return 0 if projected to A, 1 if projected to B, [0,1] if projected inside segment [A,B],
     * &lt; 0 or &gt; 1 if projected outside of the segment
     * @since 6.0.3
     * Projection factor on line AB from a point
     */
    public static double getProjectionFactorToLine(
            final double pFromX, final double pFromY,
            final double pAX, final double pAY, final double pBX, final double pBY
    ) {
        if (pAX == pBX && pAY == pBY) {
            return 0;
        }
        return dotProduct(pAX, pAY, pBX, pBY, pFromX, pFromY)
                / getSquaredDistanceToPoint(pAX, pAY, pBX, pBY);
    }

    /**
     * @return [0, 1]; 0 if projected to A, 1 if projected to B
     * @since 6.0.3
     * Projection factor on segment AB from a point
     */
    public static double getProjectionFactorToSegment(
            final double pFromX, final double pFromY,
            final double pAX, final double pAY, final double pBX, final double pBY
    ) {
        final double result = getProjectionFactorToLine(pFromX, pFromY, pAX, pAY, pBX, pBY);
        if (result < 0) {
            return 0;
        }
        if (result > 1) {
            return 1;
        }
        return result;
    }

    /**
     * Compute the dot product AB x AC
     */
    private static double dotProduct(
            final double pAX, final double pAY, final double pBX, final double pBY,
            final double pCX, final double pCY) {
        return (pBX - pAX) * (pCX - pAX) + (pBY - pAY) * (pCY - pAY);
    }
}

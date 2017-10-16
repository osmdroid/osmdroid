package org.osmdroid.util;

/**
 * Tools about 2D distance computation
 * Optimized code: we compute the square of the distance.
 * If you really want to know the distance, apply Math.sqrt
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class Distance {

    /**
     * Compute the distance between two points
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
        if (pAX == pBX && pAY == pBY) {
            return getSquaredDistanceToPoint(pAX, pAY, pFromX, pFromY);
        }
        final double cross = crossProduct(pAX, pAY, pBX, pBY, pFromX, pFromY);
        return cross * cross / getSquaredDistanceToPoint(pAX, pAY, pBX, pBY);
    }

    /**
     * Square of the distance between a point and segment AB
     */
    public static double getSquaredDistanceToSegment(
            final double pFromX, final double pFromY,
            final double pAX, final double pAY, final double pBX, final double pBY
    ) {
        if (pAX == pBX && pAY == pBY) {
            return getSquaredDistanceToPoint(pAX, pAY, pFromX, pFromY);
        }
        double dot = dotProduct(pAX, pAY, pBX, pBY, pFromX, pFromY);
        if (dot > 0) {
            return getSquaredDistanceToPoint(pFromX, pFromY, pBX, pBY);
        }
        dot = dotProduct(pBX, pBY, pAX, pAY, pFromX, pFromY);
        if (dot > 0) {
            return getSquaredDistanceToPoint(pFromX, pFromY, pAX, pAY);
        }
        final double cross = crossProduct(pAX, pAY, pBX, pBY, pFromX, pFromY);
        return cross * cross / getSquaredDistanceToPoint(pAX, pAY, pBX, pBY);
    }

    /**
     * Compute the cross product AB x AC
     */
    private static double crossProduct(
            final double pAX, final double pAY, final double pBX, final double pBY,
            final double pCX, final double pCY) {
        return (pBX - pAX) * (pCY - pAY) - (pBY - pAY) * (pCX - pAX);
    }

    /**
     * Compute the dot product AB x AC
     */
    private static double dotProduct(
            final double pAX, final double pAY, final double pBX, final double pBY,
            final double pCX, final double pCY) {
        return (pBX - pAX) * (pCX - pBX) + (pBY - pAY) * (pCY - pBY);
    }
}

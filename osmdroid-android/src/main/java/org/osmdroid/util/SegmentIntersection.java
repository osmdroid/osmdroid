package org.osmdroid.util;

/**
 * A class dedicated to the computation of 2D segments intersection points
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class SegmentIntersection {

    /**
     * @param pXA           Starting point of Segment 1 [AB]
     * @param pYA           Starting point of Segment 1
     * @param pXB           Ending point of Segment 1
     * @param pYB           Ending point of Segment 1
     * @param pXC           Starting point of Segment 2 [CD]
     * @param pYC           Starting point of Segment 2
     * @param pXD           Ending point of Segment 2
     * @param pYD           Ending point of Segment 2
     * @param pIntersection Intersection point as output; can be null
     * @return true if the segments intersect
     * Parameters are typed as double for overflow and precision reasons.
     */
    public static boolean intersection(
            final double pXA, final double pYA, final double pXB, final double pYB,
            final double pXC, final double pYC, final double pXD, final double pYD,
            final PointL pIntersection
    ) {
        if (parallelSideEffect(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection)) {
            return true;
        }
        if (divisionByZeroSideEffect(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection)) {
            return true;
        }
        final double d = (pXA - pXB) * (pYC - pYD) - (pYA - pYB) * (pXC - pXD);
        if (d == 0) {
            return false;
        }
        final double xi = ((pXC - pXD) * (pXA * pYB - pYA * pXB) - (pXA - pXB) * (pXC * pYD - pYC * pXD)) / d;
        final double yi = ((pYC - pYD) * (pXA * pYB - pYA * pXB) - (pYA - pYB) * (pXC * pYD - pYC * pXD)) / d;
        return check(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection, xi, yi);
    }

    /**
     * When the segments are parallels and overlap, the middle of the overlap is considered as the intersection
     */
    private static boolean parallelSideEffect(
            final double pXA, final double pYA, final double pXB, final double pYB,
            final double pXC, final double pYC, final double pXD, final double pYD,
            final PointL pIntersection
    ) {
        if (pXA == pXB) {
            return parallelSideEffectSameX(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection);
        }
        if (pXC == pXD) {
            return parallelSideEffectSameX(pXC, pYC, pXD, pYD, pXA, pYA, pXB, pYB, pIntersection);
        }
        // formula like "y = k*x + b"
        final double k1 = (pYB - pYA) / (pXB - pXA);
        final double k2 = (pYD - pYC) / (pXD - pXC);
        if (k1 != k2) { // not parallel
            return false;
        }
        final double b1 = pYA - k1 * pXA;
        final double b2 = pYC - k2 * pXC;
        if (b1 != b2) { // strictly parallel, no overlap
            return false;
        }
        final double xi = middle(pXA, pXB, pXC, pXD);
        final double yi = middle(pYA, pYB, pYC, pYD);
        return check(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection, xi, yi);
    }

    private static double middle(final double pA, final double pB, final double pC, final double pD) {
        return (Math.min(Math.max(pA, pB), Math.max(pC, pD))
                + Math.max(Math.min(pA, pB), Math.min(pC, pD))) / 2;
    }

    /**
     * Checks if computed intersection is valid and sets output accordingly
     *
     * @param pXI intersection x
     * @param pYI intersection y
     * @return true if OK
     */
    private static boolean check(
            final double pXA, final double pYA, final double pXB, final double pYB,
            final double pXC, final double pYC, final double pXD, final double pYD,
            final PointL pIntersection,
            final double pXI, final double pYI
    ) {
        if (pXI < Math.min(pXA, pXB) || pXI > Math.max(pXA, pXB)) {
            return false;
        }
        if (pXI < Math.min(pXC, pXD) || pXI > Math.max(pXC, pXD)) {
            return false;
        }
        if (pYI < Math.min(pYA, pYB) || pYI > Math.max(pYA, pYB)) {
            return false;
        }
        if (pYI < Math.min(pYC, pYD) || pYI > Math.max(pYC, pYD)) {
            return false;
        }
        if (pIntersection != null) {
            pIntersection.x = Math.round(pXI);
            pIntersection.y = Math.round(pYI);
        }
        return true;
    }

    /**
     * Used when we cannot use the "y = a*x + b" formula
     */
    private static boolean parallelSideEffectSameX(
            final double pXA, final double pYA, final double pXB, final double pYB,
            final double pXC, final double pYC, final double pXD, final double pYD,
            final PointL pIntersection
    ) {
        if (pXA != pXB) {
            return false;
        }
        if (pXC != pXD) {
            return false; // cannot be parallel
        }
        if (pXA != pXC) {
            return false; // not the same x
        }
        final double yi = middle(pYA, pYB, pYC, pYD);
        return check(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection, pXA, yi);
    }

    /**
     * Main intersection formula works only without division by zero
     */
    private static boolean divisionByZeroSideEffect(
            final double pXA, final double pYA, final double pXB, final double pYB,
            final double pXC, final double pYC, final double pXD, final double pYD,
            final PointL pIntersection
    ) {
        return divisionByZeroSideEffectX(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection)
                || divisionByZeroSideEffectX(pXC, pYC, pXD, pYD, pXA, pYA, pXB, pYB, pIntersection)
                || divisionByZeroSideEffectY(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection)
                || divisionByZeroSideEffectY(pXC, pYC, pXD, pYD, pXA, pYA, pXB, pYB, pIntersection);
    }

    private static boolean divisionByZeroSideEffectX(
            final double pXA, final double pYA, final double pXB, final double pYB,
            final double pXC, final double pYC, final double pXD, final double pYD,
            final PointL pIntersection
    ) {
        if (pXA != pXB) {
            return false;
        }
        if (pXC == pXD) {
            return false; // should be handled by the "parallel" side effect
        }
        final double k = (pXA - pXC) / (pXD - pXC);
        final double yi = k * (pYD - pYC) + pYC;
        return check(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection, pXA, yi);
    }

    private static boolean divisionByZeroSideEffectY(
            final double pXA, final double pYA, final double pXB, final double pYB,
            final double pXC, final double pYC, final double pXD, final double pYD,
            final PointL pIntersection
    ) {
        if (pYA != pYB) {
            return false;
        }
        if (pYC == pYD) {
            return false; // should be handled by the "parallel" side effect
        }
        final double k = (pYA - pYC) / (pYD - pYC);
        final double xi = k * (pXD - pXC) + pXC;
        return check(pXA, pYA, pXB, pYB, pXC, pYC, pXD, pYD, pIntersection, xi, pYA);
    }
}
// Created by plusminus on 20:36:01 - 26.09.2008
package org.osmdroid.util;

import org.osmdroid.views.util.constants.MathConstants;

/**
 * @author Nicolas Gramlich
 */
public class MyMath implements MathConstants {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * This is a utility class with only static members.
     */
    private MyMath() {
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public static double gudermannInverse(final double aLatitude) {
        return Math.log(Math.tan(PI_4 + (DEG2RAD * aLatitude / 2)));
    }

    public static double gudermann(final double y) {
        return RAD2DEG * Math.atan(Math.sinh(y));
    }

    public static int mod(int number, final int modulus) {
        if (number > 0)
            return number % modulus;

        while (number < 0)
            number += modulus;

        return number;
    }

    /**
     * Casting a _negative_ double into a long has a counter-intuitive result.
     * E.g. (long)(-0.4) = 0, though -1 would be expected.
     * Math.floor would be the answer, but I assume we could go faster than (long)Math.floor
     *
     * @since 6.0.0
     */
    public static long floorToLong(final double pValue) {
        final long result = (long) pValue;
        if (result <= pValue) {
            return result;
        }
        return result - 1;
    }

    /**
     * @since 6.0.0
     */
    public static int floorToInt(final double pValue) {
        final int result = (int) pValue;
        if (result <= pValue) {
            return result;
        }
        return result - 1;
    }

    /**
     * @since 6.1.0
     * Moved from another MyMath (org.osmdroid.views.util)
     * <p>
     * Calculates i.e. the increase of zoomlevel needed when the visible latitude needs to be bigger
     * by <code>factor</code>.
     * <p>
     * Assert.assertEquals(1, getNextSquareNumberAbove(1.1f)); Assert.assertEquals(2,
     * getNextSquareNumberAbove(2.1f)); Assert.assertEquals(2, getNextSquareNumberAbove(3.9f));
     * Assert.assertEquals(3, getNextSquareNumberAbove(4.1f)); Assert.assertEquals(3,
     * getNextSquareNumberAbove(7.9f)); Assert.assertEquals(4, getNextSquareNumberAbove(8.1f));
     * Assert.assertEquals(5, getNextSquareNumberAbove(16.1f));
     * <p>
     * Assert.assertEquals(-1, - getNextSquareNumberAbove(1 / 0.4f) + 1); Assert.assertEquals(-2, -
     * getNextSquareNumberAbove(1 / 0.24f) + 1);
     */
    public static int getNextSquareNumberAbove(final float factor) {
        int out = 0;
        int cur = 1;
        int i = 1;
        while (true) {
            if (cur > factor)
                return out;

            out = i;
            cur *= 2;
            i++;
        }
    }

    /**
     * @param pStart     start angle
     * @param pEnd       end angle
     * @param pClockwise if null, get the smallest difference (in absolute value)
     *                   if true, go clockwise
     *                   if false, go anticlockwise
     * @since 6.1.0
     */
    public static double getAngleDifference(double pStart, double pEnd, final Boolean pClockwise) {
        final double difference = cleanPositiveAngle(pEnd - pStart);
        if (pClockwise != null) {
            if (pClockwise) {
                return difference;
            } else {
                return difference - 360;
            }
        }
        if (difference < 180) {
            return difference;
        }
        return difference - 360;
    }

    /**
     * @param pAngle angle in degree
     * @return the same angle in [0,360[
     * @since 6.1.0
     */
    public static double cleanPositiveAngle(double pAngle) {
        while (pAngle < 0) {
            pAngle += 360;
        }
        while (pAngle >= 360) {
            pAngle -= 360;
        }
        return pAngle;
    }

    /**
     * @since 6.1.1
     * Computes the angle of a vector
     */
    public static double computeAngle(final long pX1, final long pY1, final long pX2, final long pY2) {
        return Math.atan2(pY2 - pY1, pX2 - pX1);
    }

    /**
     * @param pAngle clockwise angle, in radian, value 0 being 3 o'clock
     * @since 6.1.1
     * Computes the point of a circle from its center, its radius and the angle
     */
    public static void computeCirclePoint(final long pCenterX, final long pCenterY, final double pRadius,
                                          final double pAngle, final PointL pOutput) {
        pOutput.x = pCenterX + (long) (pRadius * Math.cos(pAngle));
        pOutput.y = pCenterY + (long) (pRadius * Math.sin(pAngle));
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}

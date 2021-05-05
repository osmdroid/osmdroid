// Created by plusminus on 20:36:01 - 26.09.2008
package org.osmdroid.views.util;

/**
 * @author Nicolas Gramlich
 * @deprecated Use {@link org.osmdroid.util.MyMath} instead
 */
@Deprecated
public class MyMath {
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

    /**
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
     *
     * @param factor
     * @return
     * @deprecated Use {@link org.osmdroid.util.MyMath#getNextSquareNumberAbove(float)} instead
     */
    @Deprecated
    public static int getNextSquareNumberAbove(final float factor) {
        return org.osmdroid.util.MyMath.getNextSquareNumberAbove(factor);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}

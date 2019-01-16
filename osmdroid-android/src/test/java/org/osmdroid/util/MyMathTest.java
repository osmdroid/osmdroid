package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @since 6.1.0
 * @author Fabrice Fontaine
 */
public class MyMathTest {

    private static final double DELTA = 1E-20;

    @Test
    public void testGetAngleDifference() {
        Assert.assertEquals(20, MyMath.getAngleDifference(10, 30, null), DELTA);
        Assert.assertEquals(20, MyMath.getAngleDifference(10, 30, Boolean.TRUE), DELTA);
        Assert.assertEquals(-340, MyMath.getAngleDifference(10, 30, Boolean.FALSE), DELTA);

        Assert.assertEquals(-20, MyMath.getAngleDifference(30, 10, null), DELTA);
        Assert.assertEquals(340, MyMath.getAngleDifference(30, 10, Boolean.TRUE), DELTA);
        Assert.assertEquals(-20, MyMath.getAngleDifference(30, 10, Boolean.FALSE), DELTA);

        Assert.assertEquals(2, MyMath.getAngleDifference(179, -179, null), DELTA);
        Assert.assertEquals(2, MyMath.getAngleDifference(179, -179, Boolean.TRUE), DELTA);
        Assert.assertEquals(-358, MyMath.getAngleDifference(179, -179, Boolean.FALSE), DELTA);

        Assert.assertEquals(2, MyMath.getAngleDifference(359, 1, null), DELTA);
        Assert.assertEquals(2, MyMath.getAngleDifference(359, 1, Boolean.TRUE), DELTA);
        Assert.assertEquals(-358, MyMath.getAngleDifference(359, 1, Boolean.FALSE), DELTA);
    }
}

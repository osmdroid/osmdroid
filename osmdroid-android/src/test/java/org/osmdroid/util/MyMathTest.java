package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
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

    /**
     * @since 6.1.1
     */
    @Test
    public void testComputeAngle() {
        final double delta = 1E-10;
        final long value = 10;
        Assert.assertEquals(0, MyMath.computeAngle(0, 0, value, 0), delta);
        Assert.assertEquals(Math.PI, MyMath.computeAngle(0, 0, -value, 0), delta);
        Assert.assertEquals(-Math.PI / 2, MyMath.computeAngle(0, 0, 0, -value), delta);
        Assert.assertEquals(Math.PI / 2, MyMath.computeAngle(0, 0, 0, value), delta);
        Assert.assertEquals(-Math.PI / 4, MyMath.computeAngle(0, 0, value, -value), delta);
        Assert.assertEquals(Math.PI / 4, MyMath.computeAngle(0, 0, value, value), delta);
        Assert.assertEquals(-3 * Math.PI / 4, MyMath.computeAngle(0, 0, -value, -value), delta);
        Assert.assertEquals(3 * Math.PI / 4, MyMath.computeAngle(0, 0, -value, value), delta);
    }

    @Test
    public void testComputeCirclePoint() {
        final PointL output = new PointL();
        final long radius = 10;

        MyMath.computeCirclePoint(0, 0, radius, Math.PI, output);
        Assert.assertEquals(-radius, output.x);
        Assert.assertEquals(0, output.y);

        MyMath.computeCirclePoint(0, 0, radius, 0, output);
        Assert.assertEquals(radius, output.x);
        Assert.assertEquals(0, output.y);

        MyMath.computeCirclePoint(0, 0, radius, Math.PI / 2, output);
        Assert.assertEquals(0, output.x);
        Assert.assertEquals(-radius, -output.y);

        MyMath.computeCirclePoint(0, 0, radius, -Math.PI / 2, output);
        Assert.assertEquals(0, output.x);
        Assert.assertEquals(radius, -output.y);

        MyMath.computeCirclePoint(0, 0, radius, Math.PI / 4, output);
        Assert.assertEquals((long) (radius * Math.sqrt(2) / 2), output.x);
        Assert.assertEquals((long) (-radius * Math.sqrt(2) / 2), -output.y);
    }
}

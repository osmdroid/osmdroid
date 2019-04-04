package org.osmdroid.util;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.views.overlay.milestones.MilestoneLister;

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

    /**
     * @since 6.1.1
     * Used to be in the {@link MilestoneLister} unit test class
     */
    @Test
    public void test_orientation() {
        Assert.assertEquals(0, MyMath.getOrientation(1, 1, 1, 1), DELTA);
        Assert.assertEquals(0, MyMath.getOrientation(1, 1, 10, 1), DELTA);
        Assert.assertEquals(45, MyMath.getOrientation(10, 10, 20, 20), DELTA);
        Assert.assertEquals(90, MyMath.getOrientation(10, 10, 10, 20), DELTA);
        Assert.assertEquals(180, MyMath.getOrientation(10, 10, 0, 10), DELTA);
        Assert.assertEquals(-90, MyMath.getOrientation(10, 10, 10, 0), DELTA);
    }
}

package org.osmdroid.util;

import android.graphics.Rect;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author Fabrice Fontaine
 * @since 6.0.2
 * <p>
 * VERY IMPORTANT NOTICE
 * In class RectL, don't use syntaxes like Point.set, Point.offset or Point.center.
 * Use "Point.x=" and "Point.y=" syntaxes instead. Same for Rect.
 * Why?
 * Because class Point - though relatively low level - is part of an Android package
 * that does not belong to standard Java.
 * As a result, using it in Unit Test is a bit heavier.
 * I prefer the light version of unit test.
 * For more info, search "android unit test mock"
 */

public class RectLTest {

    private static final Random mRandom = new Random();
    private static final int mMaxCoordinate = 2000;
    private static final int mIterations = 100;

    /**
     * The rotation center is kept whatever the degrees are
     */
    @Test
    public void testGetRotatedCenter() {
        for (int i = 0; i < mIterations; i++) {
            final long x = getRandomCoordinate();
            final long y = getRandomCoordinate();
            final double degrees = getRandomAngle();
            Assert.assertEquals(x, RectL.getRotatedX(x, y, degrees, x, y));
            Assert.assertEquals(y, RectL.getRotatedY(x, y, degrees, x, y));
        }
    }

    /**
     * For a 0 degree rotation: x = x and y = y
     */
    @Test
    public void testGetRotated0() {
        final double degrees = 0;
        for (int i = 0; i < mIterations; i++) {
            final long x = getRandomCoordinate();
            final long y = getRandomCoordinate();
            final long centerX = getRandomCoordinate();
            final long centerY = getRandomCoordinate();
            Assert.assertEquals(x, RectL.getRotatedX(x, y, degrees, centerX, centerY));
            Assert.assertEquals(y, RectL.getRotatedY(x, y, degrees, centerX, centerY));
        }
    }

    /**
     * For a 90 degree rotation: x = -y and y = x
     * With translation steps around the rotation center
     */
    @Test
    public void testGetRotated90() {
        final double degrees = 90;
        for (int i = 0; i < mIterations; i++) {
            final long x = getRandomCoordinate();
            final long y = getRandomCoordinate();
            final long centerX = getRandomCoordinate();
            final long centerY = getRandomCoordinate();
            Assert.assertEquals(centerX - (y - centerY), RectL.getRotatedX(x, y, degrees, centerX, centerY));
            Assert.assertEquals(centerY + (x - centerX), RectL.getRotatedY(x, y, degrees, centerX, centerY));
        }
    }

    /**
     * For a 180 degree rotation: x = -x and y = -y
     * With translation steps around the rotation center
     */
    @Test
    public void testGetRotated180() {
        final double degrees = 180;
        for (int i = 0; i < mIterations; i++) {
            final long x = getRandomCoordinate();
            final long y = getRandomCoordinate();
            final long centerX = getRandomCoordinate();
            final long centerY = getRandomCoordinate();
            Assert.assertEquals(centerX - (x - centerX), RectL.getRotatedX(x, y, degrees, centerX, centerY));
            Assert.assertEquals(centerY - (y - centerY), RectL.getRotatedY(x, y, degrees, centerX, centerY));
        }
    }

    /**
     * For a 270 degree rotation: x = y and y = -x
     * With translation steps around the rotation center
     */
    @Test
    public void testGetRotated270() {
        final double degrees = 270;
        for (int i = 0; i < mIterations; i++) {
            final long x = getRandomCoordinate();
            final long y = getRandomCoordinate();
            final long centerX = getRandomCoordinate();
            final long centerY = getRandomCoordinate();
            Assert.assertEquals(centerX + (y - centerY), RectL.getRotatedX(x, y, degrees, centerX, centerY));
            Assert.assertEquals(centerY - (x - centerX), RectL.getRotatedY(x, y, degrees, centerX, centerY));
        }
    }

    /**
     * For a 0 degree rotation the rect is kept
     */
    @Test
    public void testGetBounds0() {
        final double degrees = 0;
        final RectL in = new RectL();
        final RectL out = new RectL();
        for (int i = 0; i < mIterations; i++) {
            in.top = getRandomCoordinate();
            in.left = getRandomCoordinate();
            in.bottom = getRandomCoordinate();
            in.right = getRandomCoordinate();
            final long centerX = getRandomCoordinate();
            final long centerY = getRandomCoordinate();
            RectL.getBounds(in, centerX, centerY, degrees, out);
            Assert.assertEquals(in.top, out.top);
            Assert.assertEquals(in.left, out.left);
            Assert.assertEquals(in.bottom, out.bottom);
            Assert.assertEquals(in.right, out.right);
        }
    }

    /**
     * For a 180 degree rotation the rect is mirrored on the rotation center
     */
    @Test
    public void testGetBounds180() {
        final double degrees = 180;
        final RectL in = new RectL();
        final RectL out = new RectL();
        for (int i = 0; i < mIterations; i++) {
            in.top = getRandomCoordinate();
            in.left = getRandomCoordinate();
            in.bottom = getRandomCoordinate();
            in.right = getRandomCoordinate();
            final long centerX = getRandomCoordinate();
            final long centerY = getRandomCoordinate();
            RectL.getBounds(in, centerX, centerY, degrees, out);
            final long top = centerY - (in.top - centerY);
            final long bottom = centerY - (in.bottom - centerY);
            final long left = centerX - (in.left - centerX);
            final long right = centerX - (in.right - centerX);
            Assert.assertEquals(Math.min(top, bottom), out.top);
            Assert.assertEquals(Math.min(left, right), out.left);
            Assert.assertEquals(Math.max(top, bottom), out.bottom);
            Assert.assertEquals(Math.max(left, right), out.right);
        }
    }

    @Test
    public void testGetBoundsSamplesRectL() {
        final RectL in = new RectL();
        final RectL out = new RectL();

        in.set(0, 0, 4, 6);
        RectL.getBounds(in, 0, 0, 180, out);
        Assert.assertEquals(-6, out.top);
        Assert.assertEquals(0, out.bottom);
        Assert.assertEquals(-4, out.left);
        Assert.assertEquals(0, out.right);

        in.set(0, 0, 5, 7);
        RectL.getBounds(in, 0, 0, 90, out);
        Assert.assertEquals(0, out.top);
        Assert.assertEquals(-7, out.left);
        Assert.assertEquals(5, out.bottom);
        Assert.assertEquals(0, out.right);

        in.set(0, 0, 8, 8);
        RectL.getBounds(in, 0, 0, 45, out);
        Assert.assertEquals(0, out.top);
        Assert.assertEquals(-Math.round(8 * Math.sqrt(2) / 2.), out.left);
        Assert.assertEquals(Math.round(8 * Math.sqrt(2)), out.bottom);
        Assert.assertEquals(Math.round(8 * Math.sqrt(2) / 2.), out.right);
    }

    @Test
    public void testGetBoundsSamplesRect() {
        final Rect in = new Rect();
        final Rect out = new Rect();
        in.top = 0; // lousy member setting for Rect - see javadoc comments on class
        in.left = 0;

        in.bottom = 6;
        in.right = 4;
        RectL.getBounds(in, 0, 0, 180, out);
        Assert.assertEquals(-6, out.top);
        Assert.assertEquals(0, out.bottom);
        Assert.assertEquals(-4, out.left);
        Assert.assertEquals(0, out.right);

        in.bottom = 7;
        in.right = 5;
        RectL.getBounds(in, 0, 0, 90, out);
        Assert.assertEquals(0, out.top);
        Assert.assertEquals(-7, out.left);
        Assert.assertEquals(5, out.bottom);
        Assert.assertEquals(0, out.right);

        in.bottom = 8;
        in.right = 8;
        RectL.getBounds(in, 0, 0, 45, out);
        Assert.assertEquals(0, out.top);
        Assert.assertEquals(-Math.round(8 * Math.sqrt(2) / 2.), out.left);
        Assert.assertEquals(Math.round(8 * Math.sqrt(2)), out.bottom);
        Assert.assertEquals(Math.round(8 * Math.sqrt(2) / 2.), out.right);
    }

    private long getRandomCoordinate() {
        return mRandom.nextInt(mMaxCoordinate) * (mRandom.nextBoolean() ? 1 : -1);
    }

    private double getRandomAngle() {
        return mRandom.nextInt(360);
    }
}

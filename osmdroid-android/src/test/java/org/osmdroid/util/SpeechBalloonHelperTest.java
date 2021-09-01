package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit Test class for {@link SpeechBalloonHelper}
 *
 * @author Fabrice Fontaine
 * @since 6.1.1
 */
public class SpeechBalloonHelperTest {

    @Test
    public void testCompute() {
        final SpeechBalloonHelper helper = new SpeechBalloonHelper();
        final long radius = 10;
        final RectL inputRect = new RectL();
        final PointL inputPoint = new PointL();
        final PointL intersection1 = new PointL();
        final PointL intersection2 = new PointL();
        inputRect.set(0, 0, 100, 100);

        inputPoint.set(1, 1);
        Assert.assertEquals(SpeechBalloonHelper.CORNER_INSIDE,
                helper.compute(inputRect, inputPoint, radius, intersection1, intersection2));

        inputPoint.set(50, 200);
        Assert.assertEquals(SpeechBalloonHelper.CORNER_NONE,
                helper.compute(inputRect, inputPoint, radius, intersection1, intersection2));
        Assert.assertEquals(100, intersection1.y);
        Assert.assertEquals(100, intersection2.y);

        inputPoint.set(50, -200);
        Assert.assertEquals(SpeechBalloonHelper.CORNER_NONE,
                helper.compute(inputRect, inputPoint, radius, intersection1, intersection2));
        Assert.assertEquals(0, intersection1.y);
        Assert.assertEquals(0, intersection2.y);

        inputPoint.set(110, 110);
        Assert.assertEquals(SpeechBalloonHelper.CORNER_BOTTOM | SpeechBalloonHelper.CORNER_RIGHT,
                helper.compute(inputRect, inputPoint, radius, intersection1, intersection2));
        Assert.assertEquals(100, intersection1.x);
        Assert.assertEquals(100, intersection2.y);

        inputPoint.set(-10, -10);
        Assert.assertEquals(SpeechBalloonHelper.CORNER_TOP | SpeechBalloonHelper.CORNER_LEFT,
                helper.compute(inputRect, inputPoint, radius, intersection1, intersection2));
        Assert.assertEquals(0, intersection1.x);
        Assert.assertEquals(0, intersection2.y);

        inputPoint.set(-10, 110);
        Assert.assertEquals(SpeechBalloonHelper.CORNER_BOTTOM | SpeechBalloonHelper.CORNER_LEFT,
                helper.compute(inputRect, inputPoint, radius, intersection1, intersection2));
        Assert.assertEquals(0, intersection2.x);
        Assert.assertEquals(100, intersection1.y);

        inputPoint.set(110, -10);
        Assert.assertEquals(SpeechBalloonHelper.CORNER_TOP | SpeechBalloonHelper.CORNER_RIGHT,
                helper.compute(inputRect, inputPoint, radius, intersection1, intersection2));
        Assert.assertEquals(100, intersection2.x);
        Assert.assertEquals(0, intersection1.y);
    }
}

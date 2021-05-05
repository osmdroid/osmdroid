package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class SegmentIntersectionTest {

    @Test
    public void test_intersection() {
        test_intersection( // simple perpendicular segments
                new RectL(0, 500, 1000, 500),
                new RectL(500, 0, 500, 1000),
                new PointL(500, 500));
        test_intersection( // simple intersecting segments
                new RectL(0, 0, 100, 100),
                new RectL(0, 50, 100, 50),
                new PointL(50, 50));
        test_intersection( // parallel segments
                new RectL(0, 0, 100, 0),
                new RectL(0, 50, 100, 50),
                null);
        test_intersection( // overlapping parallel segments
                new RectL(0, 0, 100, 100),
                new RectL(50, 50, 1000, 1000),
                new PointL(75, 75));
        test_intersection( // non intersecting segments
                new RectL(0, 0, 100, 100),
                new RectL(0, 500, 100, 500),
                null);
        test_intersection( // high values
                new RectL(0, 0, 1 << 30, 1 << 30),
                new RectL(0, 1 << 29, 1 << 30, 1 << 29),
                new PointL(1 << 29, 1 << 29));
        test_intersection( // actual numbers
                new RectL(-33554178, 402653480, -33554178, 234881320),
                new RectL(-268435456, 268435455, 268435455, 268435455),
                new PointL(-33554178, 268435455));

    }

    private void test_intersection(final RectL pSegment1, final RectL pSegment2, final PointL pExpectedIntersection) {
        test_intersectionHelper(pSegment1, pSegment2, pExpectedIntersection);
        test_intersectionHelper(pSegment2, pSegment1, pExpectedIntersection);
    }

    private void test_intersectionHelper(final RectL pSegment1, final RectL pSegment2, final PointL pExpectedIntersection) {
        final PointL intersection = new PointL();
        final boolean result = SegmentIntersection.intersection(
                pSegment1.left, pSegment1.top, pSegment1.right, pSegment1.bottom,
                pSegment2.left, pSegment2.top, pSegment2.right, pSegment2.bottom,
                intersection);
        if (pExpectedIntersection == null) {
            Assert.assertFalse(result);
        } else {
            Assert.assertTrue(result);
            Assert.assertEquals(pExpectedIntersection.x, intersection.x);
            Assert.assertEquals(pExpectedIntersection.y, intersection.y);
        }
    }
}

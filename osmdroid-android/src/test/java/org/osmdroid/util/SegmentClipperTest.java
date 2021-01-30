package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class SegmentClipperTest {

    @Test
    public void test_clip_with_path() {
        final List<PointL> points = new ArrayList<>();

        final PointAccepter clippable = new PointAccepter() {

            @Override
            public void init() {
                points.clear();
            }

            @Override
            public void add(long pX, long pY) {
                points.add(new PointL(pX, pY));
            }

            @Override
            public void end() {
            }
        };
        final SegmentClipper segmentClipper = new SegmentClipper();
        segmentClipper.set(-600, -600, 1400, 1400, clippable, true);

        clippable.init();
        segmentClipper.clip(-2146, -2152, -145, -141);
        Assert.assertEquals(3, points.size());
        Assert.assertEquals(new PointL(-600, -600), points.get(0));
        Assert.assertEquals(new PointL(-600, -598), points.get(1));
        Assert.assertEquals(new PointL(-145, -141), points.get(2));

        clippable.init();
        segmentClipper.clip(-145, -141, 855, -1150);
        Assert.assertEquals(3, points.size());
        Assert.assertEquals(new PointL(-145, -141), points.get(0));
        Assert.assertEquals(new PointL(310, -600), points.get(1));
        Assert.assertEquals(new PointL(855, -600), points.get(2));

        clippable.init();
        segmentClipper.clip(1856, 267, -2146, 9434);
        Assert.assertEquals(4, points.size());
        Assert.assertEquals(new PointL(1400, 267), points.get(0));
        Assert.assertEquals(new PointL(1400, 1312), points.get(1));
        Assert.assertEquals(new PointL(1361, 1400), points.get(2));
        Assert.assertEquals(new PointL(-600, 1400), points.get(3));

        // both segment points are inside the clip area
        clippable.init();
        segmentClipper.clip(-30, 500, 700, 800);
        Assert.assertEquals(2, points.size());
        Assert.assertEquals(new PointL(-30, 500), points.get(0));
        Assert.assertEquals(new PointL(700, 800), points.get(1));

        // no intersection between clip area and segment
        // computed corner: top right
        clippable.init();
        segmentClipper.clip(-1000, -10000, 10000, 10000);
        Assert.assertEquals(3, points.size());
        Assert.assertEquals(new PointL(-600, -600), points.get(0));
        Assert.assertEquals(new PointL(1400, -600), points.get(1));
        Assert.assertEquals(new PointL(1400, 1400), points.get(2));

        // no intersection between clip area and segment
        // computed corner: bottom left
        clippable.init();
        segmentClipper.clip(-10000, -1000, 10000, 10000);
        Assert.assertEquals(3, points.size());
        Assert.assertEquals(new PointL(-600, -600), points.get(0));
        Assert.assertEquals(new PointL(-600, 1400), points.get(1));
        Assert.assertEquals(new PointL(1400, 1400), points.get(2));
    }

    @Test
    public void test_clip_without_path() {
        final List<PointL> points = new ArrayList<>();

        final PointAccepter clippable = new PointAccepter() {

            @Override
            public void init() {
                points.clear();
            }

            @Override
            public void add(long pX, long pY) {
                points.add(new PointL(pX, pY));
            }

            @Override
            public void end() {
            }
        };
        final SegmentClipper segmentClipper = new SegmentClipper();
        segmentClipper.set(-600, -600, 1400, 1400, clippable, false);

        clippable.init();
        segmentClipper.clip(-2146, -2152, -145, -141);
        Assert.assertEquals(2, points.size());
        Assert.assertEquals(new PointL(-600, -598), points.get(0));
        Assert.assertEquals(new PointL(-145, -141), points.get(1));

        clippable.init();
        segmentClipper.clip(-145, -141, 855, -1150);
        Assert.assertEquals(2, points.size());
        Assert.assertEquals(new PointL(-145, -141), points.get(0));
        Assert.assertEquals(new PointL(310, -600), points.get(1));

        clippable.init();
        segmentClipper.clip(1856, 267, -2146, 9434);
        Assert.assertEquals(2, points.size());
        Assert.assertEquals(new PointL(1400, 1312), points.get(0));
        Assert.assertEquals(new PointL(1361, 1400), points.get(1));

        // both segment points are inside the clip area
        clippable.init();
        segmentClipper.clip(-30, 500, 700, 800);
        Assert.assertEquals(2, points.size());
        Assert.assertEquals(new PointL(-30, 500), points.get(0));
        Assert.assertEquals(new PointL(700, 800), points.get(1));

        // no intersection between clip area and segment
        // computed corner: top right
        clippable.init();
        segmentClipper.clip(-1000, -10000, 10000, 10000);
        Assert.assertEquals(0, points.size());

        // no intersection between clip area and segment
        // computed corner: bottom left
        clippable.init();
        segmentClipper.clip(-10000, -1000, 10000, 10000);
        Assert.assertEquals(0, points.size());
    }
}

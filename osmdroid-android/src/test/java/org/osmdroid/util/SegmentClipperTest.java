package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class SegmentClipperTest {

	@Test
	public void test_clip() {
		final List<PointL> points = new ArrayList<>();

		final SegmentClipper.SegmentClippable clippable = new SegmentClipper.SegmentClippable() {

			@Override
			public void init() {
				points.clear();
			}

			@Override
			public void lineTo(long pX, long pY) {
				points.add(new PointL(pX, pY));
			}
		};
		final SegmentClipper segmentClipper = new SegmentClipper(-600, -600, 1400, 1400, clippable);

		clippable.init();
		segmentClipper.clip(new RectL(-2146, -2152, -145, -141));
		Assert.assertEquals(3, points.size());
		Assert.assertEquals(new PointL(-600, -600), points.get(0));
		Assert.assertEquals(new PointL(-600, -598), points.get(1));
		Assert.assertEquals(new PointL(-145, -141), points.get(2));

		clippable.init();
		segmentClipper.clip(new RectL(-145, -141, 855, -1150));
		Assert.assertEquals(3, points.size());
		Assert.assertEquals(new PointL(-145, -141), points.get(0));
		Assert.assertEquals(new PointL(310, -600), points.get(1));
		Assert.assertEquals(new PointL(855, -600), points.get(2));

		clippable.init();
		segmentClipper.clip(new RectL(1856, 267, -2146, 9434));
		Assert.assertEquals(4, points.size());
		Assert.assertEquals(new PointL(1400, 267), points.get(0));
		Assert.assertEquals(new PointL(1400, 1312), points.get(1));
		Assert.assertEquals(new PointL(1361, 1400), points.get(2));
		Assert.assertEquals(new PointL(-600, 1400), points.get(3));

		clippable.init();
		segmentClipper.clip(new RectL(-30, 500, 700, 800));
		Assert.assertEquals(2, points.size());
		Assert.assertEquals(new PointL(-30, 500), points.get(0));
		Assert.assertEquals(new PointL(700, 800), points.get(1));
	}
}

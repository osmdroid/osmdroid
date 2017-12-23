package org.osmdroid.views.overlay;

import junit.framework.Assert;

import org.junit.Test;
import org.osmdroid.util.PointAccepter;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.SegmentClipper;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class MilestoneListerTest {

	private static final double mDelta = 1E-10;

	@Test
	public void test_orientation() {
		Assert.assertEquals(0, MilestoneLister.getOrientation(1, 1, 1, 1), mDelta);
		Assert.assertEquals(0, MilestoneLister.getOrientation(1, 1, 10, 1), mDelta);
		Assert.assertEquals(45, MilestoneLister.getOrientation(10, 10, 20, 20), mDelta);
		Assert.assertEquals(90, MilestoneLister.getOrientation(10, 10, 10, 20), mDelta);
		Assert.assertEquals(180, MilestoneLister.getOrientation(10, 10, 0, 10), mDelta);
		Assert.assertEquals(-90, MilestoneLister.getOrientation(10, 10, 10, 0), mDelta);
	}
}

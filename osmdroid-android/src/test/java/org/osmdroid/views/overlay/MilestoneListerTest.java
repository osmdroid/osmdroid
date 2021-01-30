package org.osmdroid.views.overlay;

import junit.framework.Assert;

import org.junit.Test;
import org.osmdroid.views.overlay.milestones.MilestoneLister;

/**
 * @author Fabrice Fontaine
 * @since 6.0.0
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

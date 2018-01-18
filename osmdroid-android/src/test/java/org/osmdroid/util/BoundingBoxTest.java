package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class BoundingBoxTest {

    private static final double mDelta = 1E-10;

    @Test
    public void testGetCenterLongitude() {
        Assert.assertEquals(1.5, BoundingBox.getCenterLongitude(1, 2), mDelta);
        Assert.assertEquals(-178.5, BoundingBox.getCenterLongitude(2, 1), mDelta);
    }
}

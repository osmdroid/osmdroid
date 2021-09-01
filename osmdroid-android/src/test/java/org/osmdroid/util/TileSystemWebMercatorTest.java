package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Fabrice Fontaine
 * Unit test class related to {@link TileSystemWebMercator}
 * @since 6.0.3
 */
public class TileSystemWebMercatorTest {

    private final TileSystemWebMercator tileSystem = new TileSystemWebMercator();

    @Test
    public void test01() {
        test01Lon(tileSystem.getMaxLongitude(), 1);
        test01Lon(tileSystem.getMinLongitude(), 0);
        test01Lat(tileSystem.getMaxLatitude(), 0);
        test01Lat(tileSystem.getMinLatitude(), 1);
    }

    private void test01Lon(final double pLongitude, final double pExpected) {
        final double value = tileSystem.getX01FromLongitude(pLongitude);
        test01Value("longitude:" + pLongitude, value, pExpected);
    }

    private void test01Lat(final double pLatitude, final double pExpected) {
        final double value = tileSystem.getY01FromLatitude(pLatitude);
        test01Value("latitude:" + pLatitude, value, pExpected);
    }

    private void test01Value(final String pText, final double pValue, final double pExpected) {
        final double mDelta = 1E-10;
        Assert.assertTrue(pText, pValue >= 0);
        Assert.assertTrue(pText, pValue <= 1);
        Assert.assertEquals(pText, pExpected, pValue, mDelta);
    }
}

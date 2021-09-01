package org.osmdroid.util;

import android.graphics.Point;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author Fabrice Fontaine
 * @since 6.0.0
 */
public class TileSystemTest {

    private static final Random random = new Random();
    private static final double XY01Delta = 1E-10;
    private static final double latLongDelta = 1E-10;
    private static final int mMinZoomLevel = 0;
    private static final int mMaxZoomLevel = TileSystem.getMaximumZoomLevel();
    private static final TileSystem tileSystem = new TileSystemWebMercator();

    @Test
    public void testGetY01FromLatitude() {
        checkXY01(0, tileSystem.getY01FromLatitude(tileSystem.getMaxLatitude(), true));
        checkXY01(.5, tileSystem.getY01FromLatitude(0, true));
        checkXY01(1, tileSystem.getY01FromLatitude(tileSystem.getMinLatitude(), true));
    }

    @Test
    public void testGetX01FromLongitude() {
        final int iterations = 10;
        for (int i = 0; i <= iterations; i++) {
            final double longitude = tileSystem.getMinLongitude() + i * (tileSystem.getMaxLongitude() - tileSystem.getMinLongitude()) / iterations;
            checkXY01(((double) i) / iterations, tileSystem.getX01FromLongitude(longitude, true));
        }
    }

    /**
     * @since 6.0.0
     */
    @Test
    public void testGetLatitudeFromY01() {
        checkLatitude(tileSystem.getMaxLatitude(), tileSystem.getLatitudeFromY01(0, true));
        checkLatitude(0, tileSystem.getLatitudeFromY01(0.5, true));
        checkLatitude(tileSystem.getMinLatitude(), tileSystem.getLatitudeFromY01(1, true));
    }

    /**
     * @since 6.0.0
     */
    @Test
    public void testLatitude() {
        final int iterations = 100;
        for (int i = 0; i <= iterations; i++) {
            final double latitude = getRandomLatitude();
            checkLatitude(latitude, tileSystem.getLatitudeFromY01(tileSystem.getY01FromLatitude(latitude, true), true));
        }
    }

    /**
     * @since 6.0.0
     */
    @Test
    public void testGetLongitudeFromX01() {
        final int iterations = 10;
        for (int i = 0; i <= iterations; i++) {
            final double longitude = tileSystem.getMinLongitude() + i * (tileSystem.getMaxLongitude() - tileSystem.getMinLongitude()) / iterations;
            checkLongitude(longitude, tileSystem.getLongitudeFromX01(((double) i) / iterations, true));
        }
        checkLongitude(tileSystem.getMinLongitude(), tileSystem.getLongitudeFromX01(0, true));
        checkLongitude(0, tileSystem.getLongitudeFromX01(0.5, true));
        checkLongitude(tileSystem.getMaxLongitude(), tileSystem.getLongitudeFromX01(1, true));
    }

    /**
     * @since 6.0.0
     */
    @Test
    public void testLongitude() {
        final int iterations = 100;
        for (int i = 0; i <= iterations; i++) {
            final double longitude = getRandomLongitude();
            checkLongitude(longitude, tileSystem.getLongitudeFromX01(tileSystem.getX01FromLongitude(longitude, true), true));
        }
    }

    private void checkXY01(final double pExpected, final double pActual) {
        Assert.assertEquals(pExpected, pActual, XY01Delta);
        checkMinMax(pActual, 0, 1);
    }

    /**
     * @since 6.0.0
     */
    protected void checkLatitude(final double pExpected, final double pActual) {
        Assert.assertEquals(pExpected, pActual, latLongDelta);
        checkMinMax(pActual, tileSystem.getMinLatitude(), tileSystem.getMaxLatitude());
    }

    /**
     * @since 6.0.0
     */
    protected void checkLongitude(final double pExpected, final double pActual) {
        Assert.assertEquals(pExpected, pActual, latLongDelta);
        checkMinMax(pActual, tileSystem.getMinLongitude(), tileSystem.getMaxLongitude());
    }

    /**
     * @since 6.0.0
     */
    private void checkMinMax(final double pActual, final double pMin, final double pMax) {
        Assert.assertTrue(pActual <= pMax);
        Assert.assertTrue(pActual >= pMin);
    }

    @Test
    public void testGetBoundingBoxZoom() {
        final int tileSize = 256;
        final int screenWidth = tileSize * 2;
        final int screenHeight = screenWidth * 2;
        TileSystem.setTileSize(tileSize);

        final int iterations = 2000;
        for (int i = 0; i < iterations; i++) {
            final double north = getRandomLatitude();
            final double south = getRandomLatitude();
            final double east = getRandomLongitude();
            final double west = getRandomLongitude();
            final BoundingBox boundingBox = new BoundingBox(north, east, south, west);
            final double zoom = tileSystem.getBoundingBoxZoom(boundingBox, screenWidth, screenHeight);
            if (zoom == Double.MIN_VALUE) {
                Assert.assertTrue(north <= south || east == west);
                continue;
            }
            final double mapSize = TileSystem.MapSize(zoom);
            final long left = tileSystem.getMercatorXFromLongitude(west, mapSize, true);
            final long top = tileSystem.getMercatorYFromLatitude(north, mapSize, true);
            final long right = tileSystem.getMercatorXFromLongitude(east, mapSize, true);
            final long bottom = tileSystem.getMercatorYFromLatitude(south, mapSize, true);
            long width = right - left;
            if (east < west) {
                width += mapSize;
            }
            final long height = bottom - top;
            checkSize(width, height, screenWidth, screenHeight);
        }
    }

    /**
     * @since 6.0.0
     * Was previously in TileSystemMathTest
     * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
     */
    @Test
    public void test_MapSize() {
        for (int zoomLevel = mMinZoomLevel; zoomLevel <= mMaxZoomLevel; zoomLevel++) {
            Assert.assertEquals(256L << zoomLevel, (long) TileSystem.MapSize((double) zoomLevel));
        }
    }

    /**
     * @since 6.0.0
     * Was previously in TileSystemMathTest
     * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
     */
    @Test
    public void test_groundResolution() {
        final double delta = 1e-4;

        for (int zoomLevel = mMinZoomLevel; zoomLevel <= mMaxZoomLevel; zoomLevel++) {
            Assert.assertEquals(156543.034 / (1 << zoomLevel), TileSystem.GroundResolution(0, zoomLevel), delta);
        }
    }

    /**
     * @since 6.0.0
     * Was previously in TileSystemMathTest
     * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
     */
    @Test
    public void test_groundMapScale() {
        final double delta = 1e-2;

        for (int zoomLevel = mMinZoomLevel; zoomLevel <= mMaxZoomLevel; zoomLevel++) {
            Assert.assertEquals(591658710.9 / (1 << zoomLevel), TileSystem.MapScale(0, zoomLevel, 96), delta);
        }
    }

    /**
     * @since 6.0.2
     * Was previously in TileSystemMathTest
     */
    @Test
    public void test_LatLongToPixelXY() {
        final PointL point = tileSystem.getMercatorFromGeo(60, 60, TileSystem.MapSize((double) 10), null, true);
        Assert.assertEquals(174762, point.x);
        Assert.assertEquals(76126, point.y);
    }

    /**
     * @since 6.0.2
     * Was previously in TileSystemMathTest
     */
    @Test
    public void test_PixelXYToLatLong() {
        final int pixelX = 45;
        final int pixelY = 45;
        final int levelOfDetail = 8;
        final double delta = 1E-3;

        final GeoPoint point = tileSystem.getGeoFromMercator(pixelX, pixelY, TileSystem.MapSize((double) levelOfDetail), null, true, true);

        Assert.assertEquals(-179.752807617187, point.getLongitude(), delta);
        Assert.assertEquals(85.0297584051224, point.getLatitude(), delta);
    }

    /**
     * @since 6.0.2
     * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
     */
    @Test
    public void test_TileXYToQuadKey() {
        Assert.assertEquals("2", TileSystem.TileXYToQuadKey(0, 1, 1));
        Assert.assertEquals("13", TileSystem.TileXYToQuadKey(3, 1, 2));
        Assert.assertEquals("213", TileSystem.TileXYToQuadKey(3, 5, 3));
        String zero = "";
        String one = "";
        String two = "";
        String three = "";
        for (int zoom = 1; zoom <= TileSystem.getMaximumZoomLevel(); zoom++) {
            zero += "0";
            one += "1";
            two += "2";
            three += "3";
            final int maxTile = (1 << zoom) - 1;
            Assert.assertEquals(zero, TileSystem.TileXYToQuadKey(0, 0, zoom));
            Assert.assertEquals(one, TileSystem.TileXYToQuadKey(maxTile, 0, zoom));
            Assert.assertEquals(two, TileSystem.TileXYToQuadKey(0, maxTile, zoom));
            Assert.assertEquals(three, TileSystem.TileXYToQuadKey(maxTile, maxTile, zoom));
        }
    }

    /**
     * @since 6.0.2
     * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
     */
    @Test
    public void test_QuadKeyToTileXY() {
        testPoint(0, 1, TileSystem.QuadKeyToTileXY("2", null));
        testPoint(3, 1, TileSystem.QuadKeyToTileXY("13", null));
        testPoint(3, 5, TileSystem.QuadKeyToTileXY("213", null));

        String zero = "";
        String one = "";
        String two = "";
        String three = "";
        for (int zoom = 1; zoom <= TileSystem.getMaximumZoomLevel(); zoom++) {
            zero += "0";
            one += "1";
            two += "2";
            three += "3";
            final int maxTile = (1 << zoom) - 1;
            testPoint(0, 0, TileSystem.QuadKeyToTileXY(zero, null));
            testPoint(maxTile, 0, TileSystem.QuadKeyToTileXY(one, null));
            testPoint(0, maxTile, TileSystem.QuadKeyToTileXY(two, null));
            testPoint(maxTile, maxTile, TileSystem.QuadKeyToTileXY(three, null));
        }
    }

    /**
     * @since 6.0.2
     */
    private void testPoint(final int pExpectedX, final int pExpectedY, final Point pActualPoint) {
        Assert.assertEquals(pExpectedX, pActualPoint.x);
        Assert.assertEquals(pExpectedY, pActualPoint.y);
    }

    private double getRandomLongitude() {
        return tileSystem.getRandomLongitude(random.nextDouble());
    }

    private double getRandomLatitude() {
        return tileSystem.getRandomLatitude(random.nextDouble(), tileSystem.getMinLatitude());
    }

    private void checkSize(final long pWidth, final long pHeight, final int pScreenWidth, final int pScreenHeight) {
        final int intRoundingEqualsDelta = 2;
        final long deltaWidth = Math.abs(pWidth - pScreenWidth);
        final long deltaHeight = Math.abs(pHeight - pScreenHeight);
        if (deltaWidth <= deltaHeight) {
            Assert.assertEquals(pScreenWidth, pWidth, intRoundingEqualsDelta);
        } else {
            Assert.assertEquals(pScreenHeight, pHeight, intRoundingEqualsDelta);
        }
    }
}

package org.osmdroid.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @since 5.6.6
 * @author Fabrice Fontaine
 */
public class TileSystemTest {

    private static final Random random = new Random();
    private static final double XY01Delta = 1E-10;
    private static final double latLongDelta = 1E-10;

    @Test
    public void testGetY01FromLatitude() {
        checkXY01(0, TileSystem.getY01FromLatitude(TileSystem.MaxLatitude));
        checkXY01(.5, TileSystem.getY01FromLatitude(0));
        checkXY01(1, TileSystem.getY01FromLatitude(TileSystem.MinLatitude));
    }

    @Test
    public void testGetX01FromLongitude() {
        final int iterations = 10;
        for (int i = 0 ; i <= iterations ; i ++) {
            final double longitude = TileSystem.MinLongitude + i * (TileSystem.MaxLongitude - TileSystem.MinLongitude) / iterations;
            checkXY01(((double)i) / iterations, TileSystem.getX01FromLongitude(longitude));
        }
    }

    /**
     * @since 5.6.6
     */
    @Test
    public void testGetLatitudeFromY01() {
        checkLatitude(TileSystem.MaxLatitude, TileSystem.getLatitudeFromY01(0));
        checkLatitude(0, TileSystem.getLatitudeFromY01(0.5));
        checkLatitude(TileSystem.MinLatitude, TileSystem.getLatitudeFromY01(1));
    }

    /**
     * @since 5.6.6
     */
    @Test
    public void testLatitude() {
        final int iterations = 100;
        for (int i = 0 ; i <= iterations ; i ++) {
            final double latitude = getRandomLatitude();
            checkLatitude(latitude, TileSystem.getLatitudeFromY01(TileSystem.getY01FromLatitude(latitude)));
        }
    }

    /**
     * @since 5.6.6
     */
    @Test
    public void testGetLongitudeFromX01() {
        final int iterations = 10;
        for (int i = 0 ; i <= iterations ; i ++) {
            final double longitude = TileSystem.MinLongitude + i * (TileSystem.MaxLongitude - TileSystem.MinLongitude) / iterations;
            checkLongitude(longitude, TileSystem.getLongitudeFromX01(((double)i) / iterations));
        }
        checkLongitude(TileSystem.MinLongitude, TileSystem.getLongitudeFromX01(0));
        checkLongitude(0, TileSystem.getLongitudeFromX01(0.5));
        checkLongitude(TileSystem.MaxLongitude, TileSystem.getLongitudeFromX01(1));
    }

    /**
     * @since 5.6.6
     */
    @Test
    public void testLongitude() {
        final int iterations = 100;
        for (int i = 0 ; i <= iterations ; i ++) {
            final double longitude = getRandomLongitude();
            checkLongitude(longitude, TileSystem.getLongitudeFromX01(TileSystem.getX01FromLongitude(longitude)));
        }
    }

    private void checkXY01(final double pExpected, final double pActual) {
        Assert.assertEquals(pExpected, pActual, XY01Delta);
        checkMinMax(pActual, 0, 1);
    }

    /**
     * @since 5.6.6
     */
    private void checkLatitude(final double pExpected, final double pActual) {
        Assert.assertEquals(pExpected, pActual, latLongDelta);
        checkMinMax(pActual, TileSystem.MinLatitude, TileSystem.MaxLatitude);
    }

    /**
     * @since 5.6.6
     */
    private void checkLongitude(final double pExpected, final double pActual) {
        Assert.assertEquals(pExpected, pActual, latLongDelta);
        checkMinMax(pActual, TileSystem.MinLongitude, TileSystem.MaxLongitude);
    }

    /**
     * @since 5.6.6
     */
    private void checkMinMax(final double pActual, final double pMin, final double pMax) {
        Assert.assertTrue(pActual <= pMax);
        Assert.assertTrue(pActual >= pMin);
    }

    @Test
    public void testGetBoundingBoxZoom() throws Exception{
        final int tileSize = 256;
        final int screenWidth = tileSize * 2;
        final int screenHeight = screenWidth * 2;
        TileSystem.setTileSize(tileSize);

        final int iterations = 2000;
        for (int i = 0 ; i < iterations ; i ++) {
            final double north = getRandomLatitude();
            final double south = getRandomLatitude();
            final double east = getRandomLongitude();
            final double west = getRandomLongitude();
            final BoundingBox boundingBox = new BoundingBox(north, east, south, west);
            final double zoom = TileSystem.getBoundingBoxZoom(boundingBox, screenWidth, screenHeight);
            if (zoom == Double.MIN_VALUE) {
                Assert.assertTrue(north <= south || east == west);
                continue;
            }
            final double mapSize = TileSystem.MapSize(zoom);
            final long left = TileSystem.getMercatorXFromLongitude(west, mapSize);
            final long top = TileSystem.getMercatorYFromLatitude(north, mapSize);
            final long right = TileSystem.getMercatorXFromLongitude(east, mapSize);
            final long bottom = TileSystem.getMercatorYFromLatitude(south, mapSize);
            long width = right - left;
            if (east < west) {
                width += mapSize;
            }
            final long height = bottom - top;
            checkSize(width, height, screenWidth, screenHeight);
        }
    }

    private double getRandomLongitude() {
        return TileSystem.getRandomLongitude(random.nextDouble());
    }

    private double getRandomLatitude() {
        return TileSystem.getRandomLatitude(random.nextDouble(), TileSystem.MinLatitude);
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

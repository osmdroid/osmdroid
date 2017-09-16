package org.osmdroid.util;

import android.graphics.Point;

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

    private void checkXY01(final double pExpected, final double pActual) {
        Assert.assertEquals(pExpected, pActual, XY01Delta);
        Assert.assertTrue(pActual >= 0 && pActual <= 1);
    }

    @Test
    public void testLatLongToPixelXYMapSize() {
        final Point point = new Point();
        final int mapSize = Integer.MAX_VALUE;
        TileSystem.LatLongToPixelXYMapSize(TileSystem.MaxLatitude, TileSystem.MinLongitude, mapSize, point);
        Assert.assertEquals(0, point.x);
        Assert.assertEquals(0, point.y);
        TileSystem.LatLongToPixelXYMapSize(0, 0, mapSize, point);
        Assert.assertEquals((mapSize - 1) / 2, point.x);
        Assert.assertEquals((mapSize - 1) / 2, point.y);
        TileSystem.LatLongToPixelXYMapSize(TileSystem.MinLatitude, TileSystem.MaxLongitude, mapSize, point);
        Assert.assertEquals(mapSize - 1, point.x);
        Assert.assertEquals(mapSize - 1, point.y);
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
            final Point topLeft = TileSystem.LatLongToPixelXY(north, west, zoom, null);
            final Point bottomRight = TileSystem.LatLongToPixelXY(south, east, zoom, null);
            int width = bottomRight.x - topLeft.x;
            if (east < west) {
                width += TileSystem.MapSize(zoom);
            }
            final int height = bottomRight.y - topLeft.y;
            checkSize(width, height, screenWidth, screenHeight);
        }
    }

    private double getRandomLongitude() {
        return random.nextDouble() * (TileSystem.MaxLongitude - TileSystem.MinLongitude) + TileSystem.MinLongitude;
    }

    private double getRandomLatitude() {
        return random.nextDouble() * (TileSystem.MaxLatitude - TileSystem.MinLatitude) + TileSystem.MinLatitude;
    }

    private void checkSize(final int pWidth, final int pHeight, final int pScreenWidth, final int pScreenHeight) {
        final int intRoundingEqualsDelta = 2;
        final int deltaWidth = Math.abs(pWidth - pScreenWidth);
        final int deltaHeight = Math.abs(pHeight - pScreenHeight);
        if (deltaWidth <= deltaHeight) {
            Assert.assertEquals(pScreenWidth, pWidth, intRoundingEqualsDelta);
        } else {
            Assert.assertEquals(pScreenHeight, pHeight, intRoundingEqualsDelta);
        }
    }
}

package org.osmdroid.util;

import android.graphics.Point;
import android.graphics.Rect;

import junit.framework.Assert;

import org.junit.Test;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.Projection;

import java.util.Random;

/**
 * @author Fabrice Fontaine
 * <p>
 * VERY IMPORTANT NOTICE
 * In class Projection, don't use syntaxes like Point.set, Point.offset or Point.center.
 * Use "Point.x=" and "Point.y=" syntaxes instead. Same for Rect.
 * Why?
 * Because class Point - though relatively low level - is part of an Android package
 * that does not belong to standard Java.
 * As a result, using it in Unit Test is a bit heavier.
 * I prefer the light version of unit test.
 * For more info, search "android unit test mock"
 * @since 6.0.0
 */

public class ProjectionTest {

    private static final Random mRandom = new Random();
    private static final int mMinZoomLevel = 0;
    private static final int mMaxZoomLevel = TileSystem.getMaximumZoomLevel();
    private static final int mMinimapZoomLevelDifference = 5;
    private static final int mNbIterations = 1000;
    private static final Rect mScreenRect = new Rect();
    private static final Rect mMiniMapScreenRect = new Rect();
    private static final int mWidth = 600;
    private static final int mHeight = 800;

    static {
        mScreenRect.left = 0;
        mScreenRect.top = 0;
        mScreenRect.right = mWidth;
        mScreenRect.bottom = mHeight;
        mMiniMapScreenRect.left = mWidth / 2;
        mMiniMapScreenRect.top = mHeight / 2;
        mMiniMapScreenRect.right = mMiniMapScreenRect.left + mWidth / 4;
        mMiniMapScreenRect.bottom = mMiniMapScreenRect.top + mHeight / 4;
    }

    private static final TileSystem tileSystem = new TileSystemWebMercator();

    /**
     * "If both Projection's scrolls are 0, the geo center is projected to the screen rect center"
     */
    @Test
    public void testCenteredGeoPoint() {
        for (int zoomLevel = mMinZoomLevel; zoomLevel <= mMaxZoomLevel; zoomLevel++) {
            final double mapSize = TileSystem.MapSize((double) zoomLevel);
            for (int i = 0; i < mNbIterations; i++) {
                final GeoPoint geoPoint = getRandomGeoPoint();
                final Projection projection = getRandomProjection(zoomLevel, geoPoint, 0, 0);
                final Point pixel = projection.toPixels(geoPoint, null);

                int expectedX = mWidth / 2;
                if (mapSize < mWidth) { // side effect for low level, as the computed pixel will be the first from the left
                    while (expectedX - mapSize >= 0) {
                        expectedX -= mapSize;
                    }
                }
                Assert.assertEquals(expectedX, pixel.x);

                int expectedY = mHeight / 2;
                if (mapSize < mHeight) { // side effect for low level, as the computed pixel will be the first from the top
                    while (expectedY - mapSize >= 0) {
                        expectedY -= mapSize;
                    }
                }
                Assert.assertEquals(expectedY, pixel.y);
            }
        }
    }

    /**
     * "The geo center of an offspring matches the geo center of the parent"
     */
    @Test
    public void testOffspringSameCenter() {
        final GeoPoint center = new GeoPoint(0., 0);
        final Point pixel = new Point();
        final int centerX = (mScreenRect.right + mScreenRect.left) / 2;
        final int centerY = (mScreenRect.bottom + mScreenRect.top) / 2;
        final int miniCenterX = (mMiniMapScreenRect.right + mMiniMapScreenRect.left) / 2;
        final int miniCenterY = (mMiniMapScreenRect.bottom + mMiniMapScreenRect.top) / 2;
        for (int zoomLevel = mMinZoomLevel + mMinimapZoomLevelDifference; zoomLevel <= mMaxZoomLevel; zoomLevel++) {
            for (int i = 0; i < mNbIterations; i++) {
                final Projection projection = getRandomProjection(zoomLevel);
                final Projection miniMapProjection = projection.getOffspring(zoomLevel - mMinimapZoomLevelDifference, mMiniMapScreenRect);

                projection.fromPixels(centerX, centerY, center);
                miniMapProjection.toPixels(center, pixel);
                Assert.assertEquals(miniCenterX, pixel.x);
                Assert.assertEquals(miniCenterY, pixel.y);
            }
        }
    }

    /**
     * "When computing geo point B from pixel A, and then pixel C from geo point B, A and C match"
     */
    @Test
    public void testPixelToGeoToPixel() {
        final int deltaPixel = 2;
        for (int zoomLevel = mMinZoomLevel; zoomLevel <= mMaxZoomLevel; zoomLevel++) {
            final double mapSize = TileSystem.MapSize((double) zoomLevel);
            for (int i = 0; i < mNbIterations; i++) {
                final Point pixelIn = getRandomPixel(mapSize);
                final Projection projection = getRandomProjection(zoomLevel);
                final IGeoPoint geoPoint = projection.fromPixels(pixelIn.x, pixelIn.y);
                final Point pixelOut = projection.toPixels(geoPoint, null);
                if (mapSize < mWidth) { // side effect for low level
                    final int diff = Math.abs(pixelIn.x - pixelOut.x);
                    Assert.assertTrue(diff <= deltaPixel || Math.abs(diff - mapSize) <= deltaPixel);
                } else {
                    Assert.assertEquals(pixelIn.x, pixelOut.x, deltaPixel);
                }
                if (mapSize < mHeight) { // side effect for low level
                    final int diff = Math.abs(pixelIn.y - pixelOut.y);
                    Assert.assertTrue(diff <= deltaPixel || Math.abs(diff - mapSize) <= deltaPixel);
                } else {
                    Assert.assertEquals(pixelIn.y, pixelOut.y, deltaPixel);
                }
            }
        }
    }

    /**
     * "Tiles cover the whole screen"
     */
    @Test
    public void testTilesOverlay() {
        final RectL mercatorViewPort = new RectL();
        final Rect tiles = new Rect();
        final Rect displayedTile = new Rect();
        for (int iteration = 0; iteration < mNbIterations; iteration++) {
            final double zoomLevel = getRandomZoom();
            final double tileSize = TileSystem.getTileSize(zoomLevel);
            final Projection projection = getRandomProjection(zoomLevel);

            projection.getMercatorViewPort(mercatorViewPort);
            Assert.assertEquals(mWidth, mercatorViewPort.width());
            Assert.assertEquals(mHeight, mercatorViewPort.height());

            TileSystem.getTileFromMercator(mercatorViewPort, tileSize, tiles);
            Assert.assertTrue((tiles.right - tiles.left + 1) * tileSize >= mWidth);
            Assert.assertTrue((tiles.bottom - tiles.top + 1) * tileSize >= mHeight);

            int previousX = 0;
            int previousY = 0;
            for (int i = tiles.left; i <= tiles.right; i++) {
                for (int j = tiles.top; j <= tiles.bottom; j++) {
                    projection.getPixelFromTile(i, j, displayedTile);
                    if (j == tiles.bottom) {
                        Assert.assertTrue(displayedTile.bottom >= mHeight);
                    }
                    if (j == tiles.top) {
                        Assert.assertTrue(displayedTile.top <= 0);
                    } else {
                        Assert.assertTrue(displayedTile.top <= previousY + 1);
                    }
                    previousY = displayedTile.bottom;
                }
                if (i == tiles.right) {
                    Assert.assertTrue(displayedTile.right >= mWidth);
                }
                if (i == tiles.left) {
                    Assert.assertTrue(displayedTile.left <= 0);
                } else {
                    Assert.assertTrue(displayedTile.left <= previousX + 1);
                }
                previousX = displayedTile.right;
            }
        }
    }

    /**
     * @since 6.0.0
     */
    @Test
    public void testCheckScrollableOffset() {
        // more than enough
        testCheckScrollableOffset(0, -100, 5000);
        // limits
        testCheckScrollableOffset(0, 50, 950);
        // shorter than possible, in the middle
        testCheckScrollableOffset(-1, 52, 950);
        // shorter than possible, on the left side
        testCheckScrollableOffset(550, -100, 0);
        // shorter than possible, on the right side
        testCheckScrollableOffset(-650, 1100, 1200);
        // to the left
        testCheckScrollableOffset(-50, 100, 1950);
        // to the right
        testCheckScrollableOffset(450, -1000, 500);
    }

    /**
     * @since 6.0.0
     */
    private void testCheckScrollableOffset(final int pExpected, final int pMin, final int pMax) {
        final double worldSize = 20000;
        final int screenSize = 1000;
        final int extraSize = 50;
        Assert.assertEquals(pExpected,
                Projection.getScrollableOffset(pMin, pMax, worldSize, screenSize, extraSize));
    }

    private Point getRandomPixel(final double pMapSize) {
        final Point pixel = new Point();
        pixel.x = mRandom.nextInt((int) Math.min(mWidth, (long) pMapSize));
        pixel.y = mRandom.nextInt((int) Math.min(mHeight, (long) pMapSize));
        return pixel;
    }

    private long getRandomOffset(final double pMapSize) {
        long result = (long) (mRandom.nextDouble() * pMapSize);
        result -= result / 2;
        return result;
    }

    private GeoPoint getRandomGeoPoint() {
        return new GeoPoint(getRandomLatitude(), getRandomLongitude());
    }

    private double getRandomLongitude() {
        return tileSystem.getRandomLongitude(mRandom.nextDouble());
    }

    private double getRandomLatitude() {
        return tileSystem.getRandomLatitude(mRandom.nextDouble(), tileSystem.getMinLatitude());
    }

    private double getRandom(final double pMin, final double pMax) {
        return pMin + mRandom.nextDouble() * (pMax - pMin);
    }

    private double getRandomZoom() {
        return getRandom(mMinZoomLevel, mMaxZoomLevel);
    }

    private float getRandomOrientation() {
        return (float) getRandom(-180, 180);
    }

    private Projection getRandomProjection(
            final double pZoomLevel, final GeoPoint pGeoPoint,
            final long pOffsetX, final long pOffsetY) {
        return new Projection(pZoomLevel, mScreenRect,
                pGeoPoint,
                pOffsetX, pOffsetY,
                getRandomOrientation(),
                true, true,
                tileSystem
                , 0, 0);
    }

    private Projection getRandomProjection(final double pZoomLevel) {
        final double mapSize = TileSystem.MapSize(pZoomLevel);
        return getRandomProjection(
                pZoomLevel, getRandomGeoPoint(),
                getRandomOffset(mapSize), getRandomOffset(mapSize));
    }

    /**
     * @since 6.0.2
     * cf. https://github.com/osmdroid/osmdroid/issues/929
     */
    @Test
    public void test_conversionFromPixelsToPixels() {
        for (int zoomLevel = mMinZoomLevel; zoomLevel <= mMaxZoomLevel; zoomLevel++) {
            final Projection projection = new Projection(
                    zoomLevel,
                    new Rect(0, 0, 1080, 1536),
                    new GeoPoint(0.0, 0.0),
                    0L, 0L,
                    0,
                    false, false,
                    tileSystem,
                    0, 0
            );

            final Point inputPoint = new Point(0, 0);
            final GeoPoint geoPoint = (GeoPoint) projection.fromPixels(inputPoint.x, inputPoint.y);
            final Point outputPoint = projection.toPixels(geoPoint, null);

            Assert.assertEquals(inputPoint.x, outputPoint.x);
            Assert.assertEquals(inputPoint.y, outputPoint.y);
        }
    }
}

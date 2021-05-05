package org.osmdroid.views.overlay;

import android.graphics.Path;

import junit.framework.Assert;

import org.junit.Test;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.TileSystemWebMercator;

import java.util.Random;

/**
 * Unit tests related to {@link LinearRing}
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 */

public class LinearRingTest {

    private final Random mRandom = new Random();
    private static final TileSystem tileSystem = new TileSystemWebMercator();

    @Test
    public void testGetCenter1DLatitudesOnly() {
        testGetCenter(true, false);
    }

    @Test
    public void testGetCenter1DLongitudesOnly() {
        testGetCenter(false, true);
    }

    @Test
    public void testGetCenter2D() {
        testGetCenter(true, true);
    }

    /**
     * Test {@link LinearRing#getCenter(GeoPoint)} looping on latitudes and longitudes
     * <p>
     * As we need to give an expected center latitude and as latitudes don't project linearly,
     * we loop through opposite latitudes, so that the expected center latitude is 0.
     * <p>
     * We also loop in "snake" mode. Why? Doing so we avoid the "Carriage return" effect, and
     * the expected center longitude is therefore the half sum of min and max longitudes.
     * Let's say for instance that latitudes span from -10 to 10, and longitudes from -170 to 175.
     * After [latitude=-10;longitude=175], without the "snake" mode the next loop point is [-9;-170].
     * LinearRing is designed to assume that the points are as close as possible, and in that case
     * will transform [-9;-170] into [-9;190] (as it's closer to previous point [-10;175])
     * And that would have an impact on the computation of the expected center longitude.
     */
    private void testGetCenter(final boolean pSeveralLatitudes, final boolean pSeveralLongitudes) {
        final int iterations = 1000;
        final double delta = 1E-10;
        final Path path = new Path();
        final GeoPoint center = new GeoPoint(0., 0);
        final LinearRing linearRing = new LinearRing(path);
        boolean increasing = true; // "snake" mode where each point is close to the previous in 2D
        for (int i = 0; i < iterations; i++) {
            linearRing.clearPath();
            final int latitudeStop = getRandomPositiveLatitude();
            final int latitudeStart = pSeveralLatitudes ? -latitudeStop : latitudeStop; // latitudes are not projected linearly
            final int longitude1 = getRandomLongitude();
            final int longitude2 = pSeveralLongitudes ? getRandomLongitude() : longitude1;
            final int longitudeStart = Math.min(longitude1, longitude2);
            final int longitudeStop = Math.max(longitude1, longitude2);
            for (int latitude = latitudeStart; latitude <= latitudeStop; latitude++) {
                increasing = !increasing;
                for (int j = 0; j <= longitudeStop - longitudeStart; j++) {
                    final int longitude = increasing ? longitudeStart + j : longitudeStop - j;
                    linearRing.addPoint(new GeoPoint((double) latitude, longitude));
                }
            }
            linearRing.getCenter(center);
            Assert.assertEquals((latitudeStart + latitudeStop) / 2., center.getLatitude(), delta);
            Assert.assertEquals((longitudeStart + longitudeStop) / 2., center.getLongitude(), delta);
        }
    }

    private int getRandomPositiveLatitude() {
        return getRandom(0, (int) tileSystem.getMaxLatitude());
    }

    private int getRandomLongitude() {
        return getRandom((int) tileSystem.getMinLongitude(), (int) tileSystem.getMaxLongitude());
    }

    private int getRandom(final int min, final int max) {
        return min + mRandom.nextInt(max - min);
    }
}

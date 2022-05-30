/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.views;

import android.graphics.Point;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.R;
import org.osmdroid.StarterMapActivity;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.TileSystemWebMercator;

import java.util.Random;

import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Neil Boyd
 */
public class OpenStreetMapViewTest {

    @Rule
    public ActivityTestRule<StarterMapActivity> activityRule =
            new ActivityTestRule<>(StarterMapActivity.class);

    private static final Random random = new Random();

    private static final TileSystem tileSystem = new TileSystemWebMercator();

    public OpenStreetMapViewTest() {
        Counters.reset();
    }

    private MapView mOpenStreetMapView;

    @Before
    public void setUp() throws Exception {

        mOpenStreetMapView =
                activityRule.getActivity().findViewById(R.id.map_container).findViewWithTag("mapView");

    }

    /**
     * This test will check whether calling setExpectedCenter() will position the maps so the location is
     * at the center of the screen.
     */
    @Test
    public void test_toMapPixels_0_0() {
        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final int iterations = 100;
                final double minZoom = // minimum zoom in order to avoid the world map side effect
                        Math.log(
                                Math.max(mOpenStreetMapView.getWidth(), mOpenStreetMapView.getHeight())
                                        / (double) TileSystem.getTileSize())
                                / Math.log(2);
                for (int i = 0; i < iterations; i++) {
                    checkCenter(getRandomZoom(minZoom), getRandomGeoPoint());
                    checkCenter(getRandomZoom(minZoom), null);
                    checkCenter(null, getRandomGeoPoint());
                    checkCenter(null, null);
                }

            }
        });
    }

    /**
     * @since 6.0.0
     */
    private double getRandomLongitude() {
        return tileSystem.getRandomLongitude(random.nextDouble());
    }

    /**
     * @since 6.0.0
     */
    private double getRandomLatitude() {
        return tileSystem.getRandomLatitude(random.nextDouble(), tileSystem.getMinLatitude());
    }

    /**
     * @since 6.0.0
     */
    private double getRandomZoom(final double pMin) {
        return getRandom(pMin, TileSystem.getMaximumZoomLevel());
    }

    /**
     * @since 6.0.0
     */
    private double getRandom(final double pMin, final double pMax) {
        return pMin + random.nextDouble() * (pMax - pMin);
    }

    /**
     * @since 6.0.0
     */
    private void checkCenter(final Double expectedZoom, final GeoPoint expectedCenter) {
        if (expectedZoom != null) {
            mOpenStreetMapView.setZoomLevel(expectedZoom);
        }
        if (expectedCenter != null) {
            mOpenStreetMapView.setExpectedCenter(expectedCenter);
        }
        final Projection projection = mOpenStreetMapView.getProjection();
        if (expectedZoom != null) {
            assertEquals("the zoom level is kept", 0 + expectedZoom, projection.getZoomLevel(), 0);
        }
        checkCenter(projection, (GeoPoint) mOpenStreetMapView.getMapCenter(), "computed");
        if (expectedCenter != null) {
            checkCenter(projection, expectedCenter, "assigned");
        }
    }

    private void checkCenter(final Projection pProjection, final GeoPoint pCenter, final String tag) {
        final double roundingTolerance = 2; // as double in order to have assertEquals work with doubles, not with floats
        final int width_2 = mOpenStreetMapView.getWidth() / 2;
        final int height_2 = mOpenStreetMapView.getHeight() / 2;

        final Point point = pProjection.toPixels(pCenter, null);
        assertTrue("MapView does not have layout. Make sure device is unlocked.", width_2 > 0 && height_2 > 0);
        final Point expected = new Point(width_2, height_2);
        assertEquals("the " + tag + " center of the map is in the pixel center of the map (X)"
                        + "(zoom=" + pProjection.getZoomLevel() + ",center=" + pCenter + ")",
                expected.x, point.x, roundingTolerance);
        assertEquals("the " + tag + " center of the map is in the pixel center of the map (Y)"
                        + "(zoom=" + pProjection.getZoomLevel() + ",center=" + pCenter + ")",
                expected.y, point.y, roundingTolerance);
    }

    /**
     * @since 6.0.0
     */
    private GeoPoint getRandomGeoPoint() {
        return new GeoPoint(getRandomLatitude(), getRandomLongitude());
    }
}

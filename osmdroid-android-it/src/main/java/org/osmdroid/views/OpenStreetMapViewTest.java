/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.views;

import android.graphics.Point;
import android.support.v4.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import org.osmdroid.R;
import org.osmdroid.StarterMapActivity;
import org.osmdroid.StarterMapFragment;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;

import java.util.Random;

/**
 * @author Neil Boyd
 * 
 */
public class OpenStreetMapViewTest extends ActivityInstrumentationTestCase2<StarterMapActivity> {

	private static final Random random = new Random();

	public OpenStreetMapViewTest() {
        super(StarterMapActivity.class);
		Counters.reset();
    }

	private MapView mOpenStreetMapView;

	@Override
	protected void setUp() throws Exception {

		FragmentManager fm = getActivity().getSupportFragmentManager();
		StarterMapFragment fragment = (StarterMapFragment)fm.findFragmentById(R.id.map_container);
		mOpenStreetMapView = fragment.getMapView();

		super.setUp();
	}

	/**
	 * This test will check whether calling setCenter() will position the maps so the location is
	 * at the center of the screen.
	 */
	@UiThreadTest
	public void test_toMapPixels_0_0() {
		final int iterations = 100;
		for (int i = 0 ; i < iterations ; i ++) {
			checkCenter(null, null);
			checkCenter(getRandomZoom(), getRandomGeoPoint());
			checkCenter(getRandomZoom(), null);
			checkCenter(null, getRandomGeoPoint());
		}
	}

	/**
	 * @since 6.0.0
	 */
	private double getRandomLongitude() {
		return TileSystem.getRandomLongitude(random.nextDouble());
	}

	/**
	 * @since 6.0.0
	 */
	private double getRandomLatitude() {
		return TileSystem.getRandomLatitude(random.nextDouble(), TileSystem.MinLatitude);
	}

	/**
	 * @since 6.0.0
	 */
	private double getRandomZoom() {
		return getRandom(0, microsoft.mappoint.TileSystem.getMaximumZoomLevel());
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
		checkCenter(projection, (GeoPoint)mOpenStreetMapView.getMapCenter(), "computed");
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
				+"(zoom=" + pProjection.getZoomLevel() + ",center=" + pCenter + ")",
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

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
		final int roundingTolerance = 1;
		final int iterations = 100;
		for (int i = 0 ; i < iterations ; i ++) {
			final double zoom = getRandomZoom();
			final GeoPoint zz = new GeoPoint(getRandomLatitude(), getRandomLongitude());
			mOpenStreetMapView.getController().setZoom(zoom);
			mOpenStreetMapView.getController().setCenter(zz);
			final Projection projection = mOpenStreetMapView.getProjection();

			final Point point = projection.toPixels(zz, null);

			final int width_2 = mOpenStreetMapView.getWidth() / 2;
			final int height_2 = mOpenStreetMapView.getHeight() / 2;
			assertTrue("MapView does not have layout. Make sure device is unlocked.", width_2 > 0 && height_2 > 0);
			final Point expected = new Point(width_2, height_2);
			assertEquals("the geo center of the map is in the pixel center of the map (X)", expected.x, point.x, roundingTolerance);
			assertEquals("the geo center of the map is in the pixel center of the map (Y)", expected.y, point.y, roundingTolerance);
		}
	}

	/**
	 * @since 5.6.6
	 */
	private double getRandomLongitude() {
		return getRandom(TileSystem.MinLongitude, TileSystem.MaxLongitude);
	}

	/**
	 * @since 5.6.6
	 */
	private double getRandomLatitude() {
		return getRandom(TileSystem.MinLatitude, TileSystem.MaxLatitude);
	}

	/**
	 * @since 5.6.6
	 */
	private double getRandomZoom() {
		return getRandom(mOpenStreetMapView.getMinZoomLevel(), mOpenStreetMapView.getMaxZoomLevel());
	}

	/**
	 * @since 5.6.6
	 */
	private double getRandom(final double pMin, final double pMax) {
		return pMin + random.nextDouble() * (pMax - pMin);
	}
}

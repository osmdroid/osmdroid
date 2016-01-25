/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.views;

import org.osmdroid.StarterMapActivity;
import org.osmdroid.StarterMapFragment;
import org.osmdroid.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;

import android.graphics.Point;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.support.v4.app.FragmentManager;

/**
 * @author Neil Boyd
 * 
 */
public class OpenStreetMapViewTest extends ActivityInstrumentationTestCase2<StarterMapActivity> {

	public OpenStreetMapViewTest() {
        super(StarterMapActivity.class);
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
		final GeoPoint zz = new GeoPoint(0, 0);
		mOpenStreetMapView.getController().setCenter(zz);
		mOpenStreetMapView.getController().setZoom(8);
		final Projection projection = mOpenStreetMapView.getProjection();

		final Point point = projection.toPixels(zz, null);

		final int width_2 = mOpenStreetMapView.getWidth() / 2;
		final int height_2 = mOpenStreetMapView.getHeight() / 2;
		assertTrue("MapView does not have layout. Make sure device is unlocked.", width_2 > 0 && height_2 > 0);
		final Point expected = new Point(width_2, height_2);
		assertEquals("TODO describe test", expected, point);
	}

	/**
	 * This test was retrospectively added based on current implementation. TODO a manual
	 * calculation and verify that this test gives the correct result.
	 */
	@UiThreadTest
	public void test_toMapPixels_Hannover() {

		final GeoPoint hannover = new GeoPoint(52370816, 9735936);
		mOpenStreetMapView.getController().setCenter(hannover);
		mOpenStreetMapView.getController().setZoom(8);
		final Projection projection = mOpenStreetMapView.getProjection();

		final Point point = projection.toPixels(hannover, null);
		projection.toMercatorPixels(point.x, point.y, point);

		final Point expected = new Point(34540, 21537);
		assertEquals("TODO describe test", expected, point);
	}
}

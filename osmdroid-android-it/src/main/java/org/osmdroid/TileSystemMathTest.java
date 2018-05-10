/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid;

import org.osmdroid.util.GeoPoint;

import android.graphics.Point;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.Suppress;

import microsoft.mappoint.TileSystem;

/**
 * @author Marc Kurtz
 * @author Neil Boyd
 *
 */
@Deprecated
public class TileSystemMathTest extends AndroidTestCase {

	@Suppress // this test is here to test timings for issue 512
	public void test_divide() {
		long start = System.currentTimeMillis();
		double delta = 0.0;
		for (int i=0; i<10000000; i++){
			delta = i/1E6;
		}
		long diff = System.currentTimeMillis() - start;
		assertEquals("fail", 0, diff);
	}

	@Suppress // this test is here to test timings for issue 512
	public void test_multiply() {
		long start = System.currentTimeMillis();
		double delta = 0.0;
		for (int i=0; i<10000000; i++){
			delta = i*1E-6;
		}
		long diff = System.currentTimeMillis() - start;
		assertEquals("fail", 0, diff);
	}

	/**
	 * lat,long = 60.0, 60.0 <br>
	 * gdaltransform -s_srs WGS84 -t_srs EPSG:900913 = 6679169.44759642 8399737.88981836<br>
	 * MetersToPixels(6679169.44759642, 8399737.88981836, 10) = 174763, 76127 <br>
	 */
	@Deprecated
	public void test_LatLongToPixelXY() {
		final double latitude = 60.0d;
		final double longitude = 60.0d;
		final int levelOfDetail = 10;

		final Point point = TileSystem.LatLongToPixelXY(latitude, longitude, levelOfDetail, null);

		assertEquals("TODO describe test", 174762, point.x);
		assertEquals("TODO describe test", 76126, point.y);
	}

	/**
	 * PixelsToMeters(45, 45, 8) = -2.000999101260658E7, 2.000999101260658E7 <br>
	 * gdaltransform -s_srs EPSG:900913 -t_srs WGS84 = -179.752807617187 85.0297584051224 <br>
	 */
	@Deprecated
	public void test_PixelXYToLatLong() {
		final int pixelX = 45;
		final int pixelY = 45;
		final int levelOfDetail = 8;
		final double delta = 1E-3;

		final GeoPoint point = TileSystem.PixelXYToLatLong(pixelX, pixelY, levelOfDetail, null);

		assertEquals("TODO describe test", -179.752807617187, point.getLongitudeE6() / 1E6, delta);
		assertEquals("TODO describe test", 85.0297584051224, point.getLatitudeE6() / 1E6, delta);
		assertEquals("TODO describe test", -179.752807617187, point.getLongitude(), delta);
		assertEquals("TODO describe test", 85.0297584051224, point.getLatitude(), delta);
	}

	/**
	 * Converts EPSG:900913 to pyramid pixel coordinates in given zoom level. Using google tile
	 * referencing where the origin [0,0] is the top-left corner.
	 */
	public Point MetersToPixels(final double mx, final double my, final int zoom) {
		final double res = TileSystem.GroundResolution(0, zoom);
		final double originShift = 2 * Math.PI * 6378137 / 2.0;
		final int px = (int) Math.round((mx + originShift) / res);
		int py = (int) Math.round((my + originShift) / res);
		// This converts from TMS to Google tiles by flipping the Y-axis
		py = TileSystem.MapSize(zoom) - py;
		return new Point(px, py);
	}

	/**
	 * Converts pixel coordinates in given zoom level of pyramid to EPSG:900913. Using google tile
	 * referencing where the origin [0,0] is the top-left corner.
	 */
	public double[] PixelsToMeters(final double px, final double py, final int zoom) {
		final double res = TileSystem.GroundResolution(0, zoom);
		final double originShift = 2 * Math.PI * 6378137 / 2.0;
		final double mx = px * res - originShift;
		double my = py * res - originShift;
		// This converts from TMS to Google tiles by flipping the Y-axis
		my = -my;
		return new double[] { mx, my };
	}
}

package org.osmdroid;

import microsoft.mappoint.TileSystem;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.util.Mercator;

import android.graphics.Point;
import android.test.AndroidTestCase;

/**
 * @author Neil Boyd
 * 
 */
public class TileSystemMathTest extends AndroidTestCase {

	/**
	 * This test was retrospectively added based on current implementation. TODO a manual
	 * calculation and verify that this test gives the correct result.
	 */
	public void test_projectGeoPoint_1() {
		final GeoPoint hannover = new GeoPoint(52370816, 9735936);
		final int zoom = 8;

		final Point point = Mercator.projectGeoPoint(hannover, zoom, null);
		final Point point2 = TileSystem.LatLongToPixelXY(hannover.getLatitudeE6() / 1E6,
				hannover.getLongitudeE6() / 1E6, zoom - 8, null);

		assertEquals(point.x, point2.x, 1);
		assertEquals(point.y, point2.y, 1);

		assertEquals("TODO describe test", 134, point.x);
		assertEquals("TODO describe test", 84, point.y);
	}

	/**
	 * This test was retrospectively added based on current implementation. TODO a manual
	 * calculation and verify that this test gives the correct result.
	 */
	public void test_projectGeoPoint_2() {
		final int latE6 = 52370816;
		final int lonE6 = 9735936;
		final int zoom = 8;

		final Point point = Mercator.projectGeoPoint(latE6, lonE6, zoom, null);
		final Point point2 = TileSystem.LatLongToPixelXY(latE6 / 1E6, lonE6 / 1E6, zoom - 8, null);

		assertEquals(point.x, point2.x, 1);
		assertEquals(point.y, point2.y, 1);

		assertEquals("TODO describe test", 84, point.y);
		assertEquals("TODO describe test", 134, point.x);
	}

	/**
	 * This test was retrospectively added based on current implementation. TODO a manual
	 * calculation and verify that this test gives the correct result.
	 */
	public void test_projectGeoPoint_3() {
		final double lat = 52.370816d;
		final double lon = 9.735936d;
		final int zoom = 8;

		final Point point = Mercator.projectGeoPoint(lat, lon, zoom, null);
		final Point point2 = TileSystem.LatLongToPixelXY(lat, lon, zoom - 8, null);

		assertEquals(point.x, point2.x, 1);
		assertEquals(point.y, point2.y, 1);

		assertEquals("TODO describe test", 84, point.y);
		assertEquals("TODO describe test", 134, point.x);
	}

	/**
	 * lat,long = 60.0, 60.0 <br />
	 * gdaltransform -s_srs WGS84 -t_srs EPSG:900913 = 6679169.44759642 8399737.88981836<br />
	 * MetersToPixels(6679169.44759642, 8399737.88981836, 10) = 174763, 76127 <br />
	 */
	public void test_LatLongToPixelXY() {
		final double latitude = 60.0d;
		final double longitude = 60.0d;
		final int levelOfDetail = 10;

		final Point point = TileSystem.LatLongToPixelXY(latitude, longitude, levelOfDetail, null);

		assertEquals("TODO describe test", 174763, point.x);
		assertEquals("TODO describe test", 76127, point.y);
	}

	/**
	 * PixelsToMeters(45, 45, 8) = -2.000999101260658E7, 2.000999101260658E7 <br />
	 * gdaltransform -s_srs EPSG:900913 -t_srs WGS84 = -179.752807617187 85.0297584051224 <br />
	 */
	public void test_PixelXYToLatLong() {
		final int pixelX = 45;
		final int pixelY = 45;
		final int levelOfDetail = 8;

		GeoPoint point = TileSystem.PixelXYToLatLong(pixelX, pixelY, levelOfDetail, null);

		assertEquals("TODO describe test", -179.752807617187, point.getLongitudeE6() / 1E6, 1E-3);
		assertEquals("TODO describe test", 85.0297584051224, point.getLatitudeE6() / 1E6, 1E-3);
	}

	// No delta needed to pass!
	public void test_CompareAlgorithms() {
		int maxZoom = 21;
		int points = 10;

		for (int zoomLevel = 0; zoomLevel < maxZoom; zoomLevel++) {
			int mapSize = TileSystem.MapSize(zoomLevel);
			for (int div = 0; div < points; div++) {
				int xy = (int) (mapSize * ((float) div / (float) points));
				GeoPoint point1 = Mercator.projectPoint(xy, xy, zoomLevel + 8);
				GeoPoint point2 = TileSystem.PixelXYToLatLong(xy, xy, zoomLevel, null);
				assertEquals(point1.getLatitudeE6() / 1E6, point2.getLatitudeE6() / 1E6);
				assertEquals(point1.getLongitudeE6() / 1E6, point2.getLongitudeE6() / 1E6);
			}
		}
	}

	// Needs a delta of "2" to pass!
	public void test_CompareAlgorithms2() {
		int maxZoom = 21;
		int points = 10;

		for (int zoomLevel = 0; zoomLevel < maxZoom; zoomLevel++) {
			for (int div = 0; div < points; div++) {
				double lat = (170.0d * ((float) div / (float) points)) - 85;
				double lng = (360.0d * ((float) div / (float) points)) - 180;
				Point point1 = Mercator.projectGeoPoint(lat, lng, zoomLevel + 8, null);
				Point point2 = TileSystem.LatLongToPixelXY(lat, lng, zoomLevel, null);
				assertEquals(point1.x, point2.x, 2);
				assertEquals(point1.y, point2.y, 2);
			}
		}
	}

	/**
	 * Converts EPSG:900913 to pyramid pixel coordinates in given zoom level. Using google tile
	 * referencing where the origin [0,0] is the top-left corner.
	 */
	public Point MetersToPixels(double mx, double my, int zoom) {
		double res = TileSystem.GroundResolution(0, zoom);
		double originShift = 2 * Math.PI * 6378137 / 2.0;
		int px = (int) Math.round((mx + originShift) / res);
		int py = (int) Math.round((my + originShift) / res);
		// This converts from TMS to Google tiles by flipping the Y-axis
		py = TileSystem.MapSize(zoom) - py;
		return new Point(px, py);
	}

	/**
	 * Converts pixel coordinates in given zoom level of pyramid to EPSG:900913. Using google tile
	 * referencing where the origin [0,0] is the top-left corner.
	 */
	public double[] PixelsToMeters(double px, double py, int zoom) {
		double res = TileSystem.GroundResolution(0, zoom);
		double originShift = 2 * Math.PI * 6378137 / 2.0;
		double mx = px * res - originShift;
		double my = py * res - originShift;
		// This converts from TMS to Google tiles by flipping the Y-axis
		my = -my;
		return new double[] { mx, my };
	}
}

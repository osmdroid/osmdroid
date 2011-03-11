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
		final double delta = 1E-3;

		final GeoPoint point = TileSystem.PixelXYToLatLong(pixelX, pixelY, levelOfDetail, null);

		assertEquals("TODO describe test", -179.752807617187, point.getLongitudeE6() / 1E6, delta);
		assertEquals("TODO describe test", 85.0297584051224, point.getLatitudeE6() / 1E6, delta);
	}

	/**
	 * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
	 */
	public void test_MapSize() {
		assertEquals(512, TileSystem.MapSize(1));
		assertEquals(1024, TileSystem.MapSize(2));
		assertEquals(2048, TileSystem.MapSize(3));
		assertEquals(4096, TileSystem.MapSize(4));
		assertEquals(8192, TileSystem.MapSize(5));
		assertEquals(16384, TileSystem.MapSize(6));
		assertEquals(32768, TileSystem.MapSize(7));
		assertEquals(65536, TileSystem.MapSize(8));
		assertEquals(131072, TileSystem.MapSize(9));
		assertEquals(262144, TileSystem.MapSize(10));
		assertEquals(524288, TileSystem.MapSize(11));
		assertEquals(1048576, TileSystem.MapSize(12));
		assertEquals(2097152, TileSystem.MapSize(13));
		assertEquals(4194304, TileSystem.MapSize(14));
		assertEquals(8388608, TileSystem.MapSize(15));
		assertEquals(16777216, TileSystem.MapSize(16));
		assertEquals(33554432, TileSystem.MapSize(17));
		assertEquals(67108864, TileSystem.MapSize(18));
		assertEquals(134217728, TileSystem.MapSize(19));
		assertEquals(268435456, TileSystem.MapSize(20));
		assertEquals(536870912, TileSystem.MapSize(21));
		assertEquals(1073741824, TileSystem.MapSize(22));
		assertEquals(-2147483648, TileSystem.MapSize(23));
	}

	/**
	 * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
	 */
	public void test_groundResolution() {
		final double delta = 1e-4;

		assertEquals(78271.5170, TileSystem.GroundResolution(0, 1), delta);
		assertEquals(39135.7585, TileSystem.GroundResolution(0, 2), delta);
		assertEquals(19567.8792, TileSystem.GroundResolution(0, 3), delta);
		assertEquals(9783.9396, TileSystem.GroundResolution(0, 4), delta);
		assertEquals(4891.9698, TileSystem.GroundResolution(0, 5), delta);
		assertEquals(2445.9849, TileSystem.GroundResolution(0, 6), delta);
		assertEquals(1222.9925, TileSystem.GroundResolution(0, 7), delta);
		assertEquals(611.4962, TileSystem.GroundResolution(0, 8), delta);
		assertEquals(305.7481, TileSystem.GroundResolution(0, 9), delta);
		assertEquals(152.8741, TileSystem.GroundResolution(0, 10), delta);
		assertEquals(76.4370, TileSystem.GroundResolution(0, 11), delta);
		assertEquals(38.2185, TileSystem.GroundResolution(0, 12), delta);
		assertEquals(19.1093, TileSystem.GroundResolution(0, 13), delta);
		assertEquals(9.5546, TileSystem.GroundResolution(0, 14), delta);
		assertEquals(4.7773, TileSystem.GroundResolution(0, 15), delta);
		assertEquals(2.3887, TileSystem.GroundResolution(0, 16), delta);
		assertEquals(1.1943, TileSystem.GroundResolution(0, 17), delta);
		assertEquals(0.5972, TileSystem.GroundResolution(0, 18), delta);
		assertEquals(0.2986, TileSystem.GroundResolution(0, 19), delta);
		assertEquals(0.1493, TileSystem.GroundResolution(0, 20), delta);
		assertEquals(0.0746, TileSystem.GroundResolution(0, 21), delta);
		assertEquals(0.0373, TileSystem.GroundResolution(0, 22), delta);
		assertEquals(-0.0187, TileSystem.GroundResolution(0, 23), delta);
	}

	/**
	 * Reference values from: http://msdn.microsoft.com/en-us/library/bb259689.aspx
	 */
	public void test_groundMapScale() {
		final double delta = 1e-2;

		assertEquals(295829355.45, TileSystem.MapScale(0, 1, 96), delta);
		assertEquals(147914677.73, TileSystem.MapScale(0, 2, 96), delta);
		assertEquals(73957338.86, TileSystem.MapScale(0, 3, 96), delta);
		assertEquals(36978669.43, TileSystem.MapScale(0, 4, 96), delta);
		assertEquals(18489334.72, TileSystem.MapScale(0, 5, 96), delta);
		assertEquals(9244667.36, TileSystem.MapScale(0, 6, 96), delta);
		assertEquals(4622333.68, TileSystem.MapScale(0, 7, 96), delta);
		assertEquals(2311166.84, TileSystem.MapScale(0, 8, 96), delta);
		assertEquals(1155583.42, TileSystem.MapScale(0, 9, 96), delta);
		assertEquals(577791.71, TileSystem.MapScale(0, 10, 96), delta);
		assertEquals(288895.85, TileSystem.MapScale(0, 11, 96), delta);
		assertEquals(144447.93, TileSystem.MapScale(0, 12, 96), delta);
		assertEquals(72223.96, TileSystem.MapScale(0, 13, 96), delta);
		assertEquals(36111.98, TileSystem.MapScale(0, 14, 96), delta);
		assertEquals(18055.99, TileSystem.MapScale(0, 15, 96), delta);
		assertEquals(9028.00, TileSystem.MapScale(0, 16, 96), delta);
		assertEquals(4514.00, TileSystem.MapScale(0, 17, 96), delta);
		assertEquals(2257.00, TileSystem.MapScale(0, 18, 96), delta);
		assertEquals(1128.50, TileSystem.MapScale(0, 19, 96), delta);
		assertEquals(564.25, TileSystem.MapScale(0, 20, 96), delta);
		assertEquals(282.12, TileSystem.MapScale(0, 21, 96), delta);
		assertEquals(141.06, TileSystem.MapScale(0, 22, 96), delta);
		assertEquals(-70.53, TileSystem.MapScale(0, 23, 96), delta);
	}

	// No delta needed to pass!
	public void test_CompareAlgorithms() {
		final int maxZoom = 21;
		final int points = 10;
		final double delta = 0.0;

		for (int zoomLevel = 0; zoomLevel < maxZoom; zoomLevel++) {
			final int mapSize = TileSystem.MapSize(zoomLevel);
			for (int divX = 0; divX < points; divX++) {
				for (int divY = 0; divY < points; divY++) {
					final int x = (int) (mapSize * ((float) divX / (float) points));
					final int y = (int) (mapSize * ((float) divY / (float) points));
					final GeoPoint point1 = Mercator.projectPoint(x, y, zoomLevel + 8);
					final GeoPoint point2 = TileSystem.PixelXYToLatLong(x, y, zoomLevel, null);
					assertEquals(point1.getLatitudeE6() / 1E6, point2.getLatitudeE6() / 1E6, delta);
					assertEquals(point1.getLongitudeE6() / 1E6, point2.getLongitudeE6() / 1E6,
							delta);
				}
			}
		}
	}

	// Needs a delta of "2" to pass!
	public void test_CompareAlgorithms2() {
		final int maxZoom = 21;
		final int points = 10;
		final double delta = 2.0;

		for (int zoomLevel = 0; zoomLevel < maxZoom; zoomLevel++) {
			for (int divLat = 0; divLat < points; divLat++) {
				for (int divLong = 0; divLong < points; divLong++) {
					final double lat = (170.0d * ((float) divLat / (float) points)) - 85;
					final double lng = (360.0d * ((float) divLong / (float) points)) - 180;
					final Point point1 = Mercator.projectGeoPoint(lat, lng, zoomLevel + 8, null);
					final Point point2 = TileSystem.LatLongToPixelXY(lat, lng, zoomLevel, null);
					assertEquals(point1.x, point2.x, delta);
					assertEquals(point1.y, point2.y, delta);
				}
			}
		}
	}

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

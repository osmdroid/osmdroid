package org.andnav.osm.views.util;

import static org.junit.Assert.assertEquals;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.BasicPoint;
import org.junit.Test;

/**
 * @author Neil Boyd
 *
 */
public class MercatorTest {

	/**
	 * This test was retrospectively added based on current implementation.
	 * TODO a manual calculation and verify that this test gives the correct result.
	 */
	@Test
	public void test_projectGeoPoint_1() {
        final GeoPoint hannover = new GeoPoint(52370816, 9735936);
		final int zoom = 8;

		final BasicPoint point = Mercator.projectGeoPoint(hannover, zoom, null);
		
		assertEquals("TODO describe test", 134, point.x);
		assertEquals("TODO describe test", 84, point.y);
	}

	/**
	 * This test was retrospectively added based on current implementation.
	 * TODO a manual calculation and verify that this test gives the correct result.
	 */
	@Test
	public void test_projectGeoPoint_2() {
		final int latE6 = 52370816;
		final int lonE6 = 9735936;
		final int zoom = 8;
		
		final int[] point = Mercator.projectGeoPoint(latE6, lonE6, zoom, null);
		
		assertEquals("TODO describe test", 84, point[0]);
		assertEquals("TODO describe test", 134, point[1]);
	}

	/**
	 * This test was retrospectively added based on current implementation.
	 * TODO a manual calculation and verify that this test gives the correct result.
	 */
	@Test
	public void test_projectGeoPoint_3() {
		final double lat = 52.370816d;
		final double lon = 9.735936d;
		final int zoom = 8;
		
		final int[] point = Mercator.projectGeoPoint(lat, lon, zoom, null);
		
		assertEquals("TODO describe test", 84, point[0]);
		assertEquals("TODO describe test", 134, point[1]);
	}
}

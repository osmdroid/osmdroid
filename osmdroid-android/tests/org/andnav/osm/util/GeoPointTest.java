package org.andnav.osm.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeoPointTest {

	// private static final double CIRCUMFERENCE_AT_EQUATOR = 40075160.0; // http://en.wikipedia.org/wiki/Earth
	private static final double CIRCUMFERENCE_AT_EQUATOR = 2 * Math.PI * 6378137; // http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius + a bit of math

	@Test
	public void test_distanceTo_zero() {
		GeoPoint target = new GeoPoint(0.0, 0.0);
		GeoPoint other = new GeoPoint(0.0, 0.0);
		assertEquals("distance to self is zero", 0, target.distanceTo(other));
	}

	@Test
	public void test_distanceTo_one() {
		GeoPoint target = new GeoPoint(1.0, 1.0);
		GeoPoint other = new GeoPoint(1.0, 1.0);
		assertEquals("distance to self is zero", 0, target.distanceTo(other));
	}

	@Test
	public void test_distanceTo_one_degree() {
		GeoPoint target = new GeoPoint(0.0, 0.0);
		GeoPoint other = new GeoPoint(0.0, 1.0);
		assertEquals("distance 10 degress round equator", Math.round(CIRCUMFERENCE_AT_EQUATOR / 360), target.distanceTo(other));
	}

	@Test
	public void test_bearingTo_north() {
		GeoPoint target = new GeoPoint(0.0, 0.0);
		GeoPoint other = new GeoPoint(10.0, 0.0);
		assertEquals("directly north", 0, Math.round(target.bearingTo(other)));
	}


	@Test
	public void test_bearingTo_east() {
		GeoPoint target = new GeoPoint(0.0, 0.0);
		GeoPoint other = new GeoPoint(0.0, 10.0);
		assertEquals("directly east", 90, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_bearingTo_south() {
		GeoPoint target = new GeoPoint(0.0, 0.0);
		GeoPoint other = new GeoPoint(-10.0, 0.0);
		assertEquals("directly south", 180, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_bearingTo_west() {
		GeoPoint target = new GeoPoint(0.0, 0.0);
		GeoPoint other = new GeoPoint(0.0, -10.0);
		assertEquals("directly west", 270, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_bearingTo_north_west() {
		GeoPoint target = new GeoPoint(0.0, 0.0);
		GeoPoint other = new GeoPoint(-10.0, -10.0);
		assertEquals("north west", 180 + 45, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_destinationPoint_north_west_here() {
		// this test is based on the actual result, not calculated expectations, 
		// but it is at least a basic sanity check for rounding errors and regression
		GeoPoint start = new GeoPoint(52387524, 4891604);
		GeoPoint end   = new GeoPoint(52389882, 4885341);
		assertEquals("destinationPoint north west", end, start.destinationPoint(500, -45));
	}
}

package org.osmdroid.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeoPointTest {

	// private static final double CIRCUMFERENCE_AT_EQUATOR = 40075160.0; //
	// http://en.wikipedia.org/wiki/Earth

	private static final double CIRCUMFERENCE_AT_EQUATOR = 2 * Math.PI * 6378137;
	// http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius + a bit of math

	@Test
	public void test_distanceTo_zero() {
		final GeoPoint target = new GeoPoint(0.0, 0.0);
		final GeoPoint other = new GeoPoint(0.0, 0.0);
		assertEquals("distance to self is zero", 0, target.distanceTo(other));
	}

	@Test
	public void test_distanceTo_one() {
		final GeoPoint target = new GeoPoint(1.0, 1.0);
		final GeoPoint other = new GeoPoint(1.0, 1.0);
		assertEquals("distance to self is zero", 0, target.distanceTo(other));
	}

	@Test
	public void test_distanceTo_one_degree() {
		final GeoPoint target = new GeoPoint(0.0, 0.0);
		final GeoPoint other = new GeoPoint(0.0, 1.0);
		assertEquals("distance 10 degress round equator",
				Math.round(CIRCUMFERENCE_AT_EQUATOR / 360), target.distanceTo(other));
	}

	@Test
	public void test_bearingTo_north() {
		final GeoPoint target = new GeoPoint(0.0, 0.0);
		final GeoPoint other = new GeoPoint(10.0, 0.0);
		assertEquals("directly north", 0, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_bearingTo_east() {
		final GeoPoint target = new GeoPoint(0.0, 0.0);
		final GeoPoint other = new GeoPoint(0.0, 10.0);
		assertEquals("directly east", 90, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_bearingTo_south() {
		final GeoPoint target = new GeoPoint(0.0, 0.0);
		final GeoPoint other = new GeoPoint(-10.0, 0.0);
		assertEquals("directly south", 180, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_bearingTo_west() {
		final GeoPoint target = new GeoPoint(0.0, 0.0);
		final GeoPoint other = new GeoPoint(0.0, -10.0);
		assertEquals("directly west", 270, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_bearingTo_north_west() {
		final GeoPoint target = new GeoPoint(0.0, 0.0);
		final GeoPoint other = new GeoPoint(-10.0, -10.0);
		assertEquals("north west", 180 + 45, Math.round(target.bearingTo(other)));
	}

	@Test
	public void test_destinationPoint_north_west_here() {
		// this test is based on the actual result, not calculated expectations,
		// but it is at least a basic sanity check for rounding errors and regression
		final GeoPoint start = new GeoPoint(52387524, 4891604);
		final GeoPoint end = new GeoPoint(52390698, 4886399);
		assertEquals("destinationPoint north west", end, start.destinationPoint(500, -45));
	}

	@Test
	public void test_toFromString_withoutAltitude() {
		final GeoPoint in = new GeoPoint(52387524, 4891604);
		final GeoPoint out = GeoPoint.fromIntString("52387524,4891604");
		assertEquals("toFromString without altitude", in, out);
	}

	@Test
	public void test_toFromString_withAltitude() {
		final GeoPoint in = new GeoPoint(52387524, 4891604, 12345);
		final GeoPoint out = GeoPoint.fromIntString(in.toString());
		assertEquals("toFromString with altitude", in, out);
	}

	@Test
	public void test_toFromDoubleString_withoutAltitude() {
		final GeoPoint in = new GeoPoint(-117.123, 33.123);
		final GeoPoint out = GeoPoint.fromDoubleString("-117.123,33.123", ',');
		assertEquals("toFromString without altitude", in, out);
	}

	@Test
	public void test_toFromDoubleString_withAltitude() {
		final GeoPoint in = new GeoPoint(-117.123, 33.123, 12345);
		final GeoPoint out = GeoPoint.fromDoubleString(in.toDoubleString(), ',');
		assertEquals("toFromString with altitude", in, out);
	}

	@Test
	public void test_toFromInvertedDoubleString_withoutAltitude() {
		final GeoPoint in = new GeoPoint(-117.123, 33.123);
		final GeoPoint out = GeoPoint.fromInvertedDoubleString("33.123,-117.123", ',');
		assertEquals("toFromString without altitude", in, out);
	}

	@Test
	public void test_toFromInvertedDoubleString_withAltitude() {
		final GeoPoint in = new GeoPoint(-117.123, 33.123, 12345);
		final GeoPoint out = GeoPoint.fromInvertedDoubleString(in.toInvertedDoubleString(), ',');
		assertEquals("toFromString with altitude", in, out);
	}


}

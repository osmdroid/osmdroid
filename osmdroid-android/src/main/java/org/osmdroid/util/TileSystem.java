package org.osmdroid.util;

import android.graphics.Point;

/**
 * Proxy class for TileSystem. For coordinate conversions (tile to lat/lon and reverse) TileSystem
 * only accepts input parameters within certain ranges and crops any values outside of it. For
 * lat/lon the range is ~(-85,+85) / (-180,+180) and for tile coordinates (0,mapsize-1). Under
 * certain conditions osmdroid creates values outside of these ranges, for example when zooming out
 * and displaying the earth more that once side by side or when scrolling across the 180 degree
 * longitude (international date line). This class fixes this by wrapping input coordinates into a
 * valid range by adding/subtracting the valid span. Example: longitude +185 =&gt; -175
 *
 * @author Oliver Seiler
 */
public final class TileSystem {

	/** @see microsoft.mappoint.TileSystem#setTileSize(int) */
	public static void setTileSize(final int tileSize) {
		microsoft.mappoint.TileSystem.setTileSize(tileSize);
	}

	/** @see microsoft.mappoint.TileSystem#getTileSize() */
	public static int getTileSize() {
		return microsoft.mappoint.TileSystem.getTileSize();
	}

	/** @see microsoft.mappoint.TileSystem#MapSize(int) */
	public static int MapSize(final int levelOfDetail) {
		return microsoft.mappoint.TileSystem.MapSize(levelOfDetail);
	}

	/** @see microsoft.mappoint.TileSystem#GroundResolution(double, int) */
	public static double GroundResolution(final double latitude, final int levelOfDetail) {
		return microsoft.mappoint.TileSystem.GroundResolution(wrap(latitude, -90, 90, 180), levelOfDetail);
	}

	/** @see microsoft.mappoint.TileSystem#MapScale(double, int, int) */
	public static double MapScale(final double latitude, final int levelOfDetail, final int screenDpi) {
		return microsoft.mappoint.TileSystem.MapScale(latitude, levelOfDetail, screenDpi);
	}

	/** @see microsoft.mappoint.TileSystem#LatLongToPixelXY(double, double, int, Point) */
	public static Point LatLongToPixelXY(
			final double latitude, final double longitude, final int levelOfDetail, final Point reuse) {
		return microsoft.mappoint.TileSystem.LatLongToPixelXY(
				wrap(latitude, -90, 90, 180),
				wrap(longitude, -180, 180, 360),
				levelOfDetail, reuse);
	}

	/** @see microsoft.mappoint.TileSystem#PixelXYToLatLong(int, int, int, GeoPoint) */
	public static GeoPoint PixelXYToLatLong(
			final int pixelX, final int pixelY, final int levelOfDetail, final GeoPoint reuse) {
		final int mapSize = MapSize(levelOfDetail);
		return microsoft.mappoint.TileSystem.PixelXYToLatLong(
				(int) wrap(pixelX, 0, mapSize - 1, mapSize),
				(int) wrap(pixelY, 0, mapSize - 1, mapSize),
				levelOfDetail, reuse);
	}

	/** @see microsoft.mappoint.TileSystem#PixelXYToTileXY(int, int, Point) */
	public static Point PixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
		return microsoft.mappoint.TileSystem.PixelXYToTileXY(pixelX, pixelY, reuse);
	}

	/** @see microsoft.mappoint.TileSystem#TileXYToPixelXY(int, int, Point) */
	public static Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
		return microsoft.mappoint.TileSystem.TileXYToPixelXY(tileX, tileY, reuse);
	}

	/** @see microsoft.mappoint.TileSystem#TileXYToQuadKey(int, int, int) */
	public static String TileXYToQuadKey(final int tileX, final int tileY, final int levelOfDetail) {
		return microsoft.mappoint.TileSystem.TileXYToQuadKey(tileX, tileY, levelOfDetail);
	}

	/** @see microsoft.mappoint.TileSystem#QuadKeyToTileXY(String, Point) */
	public static Point QuadKeyToTileXY(final String quadKey, final Point reuse) {
		return microsoft.mappoint.TileSystem.QuadKeyToTileXY(quadKey, reuse);
	}

	/**
	 * Returns a value that lies within <code>minValue</code> and <code>maxValue</code> by
	 * subtracting/adding <code>interval</code>.
	 *
	 * @param n
	 *            the input number
	 * @param minValue
	 *            the minimum value
	 * @param maxValue
	 *            the maximum value
	 * @param interval
	 *            the interval length
	 * @return a value that lies within <code>minValue</code> and <code>maxValue</code> by
	 *         subtracting/adding <code>interval</code>
	 */
	private static double wrap(double n, final double minValue, final double maxValue, final double interval) {
		if (minValue > maxValue) {
			throw new IllegalArgumentException("minValue must be smaller than maxValue: "
					+ minValue + ">" + maxValue);
		}
		if (interval > maxValue - minValue + 1) {
			throw new IllegalArgumentException(
					"interval must be equal or smaller than maxValue-minValue: " + "min: "
							+ minValue + " max:" + maxValue + " int:" + interval);
		}
		while (n < minValue) {
			n += interval;
		}
		while (n > maxValue) {
			n -= interval;
		}
		return n;
	}
}

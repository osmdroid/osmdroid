package org.osmdroid.util;

import android.graphics.Point;
import android.graphics.Rect;

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

	public static final double EarthRadius = 6378137;
	public static final double MinLatitude = -85.05112878;
	public static final double MaxLatitude = 85.05112878;
	public static final double MinLongitude = -180;
	public static final double MaxLongitude = 180;

	/** @see microsoft.mappoint.TileSystem#setTileSize(int) */
	public static void setTileSize(final int tileSize) {
		microsoft.mappoint.TileSystem.setTileSize(tileSize);
	}

	/** @see microsoft.mappoint.TileSystem#getTileSize() */
	public static int getTileSize() {
		return microsoft.mappoint.TileSystem.getTileSize();
	}

	/**
	 * @since 6.0
	 */
	public static double getTileSize(final double pZoomLevel) {
		return MapSize(pZoomLevel - getInputTileZoomLevel(pZoomLevel));
	}

	/**
	 * @since 6.0
	 */
	public static int getInputTileZoomLevel(final double pZoomLevel) {
		return (int) pZoomLevel;
	}

	/** @see microsoft.mappoint.TileSystem#MapSize(int) */
	@Deprecated
	public static int MapSize(final int levelOfDetail) {
		return microsoft.mappoint.TileSystem.MapSize(levelOfDetail);
	}

	/**
	 * @since 6.0
	 * @see microsoft.mappoint.TileSystem#MapSize(int)
	 */
	public static double MapSize(final double pZoomLevel) {
		return getTileSize() * getFactor(pZoomLevel);
	}

	/**
	 * @since 6.0
	 */
	public static double getFactor(final double pZoomLevel) {
		return Math.pow(2, pZoomLevel);
	}

	/** @see microsoft.mappoint.TileSystem#GroundResolution(double, int) */
	public static double GroundResolution(final double latitude, final int levelOfDetail) {
		return microsoft.mappoint.TileSystem.GroundResolution(wrap(latitude, -90, 90, 180), levelOfDetail);
	}

	/**
	 * @since 6.0
	 */
	public static double GroundResolution(final double latitude, final double zoomLevel) {
		return GroundResolutionMapSize(wrap(latitude, -90, 90, 180), MapSize(zoomLevel));
	}

	/**
	 * @since 6.0
	 * @see microsoft.mappoint.TileSystem#GroundResolution(double, int)
	 */
	public static double GroundResolutionMapSize(double latitude, final double mapSize) {
		latitude = Clip(latitude, MinLatitude, MaxLatitude);
		return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * EarthRadius
				/ mapSize;
	}

	/** @see microsoft.mappoint.TileSystem#MapScale(double, int, int) */
	public static double MapScale(final double latitude, final int levelOfDetail, final int screenDpi) {
		return microsoft.mappoint.TileSystem.MapScale(latitude, levelOfDetail, screenDpi);
	}

	/** @see microsoft.mappoint.TileSystem#LatLongToPixelXY(double, double, int, Point) */
	@Deprecated
	public static Point LatLongToPixelXY(
			final double latitude, final double longitude, final int levelOfDetail, final Point reuse) {
		return microsoft.mappoint.TileSystem.LatLongToPixelXY(
				wrap(latitude, -90, 90, 180),
				wrap(longitude, -180, 180, 360),
				levelOfDetail, reuse);
	}

	/**
	 * @since 6.0
	 */
	public static Point LatLongToPixelXY(
			final double latitude, final double longitude, final double zoomLevel, final Point reuse) {
		return LatLongToPixelXYMapSize(
				wrap(latitude, -90, 90, 180),
				wrap(longitude, -180, 180, 360),
				MapSize(zoomLevel), reuse);
	}

	/**
	 * @since 6.0
	 * @see microsoft.mappoint.TileSystem#LatLongToPixelXY(double, double, int, android.graphics.Point)
	 */
	public static Point LatLongToPixelXYMapSize(double latitude, double longitude,
												final double mapSize, final Point reuse) {
		final Point out = (reuse == null ? new Point() : reuse);

		latitude = Clip(latitude, MinLatitude, MaxLatitude);
		longitude = Clip(longitude, MinLongitude, MaxLongitude);

		final double x = (longitude + 180) / 360;
		final double sinLatitude = Math.sin(latitude * Math.PI / 180);
		final double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

		out.x = (int) Clip(x * mapSize + 0.5, 0, mapSize - 1);
		out.y = (int) Clip(y * mapSize + 0.5, 0, mapSize - 1);
		return out;
	}

	@Deprecated
	public static GeoPoint PixelXYToLatLong(
			final int pixelX, final int pixelY, final int levelOfDetail, final GeoPoint reuse) {
		final int mapSize = MapSize(levelOfDetail);
		return microsoft.mappoint.TileSystem.PixelXYToLatLong(
				(int) wrap(pixelX, 0, mapSize - 1, mapSize),
				(int) wrap(pixelY, 0, mapSize - 1, mapSize),
				levelOfDetail, reuse);
	}

	/**
	 * @since 6.0
	 */
	public static GeoPoint PixelXYToLatLong(
			final int pixelX, final int pixelY, final double zoomLevel, final GeoPoint reuse) {
		final double mapSize = MapSize(zoomLevel);
		return PixelXYToLatLongMapSize(
				(int) wrap(pixelX, 0, mapSize - 1, mapSize),
				(int) wrap(pixelY, 0, mapSize - 1, mapSize),
				mapSize, reuse);
	}

	/**
	 * @since 6.0
	 * @see microsoft.mappoint.TileSystem#PixelXYToLatLong(int, int, int, GeoPoint)
	 */
	public static GeoPoint PixelXYToLatLongMapSize(final int pixelX, final int pixelY,
												   final double mapSize, final GeoPoint reuse) {
		final GeoPoint out = (reuse == null ? new GeoPoint(0., 0.) : reuse);
		final double x = (Clip(pixelX, 0, mapSize - 1) / (double)mapSize) - 0.5;
		final double y = 0.5 - (Clip(pixelY, 0, mapSize - 1) / (double)mapSize);
		final double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
		final double longitude = 360 * x;
		out.setLatitude(latitude);
		out.setLongitude(longitude);
		return out;
	}

	/**
	 * @since 6.0
	 * @see microsoft.mappoint.TileSystem#Clip(double, double, double)
	 */
	public static double Clip(final double n, final double minValue, final double maxValue) {
		return Math.min(Math.max(n, minValue), maxValue);
	}

	@Deprecated
	public static Point PixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
		return microsoft.mappoint.TileSystem.PixelXYToTileXY(pixelX, pixelY, reuse);
	}

	/**
	 * @since 6.0
	 * @see microsoft.mappoint.TileSystem#PixelXYToTileXY(int, int, Point)
	 */
	public static Point PixelXYToTileXY(final int pPixelX, final int pPixelY, final double pTileSize, final Point pReuse) {
		final Point out = (pReuse == null ? new Point() : pReuse);
		out.x = (int) (pPixelX / pTileSize);
		out.y = (int) (pPixelY / pTileSize);
		return out;
	}

	/**
	 * @since 6.0
	 */
	public static Rect PixelXYToTileXY(final Rect rect, final double pTileSize, final Rect pReuse) {
		final Rect out = (pReuse == null ? new Rect() : pReuse);
		out.set((int)(rect.left / pTileSize), (int) (rect.top / pTileSize),
				(int)(rect.right / pTileSize), (int) (rect.bottom / pTileSize));
		return out;
	}

	@Deprecated
	public static Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
		return microsoft.mappoint.TileSystem.TileXYToPixelXY(tileX, tileY, reuse);
	}

	/**
	 * @since 6.0
	 * @see microsoft.mappoint.TileSystem#TileXYToPixelXY(int, int, Point)
	 */
	public static Point TileXYToPixelXY(final int pTileX, final int pTileY, final double pTileSize, final Point pReuse) {
		final Point out = (pReuse == null ? new Point() : pReuse);
		out.x = (int) (pTileX * pTileSize);
		out.y = (int) (pTileY * pTileSize);
		return out;
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

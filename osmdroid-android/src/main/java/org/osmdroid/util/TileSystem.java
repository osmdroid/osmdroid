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
	 * @since 5.6.6
	 */
	public static double getTileSize(final double pZoomLevel) {
		return MapSize(pZoomLevel - getInputTileZoomLevel(pZoomLevel));
	}

	/**
	 * @since 5.6.6
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
	 * @since 5.6.6
	 * @see microsoft.mappoint.TileSystem#MapSize(int)
	 */
	public static double MapSize(final double pZoomLevel) {
		return getTileSize() * getFactor(pZoomLevel);
	}

	/**
	 * @since 5.6.6
	 */
	public static double getFactor(final double pZoomLevel) {
		return Math.pow(2, pZoomLevel);
	}

	/** @see microsoft.mappoint.TileSystem#GroundResolution(double, int) */
	public static double GroundResolution(final double latitude, final int levelOfDetail) {
		return microsoft.mappoint.TileSystem.GroundResolution(wrap(latitude, -90, 90, 180), levelOfDetail);
	}

	/**
	 * @since 5.6.6
	 */
	public static double GroundResolution(final double latitude, final double zoomLevel) {
		return GroundResolutionMapSize(wrap(latitude, -90, 90, 180), MapSize(zoomLevel));
	}

	/**
	 * @since 5.6.6
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
	 * @since 5.6.6
	 */
	public static Point LatLongToPixelXY(
			final double latitude, final double longitude, final double zoomLevel, final Point reuse) {
		return LatLongToPixelXYMapSize(
				wrap(latitude, -90, 90, 180),
				wrap(longitude, -180, 180, 360),
				MapSize(zoomLevel), reuse);
	}

	/**
	 * @since 5.6.6
	 * @see microsoft.mappoint.TileSystem#LatLongToPixelXY(double, double, int, android.graphics.Point)
	 */
	public static Point LatLongToPixelXYMapSize(double latitude, double longitude,
												final double mapSize, final Point reuse) {
		final Point out = (reuse == null ? new Point() : reuse);
		out.x = (int) Clip(getX01FromLongitude(longitude) * mapSize - 0.5, 0, mapSize - 1);
		out.y = (int) Clip(getY01FromLatitude(latitude) * mapSize - 0.5, 0, mapSize - 1);
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
	 * @since 5.6.6
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
	 * Converts a longitude to its "X01" value,
	 * id est a double between 0 and 1 for the whole longitude range
	 * @since 5.6.6
	 */
	public static double getX01FromLongitude(double longitude) {
		longitude = Clip(longitude, MinLongitude, MaxLongitude);
		return (longitude + 180) / 360;
	}

	/**
	 * Converts a latitude to its "Y01" value,
	 * id est a double between 0 and 1 for the whole latitude range
	 * @since 5.6.6
	 */
	public static double getY01FromLatitude(double latitude) {
		latitude = Clip(latitude, MinLatitude, MaxLatitude);
		final double sinus = Math.sin(latitude * Math.PI / 180);
		// the "Clip" part is there for side effects on 85.05112878 and -85.05112878
		// with 85.05112877 and -85.05112877 the result is still between 0 and 1
		return Clip(0.5 - Math.log((1 + sinus) / (1 - sinus)) / (4 * Math.PI), 0, 1);
	}

	/**
	 * @since 5.6.6
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
	 * @since 5.6.6
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
	 * @since 5.6.6
	 * @see microsoft.mappoint.TileSystem#PixelXYToTileXY(int, int, Point)
	 */
	public static Point PixelXYToTileXY(final int pPixelX, final int pPixelY, final double pTileSize, final Point pReuse) {
		final Point out = (pReuse == null ? new Point() : pReuse);
		out.x = (int) (pPixelX / pTileSize);
		out.y = (int) (pPixelY / pTileSize);
		return out;
	}

	/**
	 * @since 5.6.6
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
	 * @since 5.6.6
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
	 * @since 5.6.6
	 * @return the maximum zoom level where a bounding box fits into a screen,
	 * or Double.MIN_VALUE if bounding box is a single point
	 */
	public static double getBoundingBoxZoom(final BoundingBox pBoundingBox, final int pScreenWidth, final int pScreenHeight) {
		final double longitudeZoom = getLongitudeZoom(pBoundingBox.getLonEast(), pBoundingBox.getLonWest(), pScreenWidth);
		final double latitudeZoom = getLatitudeZoom(pBoundingBox.getLatNorth(), pBoundingBox.getLatSouth(), pScreenHeight);
		if (longitudeZoom == Double.MIN_VALUE) {
			return latitudeZoom;
		}
		if (latitudeZoom == Double.MIN_VALUE) {
			return longitudeZoom;
		}
		return Math.min(latitudeZoom, longitudeZoom);
	}

	/**
	 * @since 5.6.6
	 * @return the maximum zoom level where both longitudes fit into a screen,
	 * or Double.MIN_VALUE if longitudes are equal
	 */
	public static double getLongitudeZoom(final double pEast, final double pWest, final int pScreenWidth) {
		final double x01West = getX01FromLongitude(pWest);
		final double x01East = getX01FromLongitude(pEast);
		double span = x01East - x01West;
		if (span < 0) {
			span += 1;
		}
		if (span == 0) {
			return Double.MIN_VALUE;
		}
		return Math.log(pScreenWidth / span / getTileSize()) / Math.log(2);
	}

	/**
	 * @since 5.6.6
	 * @return the maximum zoom level where both latitudes fit into a screen,
	 * or Double.MIN_VALUE if latitudes are equal or ill positioned
	 */
	public static double getLatitudeZoom(final double pNorth, final double pSouth, final int pScreenHeight) {
		final double y01North = getY01FromLatitude(pNorth);
		final double y01South = getY01FromLatitude(pSouth);
		final double span = y01South - y01North;
		if (span <= 0) {
			return Double.MIN_VALUE;
		}
		return Math.log(pScreenHeight / span / getTileSize()) / Math.log(2);
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

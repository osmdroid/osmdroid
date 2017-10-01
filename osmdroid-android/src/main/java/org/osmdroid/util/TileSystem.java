package org.osmdroid.util;

import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.util.constants.GeoConstants;

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

	@Deprecated
	public static final double EarthRadius = GeoConstants.RADIUS_EARTH_METERS;
	public static final double MinLatitude = -85.05112877980659;
	public static final double MaxLatitude = 85.05112877980659;
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
	 * @since 6.0.0
	 */
	public static double getTileSize(final double pZoomLevel) {
		return MapSize(pZoomLevel - getInputTileZoomLevel(pZoomLevel));
	}

	/**
	 * @since 6.0.0
	 */
	public static int getInputTileZoomLevel(final double pZoomLevel) {
		return MyMath.floorToInt(pZoomLevel);
	}

	/** @see microsoft.mappoint.TileSystem#MapSize(int) */
	@Deprecated
	public static int MapSize(final int levelOfDetail) {
		return microsoft.mappoint.TileSystem.MapSize(levelOfDetail);
	}

	/**
	 * @since 6.0.0
	 * @see microsoft.mappoint.TileSystem#MapSize(int)
	 */
	public static double MapSize(final double pZoomLevel) {
		return getTileSize() * getFactor(pZoomLevel);
	}

	/**
	 * @since 6.0.0
	 */
	public static double getFactor(final double pZoomLevel) {
		return Math.pow(2, pZoomLevel);
	}

	/** @see microsoft.mappoint.TileSystem#GroundResolution(double, int) */
	public static double GroundResolution(final double latitude, final int levelOfDetail) {
		return GroundResolution(latitude, (double)levelOfDetail);
	}

	/**
	 * @since 6.0.0
	 */
	public static double GroundResolution(final double latitude, final double zoomLevel) {
		return GroundResolutionMapSize(wrap(latitude, -90, 90, 180), MapSize(zoomLevel));
	}

	/**
	 * @since 6.0.0
	 * @see microsoft.mappoint.TileSystem#GroundResolution(double, int)
	 */
	public static double GroundResolutionMapSize(double latitude, final double mapSize) {
		latitude = Clip(latitude, MinLatitude, MaxLatitude);
		return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * GeoConstants.RADIUS_EARTH_METERS
				/ mapSize;
	}

	/** @see microsoft.mappoint.TileSystem#MapScale(double, int, int) */
	public static double MapScale(final double latitude, final int levelOfDetail, final int screenDpi) {
		return GroundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
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
	 * @since 6.0.0
	 * Use {@link TileSystem#getMercatorFromGeo(double, double, double, PointL)} instead
	 */
	@Deprecated
	public static PointL LatLongToPixelXY(
			final double latitude, final double longitude, final double zoomLevel, final PointL reuse) {
		return LatLongToPixelXYMapSize(
				wrap(latitude, -90, 90, 180),
				wrap(longitude, -180, 180, 360),
				MapSize(zoomLevel), reuse);
	}

	/**
	 * @since 6.0.0
	 * Use {@link TileSystem#getMercatorFromGeo(double, double, double, PointL)} instead
	 */
	@Deprecated
	public static PointL LatLongToPixelXYMapSize(double latitude, double longitude,
												 final double mapSize, final PointL reuse) {
		return getMercatorFromGeo(latitude, longitude, mapSize, reuse);
	}

	/**
	 * Use {@link TileSystem#getGeoFromMercator(long, long, double, GeoPoint)} instead
	 */
	@Deprecated
	public static GeoPoint PixelXYToLatLong(
			final int pixelX, final int pixelY, final int levelOfDetail, final GeoPoint reuse) {
		final int mapSize = MapSize(levelOfDetail);
		return microsoft.mappoint.TileSystem.PixelXYToLatLong(
				MyMath.floorToInt(wrap(pixelX, 0, mapSize - 1, mapSize)),
				MyMath.floorToInt(wrap(pixelY, 0, mapSize - 1, mapSize)),
				levelOfDetail, reuse);
	}

	/**
	 * @since 6.0.0
	 * Use {@link TileSystem#getGeoFromMercator(long, long, double, GeoPoint)} instead
	 */
	@Deprecated
	public static GeoPoint PixelXYToLatLong(
			final int pixelX, final int pixelY, final double zoomLevel, final GeoPoint reuse) {
		return getGeoFromMercator(pixelX, pixelY, MapSize(zoomLevel), reuse);
	}

	/**
	 * Converts a longitude to its "X01" value,
	 * id est a double between 0 and 1 for the whole longitude range
	 * @since 6.0.0
	 */
	public static double getX01FromLongitude(double longitude) {
		longitude = Clip(longitude, MinLongitude, MaxLongitude);
		return (longitude + 180) / 360;
	}

	/**
	 * Converts a latitude to its "Y01" value,
	 * id est a double between 0 and 1 for the whole latitude range
	 * @since 6.0.0
	 */
	public static double getY01FromLatitude(double latitude) {
		latitude = Clip(latitude, MinLatitude, MaxLatitude);
		final double sinus = Math.sin(latitude * Math.PI / 180);
		// the "Clip" part is there for side effects on 85.05112878 and -85.05112878
		// with 85.05112877 and -85.05112877 the result is still between 0 and 1
		return Clip(0.5 - Math.log((1 + sinus) / (1 - sinus)) / (4 * Math.PI), 0, 1);
	}

	/**
	 * @since 6.0.0
	 * @see microsoft.mappoint.TileSystem#PixelXYToLatLong(int, int, int, GeoPoint)
	 * Use {@link TileSystem#getGeoFromMercator(long, long, double, GeoPoint)} instead
	 */
	@Deprecated
	public static GeoPoint PixelXYToLatLongMapSize(final int pixelX, final int pixelY,
												   final double mapSize, final GeoPoint reuse) {
		return getGeoFromMercator(pixelX, pixelY, mapSize, reuse);
	}

	/**
	 * @since 6.0.0
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
	 * @since 6.0.0
	 * @see microsoft.mappoint.TileSystem#PixelXYToTileXY(int, int, Point)
	 * Use {@link TileSystem#getTileFromMercator(long, double)} instead
	 */
	@Deprecated
	public static Point PixelXYToTileXY(final int pPixelX, final int pPixelY, final double pTileSize, final Point pReuse) {
		final Point out = (pReuse == null ? new Point() : pReuse);
		out.x = getTileFromMercator(pPixelX, pTileSize);
		out.y = getTileFromMercator(pPixelY, pTileSize);
		return out;
	}

	/**
	 * @since 6.0.0
	 * Use {@link TileSystem#getTileFromMercator(long, double)} instead
	 */
	@Deprecated
	public static Rect PixelXYToTileXY(final Rect rect, final double pTileSize, final Rect pReuse) {
		final Rect out = (pReuse == null ? new Rect() : pReuse);
		out.left = getTileFromMercator(rect.left, pTileSize);
		out.top = getTileFromMercator(rect.top, pTileSize);
		out.right = getTileFromMercator(rect.right, pTileSize);
		out.bottom = getTileFromMercator(rect.bottom, pTileSize);
		return out;
	}

	@Deprecated
	public static Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
		return microsoft.mappoint.TileSystem.TileXYToPixelXY(tileX, tileY, reuse);
	}

	/**
	 * @since 6.0.0
	 * @see microsoft.mappoint.TileSystem#TileXYToPixelXY(int, int, Point)
	 * Use {@link TileSystem#getMercatorFromTile(int, double)} instead
	 */
	@Deprecated
	public static PointL TileXYToPixelXY(final int pTileX, final int pTileY, final double pTileSize, final PointL pReuse) {
		final PointL out = (pReuse == null ? new PointL() : pReuse);
		out.x = getMercatorFromTile(pTileX, pTileSize);
		out.y = getMercatorFromTile(pTileY, pTileSize);
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
	 * @since 6.0.0
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
	 * @since 6.0.0
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
	 * @since 6.0.0
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

	/**
	 * @since 5.6.6
	 */
	public static long getMercatorYFromLatitude(final double pLatitude, final double pMapSize) {
		return getMercatorFromXY01(getY01FromLatitude(pLatitude), pMapSize);
	}

	/**
	 * @since 5.6.6
	 */
	public static long getMercatorXFromLongitude(final double pLongitude, final double pMapSize) {
		return getMercatorFromXY01(getX01FromLongitude(pLongitude), pMapSize);
	}

	/**
	 * @since 5.6.6
	 */
	public static long getMercatorFromXY01(final double pXY01, final double pMapSize) {
		return ClipToLong(pXY01 * pMapSize, pMapSize);
	}

	/**
	 * Converts a "Y01" value into latitude
	 * "Y01" is a double between 0 and 1 for the whole latitude range
	 * MaxLatitude:0 ... MinLatitude:1
	 * @since 5.6.6
	 */
	public static double getLatitudeFromY01(final double pY01) {
		return Clip(90 - 360 * Math.atan(Math.exp((pY01 - 0.5) * 2 * Math.PI)) / Math.PI, MinLatitude, MaxLatitude);
	}

	/**
	 * Converts a "X01" value into longitude
	 * "X01" is a double between 0 and 1 for the whole longitude range
	 * MinLongitude:0 ... MaxLongitude:1
	 * @since 5.6.6
	 */
	public static double getLongitudeFromX01(final double pX01) {
		return MinLongitude + (MaxLongitude - MinLongitude) * Clip(pX01, 0, 1);
	}

	/**
	 * @since 5.6.6
	 */
	public static long getCleanMercator(final long pMercator, final double pMercatorMapSize) {
		return ClipToLong(wrap(pMercator, 0, pMercatorMapSize, pMercatorMapSize), pMercatorMapSize);
	}

	/**
	 * @since 5.6.6
	 */
	public static long ClipToLong(final double value, final double max) {
		return Clip(MyMath.floorToLong(value - 0.5), 0, MyMath.floorToLong(max - 1));
	}

	/**
	 * @since 5.6.6
	 */
	public static long Clip(final long n, final long minValue, final long maxValue) {
		return Math.min(Math.max(n, minValue), maxValue);
	}

	/**
	 * @since 5.6.6
	 * Casts a long type value into an int with no harm.
	 * The typical use case is to compute pixel coordinates with high zoom
	 * (which won't fit into int but will fit into long)
	 * and to truncate them into int in order to display them on the screen (which requires int)
	 * The meaning of a pixel coordinate of MIN/MAX_VALUE is just:
	 * it's far far away and it doesn't crash the app
	 */
	public static int truncateToInt(final long value) {
		return (int)Math.max(Math.min(value, Integer.MAX_VALUE), Integer.MIN_VALUE);
	}

	/**
	 * @since 5.6.6
	 */
	public static PointL getMercatorFromGeo(final double pLatitude, final double pLongitude, final double pMapSize, final PointL pReuse) {
		final PointL out = (pReuse == null ? new PointL() : pReuse);
		out.x = getMercatorXFromLongitude(pLongitude, pMapSize);
		out.y = getMercatorYFromLatitude(pLatitude, pMapSize);
		return out;
	}

	/**
	 * @since 5.6.6
	 */
	public static GeoPoint getGeoFromMercator(final long pMercatorX, final long pMercatorY, final double pMapSize, final GeoPoint pReuse) {
		final GeoPoint out = pReuse == null ? new GeoPoint(0., 0.) : pReuse;
		out.setLatitude(getLatitudeFromY01(getXY01FromMercator(pMercatorY, pMapSize)));
		out.setLongitude(getLongitudeFromX01(getXY01FromMercator(pMercatorX, pMapSize)));
		return out;
	}

	/**
	 * @since 5.6.6
	 */
	public static double getXY01FromMercator(final long pMercator, final double pMapSize) {
		return Clip(pMercator / pMapSize, 0, 1);
	}

	/**
	 * @since 5.6.6
	 * @param pRandom01 [0,1]
	 */
	public static double getRandomLongitude(final double pRandom01) {
		return pRandom01 * (MaxLongitude - MinLongitude) + MinLongitude;
	}

	/**
	 * @since 5.6.6
	 * @param pRandom01 [0,1]
	 */
	public static double getRandomLatitude(final double pRandom01, final double pMinLatitude) {
		return pRandom01 * (MaxLatitude - pMinLatitude) + pMinLatitude;
	}

	/**
	 * @since 5.6.6
	 */
	public static int getTileFromMercator(final long pMercator, final double pTileSize) {
		return MyMath.floorToInt(pMercator / pTileSize);
	}

	/**
	 * @since 5.6.6
	 */
	public static Rect getTileFromMercator(final RectL pMercatorRect, final double pTileSize, final Rect pReuse) {
		final Rect out = (pReuse == null ? new Rect() : pReuse);
		out.left = TileSystem.getTileFromMercator(pMercatorRect.left, pTileSize);
		out.top = TileSystem.getTileFromMercator(pMercatorRect.top, pTileSize);
		out.right = TileSystem.getTileFromMercator(pMercatorRect.right, pTileSize);
		out.bottom = TileSystem.getTileFromMercator(pMercatorRect.bottom, pTileSize);
		return out;
	}

	/**
	 * @since 5.6.6
	 */
	public static long getMercatorFromTile(final int pTile, final double pTileSize) {
		return Math.round(pTile * pTileSize);
	}
}

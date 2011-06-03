package microsoft.mappoint;

/*
 * http://msdn.microsoft.com/en-us/library/bb259689.aspx
 *
 * Copyright (c) 2006-2009 Microsoft Corporation.  All rights reserved.
 *
 *
 */

import org.osmdroid.util.GeoPoint;

import android.graphics.Point;

/**
 * This class provides methods to handle the Mercator projection that is used for the osmdroid tile
 * system.
 */
public final class TileSystem {

	protected static int mTileSize = 256;
	private static final double EarthRadius = 6378137;
	private static final double MinLatitude = -85.05112878;
	private static final double MaxLatitude = 85.05112878;
	private static final double MinLongitude = -180;
	private static final double MaxLongitude = 180;

	public static void setTileSize(final int tileSize) {
		mTileSize = tileSize;
	}

	public static int getTileSize() {
		return mTileSize;
	}

	/**
	 * Clips a number to the specified minimum and maximum values.
	 * 
	 * @param n
	 *            The number to clip
	 * @param minValue
	 *            Minimum allowable value
	 * @param maxValue
	 *            Maximum allowable value
	 * @return The clipped value.
	 */
	private static double Clip(final double n, final double minValue, final double maxValue) {
		return Math.min(Math.max(n, minValue), maxValue);
	}

	/**
	 * Determines the map width and height (in pixels) at a specified level of detail.
	 * 
	 * @param levelOfDetail
	 *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
	 * @return The map width and height in pixels
	 */

	public static int MapSize(final int levelOfDetail) {
		return mTileSize << levelOfDetail;
	}

	/**
	 * Determines the ground resolution (in meters per pixel) at a specified latitude and level of
	 * detail.
	 * 
	 * @param latitude
	 *            Latitude (in degrees) at which to measure the ground resolution
	 * @param levelOfDetail
	 *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
	 * @return The ground resolution, in meters per pixel
	 */
	public static double GroundResolution(double latitude, final int levelOfDetail) {
		latitude = Clip(latitude, MinLatitude, MaxLatitude);
		return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * EarthRadius
				/ MapSize(levelOfDetail);
	}

	/**
	 * Determines the map scale at a specified latitude, level of detail, and screen resolution.
	 * 
	 * @param latitude
	 *            Latitude (in degrees) at which to measure the map scale
	 * @param levelOfDetail
	 *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
	 * @param screenDpi
	 *            Resolution of the screen, in dots per inch
	 * @return The map scale, expressed as the denominator N of the ratio 1 : N
	 */
	public static double MapScale(final double latitude, final int levelOfDetail,
			final int screenDpi) {
		return GroundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
	}

	/**
	 * Converts a point from latitude/longitude WGS-84 coordinates (in degrees) into pixel XY
	 * coordinates at a specified level of detail.
	 * 
	 * @param latitude
	 *            Latitude of the point, in degrees
	 * @param longitude
	 *            Longitude of the point, in degrees
	 * @param levelOfDetail
	 *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
	 * @param reuse
	 *            An optional Point to be recycled, or null to create a new one automatically
	 * @return Output parameter receiving the X and Y coordinates in pixels
	 */
	public static Point LatLongToPixelXY(double latitude, double longitude,
			final int levelOfDetail, final Point reuse) {
		final Point out = (reuse == null ? new Point() : reuse);

		latitude = Clip(latitude, MinLatitude, MaxLatitude);
		longitude = Clip(longitude, MinLongitude, MaxLongitude);

		final double x = (longitude + 180) / 360;
		final double sinLatitude = Math.sin(latitude * Math.PI / 180);
		final double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

		final int mapSize = MapSize(levelOfDetail);
		out.x = (int) Clip(x * mapSize + 0.5, 0, mapSize - 1);
		out.y = (int) Clip(y * mapSize + 0.5, 0, mapSize - 1);
		return out;
	}

	/**
	 * Converts a pixel from pixel XY coordinates at a specified level of detail into
	 * latitude/longitude WGS-84 coordinates (in degrees).
	 * 
	 * @param pixelX
	 *            X coordinate of the point, in pixels
	 * @param pixelY
	 *            Y coordinate of the point, in pixels
	 * @param levelOfDetail
	 *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
	 * @param reuse
	 *            An optional GeoPoint to be recycled, or null to create a new one automatically
	 * @return Output parameter receiving the latitude and longitude in degrees.
	 */
	public static GeoPoint PixelXYToLatLong(final int pixelX, final int pixelY,
			final int levelOfDetail, final GeoPoint reuse) {
		final GeoPoint out = (reuse == null ? new GeoPoint(0, 0) : reuse);

		final double mapSize = MapSize(levelOfDetail);
		final double x = (Clip(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
		final double y = 0.5 - (Clip(pixelY, 0, mapSize - 1) / mapSize);

		final double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
		final double longitude = 360 * x;

		out.setLatitudeE6((int) (latitude * 1E6));
		out.setLongitudeE6((int) (longitude * 1E6));
		return out;
	}

	/**
	 * Converts pixel XY coordinates into tile XY coordinates of the tile containing the specified
	 * pixel.
	 * 
	 * @param pixelX
	 *            Pixel X coordinate
	 * @param pixelY
	 *            Pixel Y coordinate
	 * @param reuse
	 *            An optional Point to be recycled, or null to create a new one automatically
	 * @return Output parameter receiving the tile X and Y coordinates
	 */
	public static Point PixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
		final Point out = (reuse == null ? new Point() : reuse);

		out.x = pixelX / mTileSize;
		out.y = pixelY / mTileSize;
		return out;
	}

	/**
	 * Converts tile XY coordinates into pixel XY coordinates of the upper-left pixel of the
	 * specified tile.
	 * 
	 * @param tileX
	 *            Tile X coordinate
	 * @param tileY
	 *            Tile X coordinate
	 * @param reuse
	 *            An optional Point to be recycled, or null to create a new one automatically
	 * @return Output parameter receiving the pixel X and Y coordinates
	 */
	public static Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
		final Point out = (reuse == null ? new Point() : reuse);

		out.x = tileX * mTileSize;
		out.y = tileY * mTileSize;
		return out;
	}

	/**
	 * Converts tile XY coordinates into a QuadKey at a specified level of detail.
	 * 
	 * @param tileX
	 *            Tile X coordinate
	 * @param tileY
	 *            Tile Y coordinate
	 * @param levelOfDetail
	 *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
	 * @return A string containing the QuadKey
	 */
	public static String TileXYToQuadKey(final int tileX, final int tileY, final int levelOfDetail) {
		final StringBuilder quadKey = new StringBuilder();
		for (int i = levelOfDetail; i > 0; i--) {
			char digit = '0';
			final int mask = 1 << (i - 1);
			if ((tileX & mask) != 0) {
				digit++;
			}
			if ((tileY & mask) != 0) {
				digit++;
				digit++;
			}
			quadKey.append(digit);
		}
		return quadKey.toString();
	}

	/**
	 * Converts a QuadKey into tile XY coordinates.
	 * 
	 * @param quadKey
	 *            QuadKey of the tile
	 * @param reuse
	 *            An optional Point to be recycled, or null to create a new one automatically
	 * @return Output parameter receiving the tile X and y coordinates
	 */
	public static Point QuadKeyToTileXY(final String quadKey, final Point reuse) {
		final Point out = (reuse == null ? new Point() : reuse);
		int tileX = 0;
		int tileY = 0;

		final int levelOfDetail = quadKey.length();
		for (int i = levelOfDetail; i > 0; i--) {
			final int mask = 1 << (i - 1);
			switch (quadKey.charAt(levelOfDetail - i)) {
			case '0':
				break;

			case '1':
				tileX |= mask;
				break;

			case '2':
				tileY |= mask;
				break;

			case '3':
				tileX |= mask;
				tileY |= mask;
				break;

			default:
				throw new IllegalArgumentException("Invalid QuadKey digit sequence.");
			}
		}
		out.set(tileX, tileY);
		return out;
	}
}

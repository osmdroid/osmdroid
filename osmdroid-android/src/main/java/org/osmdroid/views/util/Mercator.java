// Created by plusminus on 17:53:07 - 25.09.2008
package org.osmdroid.views.util;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.graphics.Point;

/**
 * http://wiki.openstreetmap.org/index.php/Mercator
 * http://developers.cloudmade.com/projects/tiles/examples/convert-coordinates-to-tile-numbers
 *
 * @author Nicolas Gramlich
 *
 * @deprecated Use {@link org.osmdroid.util.TileSystem} instead
 *
 */
public class Mercator implements MapViewConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	final static double DEG2RAD = Math.PI / 180;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * This is a utility class with only static members.
	 */
	private Mercator() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Mercator projection of GeoPoint at given zoom level
	 *
	 * @param aLatE6
	 *            latitude in degrees [-89000000 to 89000000]
	 * @param aLonE6
	 *            longitude in degrees [-180000000 to 180000000]
	 * @param aZoom
	 *            zoom level
	 * @param aReuse
	 * @return Point with x,y in the range [-2^(zoom-1) to 2^(zoom-1)]
	 */
	public static Point projectGeoPoint(final int aLatE6, final int aLonE6, final int aZoom,
			final Point aReuse) {
		return projectGeoPoint(aLatE6 * 1E-6, aLonE6 * 1E-6, aZoom, aReuse);
	}

	/**
	 * Mercator projection of GeoPoint at given zoom level
	 *
	 * @param aGeoPoint
	 * @param aZoom
	 *            zoom level
	 * @param aReuse
	 * @return Point with x,y in the range [-2^(zoom-1) to 2^(zoom-1)]
	 */
	public static Point projectGeoPoint(final IGeoPoint aGeoPoint, final int aZoom,
			final Point aReuse) {
		return projectGeoPoint(aGeoPoint.getLatitudeE6() * 1E-6, aGeoPoint.getLongitudeE6() * 1E-6,
				aZoom, aReuse);
	}

	/**
	 * Mercator projection of GeoPoint at given zoom level
	 *
	 * @param aLat
	 *            latitude in degrees [-89 to 89]
	 * @param aLon
	 *            longitude in degrees [-180 to 180]
	 * @param aZoom
	 *            zoom level
	 * @param aReuse
	 * @return Point with x,y in the range [-2^(zoom-1) to 2^(zoom-1)]
	 */
	public static Point projectGeoPoint(final double aLat, final double aLon, final int aZoom,
			final Point aReuse) {
		final Point p = aReuse != null ? aReuse : new Point(0, 0);

		p.x = ((int) Math.floor((aLon + 180) / 360 * (1 << aZoom)));
		p.y = ((int) Math.floor((1 - Math.log(Math.tan(aLat * DEG2RAD) + 1
				/ Math.cos(aLat * DEG2RAD))
				/ Math.PI)
				/ 2 * (1 << aZoom)));

		return p;
	}

	/**
	 * Get bounding box from reverse Mercator projection.
	 *
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param zoom
	 * @return
	 */
	public static BoundingBoxE6 getBoundingBoxFromCoords(final int left, final int top,
			final int right, final int bottom, final int zoom) {
		return new BoundingBoxE6(tile2lat(top, zoom), tile2lon(right, zoom),
				tile2lat(bottom, zoom), tile2lon(left, zoom));
	}

	/**
	 * Get bounding box from reverse Mercator projection.
	 *
	 * @param aMapTile
	 * @param aZoom
	 * @return
	 */
	public static BoundingBoxE6 getBoundingBoxFromPointInMapTile(final Point aMapTile,
			final int aZoom) {
		return new BoundingBoxE6(tile2lat(aMapTile.y, aZoom), tile2lon(aMapTile.x + 1, aZoom),
				tile2lat(aMapTile.y + 1, aZoom), tile2lon(aMapTile.x, aZoom));
	}

	/**
	 * Reverse Mercator projection of Point at given zoom level
	 *
	 */
	public static GeoPoint projectPoint(final int x, final int y, final int aZoom) {
		return new GeoPoint((int) (tile2lat(y, aZoom) * 1E6), (int) (tile2lon(x, aZoom) * 1E6));
	}

	public static double tile2lon(final int x, final int aZoom) {
		return (double) x / (1 << aZoom) * 360.0 - 180;
	}

	public static double tile2lat(final int y, final int aZoom) {
		final double n = Math.PI - 2.0 * Math.PI * y / (1 << aZoom);
		return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

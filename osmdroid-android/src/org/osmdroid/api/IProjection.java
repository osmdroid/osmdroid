package org.osmdroid.api;

import org.osmdroid.views.MapView.Projection;

import android.graphics.Point;

/**
 * An interface that resembles the Google Maps API Projection interface and is implemented by the
 * osmdroid {@link Projection} class.
 *
 * @author Neil Boyd
 *
 */
public interface IProjection {

	/**
	 * Converts the given GeoPoint to onscreen pixel coordinates, relative to the top-left of the
	 * MapView that provided this Projection.
	 *
	 * @param in
	 *            The latitude/longitude pair to convert.
	 * @param out
	 *            A pre-existing object to use for the output; if null, a new Point will be
	 *            allocated and returned.
	 * @return
	 */
	Point toPixels(IGeoPoint in, Point out);

	/**
	 * Create a new GeoPoint from pixel coordinates relative to the top-left of the MapView that
	 * provided this PixelConverter.
	 */
	IGeoPoint fromPixels(int x, int y);

	/**
	 * Converts a distance in meters (along the equator) to one in (horizontal) pixels at the
	 * current zoomlevel. In the default Mercator projection, the actual number of pixels for a
	 * given distance will get higher as you move away from the equator.
	 *
	 * @param meters
	 *            the distance in meters
	 * @return The number of pixels corresponding to the distance, if measured along the equator, at
	 *         the current zoom level. The return value may only be approximate.
	 */
	float metersToEquatorPixels(float meters);

}

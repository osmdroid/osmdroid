package org.osmdroid.api;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView.Projection;

import android.graphics.Point;

/**
 * An interface that resembles the Google Maps API Projection interface
 * and is implemented by the osmdroid {@link Projection} class.
 *
 * @author Neil Boyd
 *
 */
public interface IProjection {

	Point toPixels(GeoPoint in, android.graphics.Point out);
	GeoPoint fromPixels(int x, int y);
	float metersToEquatorPixels(float meters);

}

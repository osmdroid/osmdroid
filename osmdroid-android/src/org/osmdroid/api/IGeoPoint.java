package org.osmdroid.api;

import org.osmdroid.util.GeoPoint;

/**
 * An interface that resembles the Google Maps API GeoPoint class
 * and is implemented by the osmdroid {@link GeoPoint} class.
 *
 * @author Neil Boyd
 *
 */
public interface IGeoPoint {
	int getLatitudeE6();
	int getLongitudeE6();
}

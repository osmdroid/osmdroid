package org.osmdroid.api;

/**
 * An interface that resembles the Google Maps API GeoPoint class.
 */
public interface IGeoPoint {
	int getLatitudeE6();
	int getLongitudeE6();
	double getLatitude();
	double getLongitude();
}

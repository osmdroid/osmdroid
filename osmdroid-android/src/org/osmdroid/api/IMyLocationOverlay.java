package org.osmdroid.api;

import android.location.Location;
import android.os.Bundle;

public interface IMyLocationOverlay {

	boolean enableMyLocation();
	void disableMyLocation();
	boolean	isMyLocationEnabled();

	boolean enableCompass();
	void disableCompass();
	boolean	isCompassEnabled() ;

	boolean	runOnFirstFix(Runnable runnable);

	void onStatusChanged(String provider, int status, Bundle extras);

	Location getLastFix();

	/*
	 * In order to add the following methods to this interface
	 * we'd need to make the Google MyLocationOverlay a wrapper
	 * instead of just extending
	 */
	// GeoPoint getMyLocation();
	// boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView);

}

package org.osmdroid.api;

import org.osmdroid.views.overlay.MyLocationOverlay;

import android.location.Location;
import android.os.Bundle;

/**
 * An interface that resembles the Google Maps API MyLocationOverlay class
 * and is implemented by the osmdroid {@link MyLocationOverlay} class.
 *
 * @author Neil Boyd
 *
 */
public interface IMyLocationOverlay {

	boolean enableMyLocation();
	void disableMyLocation();
	boolean	isMyLocationEnabled();

	boolean enableCompass();
	void disableCompass();
	boolean	isCompassEnabled() ;

	public float getOrientation();

	boolean	runOnFirstFix(Runnable runnable);

	void onStatusChanged(String provider, int status, Bundle extras);

	Location getLastFix();

}

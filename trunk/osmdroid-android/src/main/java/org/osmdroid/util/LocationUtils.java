package org.osmdroid.util;

import org.osmdroid.util.constants.UtilConstants;

import android.location.Location;
import android.location.LocationManager;

public class LocationUtils implements UtilConstants {

	/**
	 * This is a utility class with only static members.
	 */
	private LocationUtils() {
	}

	/**
	 * Get the most recent location from the GPS or Network provider.
	 * @param pLocationManager
	 * @return return the most recent location, or null if there's no known location
	 */
	public static Location getLastKnownLocation(final LocationManager pLocationManager) {
		if (pLocationManager == null) {
			return null;
		}
		final Location gpsLocation =
			pLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		final Location networkLocation =
			pLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (gpsLocation == null) {
			return networkLocation;
		} else if (networkLocation == null) {
			return gpsLocation;
		} else {
			// both are non-null - use the most recent
			if (networkLocation.getTime() > gpsLocation.getTime() + GPS_WAIT_TIME) {
				return networkLocation;
			} else {
				return gpsLocation;
			}
		}
	}

}

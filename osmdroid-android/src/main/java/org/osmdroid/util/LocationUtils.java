package org.osmdroid.util;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.config.Configuration;

public class LocationUtils {

    /**
     * This is a utility class with only static members.
     */
    private LocationUtils() {
    }

    /**
     * Get the most recent location from the GPS or Network provider.
     *
     * @return return the most recent location, or null if there's no known location
     */
    public static Location getLastKnownLocation(@Nullable final LocationManager pLocationManager) {
        if (pLocationManager == null) {
            return null;
        }
        final Location gpsLocation = getLastKnownLocation(pLocationManager, LocationManager.GPS_PROVIDER);
        final Location networkLocation = getLastKnownLocation(pLocationManager, LocationManager.NETWORK_PROVIDER);
        if (gpsLocation == null) {
            return networkLocation;
        } else if (networkLocation == null) {
            return gpsLocation;
        } else {
            // both are non-null - use the most recent
            if (networkLocation.getTime() > gpsLocation.getTime() + Configuration.getInstance().getGpsWaitTime()) {
                return networkLocation;
            } else {
                return gpsLocation;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private static Location getLastKnownLocation(@NonNull final LocationManager pLocationManager, @NonNull final String pProvider) {
        try {
            if (!pLocationManager.isProviderEnabled(pProvider)) {
                return null;
            }
        } catch (final IllegalArgumentException e) {
            return null;
        }
        return pLocationManager.getLastKnownLocation(pProvider);
    }

}

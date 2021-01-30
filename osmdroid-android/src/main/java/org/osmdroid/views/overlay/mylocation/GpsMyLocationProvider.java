package org.osmdroid.views.overlay.mylocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.NetworkLocationIgnorer;

import java.util.HashSet;
import java.util.Set;

/**
 * location provider, by default, uses {@link LocationManager#GPS_PROVIDER} and {@link LocationManager#NETWORK_PROVIDER}
 */
public class GpsMyLocationProvider implements IMyLocationProvider, LocationListener {
    private LocationManager mLocationManager;
    private Location mLocation;

    private IMyLocationConsumer mMyLocationConsumer;
    private long mLocationUpdateMinTime = 0;
    private float mLocationUpdateMinDistance = 0.0f;
    private NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
    private final Set<String> locationSources = new HashSet<>();

    public GpsMyLocationProvider(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationSources.add(LocationManager.GPS_PROVIDER);
        locationSources.add(LocationManager.NETWORK_PROVIDER);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * removes all sources, again, only useful before startLocationProvider is called
     */
    public void clearLocationSources() {
        locationSources.clear();
    }

    /**
     * adds a new source to listen for location data. Has no effect after startLocationProvider has been called
     * unless startLocationProvider is called again
     */
    public void addLocationSource(String source) {
        locationSources.add(source);
    }

    /**
     * returns the live list of GPS sources that we accept, changing this list after startLocationProvider
     * has no effect unless startLocationProvider is called again
     *
     * @return
     */
    public Set<String> getLocationSources() {
        return locationSources;
    }

    public long getLocationUpdateMinTime() {
        return mLocationUpdateMinTime;
    }

    /**
     * Set the minimum interval for location updates. See
     * {@link LocationManager#requestLocationUpdates(String, long, float, LocationListener)}. Note
     * that you should call this before calling {@link MyLocationNewOverlay#enableMyLocation()}.
     *
     * @param milliSeconds
     */
    public void setLocationUpdateMinTime(final long milliSeconds) {
        mLocationUpdateMinTime = milliSeconds;
    }

    public float getLocationUpdateMinDistance() {
        return mLocationUpdateMinDistance;
    }

    /**
     * Set the minimum distance for location updates. See
     * {@link LocationManager#requestLocationUpdates(String, long, float, LocationListener)}. Note
     * that you should call this before calling {@link MyLocationNewOverlay#enableMyLocation()}.
     *
     * @param meters
     */
    public void setLocationUpdateMinDistance(final float meters) {
        mLocationUpdateMinDistance = meters;
    }

    //
    // IMyLocationProvider
    //

    /**
     * Enable location updates and show your current location on the map. By default this will
     * request location updates as frequently as possible, but you can change the frequency and/or
     * distance by calling {@link #setLocationUpdateMinTime} and/or {@link
     * #setLocationUpdateMinDistance} before calling this method.
     */
    @SuppressLint("MissingPermission")
    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        mMyLocationConsumer = myLocationConsumer;
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (locationSources.contains(provider)) {

                try {
                    mLocationManager.requestLocationUpdates(provider, mLocationUpdateMinTime,
                            mLocationUpdateMinDistance, this);
                    result = true;
                } catch (Throwable ex) {
                    Log.e(IMapView.LOGTAG, "Unable to attach listener for location provider " + provider + " check permissions?", ex);
                }
            }
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopLocationProvider() {
        mMyLocationConsumer = null;
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(this);
            } catch (Throwable ex) {
                Log.w(IMapView.LOGTAG, "Unable to deattach location listener", ex);
            }
        }
    }

    @Override
    public Location getLastKnownLocation() {
        return mLocation;
    }

    @Override
    public void destroy() {
        stopLocationProvider();
        mLocation = null;
        mLocationManager = null;
        mMyLocationConsumer = null;
        mIgnorer = null;
    }

    //
    // LocationListener
    //

    @Override
    public void onLocationChanged(final Location location) {
        if (mIgnorer == null) {
            Log.w(IMapView.LOGTAG, "GpsMyLocation provider, mIgnore is null, unexpected. Location update will be ignored");
            return;
        }
        if (location == null || location.getProvider() == null)
            return;
        // ignore temporary non-gps fix
        if (mIgnorer.shouldIgnore(location.getProvider(), System.currentTimeMillis()))
            return;

        mLocation = location;
        if (mMyLocationConsumer != null && mLocation != null)
            mMyLocationConsumer.onLocationChanged(mLocation, this);
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
    }
}

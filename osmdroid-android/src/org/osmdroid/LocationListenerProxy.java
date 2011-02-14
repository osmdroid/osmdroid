package org.osmdroid;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationListenerProxy implements LocationListener
{
	private final LocationManager mLocationManager;
	private LocationListener mListener = null;

	public LocationListenerProxy(final LocationManager pLocationManager) {
		mLocationManager = pLocationManager;
	}

	public void startListening(final LocationListener pListener,
			final long pUpdateTime, final float pUpdateDistance) {
		mListener = pListener;
		mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, pUpdateTime, pUpdateDistance, this);
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, pUpdateTime, pUpdateDistance, this);
	}

	public void stopListening() {
		mListener = null;
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(final Location arg0) {
		if( mListener != null ) {
			mListener.onLocationChanged(arg0);
		}
	}

	@Override
	public void onProviderDisabled(final String arg0) {
		if( mListener != null ) {
			mListener.onProviderDisabled(arg0);
		}
	}

	@Override
	public void onProviderEnabled(final String arg0) {
		if( mListener != null ) {
			mListener.onProviderEnabled(arg0);
		}
	}

	@Override
	public void onStatusChanged(final String arg0, final int arg1, final Bundle arg2) {
		if( mListener != null ) {
			mListener.onStatusChanged(arg0, arg1, arg2);
		}
	}
}

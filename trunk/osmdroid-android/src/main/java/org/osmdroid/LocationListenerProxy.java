package org.osmdroid;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationListenerProxy implements LocationListener {
	private final LocationManager mLocationManager;
	private LocationListener mListener = null;

	public LocationListenerProxy(final LocationManager pLocationManager) {
		mLocationManager = pLocationManager;
	}

	public boolean startListening(final LocationListener pListener, final long pUpdateTime,
			final float pUpdateDistance) {
		boolean result = false;
		mListener = pListener;
		for (final String provider : mLocationManager.getProviders(true)) {
			if (LocationManager.GPS_PROVIDER.equals(provider)
					|| LocationManager.NETWORK_PROVIDER.equals(provider)) {
				result = true;
				mLocationManager.requestLocationUpdates(provider, pUpdateTime, pUpdateDistance,
						this);
			}
		}
		return result;
	}

	public void stopListening() {
		mListener = null;
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(final Location arg0) {
		if (mListener != null) {
			mListener.onLocationChanged(arg0);
		}
	}

	@Override
	public void onProviderDisabled(final String arg0) {
		if (mListener != null) {
			mListener.onProviderDisabled(arg0);
		}
	}

	@Override
	public void onProviderEnabled(final String arg0) {
		if (mListener != null) {
			mListener.onProviderEnabled(arg0);
		}
	}

	@Override
	public void onStatusChanged(final String arg0, final int arg1, final Bundle arg2) {
		if (mListener != null) {
			mListener.onStatusChanged(arg0, arg1, arg2);
		}
	}
}

package org.osmdroid.google.wrapper.v2;

import com.google.android.gms.maps.model.LatLng;
import org.osmdroid.api.IGeoPoint;

public class GeoPoint implements IGeoPoint {

	private final LatLng mLatLng;

	public GeoPoint(final LatLng aLatLng) {
		mLatLng = aLatLng;
	}

	@Override
	public int getLatitudeE6() {
		return (int) (mLatLng.latitude * 1E6);
	}

	@Override
	public int getLongitudeE6() {
		return (int) (mLatLng.longitude * 1E6);
	}

	@Override
	public double getLatitude() {
		return mLatLng.latitude;
	}

	@Override
	public double getLongitude() {
		return mLatLng.longitude;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final GeoPoint rhs = (GeoPoint) obj;
		return rhs.mLatLng.equals(this.mLatLng);
	}
}

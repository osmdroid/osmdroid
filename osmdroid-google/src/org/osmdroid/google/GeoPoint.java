package org.osmdroid.google;

import org.osmdroid.api.IGeoPoint;

/**
 * A wrapper for the Google {@link com.google.android.maps.GeoPoint} class.
 * This implements {@link IGeoPoint}, which is also implemented by the osmdroid
 * {@link org.osmdroid.util.GeoPoint}.
 *
 * @author Neil Boyd
 *
 */
public class GeoPoint implements IGeoPoint {

	private final com.google.android.maps.GeoPoint mGeoPoint;

	public GeoPoint(final com.google.android.maps.GeoPoint pGeoPoint) {
		mGeoPoint = pGeoPoint;
	}

	@Override
	public int getLatitudeE6() {
		return mGeoPoint.getLatitudeE6();
	}

	@Override
	public int getLongitudeE6() {
		return mGeoPoint.getLongitudeE6();
	}

}

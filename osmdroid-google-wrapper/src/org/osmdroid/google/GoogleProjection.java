package org.osmdroid.google;

import org.osmdroid.api.IProjection;
import org.osmdroid.util.GeoPoint;

import android.graphics.Point;

import com.google.android.maps.Projection;

public class GoogleProjection implements IProjection {

	private final Projection mProjection;

	public GoogleProjection(final Projection pProjection) {
		mProjection = pProjection;
	}

	@Override
	public Point toPixels(final GeoPoint in, final Point out) {
		final com.google.android.maps.GeoPoint googleGeoPoint =
			new com.google.android.maps.GeoPoint(in.getLatitudeE6(), in.getLongitudeE6());
		return mProjection.toPixels(googleGeoPoint, out);
	}

	@Override
	public GeoPoint fromPixels(final int x, final int y) {
		final com.google.android.maps.GeoPoint googleGeoPoint = mProjection.fromPixels(x, y);
		return new GeoPoint(googleGeoPoint.getLatitudeE6(), googleGeoPoint.getLongitudeE6());
	}

	@Override
	public float metersToEquatorPixels(final float meters) {
		return mProjection.metersToEquatorPixels(meters);
	}
}

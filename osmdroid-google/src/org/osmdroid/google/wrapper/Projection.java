package org.osmdroid.google.wrapper;

import org.osmdroid.api.IProjection;
import org.osmdroid.util.GeoPoint;

import android.graphics.Point;

/**
 * A wrapper for the Google {@link com.google.android.maps.Projection} implementation.
 * This implements {@link IProjection}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.MapView.Projection}.
 *
 * @author Neil Boyd
 *
 */
public class Projection implements IProjection {

	private final com.google.android.maps.Projection mProjection;

	public Projection(final com.google.android.maps.Projection pProjection) {
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

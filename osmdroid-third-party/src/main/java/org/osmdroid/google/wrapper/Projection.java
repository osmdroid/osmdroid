package org.osmdroid.google.wrapper;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;

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
	private final int mWidth;
	private final int mHeight;

	public Projection(final com.google.android.maps.MapView aMapView) {
		mProjection = aMapView.getProjection();
		mWidth = aMapView.getWidth();
		mHeight = aMapView.getHeight();
	}

	@Override
	public Point toPixels(final IGeoPoint in, final Point out) {
		final com.google.android.maps.GeoPoint googleGeoPoint =
			new com.google.android.maps.GeoPoint(in.getLatitudeE6(), in.getLongitudeE6());
		return mProjection.toPixels(googleGeoPoint, out);
	}

	@Override
	public IGeoPoint fromPixels(final int x, final int y) {
		final com.google.android.maps.GeoPoint googleGeoPoint = mProjection.fromPixels(x, y);
		return new GeoPoint(googleGeoPoint);
	}

	@Override
	public float metersToEquatorPixels(final float meters) {
		return mProjection.metersToEquatorPixels(meters);
	}

	@Override
	public IGeoPoint getNorthEast() {
		return fromPixels(mWidth, 0);
	}

	@Override
	public IGeoPoint getSouthWest() {
		return fromPixels(0, mHeight);
	}
}

package org.osmdroid.google;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.api.IProjection;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A wrapper for the Google {@link com.google.android.maps.MapView} class.
 * This implements {@link IMapView}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.MapView}.
 *
 * @author Neil Boyd
 *
 */
public class MapView implements IMapView {

	private final com.google.android.maps.MapView mMapView;

	public MapView(final com.google.android.maps.MapView pMapView) {
		mMapView = pMapView;
	}

	public MapView(final Context pContext, final AttributeSet pAttrs, final int pDefStyle) {
		this(new com.google.android.maps.MapView(pContext, pAttrs, pDefStyle));
	}

	public MapView(final Context pContext, final AttributeSet pAttrs) {
		this(new com.google.android.maps.MapView(pContext, pAttrs));
	}

	public MapView(final Context pContext, final String pApiKey) {
		this(new com.google.android.maps.MapView(pContext, pApiKey));
	}

	@Override
	public IMapController getController() {
		return new MapController(mMapView.getController());
	}

	@Override
	public IProjection getProjection() {
		return new Projection(mMapView.getProjection());
	}

	@Override
	public int getZoomLevel() {
		return mMapView.getZoomLevel();
	}

	@Override
	public int getLatitudeSpan() {
		return mMapView.getLatitudeSpan();
	}

	@Override
	public int getLongitudeSpan() {
		return mMapView.getLongitudeSpan();
	}

	@Override
	public IGeoPoint getMapCenter() {
		return new GeoPoint(mMapView.getMapCenter());
	}

	@Override
	public int getMaxZoomLevel() {
		return mMapView.getMaxZoomLevel();
	}

	@Override
	public void setBackgroundColor(final int pColor) {
		// this doesn't seem to have any visible effect on the Google MapView
		mMapView.setBackgroundColor(pColor);
	}

}

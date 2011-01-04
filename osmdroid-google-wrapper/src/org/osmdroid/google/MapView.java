package org.osmdroid.google;

import org.osmdroid.api.IMapView;
import org.osmdroid.api.IProjection;

import android.content.Context;
import android.util.AttributeSet;

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
	public IProjection getProjection() {
		return new Projection(mMapView.getProjection());
	}

	@Override
	public int getZoomLevel() {
		return mMapView.getZoomLevel();
	}

}

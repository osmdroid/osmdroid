package org.osmdroid.google;

import org.osmdroid.api.IMapView;
import org.osmdroid.api.IProjection;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.maps.MapView;

public class GoogleMapView implements IMapView {

	private final MapView mMapView;

	public GoogleMapView(final MapView pMapView) {
		mMapView = pMapView;
	}

	public static IMapView getInstance(final Context pContext, final AttributeSet pAttrs, final int pDefStyle) {
		return new GoogleMapView(new MapView(pContext, pAttrs, pDefStyle));
	}

	public static IMapView getInstance(final Context pContext, final AttributeSet pAttrs) {
		return new GoogleMapView(new MapView(pContext, pAttrs));
	}

	public static IMapView getInstance(final Context pContext, final String pApiKey) {
		return new GoogleMapView(new MapView(pContext, pApiKey));
	}

	@Override
	public IProjection getProjection() {
		return new GoogleProjection(mMapView.getProjection());
	}

}

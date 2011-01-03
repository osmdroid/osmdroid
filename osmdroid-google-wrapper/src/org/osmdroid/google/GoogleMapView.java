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

	@Override
	public IProjection getProjection() {
		return new GoogleProjection(mMapView.getProjection());
	}

}

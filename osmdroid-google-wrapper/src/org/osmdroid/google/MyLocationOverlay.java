package org.osmdroid.google;

import org.osmdroid.api.IMyLocationOverlay;

import android.content.Context;

import com.google.android.maps.MapView;

public class MyLocationOverlay
extends com.google.android.maps.MyLocationOverlay
implements IMyLocationOverlay {

	public MyLocationOverlay(final Context pContext, final MapView pMapView) {
		super(pContext, pMapView);
	}
}

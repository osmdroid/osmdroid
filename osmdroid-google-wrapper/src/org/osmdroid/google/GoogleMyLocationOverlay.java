package org.osmdroid.google;

import org.osmdroid.api.IMyLocationOverlay;

import android.content.Context;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class GoogleMyLocationOverlay extends MyLocationOverlay implements IMyLocationOverlay {

	public GoogleMyLocationOverlay(final Context pContext, final MapView pMapView) {
		super(pContext, pMapView);
	}

}

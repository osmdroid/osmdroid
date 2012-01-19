package org.osmdroid.google.wrapper;

import org.osmdroid.api.IMyLocationOverlay;

import android.content.Context;

import com.google.android.maps.MapView;

/**
 * A wrapper for the Google {@link com.google.android.maps.MyLocationOverlay} class.
 * This implements {@link IMyLocationOverlay}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.overlay.MyLocationOverlay}.
 *
 * @author Neil Boyd
 *
 */
public class MyLocationOverlay
extends com.google.android.maps.MyLocationOverlay
implements IMyLocationOverlay {

	public MyLocationOverlay(final Context pContext, final MapView pMapView) {
		super(pContext, pMapView);
	}
}

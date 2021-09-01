package org.osmdroid.google.wrapper;

import android.content.Context;

import com.google.android.maps.MapView;

import org.osmdroid.api.IMyLocationOverlay;

/**
 * A wrapper for the Google {@link com.google.android.maps.MyLocationOverlay} class.
 * This implements {@link IMyLocationOverlay}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.overlay.MyLocationOverlay}.
 *
 * @author Neil Boyd
 */
@Deprecated
public class MyLocationOverlay
        extends com.google.android.maps.MyLocationOverlay
        implements IMyLocationOverlay {

    public MyLocationOverlay(final Context pContext, final MapView pMapView) {
        super(pContext, pMapView);
    }
}

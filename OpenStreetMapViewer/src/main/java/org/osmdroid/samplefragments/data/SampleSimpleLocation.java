package org.osmdroid.samplefragments.data;

import android.graphics.drawable.BitmapDrawable;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;

/**
 * see https://github.com/osmdroid/osmdroid/issues/477
 * <p>
 * Created by alex on 11/23/2016.
 */

public class SampleSimpleLocation extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Simple Location Overlay (marker)";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.icon);

        SimpleLocationOverlay layer = new SimpleLocationOverlay(drawable.getBitmap());
        layer.setLocation(new GeoPoint(38.8976763, -77.0365298));
        mMapView.getOverlayManager().add(layer);
    }
}

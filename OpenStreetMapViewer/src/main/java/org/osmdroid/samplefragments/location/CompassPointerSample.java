package org.osmdroid.samplefragments.location;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.compass.CompassOverlay;

/**
 * created on 1/29/2018.
 *
 * @author Alex O'Ree
 */

public class CompassPointerSample extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Compass Pointer";
    }


    @Override
    public void addOverlays() {
        super.addOverlays();
        CompassOverlay overlay = new CompassOverlay(getContext(), mMapView);
        overlay.setPointerMode(true);
        overlay.enableCompass();
        mMapView.getOverlayManager().add(overlay);
        mMapView.invalidate();
    }

    //NOTE This sample uses sensors, see base class for lifecycle management
}

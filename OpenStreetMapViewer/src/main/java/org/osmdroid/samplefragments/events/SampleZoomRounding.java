package org.osmdroid.samplefragments.events;

import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * @author Fabrice Fontaine
 * @since 6.0.2
 * cf. https://github.com/osmdroid/osmdroid/issues/944
 */
public class SampleZoomRounding extends BaseSampleFragment {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String TITLE = "Zoom Rounding";

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        mMapView.setZoomRounding(true);
    }
}

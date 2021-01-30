package org.osmdroid.samplefragments.drawing;

import android.view.View;

/**
 * Demos turning off map repeating
 * Map replication is OFF for this sample (only viewable for numerically lower zoom levels (higher altitude))
 *
 * @since 6.0.0
 * Created by Maradox on 11/26/17.
 */

public class SampleDrawPolylineWithoutWrapping extends SampleDrawPolyline implements View.OnClickListener {

    @Override
    public String getSampleTitle() {
        return "Draw a polyline on screen without wrapping";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        this.mMapView.setHorizontalMapRepetitionEnabled(false);
        this.mMapView.setVerticalMapRepetitionEnabled(false);
    }

}

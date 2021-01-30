package org.osmdroid.samplefragments.drawing;

/**
 * created on 11/28/2017.
 * https://github.com/osmdroid/osmdroid/issues/791
 *
 * @author Alex O'Ree
 */

public class DrawPolylineWithArrows extends SampleDrawPolyline {

    @Override
    public String getSampleTitle() {
        return "Draw a polyline with arrows";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        paint.withArrows = true;
    }
}

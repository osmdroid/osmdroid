package org.osmdroid.samplefragments.drawing;

/**
 * A simple sample to plot markers with a long press. It's a bit of noise this in the class
 * that is used to help the osmdroid devs troubleshoot things.
 *
 * Map replication is OFF for this sample (only viewable for numerically lower zoom levels (higher altitude))
 *
 * created on 11/19/2017.
 * @since 6.0.0
 * @author Alex O'Ree
 */

public class PressToPlotWithoutWrapping extends PressToPlot{
    @Override
    public String getSampleTitle() {
        return "Long Press to Plot Marker without wrapping";
    }
    @Override
    public void addOverlays() {
        super.addOverlays();
        this.mMapView.setMapRepetitionEnabled(false);
    }

}

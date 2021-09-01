package org.osmdroid.samplefragments.drawing;

/**
 * A simple sample to plot markers with a long press. It's a bit of noise this in the class
 * that is used to help the osmdroid devs troubleshoot things.
 * <p>
 * Map replication is OFF for this sample (only viewable for numerically lower zoom levels (higher altitude))
 *
 * <b>Note</b></b: when plotting a point off the map, the conversion from
 * screen coordinates to map coordinates will return values that are invalid from a latitude,longitude
 * perspective. Sometimes this is a wanted behavior and sometimes it isn't. We are leaving it up to you,
 * the developer using osmdroid to decide on what is right for your application. See
 * <a href="https://github.com/osmdroid/osmdroid/pull/722">https://github.com/osmdroid/osmdroid/pull/722</a>
 * for more information and the discussion associated with this.
 * <p>
 * created on 11/19/2017.
 *
 * @author Alex O'Ree
 * @since 6.0.0
 */

public class PressToPlotWithoutWrapping extends PressToPlot {
    @Override
    public String getSampleTitle() {
        return "Long Press to Plot Marker without wrapping";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        this.mMapView.setHorizontalMapRepetitionEnabled(false);
        this.mMapView.setVerticalMapRepetitionEnabled(false);
    }

}

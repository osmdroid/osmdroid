package org.osmdroid.samplefragments.data;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;

/**
 * An example on how to use the lat/lon gridline overlay.
 * <p>
 * basically, listen for map motion/zoom events and remove the old overlay, then add the new one.
 * you can also override the color scheme and font sizes for the labels and lines
 */
public class SampleGridlines extends BaseSampleFragment {


    @Override
    public String getSampleTitle() {
        return "Lat/Lon Gridlines";
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        mMapView.getController().setCenter(new GeoPoint(0d, 0d));
        mMapView.getController().setZoom(5);
        mMapView.setTilesScaledToDpi(true);

        mMapView.getController().setZoom(3);

        LatLonGridlineOverlay2 grids = new LatLonGridlineOverlay2();
        mMapView.getOverlayManager().add(grids);

    }

}

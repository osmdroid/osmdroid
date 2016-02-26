package org.osmdroid.samplefragments;

import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay;

/**
 * An example on how to use the lat/lon gridline overlay.
 *
 * basically, listen for map motion/zoom events and remove the old overlay, then add the new one.
 * you can also override the color scheme and font sizes for the labels and lines
 */
public class SampleGridlines extends BaseSampleFragment implements MapListener {

    FolderOverlay activeLatLonGrid;

    @Override
    public String getSampleTitle() {
        return "Lat/Lon Gridlines";
    }

    @Override
    protected void addOverlays() {
        mMapView.setTilesScaledToDpi(true);
        mMapView.setMapListener(this);
        mMapView.getController().setZoom(3);
        updateGridlines();
    }

    @Override
    public boolean onScroll(ScrollEvent scrollEvent) {
        updateGridlines();
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent zoomEvent) {
        updateGridlines();
        return false;
    }

    private void updateGridlines(){

        if (activeLatLonGrid != null)
            mMapView.getOverlays().remove(activeLatLonGrid);
        activeLatLonGrid = LatLonGridlineOverlay.getLatLonGrid(getActivity(), mMapView);
        mMapView.getOverlays().add(activeLatLonGrid);

    }

}

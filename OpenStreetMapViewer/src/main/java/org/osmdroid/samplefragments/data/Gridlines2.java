package org.osmdroid.samplefragments.data;

import android.graphics.Color;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay;

/**
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class Gridlines2 extends SampleGridlines {

    @Override
    public String getSampleTitle() {
        return "Lat/Lon Gridlines (customized)";
    }

    @Override
    protected void addOverlays() {
       super.addOverlays();
        mMapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        LatLonGridlineOverlay.fontSizeDp=14;
        LatLonGridlineOverlay.fontColor= Color.argb(255,0,255,0);
        LatLonGridlineOverlay.backgroundColor=Color.BLACK;
        LatLonGridlineOverlay.lineColor=LatLonGridlineOverlay.fontColor;
        updateGridlines();
    }
}

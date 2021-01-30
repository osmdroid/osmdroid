package org.osmdroid.samplefragments.data;

import android.graphics.Color;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;

/**
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class Gridlines2 extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Lat/Lon Gridlines (customized)";
    }

    @Override
    protected void addOverlays() {

        mMapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        LatLonGridlineOverlay2 grids = new LatLonGridlineOverlay2();
        grids.setBackgroundColor(Color.BLACK);
        grids.setFontColor(Color.RED);
        grids.setLineColor(Color.RED);
        grids.setFontSizeDp((short) 14);
        mMapView.getOverlayManager().add(grids);

    }
}

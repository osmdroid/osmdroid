package org.osmdroid.samplefragments.tilesources;

import android.graphics.Color;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.TilesOverlay;

/**
 * sample fragment to show invert tiles, aka night mode and to set the tile loading background colors
 *
 * @author alex
 */
public class SampleInvertedTiles_NightMode extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Inverted Tiles";
    }

    @Override
    public void addOverlays() {
        this.mMapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        this.mMapView.getOverlayManager().getTilesOverlay().setLoadingBackgroundColor(android.R.color.black);
        this.mMapView.getOverlayManager().getTilesOverlay().setLoadingLineColor(Color.argb(255, 0, 255, 0));
    }

}

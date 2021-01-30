package org.osmdroid.samplefragments.tilesources;

import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * Simple how to for setting a custom tile source
 *
 * @author alex
 */
public class SampleCustomTileSource extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Custom Tile Source";

    }

    @Override
    public void addOverlays() {
        mMapView.setTileSource(new USGSTileSource());

    }

}

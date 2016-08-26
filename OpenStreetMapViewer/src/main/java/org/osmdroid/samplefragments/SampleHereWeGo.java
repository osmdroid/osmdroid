package org.osmdroid.samplefragments;

import org.osmdroid.tileprovider.tilesource.HEREWeGoTileSource;

/**
 * Created by alex on 8/11/16.
 */

public class SampleHereWeGo  extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "HERE WeGo map tiles";
    }

    @Override
    public void addOverlays(){
        super.addOverlays();
        mMapView.setTileSource(new HEREWeGoTileSource(getContext()));
    }
}

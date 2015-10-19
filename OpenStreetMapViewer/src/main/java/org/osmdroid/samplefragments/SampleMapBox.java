package org.osmdroid.samplefragments;

import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;

/**
 * Created by alex on 10/18/15.
 */
public class SampleMapBox   extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "MapBox";
    }

    @Override
    public void addOverlays() {
        MapBoxTileSource b=new MapBoxTileSource("MapBox",0,19,256, ".png");
        this.mMapView.setTileSource(b);
    }

}

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

        //this bit gets the key from the manifest
       
        MapBoxTileSource b=new MapBoxTileSource("MapBox",0,19,256, ".png");
        b.retrieveAccessToken(getContext());
        b.retrieveMapBoxMapId(getContext());
        //you can also programmtically set the token and map id here
        //b.setAccessToken("KEY");
        //b.setMapboxMapid("KEY");

        this.mMapView.setTileSource(b);
    }

}

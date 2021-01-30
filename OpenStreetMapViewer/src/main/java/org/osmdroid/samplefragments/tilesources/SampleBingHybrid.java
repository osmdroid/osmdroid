package org.osmdroid.samplefragments.tilesources;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;

/**
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class SampleBingHybrid extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Bing Aerial with Labels";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        //this gets the key from the manifest
        BingMapTileSource.retrieveBingKey(this.getContext());
        final BingMapTileSource source = new BingMapTileSource(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                source.initMetaData();
            }
        }).start();
        source.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
        mMapView.setTileSource(source);

    }
}

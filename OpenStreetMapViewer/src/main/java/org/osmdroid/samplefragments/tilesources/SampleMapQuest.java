package org.osmdroid.samplefragments.tilesources;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.MapQuestTileSource;

/**
 * Created by alex on 8/11/16.
 */

public class SampleMapQuest extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "MapQuest tile source";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        mMapView.setTileSource(new MapQuestTileSource(getContext()));
    }
}

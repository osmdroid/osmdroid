package org.osmdroid.samplefragments.tileproviders;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

/**
 * test for showing the map only once without wrapping
 * https://github.com/osmdroid/osmdroid/issues/183
 * Created by Maradox on 11/26/17.
 */
public class SampleAssetsOnlyWithoutWrapping extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Assets Only Without Wrapping";
    }

    @Override
    public void addOverlays() {
        this.mMapView.setMapRepetitionEnabled(false);
        this.mMapView.setScrollableAreaLimitToMapBounds();
        this.mMapView.setUseDataConnection(false);
        MapTileAssetsProvider prov = new MapTileAssetsProvider(new SimpleRegisterReceiver(getContext()  ), getActivity().getAssets());

        this.mMapView.setTileProvider(new MapTileProviderArray(TileSourceFactory.MAPNIK, new SimpleRegisterReceiver(getContext()), new MapTileModuleProviderBase[]{ prov }));
    }
}

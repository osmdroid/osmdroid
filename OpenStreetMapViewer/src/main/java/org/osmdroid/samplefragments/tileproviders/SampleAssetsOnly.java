package org.osmdroid.samplefragments.tileproviders;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

/**
 * test to force assets only loaded
 * https://github.com/osmdroid/osmdroid/issues/272
 * Created by alex on 2/21/16.
 */
public class SampleAssetsOnly extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Assets Only";
    }

    @Override
    public void addOverlays() {
        this.mMapView.setUseDataConnection(false);
        MapTileAssetsProvider prov = new MapTileAssetsProvider(new SimpleRegisterReceiver(getContext()), getActivity().getAssets());

        this.mMapView.setTileProvider(new MapTileProviderArray(TileSourceFactory.MAPNIK, new SimpleRegisterReceiver(getContext()), new MapTileModuleProviderBase[]{prov}));
    }
}

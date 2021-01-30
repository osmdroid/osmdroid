package org.osmdroid.samplefragments.tilesources;

import android.graphics.Color;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.TilesOverlay;

/**
 * This is another example of viewing multiple tile sources at the same time.
 * created on 12/13/2016.
 *
 * @author Alex O'Ree
 */

public class SampleOpenSeaMap extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Open Sea Map";
    }

    MapTileProviderBasic mProvider;

    @Override
    public void addOverlays() {
        super.addOverlays();
        mProvider = new MapTileProviderBasic(getContext());
        TilesOverlay seaMap = new TilesOverlay(mProvider, getContext());
        seaMap.setLoadingLineColor(Color.TRANSPARENT);
        seaMap.setLoadingBackgroundColor(Color.TRANSPARENT);
        seaMap.setLoadingDrawable(null);
        mProvider.setTileSource(TileSourceFactory.OPEN_SEAMAP);
        mMapView.getOverlays().add(seaMap);
        mMapView.postInvalidate();
        mMapView.getController().setCenter(new GeoPoint(40.65716, -74.06507));
        mMapView.getController().setZoom(18);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDetach();
        mMapView = null;
        if (mProvider != null)
            mProvider.detach();
        mProvider = null;
    }
}

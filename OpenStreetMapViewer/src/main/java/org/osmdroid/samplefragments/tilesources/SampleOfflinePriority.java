package org.osmdroid.samplefragments.tilesources;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Offline First and Offline Second demos
 * The typical difference is when you pan the map to places you've never been to.
 * In the Offline First demo, you'll see an approximation of the tile before the actual downloaded
 * In the Offline Second demo, you'll see a gray square before the actual downloaded
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
abstract public class SampleOfflinePriority extends BaseSampleFragment {

    private final GeoPoint mInitialCenter = new GeoPoint(41.8905495, 12.4924348); // Rome, Italy
    private final double mInitialZoomLevel = 5;

    protected abstract boolean isOfflineFirst();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final MapTileProviderBasic provider = new MapTileProviderBasic(getActivity());
        provider.setOfflineFirst(isOfflineFirst());
        mMapView = new MapView(inflater.getContext(), provider);
        return mMapView;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        mMapView.post(new Runnable() { // "post" because we need View.getWidth() to be set
            @Override
            public void run() {
                mMapView.getController().setZoom(mInitialZoomLevel);
                mMapView.setExpectedCenter(mInitialCenter);
            }
        });
    }

    @Override
    public String getSampleTitle() {
        return "Offline " + (isOfflineFirst() ? "First" : "Second");
    }
}

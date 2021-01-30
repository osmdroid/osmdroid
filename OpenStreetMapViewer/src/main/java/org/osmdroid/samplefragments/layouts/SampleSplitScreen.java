package org.osmdroid.samplefragments.layouts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.R;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;


/**
 * Uses OSM as the upper map and MapBox (API key required) as the lower map
 * Created by alex on 6/4/16.
 */
public class SampleSplitScreen extends BaseSampleFragment implements MapListener {
    @Override
    public String getSampleTitle() {
        return "Two maps, split screen with Mapbox";
    }

    protected MapView mMapView2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_splitscreen, container, false);

        mMapView = root.findViewById(R.id.mapview1);
        mMapView2 = root.findViewById(R.id.mapview2);

        return root;
    }

    @Override
    protected void addOverlays() {
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.getController().setZoom(1);
        mMapView.getController().setCenter(new GeoPoint(39.8282, 98.5795));
        mMapView2.getController().setZoom(1);
        mMapView2.getController().setCenter(new GeoPoint(39.8282, 98.5795));
        mMapView.setMapListener(this);
        //hey, check out the other constructors for mapbox, there's a few options to load up your
        //access token and tile set preferences
        mMapView2.setTileSource(new MapBoxTileSource(getContext()));
        mMapView2.setMapListener(this);

        mMapView2.setMultiTouchControls(true);
        mMapView2.setTilesScaledToDpi(true);
    }

    long lastEvent = 0;

    @Override
    public boolean onScroll(ScrollEvent event) {

        if (lastEvent + 40 < System.currentTimeMillis()) {
            lastEvent = System.currentTimeMillis();
            if (event.getSource() == mMapView) {
                mMapView2.getController().setCenter(mMapView.getMapCenter());
            } else {
                mMapView.getController().setCenter(mMapView2.getMapCenter());
            }

        }

        return true;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        if (lastEvent + 40 < System.currentTimeMillis()) {
            lastEvent = System.currentTimeMillis();
            if (event.getSource() == mMapView) {
                mMapView2.getController().setZoom(event.getZoomLevel());
            } else
                mMapView.getController().setZoom(event.getZoomLevel());
        }
        return true;
    }
}

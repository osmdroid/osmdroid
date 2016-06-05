package org.osmdroid.samplefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;



/**
 * Created by alex on 6/4/16.
 */
public class SampleSplitScreen extends BaseSampleFragment implements MapListener {
    @Override
    public String getSampleTitle() {
        return "Two maps, split screen";
    }

    protected MapView mMapView2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_splitscreen, container, false);

        mMapView = (MapView) root.findViewById(R.id.mapview1);
        mMapView2 = (MapView) root.findViewById(R.id.mapview2);

        return root;
    }

    @Override
    protected void addOverlays() {
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setMapListener(this);
        mMapView2.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mMapView2.setMapListener(this);

        mMapView2.setBuiltInZoomControls(true);
        mMapView2.setMultiTouchControls(true);
        mMapView2.setTilesScaledToDpi(true);
    }

    long lastEvent=0;
    @Override
    public boolean onScroll(ScrollEvent event) {

        if(lastEvent+40 < System.currentTimeMillis()) {
            lastEvent=System.currentTimeMillis();
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
        if(lastEvent+40 < System.currentTimeMillis()) {
            lastEvent=System.currentTimeMillis();
            if (event.getSource() == mMapView) {
                mMapView2.getController().setZoom(event.getZoomLevel());
            } else
                mMapView.getController().setZoom(event.getZoomLevel());
        }
        return true;
    }
}

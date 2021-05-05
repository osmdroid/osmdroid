package org.osmdroid.samplefragments.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.text.DecimalFormat;

/**
 * used for testing this issue
 * https://github.com/osmdroid/osmdroid/issues/248
 * Created by alex on 2/22/16.
 */
public class SampleMapEventListener extends BaseSampleFragment {
    TextView textViewCurrentLocation;
    public static final DecimalFormat df = new DecimalFormat("#.000000");

    @Override
    public String getSampleTitle() {
        return "Map Event Listener";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_with_locationbox, container, false);

        mMapView = root.findViewById(R.id.mapview);
        textViewCurrentLocation = root.findViewById(R.id.textViewCurrentLocation);
        return root;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        updateInfo();

        mMapView.setTileSource(TileSourceFactory.USGS_SAT);
        mMapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
                //Toast.makeText(getActivity(), "onScroll", Toast.LENGTH_SHORT).show();
                updateInfo();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                updateInfo();
                return true;
            }
        });
    }

    private void updateInfo() {
        IGeoPoint mapCenter = mMapView.getMapCenter();
        textViewCurrentLocation.setText(df.format(mapCenter.getLatitude()) + "," +
                df.format(mapCenter.getLongitude())
                + ",zoom=" + mMapView.getZoomLevelDouble() + "\nBounds: " + mMapView.getBoundingBox().toString());

    }
}

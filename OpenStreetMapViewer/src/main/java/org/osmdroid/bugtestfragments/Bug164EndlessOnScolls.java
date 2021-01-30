package org.osmdroid.bugtestfragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;

import static org.osmdroid.samplefragments.events.SampleMapEventListener.df;

/**
 * <a href="https://github.com/osmdroid/osmdroid/issues/164">https://github.com/osmdroid/osmdroid/issues/164</a>
 * Created by alex on 8/28/16.
 */

public class Bug164EndlessOnScolls extends BaseSampleFragment implements View.OnClickListener {
    @Override
    public String getSampleTitle() {
        return "Bug #164 Endless onScroll callsScoll";
    }

    TextView textViewCurrentLocation;
    Button animateTo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.map_with_locationbox164, container, false);
        mMapView = root.findViewById(R.id.mapview);
        textViewCurrentLocation = root.findViewById(R.id.textViewCurrentLocation);
        animateTo = root.findViewById(R.id.animateTo);
        animateTo.setOnClickListener(this);
        Log.d(TAG, "onCreateView");
        return root;
    }


    int callsScoll = 0;
    int callsZoom = 0;

    protected void addOverlays() {
        super.addOverlays();
        //
        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(TAG, "onScroll called");
                callsScoll++;
                updateInfo();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(TAG, "onZoom called");
                callsZoom++;
                updateInfo();
                return true;
            }
        });
    }

    private void updateInfo() {
        IGeoPoint mapCenter = mMapView.getMapCenter();
        textViewCurrentLocation.setText(df.format(mapCenter.getLatitude()) + "," +
                df.format(mapCenter.getLongitude())
                + "," + mMapView.getZoomLevelDouble() + "\nonScroll: " + callsScoll + " onZoom: "
                + callsZoom);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.animateTo) {

            double lat = Math.random() * 180d - 90;
            double lon = Math.random() * 360 - 180;
            mMapView.getController().animateTo(new GeoPoint(lat, lon));
        }
    }
}

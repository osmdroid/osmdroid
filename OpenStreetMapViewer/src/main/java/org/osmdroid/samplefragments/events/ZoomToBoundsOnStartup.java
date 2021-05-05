package org.osmdroid.samplefragments.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

import static org.osmdroid.samplefragments.events.SampleMapEventListener.df;

/**
 * Created by Dad on 10/28/2016.
 */

public class ZoomToBoundsOnStartup extends BaseSampleFragment implements View.OnClickListener {
    TextView textViewCurrentLocation;
    Button animateTo;

    @Override
    public String getSampleTitle() {
        return "Zoom to bounds on Start";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_with_locationbox164, container, false);

        mMapView = root.findViewById(R.id.mapview);
        mMapView.getController().setZoom(7);
        animateTo = root.findViewById(R.id.animateTo);
        animateTo.setOnClickListener(this);
        textViewCurrentLocation = root.findViewById(R.id.textViewCurrentLocation);
        attach();
        return root;
/*
values from onFirstLayout
18=13
17=13
16=13
15=13
14=13
13=13
12=13
11=13
10=13
7=13
6=13
5=13
4=13
3=14
2=15
1=10
0=10


on a button click
18=13,18
17=13
16=13
15=13
14=13
13=13
12=13
11=13
10=13
7=13
6=13
5=13
4=13
3=14,15
2=15,15
1=10,15
0=10,15
 */

    }

    private void attach() {
        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {

            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {

            }
        });
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
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
                + "," + mMapView.getZoomLevelDouble());

    }

    @Override
    public void onClick(View v) {
        final BoundingBox boundingBox = new BoundingBox(41.906802, 12.445436, 41.900073, 12.457852);
        mMapView.zoomToBoundingBox(boundingBox, false);
        mMapView.zoomToBoundingBox(boundingBox, false);
        mMapView.invalidate();

        //Log.d(LOGTAG, "ZoomToBoundingBox calculations: " + maxZoomLatitudeSpan + ","+maxZoomLongitudeSpan + ","+requiredLatitudeZoom + ","+requiredLongitudeZoom );
        //D/OsmDroid: ZoomToBoundingBox calculations: 1.367585271809038E-4,9.655952453613281E-4,13.0,15.0
        //D/OsmDroid: ZoomToBoundingBox calculations: 0.0011179235048756064,9.655952453613281E-4,16.0,15.0
    }
}

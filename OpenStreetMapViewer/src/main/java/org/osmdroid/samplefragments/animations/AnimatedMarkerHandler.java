package org.osmdroid.samplefragments.animations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * Marker animation based on Google's sample code for animating a marker
 * API 9+
 * created on 9/2/2017.
 *
 * @author Alex O'Ree
 */

public class AnimatedMarkerHandler extends BaseSampleFragment implements View.OnClickListener {
    @Override
    public String getSampleTitle() {
        return "Marker Animation (Handler)";
    }

    Button btnCache;
    Marker marker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.sample_cachemgr, container, false);
        mMapView = new MapView(getActivity());
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        btnCache = root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Start/Stop Animation");

        marker = new Marker(mMapView);
        marker.setTitle("An animated marker");
        marker.setPosition(new GeoPoint(0d, 0d));
        mMapView.getOverlayManager().add(marker);


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCache:

                GeoPoint random = new GeoPoint((Math.random() * 180) - 90, (Math.random() * 360) - 180);
                MarkerAnimation.animateMarkerToGB(mMapView, marker, random, new GeoPointInterpolator.Spherical());
                break;
        }
    }
}

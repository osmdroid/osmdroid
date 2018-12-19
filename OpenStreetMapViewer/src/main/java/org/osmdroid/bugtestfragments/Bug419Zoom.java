package org.osmdroid.bugtestfragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Created by alex on 9/25/16.
 */

public class Bug419Zoom extends BaseSampleFragment implements View.OnClickListener {
    @Override
    public String getSampleTitle() {
        return "Zoom scaling calculations";
    }

    Button btnCache, executeJob;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container, false);

        mMapView = new MapView(getActivity());
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        btnCache = root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Zoom Test");
        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCache) {
            //center as tne middle of the us to get a visual reference point
            mMapView.getController().setCenter(new GeoPoint(38.73, -99.66));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startTest();
                }
            }).start();
        }
    }

    double i = 0;
    double x = 0;

    //call this from off the UI thread
    public void startTest() {
        try {
            for (i = mMapView.getMinZoomLevel(); i < mMapView.getMaxZoomLevel(); i++) {
                for (x = mMapView.getMaxZoomLevel(); x > mMapView.getMinZoomLevel(); x--) {
                    Log.i(TAG, "Zoom out test " + i + " to " + x);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mMapView.getController().setZoom(i);
                            mMapView.invalidate();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMapView.getController().zoomTo(x);
                            //mMapView.invalidate();
                        }
                    });
                    try {
                        //to let the tiles load
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (i = mMapView.getMaxZoomLevel(); i > mMapView.getMinZoomLevel(); i--) {
                for (x = mMapView.getMinZoomLevel(); x < mMapView.getMaxZoomLevel(); x++) {
                    Log.i(TAG, "Zoom out test " + i + " to " + x);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mMapView.getController().setZoom(i);
                            mMapView.invalidate();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMapView.getController().zoomTo(x);
                            //mMapView.invalidate();
                        }
                    });
                    try {
                        //to let the tiles load
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
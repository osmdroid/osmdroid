package org.osmdroid.samplefragments.layouts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * created on 1/3/2017.
 *
 * @author Alex O'Ree
 */

public class MapInScrollView extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Map in a scroll view";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_scoll, null);
        mMapView = v.findViewById(R.id.mapview);

        mMapView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                Log.d(TAG, "onTouch");
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        Log.d(TAG, "onCreateView");
        return v;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDetach");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }
}

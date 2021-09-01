package org.osmdroid.samplefragments.layouts.pager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.MapView;

/**
 * Created by alex on 10/22/16.
 */

public class MapFragment extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Map Fragment in a view pager";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container, false);
        mMapView = new MapView(getActivity());
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        return root;
    }
}

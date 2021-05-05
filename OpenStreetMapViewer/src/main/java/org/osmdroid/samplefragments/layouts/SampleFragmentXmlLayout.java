package org.osmdroid.samplefragments.layouts;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.samplefragments.BaseSampleFragment;

public class SampleFragmentXmlLayout extends BaseSampleFragment {

    // ===========================================================
    // Fields
    // ===========================================================

    public static final String TITLE = "MapView in XML layout";

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(org.osmdroid.R.layout.activity_starter_mapview, null);
        mMapView = v.findViewById(org.osmdroid.R.id.mapview);
        return v;
    }

}

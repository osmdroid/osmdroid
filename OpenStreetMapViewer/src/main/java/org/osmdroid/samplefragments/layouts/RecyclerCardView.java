package org.osmdroid.samplefragments.layouts;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.layouts.rec.ConstructorInfoData;
import org.osmdroid.samplefragments.layouts.rec.CustomRecycler;
import org.osmdroid.samplefragments.layouts.rec.Info;

import java.util.ArrayList;

/**
 * created on 1/13/2017.
 *
 * @author Alex O'Ree
 */

public class RecyclerCardView extends BaseSampleFragment {
    //Objects for RecyclerView and InfoData
    private RecyclerView mRecyclerView;
    private CustomRecycler mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public String getSampleTitle() {
        return "Map in a recycler/cardview layout";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recyclerview, null);
        mMapView = v.findViewById(R.id.mapview);


        //Load Data And RecyclverView
        ArrayList<Info> a;
        ConstructorInfoData b = new ConstructorInfoData();
        a = b.obtainData();
        mRecyclerView = v.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(mLayoutManager);
        //Adapter is created in the last step
        mAdapter = new CustomRecycler(a);
        mRecyclerView.setAdapter(mAdapter);

        return v;

    }
}

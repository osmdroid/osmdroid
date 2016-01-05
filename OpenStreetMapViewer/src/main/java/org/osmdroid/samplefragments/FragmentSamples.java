package org.osmdroid.samplefragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by KjellbergZ on 16.12.2015.
 */
public class FragmentSamples extends ListFragment {
    SampleFactory sampleFactory = SampleFactory.getInstance();

    public static FragmentSamples newInstance() {
        return new FragmentSamples();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Generate a ListView with Sample Maps
        final ArrayList<String> list = new ArrayList<>();
        // Put samples next
        for (int a = 0; a < sampleFactory.count(); a++) {
            final BaseSampleFragment f = sampleFactory.getSample(a);
            list.add(f.getSampleTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, list);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Replace Fragment with selected sample
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().hide(this).add(android.R.id.content, sampleFactory.getSample(position), "SampleFragment")
                .addToBackStack(null).commit();
    }
}

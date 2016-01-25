package org.osmdroid.samplefragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.osmdroid.ExtraSamplesActivity;

import java.util.ArrayList;

/**
 * Created by KjellbergZ on 16.12.2015.
 */
public class FragmentSamples extends ListFragment {
    SampleFactory sampleFactory = SampleFactory.getInstance();

    public final static String TAG="osmfragsample";
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
        BaseSampleFragment frag = sampleFactory.getSample(position);
        Log.i(TAG, "loading fragment " + frag.getSampleTitle() + ", " + frag.getClass().getCanonicalName());
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(org.osmdroid.R.id.samples_container, frag, ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG)
                .addToBackStack(null).commit();

    }

    @Override
    public void onResume(){
        super.onResume();

        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
        System.gc();
    }
}

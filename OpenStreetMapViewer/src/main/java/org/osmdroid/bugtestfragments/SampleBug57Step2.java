package org.osmdroid.bugtestfragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SampleBug57Step2 extends Fragment {


    public SampleBug57Step2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sample_bug57_step2, container, false);
    }

}

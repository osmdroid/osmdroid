package org.osmdroid;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.osmdroid.bugtestfragments.BugFactory;
import org.osmdroid.bugtestfragments.WeathForceActivity;
import org.osmdroid.model.BaseActivity;
import org.osmdroid.samplefragments.ui.FragmentSamples;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 6/29/16.
 */
public class BugsTestingActivity extends FragmentActivity {
    public static final String SAMPLES_FRAGMENT_TAG = "org.osmdroid.BUGS_FRAGMENT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.updateStoragePrefreneces(this);    //needed for unit tests
        setContentView(R.layout.activity_extra_samples);

        FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag(SAMPLES_FRAGMENT_TAG) == null) {
            List<BaseActivity> extras = new ArrayList<>() ;
            extras.add(new WeathForceActivity());
            FragmentSamples fragmentSamples = FragmentSamples.newInstance(BugFactory.getInstance(),extras);
            fm.beginTransaction().add(org.osmdroid.R.id.samples_container, fragmentSamples, SAMPLES_FRAGMENT_TAG).commit();
        }
    }
}

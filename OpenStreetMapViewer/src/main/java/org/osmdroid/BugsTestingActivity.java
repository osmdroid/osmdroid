package org.osmdroid;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.osmdroid.bugtestfragments.BugFactory;
import org.osmdroid.samplefragments.FragmentSamples;

/**
 * Created by alex on 6/29/16.
 */
public class BugsTestingActivity extends FragmentActivity {
    public static final String SAMPLES_FRAGMENT_TAG = "org.osmdroid.BUGS_FRAGMENT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_samples);

        FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag(SAMPLES_FRAGMENT_TAG) == null) {
            FragmentSamples fragmentSamples = FragmentSamples.newInstance(BugFactory.getInstance());
            fm.beginTransaction().add(org.osmdroid.R.id.samples_container, fragmentSamples, SAMPLES_FRAGMENT_TAG).commit();
        }
    }
}

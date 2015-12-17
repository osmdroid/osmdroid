// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class StarterMapActivity extends FragmentActivity {
    private static final String MAP_FRAGMENT_TAG = "org.osmdroid.MAP_FRAGMENT_TAG";

    // ===========================================================
    // Constructors
    // ===========================================================
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(org.osmdroid.R.layout.activity_starter_main);
        FragmentManager fm = this.getSupportFragmentManager();
		if (fm.findFragmentByTag(MAP_FRAGMENT_TAG) == null) {
			StarterMapFragment starterMapFragment = StarterMapFragment.newInstance();
			fm.beginTransaction().add(org.osmdroid.R.id.map_container, starterMapFragment, MAP_FRAGMENT_TAG).commit();
		}
    }
}

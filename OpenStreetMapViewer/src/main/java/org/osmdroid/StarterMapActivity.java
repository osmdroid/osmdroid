// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 */
public class StarterMapActivity extends AppCompatActivity {
    private static final String MAP_FRAGMENT_TAG = "org.osmdroid.MAP_FRAGMENT_TAG";

    /**
     * The idea behind that is to force a MapView refresh when switching from offline to online.
     * If you don't do that, the map may display - when online - approximated tiles
     * * that were computed when offline
     * * that could be replaced by downloaded tiles
     * * but as the display is not refreshed there's no try to get better tiles
     *
     * @since 6.0
     */
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                starterMapFragment.invalidateMapView();
            } catch (NullPointerException e) {
                // lazy handling of an improbable NPE
            }
        }
    };

    private StarterMapFragment starterMapFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(org.osmdroid.R.layout.activity_starter_main);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        MainActivity.updateStoragePreferences(this);    //needed for unit tests

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag(MAP_FRAGMENT_TAG) == null) {
            starterMapFragment = StarterMapFragment.newInstance();
            fm.beginTransaction().add(org.osmdroid.R.id.map_container, starterMapFragment, MAP_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * small example of keyboard events on the mapview
     * page up = zoom out
     * page down = zoom in
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_PAGE_DOWN:
                starterMapFragment.zoomIn();
                return true;
            case KeyEvent.KEYCODE_PAGE_UP:
                starterMapFragment.zoomOut();
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * @since 6.0
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(networkReceiver);
        super.onDestroy();
    }
}

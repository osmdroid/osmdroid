// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import org.osmdroid.views.MapView;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class StarterMapActivity extends FragmentActivity {
    private static final String MAP_FRAGMENT_TAG = "org.osmdroid.MAP_FRAGMENT_TAG";

    /**
     * The idea behind that is to force a MapView refresh when switching from offline to online.
     * If you don't do that, the map may display - when online - approximated tiles
     * * that were computed when offline
     * * that could be replaced by downloaded tiles
     * * but as the display is not refreshed there's no try to get better tiles
     * @since 6.0
     */
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                starterMapFragment.getMapView().invalidate();
            } catch(NullPointerException e) {
                // lazy handling of an improbable NPE
            }
        }
    };

    private StarterMapFragment starterMapFragment=null;
    // ===========================================================
    // Constructors
    // ===========================================================
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.updateStoragePrefreneces(this);    //needed for unit tests

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        this.setContentView(org.osmdroid.R.layout.activity_starter_main);
        FragmentManager fm = this.getSupportFragmentManager();
		if (fm.findFragmentByTag(MAP_FRAGMENT_TAG) == null) {
			starterMapFragment = StarterMapFragment.newInstance();
			fm.beginTransaction().add(org.osmdroid.R.id.map_container, starterMapFragment, MAP_FRAGMENT_TAG).commit();
		}
    }

    /**
     * used by unit tests only, may return null
     * @return
     */
    public MapView getMapView(){
        if (starterMapFragment !=null)
            return starterMapFragment.getMapView();
        return null;
    }

    /**
     * small example of keyboard events on the mapview
     * page up = zoom out
     * page down = zoom in
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp (int keyCode, KeyEvent event){

        MapView mMapView = getMapView();
        if (mMapView==null)
            return super.onKeyUp(keyCode,event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_PAGE_DOWN:
                mMapView.getController().zoomIn();
                return true;
            case KeyEvent.KEYCODE_PAGE_UP:
                mMapView.getController().zoomOut();
                return true;
        }
        return super.onKeyUp(keyCode,event);
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

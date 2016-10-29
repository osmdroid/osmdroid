// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.MapView;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class StarterMapActivity extends FragmentActivity {
    private static final String MAP_FRAGMENT_TAG = "org.osmdroid.MAP_FRAGMENT_TAG";

    private StarterMapFragment starterMapFragment=null;
    // ===========================================================
    // Constructors
    // ===========================================================
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.updateStoragePrefreneces(this);    //needed for unit tests

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
}

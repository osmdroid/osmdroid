package org.osmdroid.sample;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.simplemap.R;
import org.osmdroid.views.MapView;

/**
 * Bare bones osmdroid example
 * created on 2/17/2018.
 *
 * @author Alex O'Ree
 */

public class MapActivity extends Activity {
    MapView mapView=null;
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        //TODO check permissions
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
    }


    @Override
    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView!=null)
        mapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView!=null)
        mapView.onPause();
    }
}

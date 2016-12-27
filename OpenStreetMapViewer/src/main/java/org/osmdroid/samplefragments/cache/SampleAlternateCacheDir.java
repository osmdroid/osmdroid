/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid.samplefragments.cache;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;

import org.osmdroid.config.Configuration;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.views.MapView;

/**
 *
 * @author alex
 */
public class SampleAlternateCacheDir extends BaseSampleFragment {

     @Override
     public String getSampleTitle() {
          return "Alt Cache Dir";
     }
     @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // This is how to manually change the cache dir, this is commented out since the changes
         // as of 5.6 reworked how the cache is managed at startup. If you change the cache dir
         // make sure ou do it BEFORE creating the map view, either programmatically or via
         // an inflater call.
         
         // Configuration.getInstance().setOsmdroidTileCache((new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/osmdroid2")));
         View v = inflater.inflate(org.osmdroid.R.layout.activity_starter_mapview, null);

		mMapView = (MapView) v.findViewById(org.osmdroid.R.id.mapview);
          
		return v;
	}

}

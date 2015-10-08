/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid.samplefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.views.MapView;

/**
 *
 * @author alex
 */
public class SampleAlternateCacheDir extends BaseSampleFragment{

     @Override
     public String getSampleTitle() {
          return "Alt Cache Dir";
     }
     @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(org.osmdroid.example.R.layout.mapview, null);
          OpenStreetMapTileProviderConstants.DEFAULT_CACHE_DIR = new File("/sdcard/osmdroid2/");
          OpenStreetMapTileProviderConstants.addCachePath("/sdcard/osmdroid2/");
		mMapView = (MapView) v.findViewById(org.osmdroid.example.R.id.mapview);
          
		return v;
	}
     
}

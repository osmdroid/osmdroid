/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid.samplefragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.Iterator;

/**
 * An example on increasing the in memory tile cache
 * @author alex
 */
public class SampleJumboCache  extends BaseSampleFragment {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final String TITLE = "Jumbo Cache";

	@Override
	public String getSampleTitle() {
		return TITLE;
	}

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void addOverlays() {
		super.addOverlays();

		Iterator<Overlay> iterator = mMapView.getOverlays().iterator();
		while(iterator.hasNext()){
			Overlay next = iterator.next();
			if (next instanceof TilesOverlay){
				TilesOverlay x = (TilesOverlay)next;
				x.setOvershootTileCache(x.getOvershootTileCache() * 2);
				Toast.makeText(getActivity(), "Tiles overlay cache set to " + x.getOvershootTileCache(), Toast.LENGTH_LONG).show();
				break;
			}
		}
		//this will set the disk cache size in MB to 1GB , 9GB trim size
		OpenStreetMapTileProviderConstants.setCacheSizes(1000L, 900L);
	}
}

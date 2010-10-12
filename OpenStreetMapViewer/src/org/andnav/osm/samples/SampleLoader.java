// Created by plusminus on 18:23:13 - 03.10.2008
package org.andnav.osm.samples;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class SampleLoader extends ListActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ArrayList<String> list = new ArrayList<String>();

		list.add("OSMapView with Minimap, ZoomControls, Animations, Scale Bar and MyLocationOverlay");
		list.add("Sample OSMContributor");
		list.add("OSMapView with ItemizedOverlay");
		list.add("OSMapView with ItemizedOverlayWithFocus");
		list.add("OSMapView with Minimap and ZoomControls");
		list.add("Sample with tiles overlay");

		this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch(position){
			case 0:
				this.startActivity(new Intent(this, SampleExtensive.class));
				break;
			case 1:
				this.startActivity(new Intent(this, SampleOSMContributor.class));
				break;
			case 2:
				this.startActivity(new Intent(this, SampleWithMinimapItemizedoverlay.class));
				break;
			case 3:
				this.startActivity(new Intent(this, SampleWithMinimapItemizedoverlayWithFocus.class));
				break;
			case 4:
				this.startActivity(new Intent(this, SampleWithMinimapZoomcontrols.class));
				break;
			case 5:
				this.startActivity(new Intent(this, SampleWithTilesOverlay.class));
				break;
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

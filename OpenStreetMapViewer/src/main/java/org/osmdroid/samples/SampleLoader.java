// Created by plusminus on 18:23:13 - 03.10.2008
package org.osmdroid.samples;

import java.util.ArrayList;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.MapActivity;

public class SampleLoader extends ListActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	final int INTERNET=1;
	final int NETSTATE=2;
	final int LOCATION=3;
	final int LOCATIONC=4;
	final int WIFI=5;
	final int STORAGE=6;
	// ===========================================================
	// Constructors
	// ===========================================================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Assume thisActivity is the current activity
		int permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.INTERNET);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED){
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.INTERNET)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				Toast.makeText(this, "Need internet access to get map tiles if working with online sources", Toast.LENGTH_LONG).show();

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.INTERNET},
						INTERNET);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}

		permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_NETWORK_STATE);

		if (permissionCheck != PackageManager.PERMISSION_GRANTED){
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_NETWORK_STATE)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				Toast.makeText(this, "Need to check network state", Toast.LENGTH_LONG).show();

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
						NETSTATE);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}

		permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED){
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_FINE_LOCATION)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				Toast.makeText(this, "Need your location to place an icon on the map", Toast.LENGTH_LONG).show();

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						LOCATION);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}

		permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_COARSE_LOCATION);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED){
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_COARSE_LOCATION)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				Toast.makeText(this, "Need your location to place an icon on the map", Toast.LENGTH_LONG).show();

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						LOCATIONC);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}

		permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_WIFI_STATE);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED){
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_WIFI_STATE)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				Toast.makeText(this, "Access WIFI state", Toast.LENGTH_LONG).show();

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_WIFI_STATE},
						WIFI);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}

		permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED){
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				Toast.makeText(this, "We store tiles to your devices storage to reduce data usage and for reading offline tile stores", Toast.LENGTH_LONG).show();

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						STORAGE);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}
		

		final ArrayList<String> list = new ArrayList<String>();

          list.add("OSMDroid Sample map (Start Here)");
		list.add("OSMapView with Minimap, ZoomControls, Animations, Scale Bar and MyLocationOverlay");
		list.add("OSMapView with ItemizedOverlay");
		list.add("OSMapView with Minimap and ZoomControls");
		list.add("Sample with tiles overlay");
		list.add("Sample with tiles overlay and custom tile source");
          list.add("Sample with Custom Resources");

		this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				list));
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case INTERNET:{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this,"Online map sources will be unavailable", Toast.LENGTH_LONG).show();
				}
				return;
			}
			case NETSTATE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this,"Online map sources will be unavailable", Toast.LENGTH_LONG).show();
				}
				return;
			}
			case LOCATION:{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this,"My location will be unavailable via GPS", Toast.LENGTH_LONG).show();
				}
				return;
			}
			case LOCATIONC:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this,"My location will be unavailable via Network providers", Toast.LENGTH_LONG).show();
				}
				return;
			}
			case STORAGE:{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this,"Offline map data and caching will be unavailable", Toast.LENGTH_LONG).show();
				}
				return;
			}
			case WIFI:{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay!


				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this,"Data usage may not be optimized.", Toast.LENGTH_LONG).show();

				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
		switch (position) {
          case 0:
               this.startActivity(new Intent(this, MapActivity.class));
               break;
		case 1:
			this.startActivity(new Intent(this, SampleExtensive.class));
			break;
		case 2:
			this.startActivity(new Intent(this, SampleWithMinimapItemizedoverlay.class));
			break;
		case 3:
			this.startActivity(new Intent(this, SampleWithMinimapZoomcontrols.class));
			break;
		case 4:
			this.startActivity(new Intent(this, SampleWithTilesOverlay.class));
			break;
		case 5:
			this.startActivity(new Intent(this, SampleWithTilesOverlayAndCustomTileSource.class));
			break;
          case 6:
			this.startActivity(new Intent(this, SampleResourceOverride.class));
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

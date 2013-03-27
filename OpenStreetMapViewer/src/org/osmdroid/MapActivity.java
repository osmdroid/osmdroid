// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class MapActivity extends FragmentActivity {

	private static final int MENU_ABOUT = Menu.FIRST + 1;

	// private static final int MENU_LAST_ID = MENU_ABOUT + 1; // Always set to last unused id
	//
	private static final int DIALOG_ABOUT_ID = 1;

	// private SharedPreferences mPrefs;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.main);

		// FrameLayout mapContainer = (FrameLayout) findViewById(R.id.map_container);
		// RelativeLayout parentContainer = (RelativeLayout) findViewById(R.id.parent_container);
		FragmentManager fm = this.getSupportFragmentManager();

		MapFragment mapFragment = new MapFragment();

		fm.beginTransaction().add(R.id.map_container, mapFragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		super.onCreateOptionsMenu(pMenu);

		// Put "About" menu item last
		pMenu.add(0, MENU_ABOUT, Menu.CATEGORY_SECONDARY, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showDialog(DIALOG_ABOUT_ID);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		Dialog dialog;

		switch (id) {
		case DIALOG_ABOUT_ID:
			return new AlertDialog.Builder(MapActivity.this).setIcon(R.drawable.icon)
					.setTitle(R.string.app_name).setMessage(R.string.about_message)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int whichButton) {
							//
						}
					}).create();

		default:
			dialog = null;
			break;
		}
		return dialog;
	}
}

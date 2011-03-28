// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.samples.SampleLoader;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class MapActivity extends Activity implements OpenStreetMapConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MENU_SAMPLES = Menu.FIRST + 1;
	private static final int MENU_ABOUT = MENU_SAMPLES + 1;

	private static final int MENU_LAST_ID = MENU_ABOUT + 1; // Always set to last unused id

	private static final int DIALOG_ABOUT_ID = 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private SharedPreferences mPrefs;
	private MapView mOsmv;
	private MyLocationOverlay mLocationOverlay;
	private ResourceProxy mResourceProxy;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResourceProxy = new ResourceProxyImpl(getApplicationContext());

		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		final RelativeLayout rl = new RelativeLayout(this);

		CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());

		this.mOsmv = new MapView(this, 256, mResourceProxy);
		this.mLocationOverlay = new MyLocationOverlay(this.getBaseContext(), this.mOsmv,
				mResourceProxy);
		this.mOsmv.setBuiltInZoomControls(true);
		this.mOsmv.setMultiTouchControls(true);
		this.mOsmv.getOverlays().add(this.mLocationOverlay);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		this.setContentView(rl);

		mOsmv.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
		mOsmv.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));


		/*
		 * This is an example of usage of runOnFirstFix.
		 * It looks more complicated than necessary because we need to create an
		 * extra thread and a handler.
		 * If you wanted to do a non-GUI thread then you wouldn't need the handler.
		 */
		if (DEBUGMODE) {
			final Handler handler = new Handler();
			mLocationOverlay.runOnFirstFix(new Runnable() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(),
									R.string.first_fix_message,
									Toast.LENGTH_LONG).show();
						}
					});
				}
			});
		}
	}

	@Override
	protected void onPause() {
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(PREFS_TILE_SOURCE, mOsmv.getTileProvider().getTileSource().name());
		edit.putInt(PREFS_SCROLL_X, mOsmv.getScrollX());
		edit.putInt(PREFS_SCROLL_Y, mOsmv.getScrollY());
		edit.putInt(PREFS_ZOOM_LEVEL, mOsmv.getZoomLevel());
		edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());
		edit.putBoolean(PREFS_SHOW_COMPASS, mLocationOverlay.isCompassEnabled());
		edit.commit();

		this.mLocationOverlay.disableMyLocation();
		this.mLocationOverlay.disableCompass();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
				TileSourceFactory.DEFAULT_TILE_SOURCE.name());
		try {
			final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
			mOsmv.setTileSource(tileSource);
		} catch (final IllegalArgumentException ignore) {
		}
		if (mPrefs.getBoolean(PREFS_SHOW_LOCATION, false)) {
			this.mLocationOverlay.enableMyLocation();
		}
		if (mPrefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
			this.mLocationOverlay.enableCompass();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		// Put samples first
		pMenu.add(0, MENU_SAMPLES, Menu.NONE, R.string.samples).setIcon(
				android.R.drawable.ic_menu_gallery);

		// Put overlay items next
		mOsmv.getOverlayManager().onCreateOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);

		// Put "About" menu item last
		pMenu.add(0, MENU_ABOUT, Menu.NONE, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		mOsmv.getOverlayManager().onPrepareOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {

		// We can also respond to events in the overlays here
		final int overlayItemId = item.getItemId() - MENU_LAST_ID;
		if (overlayItemId == MyLocationOverlay.MENU_MY_LOCATION) {
			final int id = mLocationOverlay.isMyLocationEnabled() ? R.string.set_mode_hide_me
					: R.string.set_mode_show_me;
			Toast.makeText(this, id, Toast.LENGTH_LONG).show();
		} else if (overlayItemId == TilesOverlay.MENU_OFFLINE) {
			final int id = mOsmv.useDataConnection() ? R.string.set_mode_offline
					: R.string.set_mode_online;
			Toast.makeText(this, id, Toast.LENGTH_LONG).show();
		}

		// Now process the menu item selection
		switch (item.getItemId()) {
		case MENU_SAMPLES:
			startActivity(new Intent(this, SampleLoader.class));
			return true;

		case MENU_ABOUT:
			showDialog(DIALOG_ABOUT_ID);
			return true;

		default:
			return mOsmv.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mOsmv);
		}
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		Dialog dialog;

		switch (id) {
		case DIALOG_ABOUT_ID:
			return new AlertDialog.Builder(MapActivity.this).setIcon(R.drawable.icon)
					.setTitle(R.string.app_name).setMessage(R.string.about_message)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int whichButton) {
						}
					}).create();

		default:
			dialog = null;
			break;
		}
		return dialog;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean onTrackballEvent(final MotionEvent event) {
		return this.mOsmv.onTrackballEvent(event);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.samples.SampleLoader;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
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

	private static final int MENU_MY_LOCATION = Menu.FIRST;
	private static final int MENU_MAP_MODE = MENU_MY_LOCATION + 1;
	private static final int MENU_OFFLINE = MENU_MAP_MODE + 1;
	private static final int MENU_SAMPLES = MENU_OFFLINE + 1;
	private static final int MENU_ABOUT = MENU_SAMPLES + 1;

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

		this.mOsmv = new MapView(this, 256);
		this.mOsmv.setResourceProxy(mResourceProxy);
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
	}

	@Override
	protected void onPause() {
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(PREFS_TILE_SOURCE, mOsmv.getTileProvider().getTileSource().name());
		edit.putInt(PREFS_SCROLL_X, mOsmv.getScrollX());
		edit.putInt(PREFS_SCROLL_Y, mOsmv.getScrollY());
		edit.putInt(PREFS_ZOOM_LEVEL, mOsmv.getZoomLevel());
		edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());
		edit.commit();

		this.mLocationOverlay.disableMyLocation();

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
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		pMenu.add(0, MENU_MY_LOCATION, Menu.NONE, R.string.my_location).setIcon(
				android.R.drawable.ic_menu_mylocation);

		{
			final SubMenu mapMenu =
				pMenu.addSubMenu(0, MENU_MAP_MODE, Menu.NONE, R.string.map_mode).setIcon(
						android.R.drawable.ic_menu_mapmode);

			for (final ITileSource tileSource : TileSourceFactory.getTileSources()) {
				mapMenu.add(MENU_MAP_MODE, 1000 + tileSource.ordinal(), Menu.NONE,
						tileSource.localizedName(mResourceProxy));
			}
			mapMenu.setGroupCheckable(MENU_MAP_MODE, true, true);
		}

		pMenu.add(0, MENU_OFFLINE, Menu.NONE, R.string.offline).setIcon(R.drawable.ic_menu_offline);

		pMenu.add(0, MENU_SAMPLES, Menu.NONE, R.string.samples).setIcon(
				android.R.drawable.ic_menu_gallery);

		pMenu.add(0, MENU_ABOUT, Menu.NONE, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final int ordinal = mOsmv.getTileProvider().getTileSource().ordinal();
		menu.findItem(1000 + ordinal).setChecked(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MY_LOCATION:
			if (this.mLocationOverlay.isMyLocationEnabled()) {
				this.mLocationOverlay.disableMyLocation();
			} else {
				this.mLocationOverlay.enableMyLocation();
				final Location lastFix = this.mLocationOverlay.getLastFix();
				if (lastFix != null) {
					this.mOsmv.getController().setCenter(new GeoPoint(lastFix));
				}
			}
			Toast.makeText(
					this,
					this.mLocationOverlay.isMyLocationEnabled() ? R.string.set_mode_show_me
							: R.string.set_mode_hide_me, Toast.LENGTH_LONG).show();
			return true;

		case MENU_MAP_MODE:
			this.mOsmv.invalidate();
			return true;

		case MENU_OFFLINE:
			final boolean useDataConnection = !this.mOsmv.useDataConnection();
			final int id = useDataConnection ? R.string.set_mode_online : R.string.set_mode_offline;
			Toast.makeText(this, id, Toast.LENGTH_LONG).show();
			this.mOsmv.setUseDataConnection(useDataConnection);
			return true;

		case MENU_SAMPLES:
			startActivity(new Intent(this, SampleLoader.class));
			return true;

		case MENU_ABOUT:
			showDialog(DIALOG_ABOUT_ID);
			return true;

		default: // Map mode submenu items
			mOsmv.setTileSource(TileSourceFactory.getTileSource(item.getItemId() - 1000));
		}
		return false;
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

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			this.mLocationOverlay.followLocation(false);
		}

		return super.onTouchEvent(event);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

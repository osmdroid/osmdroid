// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.samples.SampleLoader;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class MapFragment extends Fragment implements OpenStreetMapConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MENU_SAMPLES = Menu.FIRST + 1;

	private static final int MENU_LAST_ID = MENU_SAMPLES + 1; // Always set to last unused id

	// ===========================================================
	// Fields
	// ===========================================================

	private SharedPreferences mPrefs;
	private MapView mMapView;
	private MyLocationNewOverlay mLocationOverlay;
	private CompassOverlay mCompassOverlay;
	private MinimapOverlay mMinimapOverlay;
	private RotationGestureOverlay mRotationGestureOverlay;
	private ResourceProxy mResourceProxy;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
		mMapView = new MapView(inflater.getContext(), 256, mResourceProxy);
		mMapView.setUseSafeCanvas(true);
		setHardwareAccelerationOff();
		return mMapView;
	}

	@SuppressLint("NewApi")
	private void setHardwareAccelerationOff() {
		// Turn off hardware acceleration here, or in manifest
		if (android.os.Build.VERSION.SDK_INT >= 11)
			mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		final Context context = this.getActivity();
		// mResourceProxy = new ResourceProxyImpl(getActivity().getApplicationContext());

		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		// only do static initialisation if needed
		if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
			CloudmadeUtil.retrieveCloudmadeKey(context.getApplicationContext());
		}

		this.mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(
				context), mMapView);
		this.mLocationOverlay = new MyLocationNewOverlay(context,
				new GpsMyLocationProvider(context), mMapView);
		mMinimapOverlay = new MinimapOverlay(getActivity(),
				mMapView.getTileRequestCompleteHandler());

		mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);

		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);
		mMapView.getOverlays().add(this.mLocationOverlay);
		mMapView.getOverlays().add(this.mCompassOverlay);
		mMapView.getOverlays().add(this.mMinimapOverlay);
		mMapView.getOverlays().add(this.mRotationGestureOverlay);

		mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
		mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));

		setHasOptionsMenu(true);
	}

	@Override
	public void onPause() {
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(PREFS_TILE_SOURCE, mMapView.getTileProvider().getTileSource().name());
		edit.putInt(PREFS_SCROLL_X, mMapView.getScrollX());
		edit.putInt(PREFS_SCROLL_Y, mMapView.getScrollY());
		edit.putInt(PREFS_ZOOM_LEVEL, mMapView.getZoomLevel());
		edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());
		edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
		edit.commit();

		this.mLocationOverlay.disableMyLocation();
		this.mCompassOverlay.disableCompass();

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
				TileSourceFactory.DEFAULT_TILE_SOURCE.name());
		try {
			final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
			mMapView.setTileSource(tileSource);
		} catch (final IllegalArgumentException ignore) {
		}
		if (mPrefs.getBoolean(PREFS_SHOW_LOCATION, false)) {
			this.mLocationOverlay.enableMyLocation(this.mLocationOverlay.getMyLocationProvider());
		}
		if (mPrefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
			this.mCompassOverlay.enableCompass(this.mCompassOverlay.getOrientationProvider());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Put overlay items first
		mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_LAST_ID, mMapView);

		// Put samples next
		menu.add(0, MENU_SAMPLES, Menu.NONE, R.string.samples).setIcon(
				android.R.drawable.ic_menu_gallery);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu pMenu) {
		mMapView.getOverlayManager().onPrepareOptionsMenu(pMenu, MENU_LAST_ID, mMapView);
		super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView);

		switch (item.getItemId()) {
		case MENU_SAMPLES:
			startActivity(new Intent(getActivity(), SampleLoader.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

//	@Override
//	public boolean onTrackballEvent(final MotionEvent event) {
//		return this.mMapView.onTrackballEvent(event);
//	}
}

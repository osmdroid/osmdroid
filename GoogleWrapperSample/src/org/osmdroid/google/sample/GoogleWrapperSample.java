package org.osmdroid.google.sample;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.maps.MapActivity;
import org.osmdroid.api.IMap;
import org.osmdroid.google.wrapper.v2.MapFactory;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class GoogleWrapperSample extends MapActivity {

	private static final int GOOGLE_MAP_V1_VIEW_ID = 1;
	private static final int GOOGLE_MAP_V2_VIEW_ID = 2;
	private static final int OSM_MAP_VIEW_ID = 3;
	private static final int ENABLE_MY_LOCATION_ID = 4;
	private static final int DISABLE_MY_LOCATION_ID = 5;

	private MenuItem mGoogleV1MenuItem;
	private MenuItem mGoogleV2MenuItem;
	private MenuItem mOsmMenuItem;
	private MenuItem mEnableMyLocationOverlayMenuItem;
	private MenuItem mDisableMyLocationOverlayMenuItem;

	private MapViewSelection mMapViewSelection = MapViewSelection.OSM;

	private IMap mMap;
	private MapView mMapViewV2;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onCreate(final Bundle aBundle) {
		super.onCreate(aBundle);
		if (mMapViewV2 != null) {
			mMapViewV2.onCreate(aBundle);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setMapView();
		if (mMapViewV2 != null) {
			mMapViewV2.onResume();
		}
	}

	@Override
	protected void onPause() {
		if (mMapViewV2 != null) {
			mMapViewV2.onPause();
		}
		mMap.setMyLocationEnabled(false);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mMapViewV2 != null) {
			mMapViewV2.onDestroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMapViewV2 != null) {
			mMapViewV2.onSaveInstanceState(outState);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mMapViewV2 != null) {
			mMapViewV2.onLowMemory();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		mGoogleV1MenuItem = pMenu.add(0, GOOGLE_MAP_V1_VIEW_ID, Menu.NONE, R.string.map_view_google_v1);
		mGoogleV2MenuItem = pMenu.add(0, GOOGLE_MAP_V2_VIEW_ID, Menu.NONE, R.string.map_view_google_v2);
		mOsmMenuItem = pMenu.add(0, OSM_MAP_VIEW_ID, Menu.NONE, R.string.map_view_osm);
		mEnableMyLocationOverlayMenuItem = pMenu.add(0, ENABLE_MY_LOCATION_ID, Menu.NONE, R.string.enable_my_location);
		mDisableMyLocationOverlayMenuItem = pMenu.add(0, DISABLE_MY_LOCATION_ID, Menu.NONE, R.string.disable_my_location);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		final boolean isV2Available = MapFactory.isGoogleMapsV2Supported(this);
		mGoogleV1MenuItem.setVisible(mMapViewSelection != MapViewSelection.GoogleV1);
		mGoogleV2MenuItem.setVisible(mMapViewSelection != MapViewSelection.GoogleV2 && isV2Available);
		mOsmMenuItem.setVisible(mMapViewSelection != MapViewSelection.OSM);
		mEnableMyLocationOverlayMenuItem.setVisible(!mMap.isMyLocationEnabled());
		mDisableMyLocationOverlayMenuItem.setVisible(mMap.isMyLocationEnabled());
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem) {
		if (pItem == mGoogleV1MenuItem) {
			// switch to google v1
			mMapViewSelection = MapViewSelection.GoogleV1;
			setMapView();
			return true;
		}
		if (pItem == mGoogleV2MenuItem) {
			// switch to google v2
			mMapViewSelection = MapViewSelection.GoogleV2;
			setMapView();
			return true;
		}
		if (pItem == mOsmMenuItem) {
			// switch to osm
			mMapViewSelection = MapViewSelection.OSM;
			setMapView();
			return true;
		}
		if (pItem == mEnableMyLocationOverlayMenuItem) {
			mMap.setMyLocationEnabled(true);
		}
		if (pItem == mDisableMyLocationOverlayMenuItem) {
			mMap.setMyLocationEnabled(false);
		}

		return false;
	}

	private void setMapView() {
		mMapViewV2 = null;

		if (mMapViewSelection == MapViewSelection.OSM) {
			final org.osmdroid.views.MapView mapView = new org.osmdroid.views.MapView(this, 256);
			setContentView(mapView);
			mMap = MapFactory.getMap(mapView);
		}
		if (mMapViewSelection == MapViewSelection.GoogleV1) {
			final com.google.android.maps.MapView mapView = new com.google.android.maps.MapView(this, getString(R.string.google_maps_api_key));
			setContentView(mapView);
			mMap = MapFactory.getMap(mapView);
		}
		if (mMapViewSelection == MapViewSelection.GoogleV2) {
			mMapViewV2 = new MapView(this);
			setContentView(mMapViewV2);

			mMapViewV2.onCreate(null);
			mMapViewV2.onResume();
			mMap = MapFactory.getMap(mMapViewV2);
			try {
				MapsInitializer.initialize(this);
			} catch (final GooglePlayServicesNotAvailableException e) {
				e.printStackTrace();
			}
		}

		mMap.setZoomAndCenter(14, 52370816, 9735936); // Hannover
		mMap.setMyLocationEnabled(false);
	}

	private enum MapViewSelection { GoogleV1, GoogleV2, OSM }

}

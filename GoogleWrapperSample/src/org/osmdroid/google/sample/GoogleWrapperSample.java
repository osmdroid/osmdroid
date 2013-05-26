package org.osmdroid.google.sample;

import com.google.android.maps.MapActivity;
import org.osmdroid.api.IMapView;
import org.osmdroid.api.IMyLocationOverlay;
import org.osmdroid.util.GeoPoint;

import android.view.Menu;
import android.view.MenuItem;

public class GoogleWrapperSample extends MapActivity {

	private static final int GOOGLE_MAP_VIEW_ID = 1;
	private static final int OSM_MAP_VIEW_ID = 2;
	private static final int ENABLE_MY_LOCATION_ID = 3;
	private static final int DISABLE_MY_LOCATION_ID = 4;

	private MenuItem mGoogleMenuItem;
	private MenuItem mOsmMenuItem;
	private MenuItem mEnableMyLocationOverlayMenuItem;
	private MenuItem mDisableMyLocationOverlayMenuItem;

	private MapViewSelection mMapViewSelection = MapViewSelection.OSM;

	private IMapView mMapView;
	private IMyLocationOverlay mMyLocationOverlay;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setMapView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMyLocationOverlay.disableMyLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		mGoogleMenuItem = pMenu.add(0, GOOGLE_MAP_VIEW_ID, Menu.NONE, R.string.map_view_google);
		mOsmMenuItem = pMenu.add(0, OSM_MAP_VIEW_ID, Menu.NONE, R.string.map_view_osm);
		mEnableMyLocationOverlayMenuItem = pMenu.add(0, ENABLE_MY_LOCATION_ID, Menu.NONE, R.string.enable_my_location);
		mDisableMyLocationOverlayMenuItem = pMenu.add(0, DISABLE_MY_LOCATION_ID, Menu.NONE, R.string.disable_my_location);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		mGoogleMenuItem.setVisible(mMapViewSelection == MapViewSelection.OSM);
		mOsmMenuItem.setVisible(mMapViewSelection == MapViewSelection.Google);
		mEnableMyLocationOverlayMenuItem.setVisible(!mMyLocationOverlay.isMyLocationEnabled());
		mDisableMyLocationOverlayMenuItem.setVisible(mMyLocationOverlay.isMyLocationEnabled());
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem) {
		if (pItem == mGoogleMenuItem) {
			// switch to google
			mMapViewSelection = MapViewSelection.Google;
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
			mMyLocationOverlay.enableMyLocation();
		}
		if (pItem == mDisableMyLocationOverlayMenuItem) {
			mMyLocationOverlay.disableMyLocation();
		}

		return false;
	}

	private void setMapView() {
		if (mMapViewSelection == MapViewSelection.OSM) {
			final org.osmdroid.views.MapView mapView = new org.osmdroid.views.MapView(this, 256);
			setContentView(mapView);
			mMapView = mapView;

			final org.osmdroid.views.overlay.MyLocationOverlay mlo = new org.osmdroid.views.overlay.MyLocationOverlay(this, mapView);
			mapView.getOverlays().add(mlo);
			mMyLocationOverlay = mlo;
		}
		if (mMapViewSelection == MapViewSelection.Google) {
			final com.google.android.maps.MapView mapView = new com.google.android.maps.MapView(this, getString(R.string.google_maps_api_key));
			setContentView(mapView);
			mMapView = new org.osmdroid.google.wrapper.MapView(mapView);

			final org.osmdroid.google.wrapper.MyLocationOverlay mlo = new org.osmdroid.google.wrapper.MyLocationOverlay(this, mapView);
			mapView.getOverlays().add(mlo);
			mMyLocationOverlay = mlo;
		}

		mMapView.getController().setZoom(14);
		mMapView.getController().setCenter(new GeoPoint(52370816, 9735936)); // Hannover
		mMyLocationOverlay.disableMyLocation();
	}

	private enum MapViewSelection { Google, OSM }

}

package org.osmdroid.google.sample;

import org.osmdroid.api.IMapView;
import org.osmdroid.api.IMyLocationOverlay;
import org.osmdroid.util.GeoPoint;

import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;

public class GoogleWrapperSample extends MapActivity {

	private static final String GOOGLE_API_KEY = "get your own!";

	private static final int GOOGLE_MAP_VIEW_ID = 1;
	private static final int OSM_MAP_VIEW_ID = 2;

	private MenuItem mGoogleMenuItem;
	private MenuItem mOsmMenuItem;

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
		mMyLocationOverlay.enableMyLocation();
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
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		mGoogleMenuItem.setVisible(mMapViewSelection == MapViewSelection.OSM);
		mOsmMenuItem.setVisible(mMapViewSelection == MapViewSelection.Google);
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
			final com.google.android.maps.MapView mapView = new com.google.android.maps.MapView(this, GOOGLE_API_KEY);
			setContentView(mapView);
			mMapView = new org.osmdroid.google.MapView(mapView);

			final org.osmdroid.google.MyLocationOverlay mlo = new org.osmdroid.google.MyLocationOverlay(this, mapView);
			mapView.getOverlays().add(mlo);
			mMyLocationOverlay = mlo;
		}

		mMapView.getController().setZoom(14);
		mMapView.getController().setCenter(new GeoPoint(52370816, 9735936)); // Hannover
	}

	private enum MapViewSelection { Google, OSM };

}

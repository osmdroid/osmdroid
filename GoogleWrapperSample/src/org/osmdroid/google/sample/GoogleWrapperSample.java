package org.osmdroid.google.sample;

import com.google.android.gms.maps.MapView;
import com.google.android.maps.MapActivity;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IPosition;
import org.osmdroid.api.IProjection;
import org.osmdroid.api.Marker;
import org.osmdroid.api.OnCameraChangeListener;
import org.osmdroid.google.wrapper.v2.MapFactory;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.util.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This is a simple app that demonstrates how to use a common interface ({@link org.osmdroid.api.IMap})
 * to perform functions on different map types.
 */
public class GoogleWrapperSample extends MapActivity {

	private static final Logger logger = LoggerFactory.getLogger(GoogleWrapperSample.class);

	private static final int OSM_MAP_VIEW_ID = 1;
	private static final int GOOGLE_MAP_V1_VIEW_ID = 2;
	private static final int GOOGLE_MAP_V2_VIEW_ID = 3;
	private static final int ENABLE_MY_LOCATION_ID = 4;
	private static final int DISABLE_MY_LOCATION_ID = 5;
	private static final int ROTATE_ID = 6;

	private MenuItem mOsmMenuItem;
	private MenuItem mGoogleV1MenuItem;
	private MenuItem mGoogleV2MenuItem;
	private MenuItem mEnableMyLocationOverlayMenuItem;
	private MenuItem mDisableMyLocationOverlayMenuItem;
	private MenuItem mRotateMenuItem;

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
		mOsmMenuItem = pMenu.add(0, OSM_MAP_VIEW_ID, Menu.NONE, R.string.map_view_osm);
		mGoogleV1MenuItem = pMenu.add(0, GOOGLE_MAP_V1_VIEW_ID, Menu.NONE, R.string.map_view_google_v1);
		mGoogleV2MenuItem = pMenu.add(0, GOOGLE_MAP_V2_VIEW_ID, Menu.NONE, R.string.map_view_google_v2);
		mEnableMyLocationOverlayMenuItem = pMenu.add(0, ENABLE_MY_LOCATION_ID, Menu.NONE, R.string.enable_my_location);
		mDisableMyLocationOverlayMenuItem = pMenu.add(0, DISABLE_MY_LOCATION_ID, Menu.NONE, R.string.disable_my_location);
		mRotateMenuItem = pMenu.add(0, ROTATE_ID, Menu.NONE, R.string.rotate);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		final boolean isV2Available = MapFactory.isGoogleMapsV2Supported(this);
		mOsmMenuItem.setVisible(mMapViewSelection != MapViewSelection.OSM);
		mGoogleV1MenuItem.setVisible(mMapViewSelection != MapViewSelection.GoogleV1);
		mGoogleV2MenuItem.setVisible(mMapViewSelection != MapViewSelection.GoogleV2 && isV2Available);
		mEnableMyLocationOverlayMenuItem.setVisible(!mMap.isMyLocationEnabled());
		mDisableMyLocationOverlayMenuItem.setVisible(mMap.isMyLocationEnabled());
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem) {
		if (pItem == mOsmMenuItem) {
			// switch to osm
			mMapViewSelection = MapViewSelection.OSM;
			setMapView();
			return true;
		}
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
		if (pItem == mEnableMyLocationOverlayMenuItem) {
			mMap.setMyLocationEnabled(true);
			setLastKnownLocation();
		}
		if (pItem == mDisableMyLocationOverlayMenuItem) {
			mMap.setMyLocationEnabled(false);
		}
		if(pItem == mRotateMenuItem) {
			mMap.setBearing(mMap.getBearing() + 45);
			debugProjection();
		}

		return false;
	}

	private void setMapView() {
		mMapViewV2 = null;

		if (mMap != null) {
			mMap.setMyLocationEnabled(false);
		}

		if (mMapViewSelection == MapViewSelection.OSM) {
			final org.osmdroid.views.MapView mapView = new org.osmdroid.views.MapView(this, 256);
			mapView.setBuiltInZoomControls(true);
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
		}

		final Position position = new Position(52.370816, 9.735936); // Hannover
		position.setZoomLevel(14);
		mMap.setPosition(position);
		mMap.setMyLocationEnabled(false);

		addMarkers();

		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(final IPosition position) {
				logger.debug("onCameraChange");
			}
		});
	}

	private void addMarkers() {
		mMap.addMarker(new Marker(52.370816, 9.735936).title("Hannover").snippet("Description of Hannover"));
		mMap.addMarker(new Marker(52.518333, 13.408333).title("Berlin").snippet("Description of Berlin").icon(R.drawable.berlin));
		mMap.addMarker(new Marker(38.895000, -77.036667).title("Washington").snippet("Description of Washington"));
		mMap.addMarker(new Marker(37.779300, -122.419200).title("San Francisco").snippet("Description of San Francisco"));
		mMap.addMarker(new Marker(-38.371000, 178.298000).title("Tolaga Bay").snippet("Description of Tolaga Bay"));
	}

	private void setLastKnownLocation() {
		final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final Location location = LocationUtils.getLastKnownLocation(lm);
		if (location != null) {
			mMap.setCenter(location.getLatitude(), location.getLongitude());
		}
	}

	/**
	 * This is just used for debugging
	 */
	private void debugProjection() {
		new Thread() {
			@Override
			public void run() {
				// let the map get redrawn and a new projection calculated
				try { sleep(1000); } catch (InterruptedException ignore) { }

				// then get the projection
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						final IProjection projection = mMap.getProjection();
						final IGeoPoint northEast = projection.getNorthEast();
						final IGeoPoint southWest = projection.getSouthWest();
						final IProjection breakpoint = projection;
					}
				});
			}
		}.start();
	}

	private enum MapViewSelection { OSM, GoogleV1, GoogleV2 }

}

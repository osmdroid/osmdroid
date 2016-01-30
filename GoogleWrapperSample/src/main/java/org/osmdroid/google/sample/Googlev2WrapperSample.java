package org.osmdroid.google.sample;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.MapView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IPosition;
import org.osmdroid.api.IProjection;
import org.osmdroid.api.Marker;
import org.osmdroid.api.OnCameraChangeListener;
import org.osmdroid.api.Polyline;
import org.osmdroid.google.wrapper.v2.MapFactory;
import org.osmdroid.thirdparty.Constants;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.util.Position;

/**
 * This is a simple app that demonstrates how to use a common interface ({@link IMap})
 * to perform functions on different map types.
 */
public class Googlev2WrapperSample extends FragmentActivity {

    private static final int OSM_MAP_VIEW_ID = 1;
    private static final int GOOGLE_MAP_V1_VIEW_ID = 2;
    private static final int GOOGLE_MAP_V2_VIEW_ID = 3;
    private static final int ENABLE_MY_LOCATION_ID = 4;
    private static final int DISABLE_MY_LOCATION_ID = 5;
    private static final int ROTATE_ID = 6;

    private MenuItem mEnableMyLocationOverlayMenuItem;
    private MenuItem mDisableMyLocationOverlayMenuItem;
    private MenuItem mRotateMenuItem;

    private IMap mMap;
    private MapView mMapViewV2;


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
        //TODO example with more Arc GIS maps, Mapbox, etc
        mEnableMyLocationOverlayMenuItem = pMenu.add(0, ENABLE_MY_LOCATION_ID, Menu.NONE, R.string.enable_my_location);
        mDisableMyLocationOverlayMenuItem = pMenu.add(0, DISABLE_MY_LOCATION_ID, Menu.NONE, R.string.disable_my_location);
        mRotateMenuItem = pMenu.add(0, ROTATE_ID, Menu.NONE, R.string.rotate);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu) {
        mEnableMyLocationOverlayMenuItem.setVisible(!mMap.isMyLocationEnabled());
        mDisableMyLocationOverlayMenuItem.setVisible(mMap.isMyLocationEnabled());
        return super.onPrepareOptionsMenu(pMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem) {

        if (pItem == mEnableMyLocationOverlayMenuItem) {
            mMap.setMyLocationEnabled(true);
            setLastKnownLocation();
        }
        if (pItem == mDisableMyLocationOverlayMenuItem) {
            mMap.setMyLocationEnabled(false);
        }
        if (pItem == mRotateMenuItem) {
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


        mMapViewV2 = new MapView(this);
        setContentView(mMapViewV2);

        mMapViewV2.onCreate(null);
        mMapViewV2.onResume();
        mMap = MapFactory.getMap(mMapViewV2);

        final Position position = new Position(52.370816, 9.735936); // Hannover
        position.setZoomLevel(14);
        mMap.setPosition(position);
        mMap.setMyLocationEnabled(false);

        addMarkers();

        addPolyline();

        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(final IPosition position) {
                Log.d(Constants.LOGTAG, "onCameraChange");
            }
        });
    }

    private void addMarkers() {
        mMap.addMarker(new Marker(52.370816, 9.735936).title("Hannover").snippet("Description of Hannover"));
        mMap.addMarker(new Marker(52.518333, 13.408333).title("Berlin").snippet("Description of Berlin").icon(R.drawable.berlin));
        mMap.addMarker(new Marker(38.895000, -77.036667).title("Washington").snippet("Description of Washington"));
        mMap.addMarker(new Marker(37.779300, -122.419200).title("San Francisco").snippet("Description of San Francisco"));
        mMap.addMarker(new Marker(-38.371000, 178.298000).title("Tolaga Bay").snippet("Description of Tolaga Bay"));
        // mMap.addMarker(new Marker(52.375000, 9.730000).title("Layers").snippet("Layers"));
    }

    private void addPolyline() {
        final Polyline polyline = new Polyline()
                .color(Color.BLUE)
                .width(5.0f)
                .points(new GeoPoint(52370816, 9735936), new GeoPoint(52370000, 9740000), new GeoPoint(52370000, 9730000));
        final int id = mMap.addPolyline(polyline);
        mMap.addPointsToPolyline(id, new GeoPoint(52380000, 9730000));
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
                try {
                    sleep(1000);
                } catch (InterruptedException ignore) {
                }

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

    private enum MapViewSelection {OSM, GoogleV1, GoogleV2, OSM_BING}

}

package org.osmdroid.bugtestfragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import org.osmdroid.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.model.BaseActivity;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * http://stackoverflow.com/q/40112165/1203182
 * Created by alex on 10/21/16.
 */

public class WeathForceActivity extends BaseActivity implements LocationListener, IOrientationConsumer, MapEventsReceiver {
    final String TAG = "WeathForceActivity";
    private CompassOverlay mCompassOverlay = null;
    private MyLocationNewOverlay mLocationOverlay;
    IOrientationProvider compass = null;
    int deviceOrientation = 0;
    MapView mMapView;
    float gpsspeed;
    float gpsbearing;
    float lat = 0;
    float lon = 0;
    float alt = 0;
    long timeOfFix = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter_mapview);

        Intent intent = getIntent();
        //if (intent)
        final double lat1 = 25.633;
        final double long1 = 71.094;

        //super important. Many tile servers, including open street maps, will BAN applications by user
        //agent. Do not use the sample application's user agent for your app! Use your own setting, such
        //as the app id.
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mMapView = findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);


        mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this),
                mMapView);
        mCompassOverlay.enableCompass();
        mMapView.getOverlays().add(this.mCompassOverlay);

        addOverlays();

        GeoPoint startPoint = new GeoPoint(lat1, long1);
        IMapController mapController = mMapView.getController();
        mapController.setZoom(9);
        mapController.setCenter(startPoint);
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMapView.getOverlays().add(startMarker);


        mMapView.invalidate();

    }

    public void addOverlays() {
        mLocationOverlay = new MyLocationNewOverlay(mMapView);
        mLocationOverlay.setEnableAutoStop(false);
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        this.mMapView.getOverlayManager().add(mLocationOverlay);
        mMapView.setMultiTouchControls(true);
        mMapView.setTilesScaledToDpi(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //lock the device in current screen orientation
        int orientation;
        int rotation = ((WindowManager) this.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                this.deviceOrientation = 0;
                break;
            case Surface.ROTATION_90:
                this.deviceOrientation = 90;
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                this.deviceOrientation = 180;
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            default:
                this.deviceOrientation = 270;
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }

        this.setRequestedOrientation(orientation);


        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (Exception ex) {
        }
        compass = new InternalCompassOrientationProvider(this);
        compass.startOrientationProvider(this);
        mMapView.getController().zoomTo(14);

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMapView == null)
            return;
        //after the first fix, schedule the task to change the icon
        //mMapView.getController().setExpectedCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
        mMapView.invalidate();
        gpsbearing = location.getBearing();
        gpsspeed = location.getSpeed();
        lat = (float) location.getLatitude();
        lon = (float) location.getLongitude();
        alt = (float) location.getAltitude(); //meters
        timeOfFix = location.getTime();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stopOrientationProvider();
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.removeUpdates(this);
        } catch (Exception ex) {
        }

        //unlock the orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    Float trueNorth = 0f;

    @Override
    public void onOrientationChanged(final float orientationToMagneticNorth, IOrientationProvider source) {

        GeomagneticField gf = new GeomagneticField(lat, lon, alt, timeOfFix);
        trueNorth = orientationToMagneticNorth + gf.getDeclination();
        gf = null;
        synchronized (trueNorth) {
            if (trueNorth > 360.0f) {
                trueNorth = trueNorth - 360.0f;
            }

            //use gps bearing instead of the compass
            if (gpsspeed > 0.01f) {
                float t = (360 - gpsbearing - this.deviceOrientation);
                if (t < 0) {
                    t += 360;
                }
                if (t > 360) {
                    t -= 360;
                }
                mMapView.setMapOrientation(t);
            } else {
                //this part adjusts the desired map rotation based on device orientation and compass heading

                float t = (360 - trueNorth - this.deviceOrientation);
                if (t < 0) {
                    t += 360;
                }
                if (t > 360) {
                    t -= 360;
                }
                mMapView.setMapOrientation(t);
            }

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (this != null) {
                        Log.i(TAG
                                , "GPS Speed: " + gpsspeed + "m/s  GPS Bearing: " + gpsbearing +
                                        "\nDevice Orientation: " + deviceOrientation + "  Compass heading: " + (int) orientationToMagneticNorth + "\n" +
                                        "True north: " + trueNorth.intValue() + " Map Orientation: " + (int) mMapView.getMapOrientation());
                    }
                }
            });
        }
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public String getActivityTitle() {
        return "Weather Force Test";
    }
}
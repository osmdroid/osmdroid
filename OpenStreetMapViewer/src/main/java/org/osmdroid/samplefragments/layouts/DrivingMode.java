package org.osmdroid.samplefragments.layouts;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * gps heading/bearing up, with my location at 3/4 screen position (heightwise)
 * Created by alex on 10/23/16.
 * @since 5.6
 */

public class DrivingMode extends BaseSampleFragment implements LocationListener, IOrientationConsumer {
    @Override
    public String getSampleTitle() {
        return "Driving Mode";
    }


    int deviceOrientation = 0;
    MyLocationNewOverlay mLocationOverlay = null;
    IOrientationProvider compass = null;
    float gpsspeed;
    float gpsbearing;
    float lat = 0;
    float lon = 0;
    float alt = 0;
    long timeOfFix = 0;
    String screen_orientation = "";
    int width = 0;
    int height = 0;

    Marker mylocation;
    Marker centerOnPoint;

    ImageButton btnRotateLeft, btnRotateRight;
    protected TextView textViewCurrentLocation = null;

    //add animationTo custom screen point
    //IMapController.void animateTo(IGeoPoint geoPoint, Point screenPoint, int animationDuration);
    //MapView public void setMapRotationPoint(Point point) {
    //instead of rotating about the center point of the map while in a driving mode, you can rotate at any point


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.map_with_locationbox_controls, container, false);
        mMapView = (MapView) root.findViewById(R.id.mapview);
        mMapView.getController().setZoom(12);
        textViewCurrentLocation = (TextView) root.findViewById(R.id.textViewCurrentLocation);
        return root;
    }

    public void addOverlays() {
        super.addOverlays();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        //mMapView.setMapRotationPoint(new Point(width / 2, height * 3 / 4));
        mMapView.getOverlayManager().getTilesOverlay().setOvershootTileCache(mMapView.getOverlayManager().getTilesOverlay().getOvershootTileCache() * 4);

        mLocationOverlay = new MyLocationNewOverlay(mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.setOptionsMenuEnabled(true);

        mylocation = new Marker(mMapView);
        mylocation.setImage(getResources().getDrawable(R.drawable.person));
        mylocation.setIcon(getResources().getDrawable(R.drawable.person));
        mylocation.setTitle("My location");

        centerOnPoint=new Marker(mMapView);
        centerOnPoint.setImage(getResources().getDrawable(R.drawable.icon));
        centerOnPoint.setIcon(getResources().getDrawable(R.drawable.icon));
        centerOnPoint.setTitle("Map should center here");

    }


    @Override
    public void onResume() {
        super.onResume();
        //hack for x86
        if (!"Android-x86".equalsIgnoreCase(Build.BRAND)) {
            //lock the device in current screen orientation
            int orientation;
            int rotation = ((WindowManager) getActivity().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    this.deviceOrientation = 0;
                    screen_orientation = "ROTATION_0 SCREEN_ORIENTATION_PORTRAIT";
                    break;
                case Surface.ROTATION_90:
                    this.deviceOrientation = 90;
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    screen_orientation = "ROTATION_90 SCREEN_ORIENTATION_LANDSCAPE";
                    break;
                case Surface.ROTATION_180:
                    this.deviceOrientation = 180;
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    screen_orientation = "ROTATION_180 SCREEN_ORIENTATION_REVERSE_PORTRAIT";
                    break;
                default:
                    this.deviceOrientation = 270;
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    screen_orientation = "ROTATION_270 SCREEN_ORIENTATION_REVERSE_LANDSCAPE";
                    break;
            }

            getActivity().setRequestedOrientation(orientation);
        }

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            //on API15 AVDs,network provider fails. no idea why
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) this);
        } catch (Exception ex) {
        }
        compass = new InternalCompassOrientationProvider(getActivity());
        compass.startOrientationProvider(this);

        mLocationOverlay.disableFollowLocation();   //we're going to control the location of the map
        mLocationOverlay.enableMyLocation();


    }

    @Override
    public void onPause() {
        super.onPause();
        compass.stopOrientationProvider();
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.removeUpdates(this);
        } catch (Exception ex) {
        }
        mLocationOverlay.disableFollowLocation();
        mLocationOverlay.disableMyLocation();

        //unlock the orientation
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (compass != null)
            compass.destroy();
        if (mLocationOverlay != null) {
            mLocationOverlay.disableMyLocation();
            mLocationOverlay.disableFollowLocation();
            mLocationOverlay.onDetach(mMapView);
        }
        if (mMapView != null)
            mMapView.onDetach();
        mMapView = null;
        mLocationOverlay = null;
        compass = null;
        textViewCurrentLocation = null;

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMapView == null)
            return;
        //this appears to be broken on hardware. x86/emulator is fine though

        gpsbearing = location.getBearing();
        gpsspeed = location.getSpeed();
        lat = (float) location.getLatitude();
        lon = (float) location.getLongitude();
        alt = (float) location.getAltitude(); //meters
        timeOfFix = location.getTime();

        //use gps bearing
        float t = (360 - gpsbearing - this.deviceOrientation);
        if (t < 0) {
            t += 360;
        }
        if (t > 360) {
            t -= 360;
        }
        //help smooth everything out
        t = (int) t;
        t = t / 10;
        t = (int) t;
        t = t * 10;

        mMapView.setMapOrientation(t);
        updateDisplay(location.getBearing());

        //center on current location
        mMapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
        mMapView.invalidate();

        //get the lat/lon of the point 1/4 of the map "up" of the current loction
        IGeoPoint iGeoPoint = mMapView.getProjection().fromPixels(width / 2, height - (height / 4));
        //center the map on that point, thus making my location near the screen center, 1/4 from the bottom of the map view
        //aninate to is not recommended in this case
        mMapView.getController().setCenter(iGeoPoint);

        //update the icons
        mylocation.setPosition(new GeoPoint(location.getLatitude(), location.getLongitude()));
        mMapView.getOverlayManager().remove(mylocation);
        mMapView.getOverlayManager().add(mylocation);

        //this is debug code for putting an icon at screen center
        //centerOnPoint.setPosition((GeoPoint) iGeoPoint);
        //mMapView.getOverlayManager().remove(centerOnPoint);
        //mMapView.getOverlayManager().add(centerOnPoint);

        mMapView.invalidate();
        if (mylocation.isInfoWindowShown()) {
            //https://github.com/osmdroid/osmdroid/issues/460
            mylocation.closeInfoWindow();
            mylocation.showInfoWindow();
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    Float trueNorth = 0f;

    @Override
    public void onOrientationChanged(final float orientationToMagneticNorth, IOrientationProvider source) {
        if (gpsspeed > 0.01)
            return;
        //note, on devices without a compass this never fires...
        GeomagneticField gf = new GeomagneticField(lat, lon, alt, timeOfFix);
        trueNorth = orientationToMagneticNorth + gf.getDeclination();
        gf = null;
        synchronized (trueNorth) {
            if (trueNorth > 360.0f) {
                trueNorth = trueNorth - 360.0f;
            }
            float actualHeading = 0f;

            //this part adjusts the desired map rotation based on device orientation and compass heading
            float t = (360 - trueNorth - this.deviceOrientation);
            if (t < 0) {
                t += 360;
            }
            if (t > 360) {
                t -= 360;
            }
            actualHeading = t;
            //help smooth everything out
            t = (int) t;
            t = t / 10;
            t = (int) t;
            t = t * 10;
            mMapView.setMapOrientation(t);
            updateDisplay(actualHeading);

        }
    }

    private void updateDisplay(final float bearing) {
        try {
            Activity act = getActivity();
            if (act != null)
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null && textViewCurrentLocation != null) {
                            textViewCurrentLocation.setText("GPS Speed: " + gpsspeed + "m/s  GPS Bearing: " + gpsbearing +
                                "\nDevice Orientation: " + (int) deviceOrientation + "  Compass heading: " + (int) bearing + " Location: " +lat+","+lon+"\n"+
                                "True north: " + trueNorth.intValue() + " Map Orientation: " + (int) mMapView.getMapOrientation() + "\n" +
                                screen_orientation);
                        }
                    }
                });
        } catch (Exception ex) {
        }
    }
}

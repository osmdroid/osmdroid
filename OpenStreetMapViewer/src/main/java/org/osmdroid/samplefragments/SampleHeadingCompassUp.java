package org.osmdroid.samplefragments;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * An example on setting the device's "up" based on heading, bearing, or compass
 * Created by alex on 4/30/16.
 */
public class SampleHeadingCompassUp extends BaseSampleFragment implements LocationListener, IOrientationConsumer {

    int deviceOrientation = 0;
    MyLocationNewOverlay overlay = null;
    IOrientationProvider compass = null;
    float gpsspeed;
    float gpsbearing;
    TextView textViewCurrentLocation = null;
    float lat = 0;
    float lon = 0;
    float alt = 0;
    long timeOfFix = 0;
    String screen_orientation = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_with_locationbox, container, false);

        mMapView = (MapView) root.findViewById(R.id.mapview);
        textViewCurrentLocation = (TextView) root.findViewById(R.id.textViewCurrentLocation);
        return root;
    }

    @Override
    public String getSampleTitle() {
        return "Heading/Compass Up";
    }

    @Override
    public void addOverlays() {
        overlay = new MyLocationNewOverlay(mMapView);
        overlay.setEnableAutoStop(false);
        overlay.enableFollowLocation();
        overlay.enableMyLocation();
        this.mMapView.getOverlayManager().add(overlay);
    }

    @Override
    public void onResume() {
        super.onResume();

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


        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            //on API15 AVDs,network provider fails. no idea why
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) this);
        } catch (Exception ex) {
        }
        compass = new InternalCompassOrientationProvider(getActivity());
        compass.startOrientationProvider(this);
        mMapView.getController().zoomTo(18);

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

        //unlock the orientation
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        compass.destroy();
        overlay.disableMyLocation();
        overlay.disableFollowLocation();
        overlay.onDetach(mMapView);
        if (mMapView!=null)
            mMapView.onDetach();
        mMapView=null;
        overlay=null;
        compass=null;
        textViewCurrentLocation=null;

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMapView==null)
            return;
        //after the first fix, schedule the task to change the icon
        //mMapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
        mMapView.invalidate();
        gpsbearing = location.getBearing();
        gpsspeed = location.getSpeed();
        lat = (float) location.getLatitude();
        lon = (float) location.getLongitude();
        alt = (float) location.getAltitude(); //meters
        timeOfFix = location.getTime();

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

        GeomagneticField gf = new GeomagneticField(lat, lon, alt, timeOfFix);
        trueNorth = orientationToMagneticNorth + gf.getDeclination();
        gf=null;
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

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity()!=null && textViewCurrentLocation!=null) {
                        textViewCurrentLocation.setText("GPS Speed: " + gpsspeed + "m/s  GPS Bearing: " + gpsbearing +
                                "\nDevice Orientation: " + (int) deviceOrientation + "  Compass heading: " + (int) orientationToMagneticNorth + "\n" +
                                "True north: " + trueNorth.intValue() + " Map Orientation: " + (int) mMapView.getMapOrientation() + "\n" +
                                screen_orientation);
                    }
                }
            });
        }
    }
}

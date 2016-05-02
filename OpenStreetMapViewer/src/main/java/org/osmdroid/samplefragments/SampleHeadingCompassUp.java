package org.osmdroid.samplefragments;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.WindowManager;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * Created by alex on 4/30/16.
 */
public class SampleHeadingCompassUp extends BaseSampleFragment implements LocationListener, IOrientationConsumer {

    int deviceOrientation=0;
    MyLocationNewOverlay overlay = null;
    IOrientationProvider compass = null;

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
        int orientation = getActivity().getRequestedOrientation();
        int rotation = ((WindowManager) getActivity().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                this.deviceOrientation=0;
                break;
            case Surface.ROTATION_90:
                this.deviceOrientation=90;
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                this.deviceOrientation=180;
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            default:
                this.deviceOrientation=270;
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
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
    public void onLocationChanged(Location location) {
        //after the first fix, schedule the task to change the icon
       //mMapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
        mMapView.invalidate();
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

    @Override
    public void onOrientationChanged(float orientation, IOrientationProvider source) {
        //System.out.println("compass " + orientation);
        //System.out.println("deviceOrientation " + deviceOrientation);
        //this part adjusts the desired map rotation based on device orientation and compass heading
        float t=(360-orientation-this.deviceOrientation);
        if (t < 0)
            t+=360;
        if (t > 360)
            t-=360;
        //System.out.println("screen heading to " + t);
        mMapView.setMapOrientation(t);
    }
}

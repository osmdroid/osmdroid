package org.osmdroid.samplefragments.location;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;

import java.util.Timer;
import java.util.TimerTask;

/**
 * https://github.com/osmdroid/osmdroid/issues/249
 *
 * @author alex
 */
public class SampleCustomIconDirectedLocationOverlay extends BaseSampleFragment implements LocationListener {

    private boolean hasFix = false;
    private DirectedLocationOverlay overlay;

    @Override
    public String getSampleTitle() {
        return "Directed Location Overlay";
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            //on API15 AVDs,network provider fails. no idea why
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(this);

    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        overlay = new DirectedLocationOverlay(getActivity());
        overlay.setShowAccuracy(true);
        Toast.makeText(getActivity(), "Requires location services turned on", Toast.LENGTH_LONG).show();
        mMapView.getOverlays().add(overlay);


    }

    @Override
    public void onLocationChanged(Location location) {
        //after the first fix, schedule the task to change the icon
        if (!hasFix) {
            Toast.makeText(getActivity(), "Location fixed, scheduling icon change", Toast.LENGTH_LONG).show();
            TimerTask changeIcon = new TimerTask() {
                @Override
                public void run() {
                    Activity act = getActivity();
                    if (act != null)
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.sfgpuci);
                                    overlay.setDirectionArrow(drawable.getBitmap());
                                } catch (Throwable t) {
                                    //insultates against crashing when the user rapidly switches fragments/activities
                                }
                            }
                        });

                }
            };
            Timer timer = new Timer();
            timer.schedule(changeIcon, 5000);

        }
        hasFix = true;
        overlay.setBearing(location.getBearing());
        overlay.setAccuracy((int) location.getAccuracy());
        overlay.setLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
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
}

package org.osmdroid.samplefragments.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

/**
 * See https://github.com/osmdroid/osmdroid/issues/815
 * created on 12/21/2017.
 *
 * @author Alex O'Ree
 */

public class SampleCustomMyLocation extends BaseSampleFragment implements LocationListener {
    @Override
    public String getSampleTitle() {
        return "Custom My Location Overlay";
    }

    LocationManager mgr;
    Marker myLocation;
    boolean added = false;
    boolean followme = true;

    @Override
    public void addOverlays() {
        super.addOverlays();
        myLocation = new Marker(mMapView);
        myLocation.setIcon(getResources().getDrawable(org.osmdroid.R.drawable.icon));
        myLocation.setImage(getResources().getDrawable(org.osmdroid.R.drawable.icon));


    }

    public void onResume() {
        super.onResume();
        mgr = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        if (mgr != null)
            try {
                mgr.removeUpdates(this);
                mgr = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mgr != null)
            try {
                mgr.removeUpdates(this);
                mgr = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }


    @Override
    public void onLocationChanged(Location location) {
        myLocation.setPosition(new GeoPoint(location.getLatitude(), location.getLongitude()));
        if (!added) {
            mMapView.getOverlayManager().add(myLocation);
            added = true;
        }
        if (followme) {
            mMapView.getController().animateTo(myLocation.getPosition());
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
}

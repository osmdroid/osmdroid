package org.osmdroid.diag;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.util.Iterator;
import java.util.List;

/**
 * created on 2/6/2018.
 *
 * @author Alex O'Ree
 */

public class DiagnosticsActivity extends AppCompatActivity
        implements View.OnClickListener, LocationListener, GpsStatus.Listener {
    TextView output = null;
    LocationManager lm = null;
    Location currentLocation = null;
    GpsStatus gpsStatus = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diag);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        findViewById(R.id.diag_location).setOnClickListener(this);
        findViewById(R.id.diag_orientation).setOnClickListener(this);
        findViewById(R.id.diag_gps).setOnClickListener(this);
        findViewById(R.id.diag_permissions).setOnClickListener(this);
        findViewById(R.id.diag_storage).setOnClickListener(this);
        output = findViewById(R.id.diag_output);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.diag_location:
                probeLocation();
                break;
            case R.id.diag_orientation:
                probeOrientation();
                break;
            case R.id.diag_permissions:
                checkPermissions();
                break;
            case R.id.diag_storage:
                probeStorage();
                break;
            case R.id.diag_gps:
                probeGps();
                break;
        }
    }

    public void onResume() {
        super.onResume();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.addGpsStatusListener(this);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
        } catch (RuntimeException r) {
        }
    }

    public void onPause() {
        super.onPause();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.removeUpdates(this);
            lm.removeGpsStatusListener(this);
        } catch (SecurityException e) {
        } catch (RuntimeException r) {
        }
    }

    private void probeStorage() {
        StringBuilder sb = new StringBuilder();
        List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList(this);
        for (StorageUtils.StorageInfo storageInfo : storageInfos) {
            sb.append(storageInfo.path).append("\n");
        }
        output.setText(sb.toString());
    }


    private void probeGps() {
        StringBuilder sb = new StringBuilder();
        if (currentLocation != null) {
            sb.append("Current Location:\n");
            sb.append(currentLocation.getLatitude()).append(",").append(currentLocation.getLongitude()).append("\n");
            sb.append("Alt ").append(currentLocation.getAltitude()).append("\n");
            sb.append("Accuracy ").append(currentLocation.getAccuracy()).append("\n");
            sb.append("Bearing ").append(currentLocation.getBearing()).append("\n");
            sb.append("Speed ").append(currentLocation.getSpeed()).append("\n\n");
        }
        try {
            if (gpsStatus != null) {
                Iterator<GpsSatellite> iterator = gpsStatus.getSatellites().iterator();
                while (iterator.hasNext()) {
                    GpsSatellite next = iterator.next();
                    sb.append("Sat PRN " + next.getPrn() + " Elevation " + next.getElevation() + " Azimuth " + next.getAzimuth() + "SNR " + next.getSnr()).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append(e.toString());
        }
        output.setText(sb.toString());
    }

    private void checkPermissions() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fine Location Granted: ");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            sb.append("yes\n");
        } else sb.append("no\n");
        sb.append("Write External Storage: ");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            sb.append("yes\n");
        } else sb.append("no\n");
        output.setText(sb.toString());
    }

    private void probeOrientation() {
        StringBuilder sb = new StringBuilder();
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        for (Sensor s : sensorList) {

            sb.append(s.getName() + ":" + s.toString() + "\n");
        }
        output.setText(sb.toString());
    }

    private void probeLocation() {
        StringBuilder sb = new StringBuilder();

        List<String> allProviders = lm.getAllProviders();
        for (String s : allProviders) {
            sb.append(s).append("\n");
            LocationProvider provider = lm.getProvider(s);
            sb.append("Name " + provider.getName()).append("\n");
            sb.append("Cell " + provider.requiresCell()).append("\n");
            sb.append("Network " + provider.requiresNetwork()).append("\n");
            sb.append("Satellite " + provider.requiresSatellite()).append("\n");
            sb.append("Altitude " + provider.supportsAltitude()).append("\n");
            sb.append("Bearing " + provider.supportsBearing()).append("\n");
            sb.append("Speed " + provider.supportsSpeed()).append("\n\n");
            //GpsStatus gpsStatus = lm.getGpsStatus(null);
            //gpsStatus.
        }
        output.setText(sb.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
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
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                try {
                    gpsStatus = lm.getGpsStatus(gpsStatus);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                // Do something.
                break;
        }
    }
}

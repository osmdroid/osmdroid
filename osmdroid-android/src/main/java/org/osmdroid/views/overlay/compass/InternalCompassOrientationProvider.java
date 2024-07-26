package org.osmdroid.views.overlay.compass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.osmdroid.views.MapView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressLint("LongLogTag")
public class InternalCompassOrientationProvider implements SensorEventListener, IOrientationProvider {
    private static final String TAG = "InternalCompassOrientationProvider";

    @Nullable
    private IOrientationConsumer mOrientationConsumer;
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometerSensor;
    private final Sensor mMagnetometerSensor;
    private float mOrientationDegrees;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private final float[] R = new float[9];
    private final float[] I = new float[9];
    private final float[] orientation = new float[3];

    public InternalCompassOrientationProvider(@NonNull final MapView mapView) {
        final Context cContext = mapView.getContext();
        mSensorManager = (SensorManager)cContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if ((mAccelerometerSensor == null) || (mMagnetometerSensor == null)) throw new RuntimeException("An Accelerometer AND a MagneticField are required for the Compass");
    }

    /** Enable orientation updates from the internal compass sensor and show the compass */
    @Override
    public boolean startOrientationProvider(@NonNull final IOrientationConsumer orientationConsumer) {
        mOrientationConsumer = orientationConsumer;
        boolean result = true;

        result &= mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        result &= mSensorManager.registerListener(this, mMagnetometerSensor, SensorManager.SENSOR_DELAY_UI);

        return result;
    }

    @Override
    public void stopOrientationProvider() {
        mSensorManager.unregisterListener(this);
        mOrientationConsumer = null;
    }

    @Override
    public float getLastKnownOrientation() { return mOrientationDegrees; }

    @Override
    public void destroy() {
        stopOrientationProvider();
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) { /* This is not interesting for us at the moment */ }

    @Override
    public void onSensorChanged(@NonNull final SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) mGeomagnetic = event.values;
        if ((mGravity != null) && (mGeomagnetic != null)) {
            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                SensorManager.getOrientation(R, orientation);
                mOrientationDegrees = (float)((Math.toDegrees(orientation[0]) + 360) % 360);
                if (mOrientationConsumer != null) mOrientationConsumer.onOrientationChanged(mOrientationDegrees, this);
            }
        }
    }

}

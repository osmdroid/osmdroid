package org.osmdroid.views.overlay.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InternalCompassOrientationProvider implements SensorEventListener, IOrientationProvider {
    @Nullable
    private IOrientationConsumer mOrientationConsumer;
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometerSensor;
    private final Sensor mMagnetometerSensor;
    private float mAzimuth;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private final float[] R = new float[9];
    private final float[] I = new float[9];
    private final float[] orientation = new float[3];

    public InternalCompassOrientationProvider(@NonNull final Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
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
    public float getLastKnownOrientation() { return mAzimuth; }

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
                mAzimuth = ((float)Math.toDegrees(orientation[0]) + 180.0f); // <-- orientation contains: azimut, pitch and roll
                if (mOrientationConsumer != null) mOrientationConsumer.onOrientationChanged(mAzimuth, this);
            }
        }
    }
}

package org.osmdroid.views.overlay.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class InternalCompassOrientationProvider implements SensorEventListener, IOrientationProvider
{
    private IOrientationConsumer mOrientationConsumer;
    private final SensorManager mSensorManager;
    private float mAzimuth;

    public InternalCompassOrientationProvider(Context context)
    {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    //
    // IOrientationProvider
    //

    /**
     * Enable orientation updates from the internal compass sensor and show the compass.
     */
    @Override
    public boolean startOrientationProvider(IOrientationConsumer orientationConsumer)
    {
        mOrientationConsumer = orientationConsumer;
        boolean result = false;

        final Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            result = mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
        return result;
    }

    @Override
    public void stopOrientationProvider()
    {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public float getLastKnownOrientation()
    {
        return mAzimuth;
    }

    //
    // SensorEventListener
    //

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy)
    {
        // This is not interesting for us at the moment
    }

    @Override
    public void onSensorChanged(final SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            if (event.values != null) {
                mAzimuth = event.values[0];
                if (mOrientationConsumer != null)
                    mOrientationConsumer.onOrientationChanged(mAzimuth, this);
            }
        }
    }
}

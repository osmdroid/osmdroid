package org.osmdroid;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorEventListenerProxy implements SensorEventListener {
    private final SensorManager mSensorManager;
    private SensorEventListener mListener = null;

    public SensorEventListenerProxy(final SensorManager pSensorManager) {
        mSensorManager = pSensorManager;
    }

    public boolean startListening(final SensorEventListener pListener, final int pSensorType,
                                  final int pRate) {
        final Sensor sensor = mSensorManager.getDefaultSensor(pSensorType);
        if (sensor == null)
            return false;
        mListener = pListener;
        return mSensorManager.registerListener(this, sensor, pRate);
    }

    public void stopListening() {
        mListener = null;
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(final Sensor pSensor, final int pAccuracy) {
        if (mListener != null) {
            mListener.onAccuracyChanged(pSensor, pAccuracy);
        }
    }

    @Override
    public void onSensorChanged(final SensorEvent pEvent) {
        if (mListener != null) {
            mListener.onSensorChanged(pEvent);
        }
    }

}

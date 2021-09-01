package org.osmdroid.views.overlay.gestures;

import android.view.MotionEvent;

import org.osmdroid.events.MapListener;
import org.osmdroid.views.MapView;

/**
 * heads up, this class is used internally by osmdroid, you're welcome to use but it the interface
 * {@link RotationListener} will not fire as expected. It is used internally by osmdroid. If you want
 * to listen for rotation changes on the {@link org.osmdroid.views.MapView} then use {@link org.osmdroid.views.MapView#setMapListener(MapListener)}
 * and check for {@link MapView#getMapOrientation()}. See <a href="https://github.com/osmdroid/osmdroid/issues/628">https://github.com/osmdroid/osmdroid/issues/628</a>
 */
public class RotationGestureDetector {

    /**
     * heads up, this class is used internally by osmdroid, you're welcome to use but it the interface
     * {@link RotationListener} will not fire as expected. It is used internally by osmdroid. If you want
     * to listen for rotation changes on the {@link org.osmdroid.views.MapView} then use {@link org.osmdroid.views.MapView#setMapListener(MapListener)}
     * and check for {@link MapView#getMapOrientation()}
     * See <a href="https://github.com/osmdroid/osmdroid/issues/628">https://github.com/osmdroid/osmdroid/issues/628</a>
     */
    public interface RotationListener {
        void onRotate(float deltaAngle);
    }

    protected float mRotation;
    private RotationListener mListener;
    private boolean mEnabled = true;

    public RotationGestureDetector(RotationListener listener) {
        mListener = listener;
    }

    private static float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void onTouch(MotionEvent e) {
        if (e.getPointerCount() != 2)
            return;

        if (e.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            mRotation = rotation(e);
        }

        float rotation = rotation(e);
        float delta = rotation - mRotation;

        //we have to allow detector to capture and store the new rotation to avoid UI jump when
        //user enables the overlay again
        if (mEnabled) {
            mRotation += delta;
            mListener.onRotate(delta);
        } else {
            mRotation = rotation;
        }
    }

    public void setEnabled(final boolean pEnabled) {
        this.mEnabled = pEnabled;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }
}

package org.osmdroid.views.overlay.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

@SuppressLint("NewApi")
public class OneFingerZoomOverlay extends Overlay {
    private boolean mIsDoubleClick = false;
    private float mLastY;

    @Override
    public boolean onDoubleTapEvent(MotionEvent event, MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsDoubleClick = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mIsDoubleClick = false;
        }
        return super.onDoubleTapEvent(event, mapView);
    }

    @Override
    public boolean onDoubleTap(MotionEvent event, MapView mapView) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if (mIsDoubleClick) {
            VelocityTracker velocityTracker = VelocityTracker.obtain();
            velocityTracker.addMovement(event);
            velocityTracker.computeCurrentVelocity(100, 1000f);
            float velocityY = Math.abs(velocityTracker.getYVelocity()) / 1000;
            if (mLastY > event.getY()) {
                mapView.getController().setZoom(mapView.getZoomLevelDouble() - velocityY);
            } else {
                mapView.getController().setZoom(mapView.getZoomLevelDouble() + velocityY);
            }
            mLastY = event.getY();
            velocityTracker.recycle();
        }
        return super.onTouchEvent(event, mapView);
    }
}

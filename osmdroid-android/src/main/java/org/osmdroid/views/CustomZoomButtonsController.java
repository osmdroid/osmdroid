package org.osmdroid.views;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class CustomZoomButtonsController {

    public enum Visibility {ALWAYS, NEVER, SHOW_AND_FADEOUT}
    private static final int TH_MESSAGE_POSTPONEFADEOUT = 1;

    private final MapView mMapView;
    private final ValueAnimator mFadeOutAnimation;
    private CustomZoomButtonsDisplay mDisplay;
    private OnZoomListener mListener;
    private boolean mZoomInEnabled;
    private boolean mZoomOutEnabled;
    private float mAlpha01;
    private boolean detached;
    private Visibility mVisibility = Visibility.NEVER;
    private int mFadeOutAnimationDurationInMillis = 500;
    private int mShowDelayInMillis = 3500;
    private boolean mJustActivated;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @UiThread @MainThread
        @Override
        public void handleMessage(@NonNull final Message msg) {
            switch (msg.what) {
                case TH_MESSAGE_POSTPONEFADEOUT: {
                    startFadeOut();
                    break;
                }
            }
        }
    };

    public CustomZoomButtonsController(final MapView pMapView) {
        mMapView = pMapView;
        mDisplay = new CustomZoomButtonsDisplay(mMapView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mFadeOutAnimation = ValueAnimator.ofFloat(0, 1);
            mFadeOutAnimation.setInterpolator(new LinearInterpolator());
            mFadeOutAnimation.setDuration(mFadeOutAnimationDurationInMillis);
            mFadeOutAnimation.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            if (detached) {
                                mFadeOutAnimation.cancel();
                                return;
                            }
                            mAlpha01 = 1 - (float) valueAnimator.getAnimatedValue();
                            invalidate();
                        }
                    }
            );
        } else {
            mFadeOutAnimation = null;
        }
    }

    public void setZoomInEnabled(final boolean pEnabled) {
        mZoomInEnabled = pEnabled;
    }

    public void setZoomOutEnabled(final boolean pEnabled) {
        mZoomOutEnabled = pEnabled;
    }

    public CustomZoomButtonsDisplay getDisplay() {
        return mDisplay;
    }

    public void setOnZoomListener(final OnZoomListener pListener) {
        mListener = pListener;
    }

    public void setVisibility(final Visibility pVisibility) {
        mVisibility = pVisibility;
        switch (mVisibility) {
            case ALWAYS:
                mAlpha01 = 1;
                break;
            case NEVER:
            case SHOW_AND_FADEOUT:
                mAlpha01 = 0;
                break;
        }
    }

    public void setShowFadeOutDelays(final int pShowDelayInMillis,
                                     final int pFadeOutAnimationDurationInMillis) {
        mShowDelayInMillis = pShowDelayInMillis;
        mFadeOutAnimationDurationInMillis = pFadeOutAnimationDurationInMillis;
    }

    public void onDetach() {
        detached = true;
        stopFadeOut();
    }

    private void startFadeOut() {
        if (detached) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mFadeOutAnimation.setStartDelay(0);
            mFadeOutAnimation.start();
        } else {
            mAlpha01 = 0;
            invalidate();
        }
    }

    private void stopFadeOut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mFadeOutAnimation.cancel();
        }
    }

    private void invalidate() {
        if (detached) {
            return;
        }
        mMapView.postInvalidate();
    }

    public void activate() {
        if (detached) {
            return;
        }
        if (mVisibility != Visibility.SHOW_AND_FADEOUT) {
            return;
        }
        final float alpha = mAlpha01;
        if (!mJustActivated) {
            mJustActivated = alpha == 0;
        } else {
            mJustActivated = false;
        }
        stopFadeOut();
        mAlpha01 = 1;
        invalidate();

        postToMainThread_postponeFadeOut();
    }

    private boolean postToMainThread_postponeFadeOut() {
        mMainHandler.removeCallbacksAndMessages(null);
        return mMainHandler.sendEmptyMessageDelayed(TH_MESSAGE_POSTPONEFADEOUT, mShowDelayInMillis);
    }

    private boolean checkJustActivated() {
        if (mJustActivated) {
            mJustActivated = false;
            return true;
        }
        return false;
    }

    public boolean isTouched(final MotionEvent pMotionEvent) {
        if (mAlpha01 == 0) {
            return false;
        }
        if (checkJustActivated()) {
            return false;
        }
        if (mDisplay.isTouched(pMotionEvent, true)) {
            if (mZoomInEnabled && mListener != null) {
                mListener.onZoom(true);
            }
            return true;
        }
        if (mDisplay.isTouched(pMotionEvent, false)) {
            if (mZoomOutEnabled && mListener != null) {
                mListener.onZoom(false);
            }
            return true;
        }
        return false;
    }

    public interface OnZoomListener {
        void onVisibilityChanged(boolean b);

        void onZoom(boolean b);
    }

    @Deprecated
    public boolean onSingleTapConfirmed(final MotionEvent pMotionEvent) {
        return isTouched(pMotionEvent);
    }

    @Deprecated
    public boolean onLongPress(final MotionEvent pMotionEvent) {
        return isTouched(pMotionEvent);
    }

    @UiThread @MainThread
    public void draw(final Canvas pCanvas) {
        mDisplay.draw(pCanvas, mAlpha01, mZoomInEnabled, mZoomOutEnabled);
    }
}
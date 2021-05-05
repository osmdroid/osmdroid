package org.osmdroid.views;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.os.Build;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class CustomZoomButtonsController {

    public enum Visibility {ALWAYS, NEVER, SHOW_AND_FADEOUT}

    private final Object mThreadSync = new Object();
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
    private long mLatestActivation;
    private Thread mThread;
    private final Runnable mRunnable;

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
        mRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    final long pending = mLatestActivation + mShowDelayInMillis - nowInMillis();
                    if (pending <= 0) {
                        break;
                    }
                    try {
                        Thread.sleep(pending, 0);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                startFadeOut();
            }
        };
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

    private long nowInMillis() {
        return System.currentTimeMillis();
    }

    private void startFadeOut() {
        if (detached) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mFadeOutAnimation.setStartDelay(0);
            mMapView.post(new Runnable() {
                @Override
                public void run() {
                    mFadeOutAnimation.start();
                }
            });
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
        mLatestActivation = nowInMillis();
        invalidate();
        if (mThread == null || mThread.getState() == Thread.State.TERMINATED) {
            synchronized (mThreadSync) {
                if (mThread == null || mThread.getState() == Thread.State.TERMINATED) {
                    mThread = new Thread(mRunnable);
                    mThread.setName(this.getClass().getName() + "#active");
                    mThread.start();
                }
            }
        }
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

    public void draw(final Canvas pCanvas) {
        mDisplay.draw(pCanvas, mAlpha01, mZoomInEnabled, mZoomOutEnabled);
    }
}
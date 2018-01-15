// Created by plusminus on 21:37:08 - 27.09.2008
package org.osmdroid.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView.OnFirstLayoutListener;
import org.osmdroid.views.util.MyMath;

import java.util.LinkedList;


/**
 * @author Nicolas Gramlich
 * @author Marc Kurtz
 */
public class MapController implements IMapController, OnFirstLayoutListener {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected final MapView mMapView;

    // Zoom animations
    private ScaleAnimation mZoomInAnimationOld;
    private ScaleAnimation mZoomOutAnimationOld;

    private Animator mCurrentAnimator;

    // Keep track of calls before initial layout
    private ReplayController mReplayController;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapController(MapView mapView) {
        mMapView = mapView;

        // Keep track of initial layout
        mReplayController = new ReplayController();
        if (!mMapView.isLayoutOccurred()) {
            mMapView.addOnFirstLayoutListener(this);
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            ZoomAnimationListener zoomAnimationListener = new ZoomAnimationListener(this);
            mZoomInAnimationOld = new ScaleAnimation(1, 2, 1, 2, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
            mZoomOutAnimationOld = new ScaleAnimation(1, 0.5f, 1, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mZoomInAnimationOld.setDuration(Configuration.getInstance().getAnimationSpeedShort());
            mZoomOutAnimationOld.setDuration(Configuration.getInstance().getAnimationSpeedShort());
            mZoomInAnimationOld.setAnimationListener(zoomAnimationListener);
            mZoomOutAnimationOld.setAnimationListener(zoomAnimationListener);
        }
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        mReplayController.replayCalls();
    }

    @Deprecated
    public void zoomToSpan(final BoundingBoxE6 bb) {
        zoomToSpan(bb.getLatitudeSpanE6(), bb.getLongitudeSpanE6());
    }

    @Override
    public void zoomToSpan(double latSpan, double lonSpan) {
        if (latSpan <= 0 || lonSpan <= 0) {
            return;
        }

        // If no layout, delay this call
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.zoomToSpan(latSpan, lonSpan);
            return;
        }

        final BoundingBox bb = this.mMapView.getProjection().getBoundingBox();
        final double curZoomLevel = this.mMapView.getProjection().getZoomLevel();

        final double curLatSpan = bb.getLatitudeSpan();
        final double curLonSpan = bb.getLongitudeSpan();

        final double diffNeededLat = (double) latSpan / curLatSpan; // i.e. 600/500 = 1,2
        final double diffNeededLon = (double) lonSpan / curLonSpan; // i.e. 300/400 = 0,75

        final double diffNeeded = Math.max(diffNeededLat, diffNeededLon); // i.e. 1,2

        if (diffNeeded > 1) { // Zoom Out
            this.mMapView.setZoomLevel(curZoomLevel - MyMath.getNextSquareNumberAbove((float) diffNeeded));
        } else if (diffNeeded < 0.5) { // Can Zoom in
            this.mMapView.setZoomLevel(curZoomLevel
                + MyMath.getNextSquareNumberAbove(1 / (float) diffNeeded) - 1);
        }
    }

    // TODO rework zoomToSpan
    @Override
    public void zoomToSpan(int latSpanE6, int lonSpanE6) {
        zoomToSpan(latSpanE6 * 1E-6, lonSpanE6 * 1E-6);
    }

    /**
     * Start animating the map towards the given point.
     */
    @Override
    public void animateTo(final IGeoPoint point) {
        // If no layout, delay this call
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.animateTo(point);
            return;
        }
        Point p = mMapView.getProjection().toPixels(point, null);
        animateTo(p.x, p.y);
    }

    /**
     * Start animating the map towards the given point.
     */
    @Override
    public void animateTo(int x, int y) {
        // If no layout, delay this call
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.animateTo(x, y);
            return;
        }

        if (!mMapView.isAnimating()) {
            mMapView.mIsFlinging = false;
            final int xStart = (int)mMapView.getMapScrollX();
            final int yStart = (int)mMapView.getMapScrollY();

            final int dx = x - mMapView.getWidth() / 2;
            final int dy = y - mMapView.getHeight() / 2;

            if (dx != xStart || dy != yStart) {
                mMapView.getScroller().startScroll(xStart, yStart, dx, dy, Configuration.getInstance().getAnimationSpeedDefault());
                mMapView.postInvalidate();
            }
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        this.mMapView.scrollBy(x, y);
    }

    /**
     * Set the map view to the given center. There will be no animation.
     */
    @Override
    public void setCenter(final IGeoPoint point) {
        // If no layout, delay this call
        if (mMapView.mListener != null) {
            mMapView.mListener.onScroll(new ScrollEvent(mMapView, 0, 0));
        }
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.setCenter(point);
            return;
        }
        mMapView.setExpectedCenter(point);
    }

    @Override
    public void stopPanning() {
        mMapView.mIsFlinging = false;
        mMapView.getScroller().forceFinished(true);
    }

    /**
     * Stops a running animation.
     *
     * @param jumpToTarget
     */
    @Override
    public void stopAnimation(final boolean jumpToTarget) {

        if (!mMapView.getScroller().isFinished()) {
            if (jumpToTarget) {
                mMapView.mIsFlinging = false;
                mMapView.getScroller().abortAnimation();
            } else
                stopPanning();
        }

        // We ignore the jumpToTarget for zoom levels since it doesn't make sense to stop
        // the animation in the middle. Maybe we could have it cancel the zoom operation and jump
        // back to original zoom level?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final Animator currentAnimator = this.mCurrentAnimator;
            if (mMapView.mIsAnimating.get()) {
                currentAnimator.end();
            }
        } else {
            if (mMapView.mIsAnimating.get()) {
                mMapView.clearAnimation();
            }
        }
    }

    @Override
    public int setZoom(final int zoomlevel) {
        return (int) setZoom((double) zoomlevel);
    }

    /**
     * @since 6.0
     */
    @Override
    public double setZoom(final double pZoomlevel) {
        return mMapView.setZoomLevel(pZoomlevel);
    }

    /**
     * Zoom in by one zoom level.
     */
    @Override
    public boolean zoomIn() {
        return zoomIn(null);
    }

    @Override
    public boolean zoomIn(Long animationSpeed) {
        return zoomTo(mMapView.getZoomLevelDouble() + 1, animationSpeed);
    }

    /**
     * @param xPixel
     * @param yPixel
     * @param zoomAnimation if null, the default is used
     * @return
     */
    @Override
    public boolean zoomInFixing(final int xPixel, final int yPixel, Long zoomAnimation) {
        return zoomToFixing(mMapView.getZoomLevelDouble() + 1, xPixel, yPixel, zoomAnimation);
    }

    @Override
    public boolean zoomInFixing(final int xPixel, final int yPixel) {
        return zoomInFixing(xPixel, yPixel, null);
    }

    @Override
    public boolean zoomOut(Long animationSpeed) {
        return zoomTo(mMapView.getZoomLevelDouble() - 1, animationSpeed);
    }

    /**
     * Zoom out by one zoom level.
     */
    @Override
    public boolean zoomOut() {
        return zoomOut(null);
    }

    @Deprecated
    @Override
    public boolean zoomOutFixing(final int xPixel, final int yPixel) {
        return zoomToFixing(mMapView.getZoomLevelDouble() - 1, xPixel, yPixel, null);
    }

    @Override
    public boolean zoomTo(int zoomLevel) {
        return zoomTo(zoomLevel, null);
    }

    /**
     * @since 6.0
     */
    @Override
    public boolean zoomTo(int zoomLevel, Long animationSpeed) {
        return zoomTo((double)zoomLevel, animationSpeed);
    }

    /**
     * @param zoomLevel
     * @param xPixel
     * @param yPixel
     * @param zoomAnimationSpeed time in milliseconds, if null, the default settings will be used
     * @return
     * @since 6.0.0
     */
    @Override
    public boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel, Long zoomAnimationSpeed) {
        return zoomToFixing((double) zoomLevel, xPixel, yPixel, zoomAnimationSpeed);
    }

    @Override
    public boolean zoomTo(double pZoomLevel, Long animationSpeed) {
        return zoomToFixing(pZoomLevel, mMapView.getWidth() / 2, mMapView.getHeight() / 2, animationSpeed);
    }

    @Override
    public boolean zoomTo(double pZoomLevel) {
        return zoomTo(pZoomLevel, null);
    }


    @Override
    public boolean zoomToFixing(double zoomLevel, int xPixel, int yPixel, Long zoomAnimationSpeed) {
        zoomLevel = zoomLevel > mMapView.getMaxZoomLevel() ? mMapView.getMaxZoomLevel() : zoomLevel;
        zoomLevel = zoomLevel < mMapView.getMinZoomLevel() ? mMapView.getMinZoomLevel() : zoomLevel;

        double currentZoomLevel = mMapView.getZoomLevelDouble();
        boolean canZoom = zoomLevel < currentZoomLevel && mMapView.canZoomOut() ||
            zoomLevel > currentZoomLevel && mMapView.canZoomIn();

        if (!canZoom) {
            return false;
        }
        if (mMapView.mIsAnimating.getAndSet(true)) {
            // TODO extend zoom (and return true)
            return false;
        }
        if (mMapView.mListener != null) {
            mMapView.mListener.onZoom(new ZoomEvent(mMapView, zoomLevel));
        }
        mMapView.setMultiTouchScaleInitPoint(xPixel, yPixel);
        mMapView.startAnimation();

        float end = (float) Math.pow(2.0, zoomLevel - currentZoomLevel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ZoomAnimatorListener zoomAnimatorListener = new ZoomAnimatorListener(this);
            ValueAnimator zoomToAnimator = ValueAnimator.ofFloat(1f, end);
            zoomToAnimator.addListener(zoomAnimatorListener);
            zoomToAnimator.addUpdateListener(zoomAnimatorListener);
            if (zoomAnimationSpeed == null) {
                zoomToAnimator.setDuration(Configuration.getInstance().getAnimationSpeedShort());
            } else {
                zoomToAnimator.setDuration(zoomAnimationSpeed);
            }

            mCurrentAnimator = zoomToAnimator;
            zoomToAnimator.start();
            return true;
        }
        if (zoomLevel > currentZoomLevel)
            mMapView.startAnimation(mZoomInAnimationOld);
        else
            mMapView.startAnimation(mZoomOutAnimationOld);
        ScaleAnimation scaleAnimation;

        scaleAnimation = new ScaleAnimation(
                1f, end, //X
                1f, end, //Y
                Animation.RELATIVE_TO_SELF, 0.5f, //Pivot X
                Animation.RELATIVE_TO_SELF, 0.5f); //Pivot Y
        if (zoomAnimationSpeed == null) {
            scaleAnimation.setDuration(Configuration.getInstance().getAnimationSpeedShort());
        } else {
            scaleAnimation.setDuration(zoomAnimationSpeed);
        }
        scaleAnimation.setAnimationListener(new ZoomAnimationListener(this));
        return true;
    }

    /**
     * @since 6.0
     */
    @Override
    public boolean zoomToFixing(double zoomLevel, int xPixel, int yPixel) {
        return zoomToFixing(zoomLevel, xPixel, yPixel, null);
    }

    @Override
    public boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel) {
        return zoomToFixing(zoomLevel, xPixel, yPixel, null);
    }


    protected void onAnimationStart() {
        mMapView.mIsAnimating.set(true);
    }

    protected void onAnimationEnd() {
        mMapView.mIsAnimating.set(false);
        mMapView.resetMultiTouchScale();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mCurrentAnimator = null;
        } else { // Fix for issue 477
            mMapView.clearAnimation();
            mZoomInAnimationOld.reset();
            mZoomOutAnimationOld.reset();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class ZoomAnimatorListener
        implements Animator.AnimatorListener, AnimatorUpdateListener {

        private MapController mMapController;

        public ZoomAnimatorListener(MapController mapController) {
            mMapController = mapController;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            mMapController.onAnimationStart();
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mMapController.onAnimationEnd();
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            //noOp
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
            //noOp
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mMapController.mMapView.setMultiTouchScale((Float) valueAnimator.getAnimatedValue());
            mMapController.mMapView.invalidate();
        }
    }

    protected static class ZoomAnimationListener implements AnimationListener {

        private MapController mMapController;

        public ZoomAnimationListener(MapController mapController) {
            mMapController = mapController;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            mMapController.onAnimationStart();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mMapController.onAnimationEnd();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //noOp
        }
    }

    private enum ReplayType {
        ZoomToSpanPoint, AnimateToPoint, AnimateToGeoPoint, SetCenterPoint
    }

    ;

    private class ReplayController {
        private LinkedList<ReplayClass> mReplayList = new LinkedList<ReplayClass>();

        public void animateTo(IGeoPoint geoPoint) {
            mReplayList.add(new ReplayClass(ReplayType.AnimateToGeoPoint, null, geoPoint));
        }

        public void animateTo(int x, int y) {
            mReplayList.add(new ReplayClass(ReplayType.AnimateToPoint, new Point(x, y), null));
        }

        public void setCenter(IGeoPoint geoPoint) {
            mReplayList.add(new ReplayClass(ReplayType.SetCenterPoint, null, geoPoint));
        }

        public void zoomToSpan(int x, int y) {
            mReplayList.add(new ReplayClass(ReplayType.ZoomToSpanPoint, new Point(x, y), null));
        }

        public void zoomToSpan(double x, double y) {
            mReplayList.add(new ReplayClass(ReplayType.ZoomToSpanPoint, new Point((int) (x * 1E6), (int) (y * 1E6)), null));
        }


        public void replayCalls() {
            for (ReplayClass replay : mReplayList) {
                switch (replay.mReplayType) {
                    case AnimateToGeoPoint:
                        if (replay.mGeoPoint != null)
                            MapController.this.animateTo(replay.mGeoPoint);
                        break;
                    case AnimateToPoint:
                        if (replay.mPoint != null)
                            MapController.this.animateTo(replay.mPoint.x, replay.mPoint.y);
                        break;
                    case SetCenterPoint:
                        if (replay.mGeoPoint != null)
                            MapController.this.setCenter(replay.mGeoPoint);
                        break;
                    case ZoomToSpanPoint:
                        if (replay.mPoint != null)
                            MapController.this.zoomToSpan(replay.mPoint.x, replay.mPoint.y);
                        break;
                }
            }
            mReplayList.clear();
        }

        private class ReplayClass {
            private ReplayType mReplayType;
            private Point mPoint;
            private IGeoPoint mGeoPoint;

            public ReplayClass(ReplayType mReplayType, Point mPoint, IGeoPoint mGeoPoint) {
                super();
                this.mReplayType = mReplayType;
                this.mPoint = mPoint;
                this.mGeoPoint = mGeoPoint;
            }
        }
    }

}

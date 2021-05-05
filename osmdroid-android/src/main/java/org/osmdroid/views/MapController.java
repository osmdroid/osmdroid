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
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MyMath;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView.OnFirstLayoutListener;

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
    private double mTargetZoomLevel = 0;

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
        animateTo(point, null, null);
    }

    /**
     * @since 6.0.3
     */
    @Override
    public void animateTo(final IGeoPoint point, final Double pZoom, final Long pSpeed, final Float pOrientation) {
        animateTo(point, pZoom, pSpeed, pOrientation, null);
    }

    /**
     * @since 6.1.0
     */
    @Override
    public void animateTo(final IGeoPoint point, final Double pZoom, final Long pSpeed, final Float pOrientation, final Boolean pClockwise) {
        // If no layout, delay this call
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.animateTo(point, pZoom, pSpeed, pOrientation, pClockwise);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final IGeoPoint currentCenter = new GeoPoint(mMapView.getProjection().getCurrentCenter());
            final MapAnimatorListener mapAnimatorListener =
                    new MapAnimatorListener(this,
                            mMapView.getZoomLevelDouble(), pZoom,
                            currentCenter, point,
                            mMapView.getMapOrientation(), pOrientation, pClockwise);
            final ValueAnimator mapAnimator = ValueAnimator.ofFloat(0, 1);
            mapAnimator.addListener(mapAnimatorListener);
            mapAnimator.addUpdateListener(mapAnimatorListener);
            if (pSpeed == null) {
                mapAnimator.setDuration(Configuration.getInstance().getAnimationSpeedDefault());
            } else {
                mapAnimator.setDuration(pSpeed);
            }

            if (mCurrentAnimator != null) {
                mapAnimatorListener.onAnimationCancel(mCurrentAnimator);
            }
            mCurrentAnimator = mapAnimator;
            mapAnimator.start();
            return;
        }
        // TODO handle the zoom and orientation parts for the .3% of the population below HONEYCOMB (Feb. 2018)
        Point p = mMapView.getProjection().toPixels(point, null);
        animateTo(p.x, p.y);
    }

    /**
     * @since 6.0.2
     */
    @Override
    public void animateTo(final IGeoPoint pPoint, final Double pZoom, final Long pSpeed) {
        animateTo(pPoint, pZoom, pSpeed, null);
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
            final int xStart = (int) mMapView.getMapScrollX();
            final int yStart = (int) mMapView.getMapScrollY();

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final Animator currentAnimator = this.mCurrentAnimator;
            if (mMapView.mIsAnimating.get()) {
                if (jumpToTarget) {
                    currentAnimator.end();
                } else {
                    currentAnimator.cancel();
                }
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
        return zoomTo((double) zoomLevel, animationSpeed);
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
        ZoomEvent event = null;
        for (MapListener mapListener : mMapView.mListners) {
            mapListener.onZoom(event != null ? event : (event = new ZoomEvent(mMapView, zoomLevel)));
        }
        mMapView.setMultiTouchScaleInitPoint(xPixel, yPixel);
        mMapView.startAnimation();

        float end = (float) Math.pow(2.0, zoomLevel - currentZoomLevel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final MapAnimatorListener zoomAnimatorListener = new MapAnimatorListener(this,
                    currentZoomLevel, zoomLevel,
                    null, null,
                    null, null, null);
            final ValueAnimator zoomToAnimator = ValueAnimator.ofFloat(0, 1);
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
        mTargetZoomLevel = zoomLevel;
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
            setZoom(mTargetZoomLevel);
        }
        mMapView.invalidate();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class MapAnimatorListener
            implements Animator.AnimatorListener, AnimatorUpdateListener {

        private final GeoPoint mCenter = new GeoPoint(0., 0);
        private final MapController mMapController;
        private final Double mZoomStart;
        private final Double mZoomEnd;
        private final IGeoPoint mCenterStart;
        private final IGeoPoint mCenterEnd;
        private final Float mOrientationStart;
        private final Float mOrientationSpan;

        public MapAnimatorListener(final MapController pMapController,
                                   final Double pZoomStart, final Double pZoomEnd,
                                   final IGeoPoint pCenterStart, final IGeoPoint pCenterEnd,
                                   final Float pOrientationStart, final Float pOrientationEnd,
                                   final Boolean pClockwise) {
            mMapController = pMapController;
            mZoomStart = pZoomStart;
            mZoomEnd = pZoomEnd;
            mCenterStart = pCenterStart;
            mCenterEnd = pCenterEnd;
            if (pOrientationEnd == null) {
                mOrientationStart = null;
                mOrientationSpan = null;
            } else {
                mOrientationStart = pOrientationStart;
                mOrientationSpan = (float) org.osmdroid.util.MyMath.getAngleDifference(mOrientationStart, pOrientationEnd, pClockwise);
            }
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
            mMapController.onAnimationEnd();
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
            //noOp
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            final float value = (Float) valueAnimator.getAnimatedValue();
            if (mZoomEnd != null) {
                final double zoom = mZoomStart + (mZoomEnd - mZoomStart) * value;
                //map events listeners are triggered by this call
                mMapController.mMapView.setZoomLevel(zoom);
            }
            if (mOrientationSpan != null) {
                final float orientation = mOrientationStart + mOrientationSpan * value;
                //map events listeners are triggered by this call
                mMapController.mMapView.setMapOrientation(orientation);
            }
            if (mCenterEnd != null) {
                final TileSystem tileSystem = mMapController.mMapView.getTileSystem();
                final double longitudeStart = tileSystem.cleanLongitude(mCenterStart.getLongitude());
                final double longitudeEnd = tileSystem.cleanLongitude(mCenterEnd.getLongitude());
                final double longitude = tileSystem.cleanLongitude(longitudeStart + (longitudeEnd - longitudeStart) * value);
                final double latitudeStart = tileSystem.cleanLatitude(mCenterStart.getLatitude());
                final double latitudeEnd = tileSystem.cleanLatitude(mCenterEnd.getLatitude());
                final double latitude = tileSystem.cleanLatitude(latitudeStart + (latitudeEnd - latitudeStart) * value);
                mCenter.setCoords(latitude, longitude);
                mMapController.mMapView.setExpectedCenter(mCenter);
            }
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

        public void animateTo(IGeoPoint geoPoint,
                              Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise) {
            mReplayList.add(new ReplayClass(ReplayType.AnimateToGeoPoint, null, geoPoint,
                    pZoom, pSpeed, pOrientation, pClockwise));
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
                            MapController.this.animateTo(replay.mGeoPoint, replay.mZoom, replay.mSpeed, replay.mOrientation, replay.mClockwise);
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
            private final Long mSpeed;
            private final Double mZoom;
            private final Float mOrientation;
            private final Boolean mClockwise;

            public ReplayClass(ReplayType mReplayType, Point mPoint, IGeoPoint mGeoPoint) {
                this(mReplayType, mPoint, mGeoPoint, null, null, null, null);
            }

            /**
             * @since 6.0.2
             */
            public ReplayClass(ReplayType pReplayType, Point pPoint, IGeoPoint pGeoPoint,
                               Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise) {
                mReplayType = pReplayType;
                mPoint = pPoint;
                mGeoPoint = pGeoPoint;
                mSpeed = pSpeed;
                mZoom = pZoom;
                mOrientation = pOrientation;
                mClockwise = pClockwise;
            }
        }
    }

}

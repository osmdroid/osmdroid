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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MyMath;
import org.osmdroid.util.ReusablePool;
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
    private final ReplayController mReplayController;
    private final ReusablePool<ZoomEvent> mZoomEventsCache = new ReusablePool<>(ZoomEvent::newInstanceForReusablePool, 2);

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapController(@NonNull final MapView mapView) {
        mMapView = mapView;

        // Keep track of initial layout
        mReplayController = new ReplayController();
        if (!mMapView.isLayoutOccurred()) {
            mMapView.addOnFirstLayoutListener(this);
        }
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        mReplayController.replayCalls();
    }

    @Override
    public void zoomToSpan(final double latSpan, final double lonSpan) {
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

        final double diffNeededLat = latSpan / curLatSpan; // i.e. 600/500 = 1,2
        final double diffNeededLon = lonSpan / curLonSpan; // i.e. 300/400 = 0,75

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
    public void animateTo(@NonNull final IGeoPoint point) {
        animateTo(point.getLatitude(), point.getLongitude(), null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void animateTo(final double lat, final double lon) {
        animateTo(lat, lon, null, null, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void animateTo(@NonNull final IGeoPoint point, final Double pZoom, final Long pSpeed, final Float pOrientation) {
        animateTo(point.getLatitude(), point.getLongitude(), pZoom, pSpeed, pOrientation, null);
    }

    /** {@inheritDoc} */
    @Override
    public void animateTo(@NonNull final Double pointLat, @NonNull final Double pointLon, final Double pZoom, final Long pSpeed, final Float pOrientation) {
        animateTo(pointLat, pointLon, pZoom, pSpeed, pOrientation, null);
    }

    /** {@inheritDoc} */
    @Override
    public void animateTo(@NonNull final IGeoPoint point, final Double pZoom, final Long pSpeed, final Float pOrientation, final Boolean pClockwise) {
        animateTo(point.getLatitude(), point.getLongitude(), pZoom, pSpeed, pOrientation, pClockwise);
    }

    /** {@inheritDoc} */
    @Override
    public void animateTo(final double pointLat, final double pointLon, final Double pZoom, final Long pSpeed, final Float pOrientation, final Boolean pClockwise) {
        // If no layout, delay this call
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.animateTo(pointLat, pointLon, pZoom, pSpeed, pOrientation, pClockwise);
            return;
        }
        final IGeoPoint currentCenter = mMapView.getProjection().getCurrentCenter();
        final MapAnimatorListener mapAnimatorListener =
            new MapAnimatorListener(this,
                mMapView.getZoomLevelDouble(), pZoom,
                currentCenter.getLatitude(), currentCenter.getLongitude(),
                pointLat, pointLon,
                mMapView.getMapOrientation(), pOrientation, pClockwise);
        final ValueAnimator mapAnimator = ValueAnimator.ofFloat(0, 1);
        mapAnimator.addListener(mapAnimatorListener);
        mapAnimator.addUpdateListener(mapAnimatorListener);
        if (pSpeed == null) {
            mapAnimator.setDuration(Configuration.getInstance().getAnimationSpeedDefault());
        } else {
            mapAnimator.setDuration(pSpeed);
        }

        if (mCurrentAnimator != null) mapAnimatorListener.onAnimationCancel(mCurrentAnimator);
        mCurrentAnimator = mapAnimator;
        mapAnimator.start();
    }

    /**
     * @since 6.0.2
     */
    @Override
    public void animateTo(@NonNull final IGeoPoint pPoint, final Double pZoom, final Long pSpeed) {
        animateTo(pPoint, pZoom, pSpeed, null);
    }

    /**
     * @since 6.1.18
     */
    @Override
    public void animateTo(@NonNull final Double pointLat, @NonNull Double pointLon, Double pZoom, Long pSpeed) {
        animateTo(pointLat, pointLon, pZoom, pSpeed, null);
    }

    /**
     * Start animating the map towards the given point.
     */
    @Override
    public void animateTo(final int x, final int y) {
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
    public void setCenter(@NonNull final IGeoPoint point) {
        // If no layout, delay this call
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.setCenter(point);
            return;
        }
        mMapView.setExpectedCenter(point);
    }

    /**
     * Set the map view to the given center. There will be no animation.
     */
    @Override
    public void setCenter(final double lat, final double lon) {
        // If no layout, delay this call
        if (!mMapView.isLayoutOccurred()) {
            mReplayController.setCenter(lat, lon);
            return;
        }
        mMapView.setExpectedCenter(lat, lon);
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

        final Animator currentAnimator = this.mCurrentAnimator;
        if (mMapView.mIsAnimating.get()) {
            if (jumpToTarget) {
                currentAnimator.end();
            } else {
                currentAnimator.cancel();
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
    public boolean zoomIn(@Nullable final Long animationSpeed) {
        return zoomTo(mMapView.getZoomLevelDouble() + 1, animationSpeed);
    }

    /**
     * @param xPixel
     * @param yPixel
     * @param zoomAnimation if null, the default is used
     * @return
     */
    @Override
    public boolean zoomInFixing(final int xPixel, final int yPixel, @Nullable final Long zoomAnimation) {
        return zoomToFixing(mMapView.getZoomLevelDouble() + 1, xPixel, yPixel, zoomAnimation);
    }

    @Override
    public boolean zoomInFixing(final int xPixel, final int yPixel) {
        return zoomInFixing(xPixel, yPixel, null);
    }

    @Override
    public boolean zoomOut(@Nullable final Long animationSpeed) {
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
    public boolean zoomTo(int zoomLevel, @Nullable final Long animationSpeed) {
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
    public boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel, @Nullable final Long zoomAnimationSpeed) {
        return zoomToFixing((double) zoomLevel, xPixel, yPixel, zoomAnimationSpeed);
    }

    @Override
    public boolean zoomTo(double pZoomLevel, @Nullable final Long animationSpeed) {
        return zoomToFixing(pZoomLevel, mMapView.getWidth() / 2, mMapView.getHeight() / 2, animationSpeed);
    }

    @Override
    public boolean zoomTo(double pZoomLevel) {
        return zoomTo(pZoomLevel, null);
    }


    @Override
    public boolean zoomToFixing(double zoomLevel, int xPixel, int yPixel, @Nullable final Long zoomAnimationSpeed) {
        zoomLevel = zoomLevel > mMapView.getMaxZoomLevel() ? mMapView.getMaxZoomLevel() : zoomLevel;
        zoomLevel = Math.max(zoomLevel, mMapView.getMinZoomLevel());

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
        final ZoomEvent event = mZoomEventsCache.getFreeItemFromPoll();
        ZoomEvent.set(event, mMapView, zoomLevel);
        for (MapListener mapListener : mMapView.mListeners) {
            mapListener.onZoom(event);
        }
        mZoomEventsCache.returnItemToPool(event);
        mMapView.setMultiTouchScaleInitPoint(xPixel, yPixel);
        mMapView.startAnimation();

        float end = (float) Math.pow(2.0, zoomLevel - currentZoomLevel);
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
        mCurrentAnimator = null;
        mMapView.invalidate();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class MapAnimatorListener
            implements Animator.AnimatorListener, AnimatorUpdateListener {

        private final GeoPoint mCenter = new GeoPoint(0., 0);
        private final MapController mMapController;
        private final Double mZoomStart;
        private final Double mZoomEnd;
        private final Double mCenterStartLat;
        private final Double mCenterStartLon;
        private final Double mCenterEndLat;
        private final Double mCenterEndLon;
        private final Float mOrientationStart;
        private final Float mOrientationSpan;

        public MapAnimatorListener(final MapController pMapController,
                                   final Double pZoomStart, final Double pZoomEnd,
                                   @Nullable final IGeoPoint pCenterStart,
                                   @Nullable final IGeoPoint pCenterEnd,
                                   final Float pOrientationStart, final Float pOrientationEnd,
                                   final Boolean pClockwise) {
            this(pMapController, pZoomStart, pZoomEnd, ((pCenterStart != null) ? pCenterStart.getLatitude() : null), ((pCenterStart != null) ? pCenterStart.getLongitude() : null), ((pCenterEnd != null) ? pCenterEnd.getLatitude() : null), ((pCenterEnd != null) ? pCenterEnd.getLongitude() : null), pOrientationStart, pOrientationEnd, pClockwise);
        }
        public MapAnimatorListener(final MapController pMapController,
                                   final Double pZoomStart, final Double pZoomEnd,
                                   @Nullable final Double pCenterStartLat, @Nullable final Double pCenterStartLon,
                                   @Nullable final Double pCenterEndLat, @Nullable final Double pCenterEndLon,
                                   final Float pOrientationStart, final Float pOrientationEnd,
                                   final Boolean pClockwise) {
            mMapController = pMapController;
            mZoomStart = pZoomStart;
            mZoomEnd = pZoomEnd;
            mCenterStartLat = pCenterStartLat;
            mCenterStartLon = pCenterStartLon;
            mCenterEndLat = pCenterEndLat;
            mCenterEndLon = pCenterEndLon;
            if (pOrientationEnd == null) {
                mOrientationStart = null;
                mOrientationSpan = null;
            } else {
                mOrientationStart = pOrientationStart;
                mOrientationSpan = (float) org.osmdroid.util.MyMath.getAngleDifference(mOrientationStart, pOrientationEnd, pClockwise);
            }
        }

        @Override
        public void onAnimationStart(@NonNull Animator animator) {
            mMapController.onAnimationStart();
        }

        @Override
        public void onAnimationEnd(@NonNull Animator animator) {
            mMapController.onAnimationEnd();
        }

        @Override
        public void onAnimationCancel(@NonNull Animator animator) {
            mMapController.onAnimationEnd();
        }

        @Override
        public void onAnimationRepeat(@NonNull Animator animator) {
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
            if ((mCenterEndLat != null) && (mCenterEndLon != null)) {
                final TileSystem tileSystem = MapView.getTileSystem();
                final double longitudeStart = tileSystem.cleanLongitude(mCenterStartLon);
                final double longitudeEnd = tileSystem.cleanLongitude(mCenterEndLon);
                final double longitude = tileSystem.cleanLongitude(longitudeStart + (longitudeEnd - longitudeStart) * value);
                final double latitudeStart = tileSystem.cleanLatitude(mCenterStartLat);
                final double latitudeEnd = tileSystem.cleanLatitude(mCenterEndLat);
                final double latitude = tileSystem.cleanLatitude(latitudeStart + (latitudeEnd - latitudeStart) * value);
                mCenter.setCoords(latitude, longitude);
                mMapController.mMapView.setExpectedCenter(mCenter);
            }
            mMapController.mMapView.invalidate();
        }
    }

    protected static class ZoomAnimationListener implements AnimationListener {

        private final MapController mMapController;

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
        private final ReusablePool<ReplayClass> mReusablePool = new ReusablePool<ReplayClass>(new ReusablePool.IReusablePoolItemCallback<ReplayClass>() {
            @Override public ReplayClass newInstance() { return new ReplayClass(); }
        }, 16);
        private final LinkedList<ReplayClass> mReplayList = new LinkedList<ReplayClass>();

        private void animateTo(@NonNull final IGeoPoint geoPoint, Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise) {
            mReplayList.add(this.mReusablePool.getFreeItemFromPoll().set(ReplayType.AnimateToGeoPoint, null, null, geoPoint, pZoom, pSpeed, pOrientation, pClockwise));
        }

        private void animateTo(@Nullable Double geoPointLat, @Nullable Double geoPointLon, Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise) {
            mReplayList.add(this.mReusablePool.getFreeItemFromPoll().set(ReplayType.AnimateToGeoPoint, null, null, geoPointLat, geoPointLon, pZoom, pSpeed, pOrientation, pClockwise));
        }

        private void animateTo(int x, int y) {
            mReplayList.add(this.mReusablePool.getFreeItemFromPoll().set(ReplayType.AnimateToPoint, x, y, null));
        }

        private void setCenter(@NonNull final IGeoPoint geoPoint) {
            mReplayList.add(this.mReusablePool.getFreeItemFromPoll().set(ReplayType.SetCenterPoint, null, null, geoPoint));
        }
        private void setCenter(final double geoPointLat, final double geoPointLon) {
            mReplayList.add(this.mReusablePool.getFreeItemFromPoll().set(ReplayType.SetCenterPoint, null, null, geoPointLat, geoPointLon));
        }

        private void zoomToSpan(int x, int y) {
            mReplayList.add(this.mReusablePool.getFreeItemFromPoll().set(ReplayType.ZoomToSpanPoint, x, y, null));
        }

        private void zoomToSpan(double x, double y) {
            mReplayList.add(this.mReusablePool.getFreeItemFromPoll().set(ReplayType.ZoomToSpanPoint, (int) (x * 1E6), (int) (y * 1E6), null));
        }


        private void replayCalls() {
            for (ReplayClass replay : mReplayList) {
                switch (replay.mReplayType) {
                    case AnimateToGeoPoint:
                        if ((replay.mGeoPointLat != null) && (replay.mGeoPointLon != null))
                            MapController.this.animateTo(replay.mGeoPointLat, replay.mGeoPointLon, replay.mZoom, replay.mSpeed, replay.mOrientation, replay.mClockwise);
                        break;
                    case AnimateToPoint:
                        if ((replay.mX != null) && (replay.mY != null))
                            MapController.this.animateTo(replay.mX, replay.mY);
                        break;
                    case SetCenterPoint:
                        if ((replay.mGeoPointLat != null) && (replay.mGeoPointLon != null))
                            MapController.this.setCenter(replay.mGeoPointLat, replay.mGeoPointLon);
                        break;
                    case ZoomToSpanPoint:
                        if ((replay.mX != null) && (replay.mY != null))
                            MapController.this.zoomToSpan(replay.mX, replay.mY);
                        break;
                }
            }
            this.mReusablePool.returnItemsToPool(mReplayList);
            mReplayList.clear();
        }
    }

    private static class ReplayClass {
        private ReplayType mReplayType;
        private Integer mX;
        private Integer mY;
        private Double mGeoPointLat;
        private Double mGeoPointLon;
        private Long mSpeed;
        private Double mZoom;
        private Float mOrientation;
        private Boolean mClockwise;

        private ReplayClass() {

        }

        private ReplayClass(ReplayType mReplayType, @Nullable final Integer x, @Nullable final Integer y, @NonNull final IGeoPoint mGeoPoint) {
            this(mReplayType, x, y, mGeoPoint, null, null, null, null);
        }

        /**
         * @since 6.0.2
         */
        private ReplayClass(ReplayType pReplayType, @Nullable final Integer x, @Nullable final Integer y, @NonNull final IGeoPoint pGeoPoint,
                            Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise) {
            this.set(pReplayType, x, y, pGeoPoint, pZoom, pSpeed, pOrientation, pClockwise);
        }

        private ReplayClass set(ReplayType mReplayType, @Nullable final Integer x, @Nullable final Integer y, @NonNull final IGeoPoint pGeoPoint) {
            return this.set(mReplayType, x, y, pGeoPoint, null, null, null, null);
        }
        private ReplayClass set(ReplayType pReplayType, @Nullable final Integer x, @Nullable final Integer y, @NonNull final IGeoPoint pGeoPoint,
                         Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise) {
            return this.set(pReplayType, x, y, pGeoPoint.getLatitude(), pGeoPoint.getLongitude(), pZoom, pSpeed, pOrientation, pClockwise);
        }
        private ReplayClass set(ReplayType mReplayType, @Nullable final Integer x, @Nullable final Integer y, final double mGeoPointLat, final double mGeoPointLon) {
            return this.set(mReplayType, x, y, mGeoPointLat, mGeoPointLon, null, null, null, null);
        }
        private ReplayClass set(ReplayType pReplayType, @Nullable final Integer x, @Nullable final Integer y, @Nullable final Double pGeoPointLat, @Nullable Double pGeoPointLon,
                                Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise) {
            mReplayType = pReplayType;
            mX = x;
            mY = y;
            mGeoPointLat = pGeoPointLat;
            mGeoPointLon = pGeoPointLon;
            mSpeed = pSpeed;
            mZoom = pZoom;
            mOrientation = pOrientation;
            mClockwise = pClockwise;
            return this;
        }
    }

}

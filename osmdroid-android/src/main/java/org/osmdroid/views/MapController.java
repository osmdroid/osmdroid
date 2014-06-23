// Created by plusminus on 21:37:08 - 27.09.2008
package org.osmdroid.views;

import java.util.LinkedList;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView.OnFirstLayoutListener;
import org.osmdroid.views.util.MyMath;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

/**
 * 
 * @author Nicolas Gramlich
 * @author Marc Kurtz
 */
public class MapController implements IMapController, MapViewConstants, OnFirstLayoutListener {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final MapView mMapView;

	// Zoom animations
	private ValueAnimator mZoomInAnimation;
	private ValueAnimator mZoomOutAnimation;
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mZoomInAnimation = ValueAnimator.ofFloat(1f, 2f);
			mZoomInAnimation.addListener(new MyZoomAnimatorListener());
			mZoomInAnimation.addUpdateListener(new MyZoomAnimatorUpdateListener());
			mZoomInAnimation.setDuration(ANIMATION_DURATION_SHORT);

			mZoomOutAnimation = ValueAnimator.ofFloat(1f, 0.5f);
			mZoomOutAnimation.addListener(new MyZoomAnimatorListener());
			mZoomOutAnimation.addUpdateListener(new MyZoomAnimatorUpdateListener());
			mZoomOutAnimation.setDuration(ANIMATION_DURATION_SHORT);
		} else {
			mZoomInAnimationOld = new ScaleAnimation(1, 2, 1, 2, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			mZoomOutAnimationOld = new ScaleAnimation(1, 0.5f, 1, 0.5f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			mZoomInAnimationOld.setDuration(ANIMATION_DURATION_SHORT);
			mZoomOutAnimationOld.setDuration(ANIMATION_DURATION_SHORT);
			mZoomInAnimationOld.setAnimationListener(new MyZoomAnimationListener());
			mZoomOutAnimationOld.setAnimationListener(new MyZoomAnimationListener());
		}
	}
	
	@Override
	public void onFirstLayout(View v, int left, int top, int right, int bottom) {
		mReplayController.replayCalls();
	}

	public void zoomToSpan(final BoundingBoxE6 bb) {
		zoomToSpan(bb.getLatitudeSpanE6(), bb.getLongitudeSpanE6());
	}

	// TODO rework zoomToSpan
	@Override
	public void zoomToSpan(int latSpanE6, int lonSpanE6) {
		if (latSpanE6 <= 0 || lonSpanE6 <= 0) {
			return;
		}

		// If no layout, delay this call
		if (!mMapView.isLayoutOccurred()) {
			mReplayController.zoomToSpan(latSpanE6, lonSpanE6);
			return;
		}

		final BoundingBoxE6 bb = this.mMapView.getProjection().getBoundingBox();
		final int curZoomLevel = this.mMapView.getProjection().getZoomLevel();

		final int curLatSpan = bb.getLatitudeSpanE6();
		final int curLonSpan = bb.getLongitudeSpanE6();

		final float diffNeededLat = (float) latSpanE6 / curLatSpan; // i.e. 600/500 = 1,2
		final float diffNeededLon = (float) lonSpanE6 / curLonSpan; // i.e. 300/400 = 0,75

		final float diffNeeded = Math.max(diffNeededLat, diffNeededLon); // i.e. 1,2

		if (diffNeeded > 1) { // Zoom Out
			this.mMapView.setZoomLevel(curZoomLevel - MyMath.getNextSquareNumberAbove(diffNeeded));
		} else if (diffNeeded < 0.5) { // Can Zoom in
			this.mMapView.setZoomLevel(curZoomLevel
					+ MyMath.getNextSquareNumberAbove(1 / diffNeeded) - 1);
		}
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
	public void animateTo(int x, int y) {
		// If no layout, delay this call
		if (!mMapView.isLayoutOccurred()) {
			mReplayController.animateTo(x, y);
			return;
		}

		if (!mMapView.isAnimating()) {
			mMapView.mIsFlinging = false;
			Point mercatorPoint = mMapView.getProjection().toMercatorPixels(x, y, null);
			// The points provided are "center", we want relative to upper-left for scrolling
			mercatorPoint.offset(-mMapView.getWidth() / 2, -mMapView.getHeight() / 2);
			final int xStart = mMapView.getScrollX();
			final int yStart = mMapView.getScrollY();
			mMapView.getScroller().startScroll(xStart, yStart, mercatorPoint.x - xStart,
					mercatorPoint.y - yStart, ANIMATION_DURATION_DEFAULT);
			mMapView.postInvalidate();
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

		Point p = mMapView.getProjection().toPixels(point, null);
		p = mMapView.getProjection().toMercatorPixels(p.x, p.y, p);
		// The points provided are "center", we want relative to upper-left for scrolling
		p.offset(-mMapView.getWidth() / 2, -mMapView.getHeight() / 2);
		mMapView.scrollTo(p.x, p.y);
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
		return mMapView.setZoomLevel(zoomlevel);
	}

	/**
	 * Zoom in by one zoom level.
	 */
	@Override
	public boolean zoomIn() {
		return zoomInFixing(mMapView.getWidth() / 2, mMapView.getHeight() / 2);
	}

	@Override
	public boolean zoomInFixing(final int xPixel, final int yPixel) {
		mMapView.mMultiTouchScalePoint.set(xPixel, yPixel);
		if (mMapView.canZoomIn()) {
			if (mMapView.mIsAnimating.getAndSet(true)) {
				// TODO extend zoom (and return true)
				return false;
			} else {
				mMapView.mTargetZoomLevel.set(mMapView.getZoomLevel(false) + 1);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mCurrentAnimator = mZoomInAnimation;
					mZoomInAnimation.start();
				} else {
					mMapView.startAnimation(mZoomInAnimationOld);
				}
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Zoom out by one zoom level.
	 */
	@Override
	public boolean zoomOut() {
		return zoomOutFixing(mMapView.getWidth() / 2, mMapView.getHeight() / 2);
	}

	@Override
	public boolean zoomOutFixing(final int xPixel, final int yPixel) {
		mMapView.mMultiTouchScalePoint.set(xPixel, yPixel);
		if (mMapView.canZoomOut()) {
			if (mMapView.mIsAnimating.getAndSet(true)) {
				// TODO extend zoom (and return true)
				return false;
			} else {
				mMapView.mTargetZoomLevel.set(mMapView.getZoomLevel(false) - 1);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mCurrentAnimator = mZoomOutAnimation;
					mZoomOutAnimation.start();
				} else {
					mMapView.startAnimation(mZoomOutAnimationOld);
				}
				return true;
			}
		} else {
			return false;
		}
	}

	protected void onAnimationStart() {
		mMapView.mIsAnimating.set(true);
	}

	protected void onAnimationEnd() {
		final Rect screenRect = mMapView.getProjection().getScreenRect();
		Point p = mMapView.getProjection().unrotateAndScalePoint(screenRect.centerX(),
				screenRect.centerY(), null);
		p = mMapView.getProjection().toMercatorPixels(p.x, p.y, p);
		// The points provided are "center", we want relative to upper-left for scrolling
		p.offset(-mMapView.getWidth() / 2, -mMapView.getHeight() / 2);
		mMapView.mIsAnimating.set(false);
		mMapView.scrollTo(p.x, p.y);
		setZoom(mMapView.mTargetZoomLevel.get());
		mMapView.mMultiTouchScale = 1f;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mCurrentAnimator = null;
		}

		// Fix for issue 477
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			mMapView.clearAnimation();
			mZoomInAnimationOld.reset();
			mZoomOutAnimationOld.reset();
		}
	}

	protected class MyZoomAnimatorListener extends AnimatorListenerAdapter {
		@Override
		public void onAnimationStart(Animator animation) {
			MapController.this.onAnimationStart();
			super.onAnimationStart(animation);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			MapController.this.onAnimationEnd();
			super.onAnimationEnd(animation);
		}
	}

	protected class MyZoomAnimatorUpdateListener implements AnimatorUpdateListener {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			mMapView.mMultiTouchScale = (Float) animation.getAnimatedValue();
			mMapView.invalidate();
		}
	}

	protected class MyZoomAnimationListener implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {
			MapController.this.onAnimationStart();
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			MapController.this.onAnimationEnd();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// Nothing to do here...
		}
	}

	private enum ReplayType {
		ZoomToSpanPoint, AnimateToPoint, AnimateToGeoPoint, SetCenterPoint
	};

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

		public void replayCalls() {
			for (ReplayClass replay : mReplayList) {
				switch (replay.mReplayType) {
				case AnimateToGeoPoint:
					MapController.this.animateTo(replay.mGeoPoint);
					break;
				case AnimateToPoint:
					MapController.this.animateTo(replay.mPoint.x, replay.mPoint.y);
					break;
				case SetCenterPoint:
					MapController.this.setCenter(replay.mGeoPoint);
					break;
				case ZoomToSpanPoint:
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

// Created by plusminus on 21:37:08 - 27.09.2008
package org.osmdroid.views;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.util.MyMath;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

/**
 * 
 * @author Nicolas Gramlich
 * @author Marc Kurtz
 */
public class MapController implements IMapController, MapViewConstants {

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

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapController(MapView mapView) {
		mMapView = mapView;

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

	public void zoomToSpan(final BoundingBoxE6 bb) {
		zoomToSpan(bb.getLatitudeSpanE6(), bb.getLongitudeSpanE6());
	}

	// TODO rework zoomToSpan
	@Override
	public void zoomToSpan(int latSpanE6, int lonSpanE6) {
		if (latSpanE6 <= 0 || lonSpanE6 <= 0) {
			return;
		}

		final BoundingBoxE6 bb = this.mMapView.getBoundingBox();
		final int curZoomLevel = this.mMapView.getZoomLevel();

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
		Point p = mMapView.getProjection().toMapPixels(point, null);
		animateTo(p.x, p.y);
	}

	/**
	 * Start animating the map towards the given point.
	 */
	public void animateTo(int x, int y) {
		if (!mMapView.isAnimating()) {
			mMapView.mIsFlinging = false;
			final int xStart = mMapView.getScrollX();
			final int yStart = mMapView.getScrollY();
			mMapView.getScroller().startScroll(xStart, yStart, x - xStart, y - yStart,
					ANIMATION_DURATION_DEFAULT);
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
		Point p = mMapView.getProjection().toMapPixels(point, null);
		this.mMapView.scrollTo(p.x, p.y);
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
		Point coords = mMapView.getProjection().toMapPixels(mMapView.getMapCenter(), null);
		return zoomInFixing(coords.x, coords.y);
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
		Point coords = mMapView.getProjection().toMapPixels(mMapView.getMapCenter(), null);
		return zoomOutFixing(coords.x, coords.y);
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
		final Matrix m = new Matrix();
		m.setScale(1 / mMapView.mMultiTouchScale, 1 / mMapView.mMultiTouchScale,
				mMapView.mMultiTouchScalePoint.x, mMapView.mMultiTouchScalePoint.y);
		m.postRotate(-mMapView.getMapOrientation(), screenRect.exactCenterX(),
				screenRect.exactCenterY());
		float[] pts = new float[2];
		pts[0] = mMapView.getScrollX();
		pts[1] = mMapView.getScrollY();
		m.mapPoints(pts);
		mMapView.scrollTo((int) pts[0], (int) pts[1]);
		setZoom(mMapView.mTargetZoomLevel.get());
		mMapView.mMultiTouchScale = 1f;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mCurrentAnimator = null;
		}
		mMapView.mIsAnimating.set(false);

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
}

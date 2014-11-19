// Created by plusminus on 21:37:08 - 27.09.2008
package org.osmdroid.views;

import microsoft.mappoint.TileSystem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.util.MyMath;
import org.osmdroid.views.util.constants.MapViewConstants;
import org.osmdroid.views.util.constants.MathConstants;

import android.graphics.Point;

/**
 * 
 * @author Nicolas Gramlich
 * 
 * @deprecated Use MapController instead.
 */
public class MapControllerOld implements IMapController, MapViewConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final MapView mOsmv;
	private AbstractAnimationRunner mCurrentAnimationRunner;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapControllerOld(final MapView osmv) {
		this.mOsmv = osmv;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void zoomToSpan(final BoundingBox bb) {
		zoomToSpan(bb.getLatitudeSpan(), bb.getLongitudeSpan());
	}

	// TODO rework zoomToSpan
	@Override
	public void zoomToSpan(final double reqLatSpan, final double reqLonSpan) {
		if (reqLatSpan <= 0 || reqLonSpan <= 0) {
			return;
		}

		final BoundingBox bb  = this.mOsmv.getProjection().getBoundingBox();
		final int curZoomLevel = this.mOsmv.getProjection().getZoomLevel();

		final double curLatSpan = bb.getLatitudeSpan();
		final double curLonSpan = bb.getLongitudeSpan();

		final double diffNeededLat = reqLatSpan / curLatSpan; // i.e. 600/500 = 1,2
		final double diffNeededLon = reqLonSpan / curLonSpan; // i.e. 300/400 = 0,75

		final double diffNeeded = Math.max(diffNeededLat, diffNeededLon); // i.e. 1,2

		if (diffNeeded > 1) { // Zoom Out
			this.mOsmv.setZoomLevel(curZoomLevel - MyMath.getNextSquareNumberAbove((float)diffNeeded));
		} else if (diffNeeded < 0.5) { // Can Zoom in
			this.mOsmv.setZoomLevel(curZoomLevel + MyMath.getNextSquareNumberAbove(1 / (float)diffNeeded)
					- 1);
		}
	}

	/**
	 * Start animating the map towards the given point.
	 */
	@Override
	public void animateTo(final IGeoPoint point) {
		animateTo(point.getLatitude(), point.getLongitude());
	}

	/**
	 * Start animating the map towards the given point.
	 */
	public void animateTo(final double latitude, final double longitude) {
		final int x = mOsmv.getScrollX();
		final int y = mOsmv.getScrollY();
		final Point p = TileSystem.LatLongToPixelXY(latitude, longitude, mOsmv.getZoomLevel(), null);
		final int worldSize_2 = TileSystem.MapSize(mOsmv.getZoomLevel()) / 2;
		mOsmv.getScroller().startScroll(x, y, p.x - worldSize_2 - x, p.y - worldSize_2 - y,
				ANIMATION_DURATION_DEFAULT);
		mOsmv.postInvalidate();
	}

	/**
	 * Animates the underlying {@link MapView} that it centers the passed {@link GeoPoint} in the
	 * end. Uses: {@link MapControllerOld.ANIMATION_SMOOTHNESS_DEFAULT} and
	 * {@link MapControllerOld.ANIMATION_DURATION_DEFAULT}.
	 *
	 * @param gp
	 */
	public void animateTo(final GeoPoint gp, final AnimationType aAnimationType) {
		animateTo(gp.getLatitude(), gp.getLongitude(), aAnimationType,
				ANIMATION_DURATION_DEFAULT, ANIMATION_SMOOTHNESS_DEFAULT);
	}

	/**
	 * Animates the underlying {@link MapView} that it centers the passed {@link GeoPoint} in the
	 * end.
	 *
	 * @param gp
	 *            GeoPoint to be centered in the end.
	 * @param aSmoothness
	 *            steps made during animation. I.e.: {@link MapControllerOld.ANIMATION_SMOOTHNESS_LOW},
	 *            {@link MapControllerOld.ANIMATION_SMOOTHNESS_DEFAULT},
	 *            {@link MapControllerOld.ANIMATION_SMOOTHNESS_HIGH}
	 * @param aDuration
	 *            in Milliseconds. I.e.: {@link MapControllerOld.ANIMATION_DURATION_SHORT},
	 *            {@link MapControllerOld.ANIMATION_DURATION_DEFAULT},
	 *            {@link MapControllerOld.ANIMATION_DURATION_LONG}
	 */
	public void animateTo(final GeoPoint gp, final AnimationType aAnimationType,
			final int aSmoothness, final int aDuration) {
		animateTo(gp.getLatitude(), gp.getLongitude(), aAnimationType, aSmoothness, aDuration);
	}

	/**
	 * Animates the underlying {@link MapView} that it centers the passed coordinates in the end.
	 * Uses: {@link MapControllerOld.ANIMATION_SMOOTHNESS_DEFAULT} and
	 * {@link MapControllerOld.ANIMATION_DURATION_DEFAULT}.
	 *
	 * @param aLatitude
	 * @param aLongitude
	 */
	public void animateTo(final double aLatitude, final double aLongitude,
			final AnimationType aAnimationType) {
		animateTo(aLatitude, aLongitude, aAnimationType, ANIMATION_SMOOTHNESS_DEFAULT,
				ANIMATION_DURATION_DEFAULT);
	}

	/**
	 * Animates the underlying {@link MapView} that it centers the passed coordinates in the end.
	 *
	 * @param aLatitude
	 * @param aLongitude
	 * @param aSmoothness
	 *            steps made during animation. I.e.: {@link MapControllerOld.ANIMATION_SMOOTHNESS_LOW},
	 *            {@link MapControllerOld.ANIMATION_SMOOTHNESS_DEFAULT},
	 *            {@link MapControllerOld.ANIMATION_SMOOTHNESS_HIGH}
	 * @param aDuration
	 *            in Milliseconds. I.e.: {@link MapControllerOld.ANIMATION_DURATION_SHORT},
	 *            {@link MapControllerOld.ANIMATION_DURATION_DEFAULT},
	 *            {@link MapControllerOld.ANIMATION_DURATION_LONG}
	 */
	public void animateTo(final double aLatitude, final double aLongitude,
			final AnimationType aAnimationType, final int aSmoothness, final int aDuration) {
		this.stopAnimation(false);

		switch (aAnimationType) {
		case LINEAR:
			this.mCurrentAnimationRunner = new LinearAnimationRunner(aLatitude, aLongitude,
					aSmoothness, aDuration);
			break;
		case EXPONENTIALDECELERATING:
			this.mCurrentAnimationRunner = new ExponentialDeceleratingAnimationRunner(aLatitude,
					aLongitude, aSmoothness, aDuration);
			break;
		case QUARTERCOSINUSALDECELERATING:
			this.mCurrentAnimationRunner = new QuarterCosinusalDeceleratingAnimationRunner(
					aLatitude, aLongitude, aSmoothness, aDuration);
			break;
		case HALFCOSINUSALDECELERATING:
			this.mCurrentAnimationRunner = new HalfCosinusalDeceleratingAnimationRunner(
					aLatitude, aLongitude, aSmoothness, aDuration);
			break;
		case MIDDLEPEAKSPEED:
			this.mCurrentAnimationRunner = new MiddlePeakSpeedAnimationRunner(aLatitude,
					aLongitude, aSmoothness, aDuration);
			break;
		}

		this.mCurrentAnimationRunner.start();
	}

	public void scrollBy(final int x, final int y) {
		this.mOsmv.scrollBy(x, y);
	}

	/**
	 * Set the map view to the given center. There will be no animation.
	 */
	@Override
	public void setCenter(final IGeoPoint point) {
		final Point p = TileSystem.LatLongToPixelXY(point.getLatitude(),
				point.getLongitude(), this.mOsmv.getZoomLevel(), null);
		final int worldSize_2 = TileSystem.MapSize(this.mOsmv.getZoomLevel()) / 2;
		this.mOsmv.scrollTo(p.x - worldSize_2, p.y - worldSize_2);
	}

	/**
	 * Stops a running animation.
	 *
	 * @param jumpToTarget
	 */
	public void stopAnimation(final boolean jumpToTarget) {
		final AbstractAnimationRunner currentAnimationRunner = this.mCurrentAnimationRunner;

		if (currentAnimationRunner != null && !currentAnimationRunner.isDone()) {
			currentAnimationRunner.interrupt();
			if (jumpToTarget) {
				setCenter(new GeoPoint(currentAnimationRunner.mTargetLatitude,
						currentAnimationRunner.mTargetLongitude));
			}
		}
	}

	@Override
	public void stopPanning() {
		mOsmv.getScroller().forceFinished(true);
	}

	@Override
	public int setZoom(final int zoomlevel) {
		return mOsmv.setZoomLevel(zoomlevel);
	}

	/**
	 * Zoom in by one zoom level.
	 */
	@Override
	public boolean zoomIn() {
		return mOsmv.zoomIn();
	}

	public boolean zoomInFixing(final GeoPoint point) {
		return mOsmv.zoomInFixing(point);
	}

	@Override
	public boolean zoomInFixing(final int xPixel, final int yPixel) {
		return mOsmv.zoomInFixing(xPixel, yPixel);
	}

	/**
	 * Zoom out by one zoom level.
	 */
	@Override
	public boolean zoomOut() {
		return mOsmv.zoomOut();
	}

	public boolean zoomOutFixing(final GeoPoint point) {
		return mOsmv.zoomOutFixing(point);
	}

	@Override
	public boolean zoomOutFixing(final int xPixel, final int yPixel) {
		return mOsmv.zoomOutFixing(xPixel, yPixel);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * Choose on of the Styles of approacing the target Coordinates.
	 * <ul>
	 * <li><code>LINEAR</code>
	 * <ul>
	 * <li>Uses ses linear interpolation</li>
	 * <li>Values produced: 10%, 20%, 30%, 40%, 50%, ...</li>
	 * <li>Style: Always average speed.</li>
	 * </ul>
	 * </li>
	 * <li><code>EXPONENTIALDECELERATING</code>
	 * <ul>
	 * <li>Uses a exponential interpolation/li>
	 * <li>Values produced: 50%, 75%, 87.5%, 93.5%, ...</li>
	 * <li>Style: Starts very fast, really slow in the end.</li>
	 * </ul>
	 * </li>
	 * <li><code>QUARTERCOSINUSALDECELERATING</code>
	 * <ul>
	 * <li>Uses the first quarter of the cos curve (from zero to PI/2) for interpolation.</li>
	 * <li>Values produced: See cos curve :)</li>
	 * <li>Style: Average speed, slows out medium.</li>
	 * </ul>
	 * </li>
	 * <li><code>HALFCOSINUSALDECELERATING</code>
	 * <ul>
	 * <li>Uses the first half of the cos curve (from zero to PI) for interpolation</li>
	 * <li>Values produced: See cos curve :)</li>
	 * <li>Style: Average speed, slows out smoothly.</li>
	 * </ul>
	 * </li>
	 * <li><code>MIDDLEPEAKSPEED</code>
	 * <ul>
	 * <li>Uses the values of cos around the 0 (from -PI/2 to +PI/2) for interpolation</li>
	 * <li>Values produced: See cos curve :)</li>
	 * <li>Style: Starts medium, speeds high in middle, slows out medium.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	public static enum AnimationType {
		/**
		 * <ul>
		 * <li><code>LINEAR</code>
		 * <ul>
		 * <li>Uses ses linear interpolation</li>
		 * <li>Values produced: 10%, 20%, 30%, 40%, 50%, ...</li>
		 * <li>Style: Always average speed.</li>
		 * </ul>
		 * </li>
		 * </ul>
		 */
		LINEAR,
		/**
		 * <ul>
		 * <li><code>EXPONENTIALDECELERATING</code>
		 * <ul>
		 * <li>Uses a exponential interpolation/li>
		 * <li>Values produced: 50%, 75%, 87.5%, 93.5%, ...</li>
		 * <li>Style: Starts very fast, really slow in the end.</li>
		 * </ul>
		 * </li>
		 * </ul>
		 */
		EXPONENTIALDECELERATING,
		/**
		 * <ul>
		 * <li><code>QUARTERCOSINUSALDECELERATING</code>
		 * <ul>
		 * <li>Uses the first quarter of the cos curve (from zero to PI/2) for interpolation.</li>
		 * <li>Values produced: See cos curve :)</li>
		 * <li>Style: Average speed, slows out medium.</li>
		 * </ul>
		 * </li>
		 * </ul>
		 */
		QUARTERCOSINUSALDECELERATING,
		/**
		 * <ul>
		 * <li><code>HALFCOSINUSALDECELERATING</code>
		 * <ul>
		 * <li>Uses the first half of the cos curve (from zero to PI) for interpolation</li>
		 * <li>Values produced: See cos curve :)</li>
		 * <li>Style: Average speed, slows out smoothly.</li>
		 * </ul>
		 * </li>
		 * </ul>
		 */
		HALFCOSINUSALDECELERATING,
		/**
		 * <ul>
		 * <li><code>MIDDLEPEAKSPEED</code>
		 * <ul>
		 * <li>Uses the values of cos around the 0 (from -PI/2 to +PI/2) for interpolation</li>
		 * <li>Values produced: See cos curve :)</li>
		 * <li>Style: Starts medium, speeds high in middle, slows out medium.</li>
		 * </ul>
		 * </li>
		 * </ul>
		 */
		MIDDLEPEAKSPEED
	}

	/**
	 * @deprecated Do not use - this appears to modify UI elements and MapView fields on a
	 *             background thread and is not thread-safe.
	 */
	private abstract class AbstractAnimationRunner extends Thread {

		// ===========================================================
		// Fields
		// ===========================================================

		protected final int mSmoothness;
		protected final double mTargetLatitude, mTargetLongitude;
		protected boolean mDone = false;

		protected final int mStepDuration;

		protected final double mPanTotalLatitude, mPanTotalLongitude;

		// ===========================================================
		// Constructors
		// ===========================================================

		@SuppressWarnings("unused")
		public AbstractAnimationRunner(final MapControllerOld mapViewController,
				final double aTargetLatitude, final double aTargetLongitude) {
			this(aTargetLatitude, aTargetLongitude,
					MapViewConstants.ANIMATION_SMOOTHNESS_DEFAULT,
					MapViewConstants.ANIMATION_DURATION_DEFAULT);
		}

		public AbstractAnimationRunner(final double aTargetLatitude, final double aTargetLongitude,
				final int aSmoothness, final int aDuration) {
			this.mTargetLatitude = aTargetLatitude;
			this.mTargetLongitude = aTargetLongitude;
			this.mSmoothness = aSmoothness;
			this.mStepDuration = aDuration / aSmoothness;

			/* Get the current mapview-center. */
			final MapView mapview = MapControllerOld.this.mOsmv;
			final IGeoPoint mapCenter = mapview.getMapCenter();

			this.mPanTotalLatitude = mapCenter.getLatitude() - aTargetLatitude;
			this.mPanTotalLongitude = mapCenter.getLongitude() - aTargetLongitude;
		}

		@Override
		public void run() {
			onRunAnimation();
			this.mDone = true;
		}

		public boolean isDone() {
			return this.mDone;
		}

		public abstract void onRunAnimation();
	}

	private class LinearAnimationRunner extends AbstractAnimationRunner {

		// ===========================================================
		// Fields
		// ===========================================================

		protected final double mPanPerStepLatitude, mPanPerStepLongitude;

		// ===========================================================
		// Constructors
		// ===========================================================

		@SuppressWarnings("unused")
		public LinearAnimationRunner(final double aTargetLatitude, final double aTargetLongitude) {
			this(aTargetLatitude, aTargetLongitude, ANIMATION_SMOOTHNESS_DEFAULT,
					ANIMATION_DURATION_DEFAULT);
		}

		public LinearAnimationRunner(final double aTargetLatitude, final double aTargetLongitude,
				final int aSmoothness, final int aDuration) {
			super(aTargetLatitude, aTargetLongitude, aSmoothness, aDuration);

			/* Get the current mapview-center. */
			final MapView mapview = MapControllerOld.this.mOsmv;
			final IGeoPoint mapCenter = mapview.getMapCenter();

			this.mPanPerStepLatitude = (mapCenter.getLatitude() - aTargetLatitude)
					/ aSmoothness;
			this.mPanPerStepLongitude = (mapCenter.getLongitude() - aTargetLongitude)
					/ aSmoothness;

			this.setName("LinearAnimationRunner");
		}

		// ===========================================================
		// Methods from SuperClass/Interfaces
		// ===========================================================

		@Override
		public void onRunAnimation() {
			final MapView mapview = MapControllerOld.this.mOsmv;
			final IGeoPoint mapCenter = mapview.getMapCenter();
			final double panPerStepLatitude = this.mPanPerStepLatitude;
			final double panPerStepLongitude = this.mPanPerStepLongitude;
			final int stepDuration = this.mStepDuration;
			try {
                double newMapCenterLat;
                double newMapCenterLon;

				for (int i = this.mSmoothness; i > 0; i--) {

					newMapCenterLat = mapCenter.getLatitude() - panPerStepLatitude;
					newMapCenterLon = mapCenter.getLongitude() - panPerStepLongitude;
					mapview.setMapCenter(new GeoPoint(newMapCenterLat, newMapCenterLon));

					Thread.sleep(stepDuration);
				}
			} catch (final Exception e) {
				this.interrupt();
			}
		}
	}

	private class ExponentialDeceleratingAnimationRunner extends AbstractAnimationRunner {

		// ===========================================================
		// Fields
		// ===========================================================

		// ===========================================================
		// Constructors
		// ===========================================================

		@SuppressWarnings("unused")
		public ExponentialDeceleratingAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude) {
			this(aTargetLatitude, aTargetLongitude, ANIMATION_SMOOTHNESS_DEFAULT,
					ANIMATION_DURATION_DEFAULT);
		}

		public ExponentialDeceleratingAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude, final int aSmoothness, final int aDuration) {
			super(aTargetLatitude, aTargetLongitude, aSmoothness, aDuration);

			this.setName("ExponentialDeceleratingAnimationRunner");
		}

		// ===========================================================
		// Methods from SuperClass/Interfaces
		// ===========================================================

		@Override
		public void onRunAnimation() {
			final MapView mapview = MapControllerOld.this.mOsmv;
			final IGeoPoint mapCenter = mapview.getMapCenter();
			final int stepDuration = this.mStepDuration;
			try {
				double newMapCenterLat;
                double newMapCenterLon;

				for (int i = 0; i < this.mSmoothness; i++) {

					final double delta = Math.pow(0.5, i + 1);
					final double deltaLatitude = this.mPanTotalLatitude * delta;
					final double detlaLongitude = this.mPanTotalLongitude * delta;

					newMapCenterLat = mapCenter.getLatitude() - deltaLatitude;
					newMapCenterLon = mapCenter.getLongitude() - detlaLongitude;
					mapview.setMapCenter(new GeoPoint(newMapCenterLat, newMapCenterLon));

					Thread.sleep(stepDuration);
				}
				mapview.setMapCenter(new GeoPoint(super.mTargetLatitude, super.mTargetLongitude));
			} catch (final Exception e) {
				this.interrupt();
			}
		}
	}

	private class CosinusalBasedAnimationRunner extends AbstractAnimationRunner implements
			MathConstants {
		// ===========================================================
		// Fields
		// ===========================================================

		protected final float mStepIncrement, mAmountStretch;
		protected final float mYOffset, mStart;

		// ===========================================================
		// Constructors
		// ===========================================================

		@SuppressWarnings("unused")
		public CosinusalBasedAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude, final float aStart, final float aRange,
				final float aYOffset) {
			this(aTargetLatitude, aTargetLongitude, ANIMATION_SMOOTHNESS_DEFAULT,
					ANIMATION_DURATION_DEFAULT, aStart, aRange, aYOffset);
		}

		public CosinusalBasedAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude, final int aSmoothness, final int aDuration,
				final float aStart, final float aRange, final float aYOffset) {
			super(aTargetLatitude, aTargetLongitude, aSmoothness, aDuration);
			this.mYOffset = aYOffset;
			this.mStart = aStart;

			this.mStepIncrement = aRange / aSmoothness;

			/* We need to normalize the amount in the end, so wee need the the: sum^(-1) . */
			float amountSum = 0;
			for (int i = 0; i < aSmoothness; i++) {
				amountSum += aYOffset + Math.cos(this.mStepIncrement * i + aStart);
			}

			this.mAmountStretch = 1 / amountSum;

			this.setName("QuarterCosinusalDeceleratingAnimationRunner");
		}

		// ===========================================================
		// Methods from SuperClass/Interfaces
		// ===========================================================

		@Override
		public void onRunAnimation() {
			final MapView mapview = MapControllerOld.this.mOsmv;
			final IGeoPoint mapCenter = mapview.getMapCenter();
			final int stepDuration = this.mStepDuration;
			final float amountStretch = this.mAmountStretch;
			try {
				double newMapCenterLat;
                double newMapCenterLon;

				for (int i = 0; i < this.mSmoothness; i++) {

					final double delta = (this.mYOffset + Math.cos(this.mStepIncrement * i
							+ this.mStart))
							* amountStretch;
					final double deltaLatitude =this.mPanTotalLatitude * delta;
					final double deltaLongitude = this.mPanTotalLongitude * delta;

					newMapCenterLat = mapCenter.getLatitude() - deltaLatitude;
					newMapCenterLon = mapCenter.getLongitude() - deltaLongitude;
					mapview.setMapCenter(new GeoPoint(newMapCenterLat, newMapCenterLon));

					Thread.sleep(stepDuration);
				}
				mapview.setMapCenter(new GeoPoint(super.mTargetLatitude, super.mTargetLongitude));
			} catch (final Exception e) {
				this.interrupt();
			}
		}
	}

	protected class QuarterCosinusalDeceleratingAnimationRunner extends
			CosinusalBasedAnimationRunner implements MathConstants {
		// ===========================================================
		// Constructors
		// ===========================================================

		protected QuarterCosinusalDeceleratingAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude) {
			this(aTargetLatitude, aTargetLongitude, ANIMATION_SMOOTHNESS_DEFAULT,
					ANIMATION_DURATION_DEFAULT);
		}

		protected QuarterCosinusalDeceleratingAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude, final int aSmoothness, final int aDuration) {
			super(aTargetLatitude, aTargetLongitude, aSmoothness, aDuration, 0, PI_2, 0);
		}
	}

	protected class HalfCosinusalDeceleratingAnimationRunner extends CosinusalBasedAnimationRunner
			implements MathConstants {
		// ===========================================================
		// Constructors
		// ===========================================================

		protected HalfCosinusalDeceleratingAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude) {
			this(aTargetLatitude, aTargetLongitude, ANIMATION_SMOOTHNESS_DEFAULT,
					ANIMATION_DURATION_DEFAULT);
		}

		protected HalfCosinusalDeceleratingAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude, final int aSmoothness, final int aDuration) {
			super(aTargetLatitude, aTargetLongitude, aSmoothness, aDuration, 0, PI, 1);
		}
	}

	protected class MiddlePeakSpeedAnimationRunner extends CosinusalBasedAnimationRunner implements
			MathConstants {
		// ===========================================================
		// Constructors
		// ===========================================================

		protected MiddlePeakSpeedAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude) {
			this(aTargetLatitude, aTargetLongitude, ANIMATION_SMOOTHNESS_DEFAULT,
					ANIMATION_DURATION_DEFAULT);
		}

		protected MiddlePeakSpeedAnimationRunner(final double aTargetLatitude,
				final double aTargetLongitude, final int aSmoothness, final int aDuration) {
			super(aTargetLatitude, aTargetLongitude, aSmoothness, aDuration, -PI_2, PI, 0);
		}
	}
}

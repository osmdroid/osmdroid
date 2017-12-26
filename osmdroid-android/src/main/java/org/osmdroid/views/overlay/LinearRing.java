package org.osmdroid.views.overlay;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.util.Distance;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PathBuilder;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.SegmentClipper;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding one ring: the polygon outline, or a hole inside the polygon
 * Used to be an inner class of {@link Polygon}
 * @since 6.0.0
 * @author Fabrice Fontaine
 */
class LinearRing{

	/**
	 * We build a virtual area [mClipMin, mClipMin, mClipMax, mClipMax]
	 * used to clip our Path in order to cope with
	 * - very high pixel values (that go beyond the int values, for instance on zoom 29)
	 * - some kind of Android bug related to hardware acceleration
	 * "One size fits all" clip area values cannot be determined
	 * as there's not explicit value given by Android to avoid Path drawing issues.
	 * If the size is too big (magnitude around Integer.MAX_VALUE), the Path won't show properly.
	 * If it's small (just above the size of the screen), the approximations of the clipped Path
	 * may look gross, particularly if you zoom out in animation.
	 * The smaller it is, the better it is for performances because the clip
	 * will then often approximate consecutive Path segments as identical, and we only add
	 * distinct points to the Path as an optimization.
	 * The best idea so far is to compute the clip area border values
	 * from the current MapView's characteristics (width, height, scale, orientation)
	 */

	private final ArrayList<GeoPoint> mOriginalPoints = new ArrayList<>();
	private final ArrayList<PointL> mProjectedPoints = new ArrayList<>();
	private SegmentClipper mSegmentClipper = new SegmentClipper();
	private final Path mPath;
	private boolean mPrecomputed;
	private boolean isHorizontalRepeating = true;
	private boolean isVerticalRepeating  = true;
	private final List<PointL> mGatheredPoints = new ArrayList<>();
	private final PathBuilder mPathBuilder;

	public LinearRing(final Path pPath) {
		mPath = pPath;
		mPathBuilder = new PathBuilder(pPath);
	}

	void clearPath() {
		mOriginalPoints.clear();
		mPrecomputed = false;
	}

	void addPoint(final GeoPoint pGeoPoint) {
		mOriginalPoints.add(pGeoPoint);
		mPrecomputed = false;
	}

	ArrayList<GeoPoint> getPoints(){
		return mOriginalPoints;
	}

	void setPoints(final List<GeoPoint> points) {
		clearPath();
		mOriginalPoints.addAll(points);
	}

	/**
	 * Feed the path with the segments corresponding to the GeoPoint pairs
	 * projected using pProjection and clipped into a "reasonable" clip area
	 * In most cases (Polygon without holes, Polyline) the offset parameter will be null.
	 * In the case of a Polygon with holes, the first path will use a null offset.
	 * Then this method will return the pixel offset computed for this path so that
	 * the path is in the best possible place on the map (maximum area on the map + top left).
	 * Then, this computed offset must be injected into the buildPathPortion for each hole,
	 * in order to have the main polygon and its holes at the same place on the map.
	 * @return the initial offset if not null, or the computed offset
	 */
	PointL buildPathPortion(final Projection pProjection,
							final boolean pClosePath, final PointL pOffset){
		final int size = mOriginalPoints.size();
		if (size < 2) { // nothing to paint
			return pOffset;
		}

		if (!mPrecomputed){
			getProjectedFromGeo(mOriginalPoints, mProjectedPoints, pProjection);
			mPrecomputed = true;
		}

		getGatheredPointsFromProjected(pProjection, mProjectedPoints);
		final PointL offset;
		if (pOffset != null) {
			offset = pOffset;
		} else {
			offset = new PointL();
			getBestOffset(pProjection, offset);
		}
		applyOffset(offset);
		if (pClosePath) {
			if (mGatheredPoints.size() > 0) {
				mGatheredPoints.add(mGatheredPoints.get(0));
			}
		}
		mPathBuilder.init();
		mSegmentClipper.init();
		for (final PointL point : mGatheredPoints) {
			mSegmentClipper.add(point.x, point.y);
		}
		mSegmentClipper.end();
		mPathBuilder.end();
		if (pClosePath) {
			mPath.close();
		}
		return offset;
	}

	/**
	 * @since 6.0.0
	 */
	public List<PointL> getGatheredPoints() {
		return mGatheredPoints;
	}

	/**
	 * Compute the pixel offset so that a list of pixel segments display in the best possible way:
	 * the biggest possible covered area, and top left.
	 * This notion of pixel offset only has a meaning on very low zoom level,
	 * when a GeoPoint can be projected on different places on the screen.
	 */
	private void getBestOffset(final Projection pProjection, final PointL pOffset) {
		final Rect screenRect = pProjection.getIntrinsicScreenRect();
		final double worldSize = TileSystem.MapSize(pProjection.getZoomLevel());
		final RectL boundingBox = getBoundingBox(mGatheredPoints, null);
		getBestOffset(boundingBox, screenRect, worldSize, pOffset);
	}

	private void getBestOffset(final RectL pBoundingBox, final Rect pScreenRect,
							   final double pWorldSize, final PointL pOffset) {
		final long worldSize = Math.round(pWorldSize);
		int deltaXPositive = getBestOffset(pBoundingBox, pScreenRect, worldSize, 0);
		int deltaXNegative = getBestOffset(pBoundingBox, pScreenRect, -worldSize, 0);
		int deltaYPositive = getBestOffset(pBoundingBox, pScreenRect, 0, worldSize);
		int deltaYNegative = getBestOffset(pBoundingBox, pScreenRect, 0, -worldSize);
		if (!isVerticalRepeating) {
			deltaYPositive=0;
			deltaYNegative=0;
		}
		if (!isHorizontalRepeating) {
			deltaXPositive=0;
			deltaXNegative=0;
		}
		pOffset.x = worldSize * (deltaXPositive > deltaXNegative ? deltaXPositive:-deltaXNegative);
		pOffset.y = worldSize * (deltaYPositive > deltaYNegative ? deltaYPositive:-deltaYNegative);
	}

	private int getBestOffset(final RectL pBoundingBox, final Rect pScreenRect,
							  final long pDeltaX, final long pDeltaY) {
		if (!isHorizontalRepeating && !isVerticalRepeating ) {
			return 0;
		}
		final double boundingBoxCenterX = (pBoundingBox.left + pBoundingBox.right) / 2.;
		final double boundingBoxCenterY = (pBoundingBox.top + pBoundingBox.bottom) / 2.;
		final double screenRectCenterX = (pScreenRect.left + pScreenRect.right) / 2.;
		final double screenRectCenterY = (pScreenRect.top + pScreenRect.bottom) / 2.;
		double squaredDistance = 0;
		int i = 0;
		while(true) {
			final double tmpSquaredDistance = Distance.getSquaredDistanceToPoint(
					boundingBoxCenterX + i * pDeltaX, boundingBoxCenterY + i * pDeltaY,
					screenRectCenterX, screenRectCenterY);
			if (i == 0 || squaredDistance > tmpSquaredDistance) {
				squaredDistance = tmpSquaredDistance;
				i ++;
			} else {
				break;
			}
		}
		return i - 1;
	}

	private RectL getBoundingBox(final List<PointL> pPoints, final RectL pReuse) {
		final RectL out = pReuse != null ? pReuse : new RectL();
		boolean first = true;
		for (final PointL point : pPoints) {
			if (first) {
				first = false;
				out.left = out.right = point.x;
				out.top = out.bottom = point.y;
			} else {
				if (out.left > point.x) {
					out.left = point.x;
				}
				if (out.top > point.y) {
					out.top = point.y;
				}
				if (out.right < point.x) {
					out.right = point.x;
				}
				if (out.bottom < point.y) {
					out.bottom = point.y;
				}
			}
		}
		return out;
	}

	private void getProjectedFromGeo(final List<GeoPoint> pGeo, final ArrayList<PointL> pProjected,
									final Projection pProjection) {
		pProjected.clear();
		pProjected.ensureCapacity(pGeo.size());
		for (final GeoPoint geoPoint : pGeo) {
			pProjected.add(pProjection.toProjectedPixels(
					geoPoint.getLatitude(), geoPoint.getLongitude(), null));
		}
	}

	/**
	 * @since 6.0.0
	 */
	private void getGatheredPointsFromProjected(final Projection pProjection,
												final List<PointL> pProjectedPoints) {
		mGatheredPoints.clear();
		final double worldSize = TileSystem.MapSize(pProjection.getZoomLevel());
		final double powerDifference = pProjection.getProjectedPowerDifference();
		final PointL screenPoint0 = new PointL(); // points on screen
		final PointL screenPoint1 = new PointL();
		PointL firstPoint = null;
		for (final PointL projectedPoint : pProjectedPoints) {
			// compute next points
			pProjection.getLongPixelsFromProjected(
					projectedPoint, powerDifference, false, screenPoint1);
			if (firstPoint == null) {
				firstPoint = new PointL(screenPoint1);
			} else {
				setCloserPoint(screenPoint0, screenPoint1, worldSize);
			}
			mGatheredPoints.add(new PointL(screenPoint1));

			// update starting point to next position
			screenPoint0.set(screenPoint1);
		}
	}

	/**
	 * We want consecutive projected points to be as close as possible,
	 * and not a world away (typically when dealing with very low zoom levels)
	 */
	private void setCloserPoint(final PointL pPrevious, final PointL pNext,
								final double pWorldSize) {
		while (Math.abs(pNext.x - pWorldSize - pPrevious.x) < Math.abs(pNext.x - pPrevious.x)) {
			pNext.x -= pWorldSize;
		}
		while (Math.abs(pNext.x + pWorldSize - pPrevious.x) < Math.abs(pNext.x - pPrevious.x)) {
			pNext.x += pWorldSize;
		}
		while (Math.abs(pNext.y - pWorldSize - pPrevious.y) < Math.abs(pNext.y - pPrevious.y)) {
			pNext.y -= pWorldSize;
		}
		while (Math.abs(pNext.y + pWorldSize - pPrevious.y) < Math.abs(pNext.y - pPrevious.y)) {
			pNext.y += pWorldSize;
		}
	}

	/**
	 * Detection is done in screen coordinates.
	 * @param tolerance in pixels
	 * @return true if the Polyline is close enough to the point.
	 */
	boolean isCloseTo(final GeoPoint pPoint, final double tolerance,
							 final Projection pProjection) {
		if (!mPrecomputed){
			getProjectedFromGeo(mOriginalPoints, mProjectedPoints, pProjection);
			mPrecomputed = true;
		}
		final Point pixel = pProjection.toPixels(pPoint, null);
		getGatheredPointsFromProjected(pProjection, mProjectedPoints);
		final PointL offset = new PointL();
		getBestOffset(pProjection, offset);
		applyOffset(offset);
		final double squaredTolerance = tolerance * tolerance;
		final PointL point0 = new PointL();
		final PointL point1 = new PointL();
		boolean first = true;
		for (final PointL point : mGatheredPoints) {
			point1.set(point);
			if (first) {
				first = false;
			} else if (squaredTolerance > Distance.getSquaredDistanceToSegment(
					pixel.x, pixel.y, point0.x, point0.y, point1.x, point1.y)) {
				return true;
			}
			point0.set(point1);
		}
		return false;
	}

	private void applyOffset(final PointL pOffset) {
		if (pOffset.x == 0 && pOffset.y == 0) {
			return;
		}
		for (final PointL point : mGatheredPoints) {
			if (pOffset.x != 0) {
				pOffset.x += pOffset.x;
			}
			if (pOffset.y != 0) {
				point.y += pOffset.y;
			}
		}
	}

	/**
	 * @since 6.0.0
	 * Mandatory use before clipping.
	 */
	public void setClipArea(final long pXMin, final long pYMin, final long pXMax, final long pYMax) {
		mSegmentClipper.set(pXMin, pYMin, pXMax, pYMax, mPathBuilder);
	}

	/**
	 * @since 6.0.0
	 * Mandatory use before clipping.
	 */
	public void setClipArea(final MapView pMapView) {
		final double border = .1;
		final int halfWidth = pMapView.getWidth() / 2;
		final int halfHeight = pMapView.getHeight() / 2;
		// People less lazy than me would do more refined computations for width and height
		// that include the map orientation: the covered area would be smaller but still big enough
		// Now we use the circle which contains the `MapView`'s 4 corners
		final double radius = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
		final int scaledRadius = (int) (radius * (1 + border));
		setClipArea(
				halfWidth - scaledRadius, halfHeight - scaledRadius,
				halfWidth + scaledRadius, halfHeight + scaledRadius
		);
		// TODO: Not sure if this is the correct approach 
		this.isHorizontalRepeating = pMapView.isHorizontalMapRepetitionEnabled();
		this.isVerticalRepeating = pMapView.isVerticalMapRepetitionEnabled();
	}
}

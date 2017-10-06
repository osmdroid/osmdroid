package org.osmdroid.views.overlay;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.util.Distance;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.SegmentIntersection;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding one ring: the polygon outline, or a hole inside the polygon
 * Used to be an inner class of {@link Polygon}
 * @since 6.0.0
 * @author Fabrice Fontaine
 */
class LinearRing {

	/**
	 * We build a virtual area [mClipMin, mClipMin, mClipMax, mClipMax]
	 * used to clip our Path in order to cope with
	 * - very high pixel values (that go beyond the int values, for instance on zoom 29)
	 * - some kind of Android bug related to hardware acceleration
	 * The size of the clip area was determined running experiments on my own device
	 * as there's not explicit value given by Android to avoid Path drawing issues.
	 * If the size is too big (magnitude around Integer.MAX_VALUE), the Path won't show properly.
	 * If it's small (just above the size of the screen), the approximations of the clipped Path
	 * may look gross, particularly if you zoom out in animation.
	 * The smaller it is, the better it is for performances because the clip
	 * will then often approximate consecutive Path segments as identical, and we only add
	 * distinct points to the Path as an optimization.
	 * As an indication, the initial min/max values of the clip area size were
	 * Integer.MIN_VALUE / 8 and Integer.MAX_VALUE / 8.
	 */
	private static final int mClipMax = Integer.MAX_VALUE / 8; // "big enough but not too much"
	private static final int mClipMin = Integer.MIN_VALUE / 8;

	private final ArrayList<GeoPoint> mOriginalPoints = new ArrayList<>();
	private final ArrayList<PointL> mProjectedPoints = new ArrayList<>();
	private boolean mPrecomputed;

	// for optimization reasons: avoiding to create objects all the time
	private final PointL mOptimIntersection = new PointL();
	private final PointL mOptimIntersection1 = new PointL();
	private final PointL mOptimIntersection2 = new PointL();

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
							final Path pPath, final boolean pClosePath, final PointL pOffset){
		final int size = mOriginalPoints.size();
		if (size < 2) { // nothing to paint
			return pOffset;
		}

		if (!mPrecomputed){
			getProjectedFromGeo(mOriginalPoints, mProjectedPoints, pProjection);
			mPrecomputed = true;
		}

		final List<PointL> segmentStartPoints = new ArrayList<>();
		final List<PointL> segmentEndPoints = new ArrayList<>();
		getSegmentsFromProjected(
				pProjection, mProjectedPoints, segmentStartPoints, segmentEndPoints);
		final PointL offset;
		if (pOffset != null) {
			offset = pOffset;
		} else {
			offset = new PointL();
			getBestOffset(segmentStartPoints, segmentEndPoints, pProjection, offset);
		}
		getPathFromSegments(segmentStartPoints, segmentEndPoints, pPath, pClosePath, offset);
		return offset;
	}

	/**
	 * Compute the pixel offset so that a list of pixel segments display in the best possible way:
	 * the biggest possible covered area, and top left.
	 * This notion of pixel offset only has a meaning on very low zoom level,
	 * when a GeoPoint can be projected on different places on the screen.
	 */
	private void getBestOffset(final List<PointL> pSegmentStartPoints,
							   final List<PointL> pSegmentEndPoints,
							   final Projection pProjection, final PointL pOffset) {
		final Rect screenRect = pProjection.getIntrinsicScreenRect();
		final double worldSize = TileSystem.MapSize(pProjection.getZoomLevel());
		final RectL boundingBox1 = new RectL();
		final RectL boundingBox2 = new RectL();
		getBoundingBoxFromPoints(pSegmentStartPoints, boundingBox1);
		getBoundingBoxFromPoints(pSegmentEndPoints, boundingBox2);
		final RectL boundingBox = new RectL(
				Math.min(boundingBox1.left, boundingBox2.left),
				Math.min(boundingBox1.top, boundingBox2.top),
				Math.max(boundingBox1.right, boundingBox2.right),
				Math.max(boundingBox1.bottom, boundingBox2.bottom)
		);
		getBestOffset(boundingBox, screenRect, worldSize, pOffset);
	}

	private void getBestOffset(final RectL pBoundingBox, final Rect pScreenRect,
							   final double pWorldSize, final PointL pOffset) {
		final long worldSize = Math.round(pWorldSize);
		final int deltaXPositive = getBestOffset(pBoundingBox, pScreenRect, worldSize, 0);
		final int deltaXNegative = getBestOffset(pBoundingBox, pScreenRect, -worldSize, 0);
		final int deltaYPositive = getBestOffset(pBoundingBox, pScreenRect, 0, worldSize);
		final int deltaYNegative = getBestOffset(pBoundingBox, pScreenRect, 0, -worldSize);
		pOffset.x = worldSize * (deltaXPositive > deltaXNegative ? deltaXPositive:-deltaXNegative);
		pOffset.y = worldSize * (deltaYPositive > deltaYNegative ? deltaYPositive:-deltaYNegative);
	}

	private int getBestOffset(final RectL pBoundingBox, final Rect pScreenRect,
							  final long pDeltaX, final long pDeltaY) {
		long area = 0;
		int i = 0;
		while(true) {
			final long tmpArea = getCommonArea(pScreenRect, pBoundingBox, i * pDeltaX, i * pDeltaY);
			if (i == 0 || area < tmpArea) {
				area = tmpArea;
				i ++;
			} else {
				break;
			}
		}
		return i - 1;
	}

	private long getCommonArea(final Rect pReference,
							   final RectL pWithOffset, final long pOffsetX, final long pOffsetY) {
		final long left = pWithOffset.left + pOffsetX;
		final long right = pWithOffset.right + pOffsetX;
		final long top = pWithOffset.top + pOffsetY;
		final long bottom = pWithOffset.bottom + pOffsetY;
		if (left < pReference.right && pReference.left < right
				&& top < pReference.bottom && pReference.top < bottom) {
			return (1 + Math.min(right, pReference.right) - Math.max(left, pReference.left))
					* (1 + Math.min(bottom, pReference.bottom) - Math.max(top, pReference.top));
		}
		return 0;
	}

	private void getBoundingBoxFromPoints(final List<PointL> pPoints, final RectL pBoundingBox) {
		boolean first = true;
		for (final PointL point : pPoints) {
			if (first) {
				first = false;
				pBoundingBox.left = pBoundingBox.right = point.x;
				pBoundingBox.top = pBoundingBox.bottom = point.y;
			} else {
				if (pBoundingBox.left > point.x) {
					pBoundingBox.left = point.x;
				}
				if (pBoundingBox.top > point.y) {
					pBoundingBox.top = point.y;
				}
				if (pBoundingBox.right < point.x) {
					pBoundingBox.right = point.x;
				}
				if (pBoundingBox.bottom < point.y) {
					pBoundingBox.bottom = point.y;
				}
			}
		}
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

	private void getSegmentsFromProjected(final Projection pProjection,
										  final List<PointL> pProjectedPoints,
										  final List<PointL> pSegmentStartPoints,
										  final List<PointL> pSegmentEndPoints) {
		final double worldSize = TileSystem.MapSize(pProjection.getZoomLevel());
		final double powerDifference = pProjection.getProjectedPowerDifference();
		final PointL screenPoint0 = new PointL(); // points on screen
		final PointL screenPoint1 = new PointL();
		final RectL currentSegment = new RectL();
		boolean firstPoint = true;
		for (final PointL projectedPoint : pProjectedPoints) {
			// compute next points
			pProjection.getLongPixelsFromProjected(
					projectedPoint, powerDifference, false, screenPoint1);
			if (firstPoint) {
				firstPoint = false;
			} else {
				setCloserPoint(screenPoint0, screenPoint1, worldSize);
				currentSegment.set(screenPoint0.x, screenPoint0.y, screenPoint1.x, screenPoint1.y);
				clip(currentSegment);
				pSegmentStartPoints.add(new PointL(currentSegment.left, currentSegment.top));
				pSegmentEndPoints.add(new PointL(currentSegment.right, currentSegment.bottom));
			}

			// update starting point to next position
			screenPoint0.set(screenPoint1);
		}
	}

	private void getPathFromSegments(final List<PointL> pSegmentStartPoints,
									final List<PointL> pSegmentEndPoints,
									final Path pPath, final boolean pClosePath,
									final PointL pOffset) {
		final PointL latestPathPoint = new PointL();
		boolean firstSegment = true;
		final int nb = pSegmentStartPoints.size();
		for (int i = 0 ; i < nb ; i ++) {
			final PointL firstPixel = pSegmentStartPoints.get(i);
			final PointL lastPixel = pSegmentEndPoints.get(i);
			if (firstSegment) {
				firstSegment = false;
				pPath.moveTo(firstPixel.x + pOffset.x,firstPixel.y + pOffset.y);
				latestPathPoint.set(firstPixel.x + pOffset.x, firstPixel.y + pOffset.y);
			}
			lineTo(firstPixel.x + pOffset.x, firstPixel.y + pOffset.y, latestPathPoint, pPath);
			lineTo(lastPixel.x + pOffset.x, lastPixel.y + pOffset.y, latestPathPoint, pPath);
		}
		if (pClosePath) {
			pPath.close();
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

	private void lineTo(final long pX, final long pY, final PointL pLatest, final Path pPath) {
		if (pLatest.x != pX || pLatest.y != pY) {
			pPath.lineTo(pX, pY);
			pLatest.set(pX, pY);
		}
	}

	/**
	 * Check if a point is in the clip area
	 */
	private boolean isInClipArea(final long pX, final long pY) {
		return pX > mClipMin && pX < mClipMax && pY > mClipMin && pY < mClipMax;
	}

	/**
	 * Clip a value into the clip area min/max
	 */
	private long clip(final long value) {
		return value <= mClipMin ? mClipMin : value >= mClipMax ? mClipMax : 0;
	}

	/**
	 * Clip a segment into the clip area
	 */
	private void clip(final RectL pSegment) {
		if (isInClipArea(pSegment.left, pSegment.top)) {
			if (isInClipArea(pSegment.right, pSegment.bottom)) {
				return; // nothing to do
			}
			if (intersection(pSegment)) {
				pSegment.right = mOptimIntersection.x;
				pSegment.bottom = mOptimIntersection.y;
				return;
			}
			throw new RuntimeException("Cannot find expected mOptimIntersection for " + pSegment);
		}
		if (isInClipArea(pSegment.right, pSegment.bottom)) {
			if (intersection(pSegment)) {
				pSegment.left = mOptimIntersection.x;
				pSegment.top = mOptimIntersection.y;
				return;
			}
			throw new RuntimeException("Cannot find expected mOptimIntersection for " + pSegment);
		}
		// no point is on the screen
		int count = 0;
		if (intersection(pSegment, mClipMin, mClipMin, mClipMin, mClipMax)) { // x mClipMin segment
			final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
			point.set(mOptimIntersection);
		}
		if (intersection(pSegment, mClipMax, mClipMin, mClipMax, mClipMax)) { // x mClipMax segment
			final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
			point.set(mOptimIntersection);
		}
		if (intersection(pSegment, mClipMin, mClipMin, mClipMax, mClipMin)) { // y mClipMin segment
			final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
			point.set(mOptimIntersection);
		}
		if (intersection(pSegment, mClipMin, mClipMax, mClipMax, mClipMax)) { // y mClipMax segment
			final PointL point = count ++ == 0 ? mOptimIntersection1 : mOptimIntersection2;
			point.set(mOptimIntersection);
		}
		if (count == 2) {
			final double distance1 = Distance.getSquaredDistanceToPoint(
					mOptimIntersection1.x, mOptimIntersection1.y, pSegment.left, pSegment.top);
			final double distance2 = Distance.getSquaredDistanceToPoint(
					mOptimIntersection2.x, mOptimIntersection2.y, pSegment.left, pSegment.top);
			final PointL start = distance1 < distance2 ? mOptimIntersection1 : mOptimIntersection2;
			final PointL end =  distance1 < distance2 ? mOptimIntersection2 : mOptimIntersection1;
			pSegment.left = start.x;
			pSegment.top = start.y;
			pSegment.right = end.x;
			pSegment.bottom = end.y;
			return;
		}
		if (count == 1) {
			pSegment.left = mOptimIntersection1.x;
			pSegment.top = mOptimIntersection1.y;
			pSegment.right = mOptimIntersection1.x;
			pSegment.bottom = mOptimIntersection1.y;
			return;
		}
		if (count == 0) {
			pSegment.left = clip(pSegment.left);
			pSegment.right = clip(pSegment.right);
			pSegment.top = clip(pSegment.top);
			pSegment.bottom = clip(pSegment.bottom);
			return;
		}
		throw new RuntimeException("Impossible mOptimIntersection count (" + count + ")");
	}

	/**
	 * Intersection of two segments
	 */
	private boolean intersection(
			final RectL segment, final long x3, final long y3, final long x4, final long y4
	) {
		return SegmentIntersection.intersection(
				segment.left, segment.top, segment.right, segment.bottom,
				x3, y3, x4, y4, mOptimIntersection);
	}

	/**
	 * Intersection of a segment with the 4 segments of the clip area
	 */
	private boolean intersection(final RectL pSegment) {
		return intersection(pSegment, mClipMin, mClipMin, mClipMin, mClipMax) // x min segment
				|| intersection(pSegment, mClipMax, mClipMin, mClipMax, mClipMax) // x max segment
				|| intersection(pSegment, mClipMin, mClipMin, mClipMax, mClipMin) // y min segment
				|| intersection(pSegment, mClipMin, mClipMax, mClipMax, mClipMax); // y max segment
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
		final List<PointL> segmentStartPoints = new ArrayList<>();
		final List<PointL> segmentEndPoints = new ArrayList<>();
		getSegmentsFromProjected(
				pProjection, mProjectedPoints, segmentStartPoints, segmentEndPoints);
		final double squaredTolerance = tolerance * tolerance;
		final int size = segmentStartPoints.size();
		for (int i = 0 ; i < size ; i ++) {
			final PointL start = segmentStartPoints.get(i);
			final PointL end = segmentEndPoints.get(i);
			if (squaredTolerance > Distance.getSquaredDistanceToSegment(
					pixel.x, pixel.y, start.x, start.y, end.x, end.y)) {
				return true;
			}
		}
		return false;
	}
}

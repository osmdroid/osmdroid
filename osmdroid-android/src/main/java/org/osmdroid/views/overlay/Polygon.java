package org.osmdroid.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.SegmentIntersection;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;

/**
 * A polygon on the earth's surface that can have a
 * popup-{@link org.osmdroid.views.overlay.infowindow.InfoWindow} (a bubble).
 *
 * Mimics the Polygon class from Google Maps Android API v2 as much as possible. Main differences:<br>
 * - Doesn't support: Z-Index, Geodesic mode<br>
 * - Supports InfoWindow. 
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='src='./doc-files/marker-infowindow-classes.png' />
 *
 * @author Viesturs Zarins, Martin Pearman for efficient PathOverlay.draw method
 * @author M.Kergall: transformation from PathOverlay to Polygon
 * @see <a href="http://developer.android.com/reference/com/google/android/gms/maps/model/Polygon.html">Google Maps Polygon</a>
 */
public class Polygon extends OverlayWithIW {

	/** inner class holding one ring: the polygon outline, or a hole inside the polygon */
	class LinearRing {
		/** original GeoPoints */
		private double mOriginalPoints[][]; //as an array, to reduce object creation
		
		/** Stores points, converted to the map projection. */
		ArrayList<PointL> mConvertedPoints;

		/** is precomputation of points done or not */
		boolean mPrecomputed;
		
		LinearRing(){
			mOriginalPoints = new double[0][2];
			mConvertedPoints = new ArrayList<>(0);
			mPrecomputed = false;
		}
		
		ArrayList<GeoPoint> getPoints(){
			int size = mOriginalPoints.length;
			ArrayList<GeoPoint> result = new ArrayList<GeoPoint>(size);
			for (int i=0; i<size; i++){
				GeoPoint gp = new GeoPoint(mOriginalPoints[i][0], mOriginalPoints[i][1]);
				result.add(gp);
			}
			return result;
		}
		
		void setPoints(final List<GeoPoint> points) {
			int size = points.size();
			mOriginalPoints = new double[size][2];
			mConvertedPoints = new ArrayList<>(size);
			int i=0;
			for (GeoPoint p:points){
				mOriginalPoints[i][0] = p.getLatitude();
				mOriginalPoints[i][1] = p.getLongitude();
				i++;
			}
			mPrecomputed = false;
		}
		
		/** 
		 * Note - highly optimized to handle long paths, proceed with care.
		 * Should be fine up to 10K points.
		 */
		protected void buildPathPortion(Projection pj){
			final int size = mOriginalPoints.length;
			if (size < 2) // nothing to paint
				return;

			// precompute new points to the intermediate projection.
			if (!mPrecomputed){
				for (int i=0; i<size; i++) {
					mConvertedPoints.add(pj.toProjectedPixels(mOriginalPoints[i][0], mOriginalPoints[i][1], null));
				}
				mPrecomputed = true;
			}

			final double powerDifference = pj.getProjectedPowerDifference();
			final PointL screenPoint0 = new PointL(); // points on screen
			final PointL screenPoint1 = new PointL();
			final RectL currentSegment = new RectL();
			final PointL latestPathPoint = new PointL();
			final double worldSize = TileSystem.MapSize(pj.getZoomLevel());
			boolean firstPoint = true;
			boolean firstSegment = true;
			for (final PointL projectedPoint : mConvertedPoints) {
				// compute next points
				pj.getLongPixelsFromProjected(projectedPoint, powerDifference, screenPoint1);
				if (firstPoint) {
					firstPoint = false;
				} else {
					setCloserPoint(screenPoint0, screenPoint1, worldSize);
					currentSegment.set(screenPoint0.x, screenPoint0.y, screenPoint1.x, screenPoint1.y);
					clip(currentSegment);
					if (firstSegment) {
						firstSegment = false;
						mPath.moveTo(currentSegment.left, currentSegment.top);
						latestPathPoint.set(currentSegment.left, currentSegment.top);
					}
					lineTo(currentSegment.left, currentSegment.top, latestPathPoint);
					lineTo(currentSegment.right, currentSegment.bottom, latestPathPoint);
				}

				// update starting point to next position
				screenPoint0.set(screenPoint1);
			}
			mPath.close();
		}

		private void setCloserPoint(final PointL pPrevious, final PointL pNext, final double pWorldSize) {
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
		 * @since 6.0.0
		 */
		private void lineTo(final long pX, final long pY, final PointL pLatest) {
			if (pLatest.x != pX || pLatest.y != pY) {
				mPath.lineTo(pX, pY);
				pLatest.set(pX, pY);
			}
		}

		/**
		 * We build a virtual area [clipMin, clipMin, clipMax, clipMax]
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
		private static final int clipMax = Integer.MAX_VALUE / 8; // "big enough but not too much"
		private static final int clipMin = Integer.MIN_VALUE / 8;
		private final PointL intersection = new PointL();
		private final PointL intersection1 = new PointL();
		private final PointL intersection2 = new PointL();

		/**
		 * @since 6.0.0
		 */
		private boolean isInClipArea(final long pX, final long pY) {
			return pX > clipMin && pX < clipMax && pY > clipMin && pY < clipMax;
		}

		/**
		 * @since 6.0.0
		 */
		private long clip(final long value) {
			return value <= clipMin ? clipMin : value >= clipMax ? clipMax : 0;
		}

		/**
		 * @since 6.0.0
		 */
		private void clip(final RectL pSegment) {
			if (isInClipArea(pSegment.left, pSegment.top)) {
				if (isInClipArea(pSegment.right, pSegment.bottom)) {
					return; // nothing to do
				}
				if (intersection(pSegment)) {
					pSegment.right = intersection.x;
					pSegment.bottom = intersection.y;
					return;
				}
				throw new RuntimeException("Cannot find expected intersection for " + pSegment);
			}
			if (isInClipArea(pSegment.right, pSegment.bottom)) {
				if (intersection(pSegment)) {
					pSegment.left = intersection.x;
					pSegment.top = intersection.y;
					return;
				}
				throw new RuntimeException("Cannot find expected intersection for " + pSegment);
			}
			// no point is on the screen
			int count = 0;
			if (intersection(pSegment, clipMin, clipMin, clipMin, clipMax)) { // x clipMin segment
				final PointL point = count ++ == 0 ? intersection1 : intersection2;
				point.set(intersection);
			}
			if (intersection(pSegment, clipMax, clipMin, clipMax, clipMax)) { // x clipMax segment
				final PointL point = count ++ == 0 ? intersection1 : intersection2;
				point.set(intersection);
			}
			if (intersection(pSegment, clipMin, clipMin, clipMax, clipMin)) { // y clipMin segment
				final PointL point = count ++ == 0 ? intersection1 : intersection2;
				point.set(intersection);
			}
			if (intersection(pSegment, clipMin, clipMax, clipMax, clipMax)) { // y clipMax segment
				final PointL point = count ++ == 0 ? intersection1 : intersection2;
				point.set(intersection);
			}
			if (count == 2) {
				final double distance1 = intersection1.squareDistanceTo(pSegment.left, pSegment.top);
				final double distance2 = intersection2.squareDistanceTo(pSegment.left, pSegment.top);
				final PointL start = distance1 < distance2 ? intersection1 : intersection2;
				final PointL end =  distance1 < distance2 ? intersection2 : intersection1;
				pSegment.left = start.x;
				pSegment.top = start.y;
				pSegment.right = end.x;
				pSegment.bottom = end.y;
				return;
			}
			if (count == 1) {
				pSegment.left = intersection1.x;
				pSegment.top = intersection1.y;
				pSegment.right = intersection1.x;
				pSegment.bottom = intersection1.y;
				return;
			}
			if (count == 0) {
				pSegment.left = clip(pSegment.left);
				pSegment.right = clip(pSegment.right);
				pSegment.top = clip(pSegment.top);
				pSegment.bottom = clip(pSegment.bottom);
				return;
			}
			throw new RuntimeException("Impossible intersection count (" + count + ")");
		}

		/**
		 * @since 6.0.0
		 */
		private boolean intersection(
				final RectL segment, final long x3, final long y3, final long x4, final long y4
		) {
			return SegmentIntersection.intersection(
					segment.left, segment.top, segment.right, segment.bottom,
					x3, y3, x4, y4, intersection);
		}

		/**
		 * @since 6.0.0
		 */
		private boolean intersection(final RectL pSegment) {
			return intersection(pSegment, clipMin, clipMin, clipMin, clipMax) // x clipMin segment
					|| intersection(pSegment, clipMax, clipMin, clipMax, clipMax) // x clipMax segment
					|| intersection(pSegment, clipMin, clipMin, clipMax, clipMin) // y clipMin segment
					|| intersection(pSegment, clipMin, clipMax, clipMax, clipMax); // y clipMax segment
		}
	}

	private LinearRing mOutline;
	private ArrayList<LinearRing> mHoles;
	
	/** Paint settings. */
	protected Paint mFillPaint;
	protected Paint mOutlinePaint;

	private final Path mPath = new Path(); //Path drawn is kept for click detection

	// ===========================================================
	// Constructors
	// ===========================================================

	/** Use {@link #Polygon()} instead */
	@Deprecated
	public Polygon(final Context ctx) {
		this();
	}

	public Polygon() {
		super();
		mFillPaint = new Paint();
		mFillPaint.setColor(Color.TRANSPARENT);
		mFillPaint.setStyle(Paint.Style.FILL);
		mOutlinePaint = new Paint();
		mOutlinePaint.setColor(Color.BLACK);
		mOutlinePaint.setStrokeWidth(10.0f);
		mOutlinePaint.setStyle(Paint.Style.STROKE);
		mOutlinePaint.setAntiAlias(true);
		mOutline = new LinearRing();
		mHoles = new ArrayList<LinearRing>(0);
		mPath.setFillType(Path.FillType.EVEN_ODD); //for correct support of holes
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getFillColor() {
		return mFillPaint.getColor();
	}

	public int getStrokeColor() {
		return mOutlinePaint.getColor();
	}

	public float getStrokeWidth() {
		return mOutlinePaint.getStrokeWidth();
	}
	
	/** @return the Paint used for the outline. This allows to set advanced Paint settings. */
	public Paint getOutlinePaint(){
		return mOutlinePaint;
	}
	
	/**
	 * @return a copy of the list of polygon's vertices. 
	 */
	public List<GeoPoint> getPoints(){
		return mOutline.getPoints();
	}

	public boolean isVisible(){
		return isEnabled();
	}
	
	public void setFillColor(final int fillColor) {
		mFillPaint.setColor(fillColor);
	}

	public void setStrokeColor(final int color) {
		mOutlinePaint.setColor(color);
	}
	
	public void setStrokeWidth(final float width) {
		mOutlinePaint.setStrokeWidth(width);
	}
	
	public void setVisible(boolean visible){
		setEnabled(visible);
	}

	/**
	 * This method will take a copy of the points.
	 */
	public void setPoints(final List<GeoPoint> points) {
		mOutline.setPoints(points);
	}

	public void setHoles(List<? extends List<GeoPoint>> holes){
		mHoles = new ArrayList<LinearRing>(holes.size());
		for (List<GeoPoint> sourceHole:holes){
			LinearRing newHole = new LinearRing();
			newHole.setPoints(sourceHole);
			mHoles.add(newHole);
		}
	}

	/**
	 * returns a copy of the holes this polygon contains
	 * @return never null
	 */
	public List<List<GeoPoint>> getHoles(){
		List<List<GeoPoint>> result = new ArrayList<List<GeoPoint>>(mHoles.size());
		for (LinearRing hole:mHoles){
			result.add(hole.getPoints());
		}
		return result;
	}

	/** Build a list of GeoPoint as a circle. 
	 * @param center center of the circle
	 * @param radiusInMeters
	 * @return the list of GeoPoint
	 */
	public static ArrayList<GeoPoint> pointsAsCircle(GeoPoint center, double radiusInMeters){
		ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>(360/6);
		for (int f = 0; f < 360; f += 6){
			GeoPoint onCircle = center.destinationPoint(radiusInMeters, f);
			circlePoints.add(onCircle);
		}
		return circlePoints;
	}
	
	/** Build a list of GeoPoint as a rectangle. 
	 * @param rectangle defined as a BoundingBox 
	 * @return the list of 4 GeoPoint */
	@Deprecated
	public static ArrayList<IGeoPoint> pointsAsRect(BoundingBoxE6 rectangle){
		ArrayList<IGeoPoint> points = new ArrayList<IGeoPoint>(4);
		points.add(new GeoPoint(rectangle.getLatNorthE6(), rectangle.getLonWestE6()));
		points.add(new GeoPoint(rectangle.getLatNorthE6(), rectangle.getLonEastE6()));
		points.add(new GeoPoint(rectangle.getLatSouthE6(), rectangle.getLonEastE6()));
		points.add(new GeoPoint(rectangle.getLatSouthE6(), rectangle.getLonWestE6()));
		return points;
	}

	/** Build a list of GeoPoint as a rectangle.
	 * @param rectangle defined as a BoundingBox
	 * @return the list of 4 GeoPoint */
	public static ArrayList<IGeoPoint> pointsAsRect(BoundingBox rectangle){
		ArrayList<IGeoPoint> points = new ArrayList<IGeoPoint>(4);
		points.add(new GeoPoint(rectangle.getLatNorth(), rectangle.getLonWest()));
		points.add(new GeoPoint(rectangle.getLatNorth(), rectangle.getLonEast()));
		points.add(new GeoPoint(rectangle.getLatSouth(), rectangle.getLonEast()));
		points.add(new GeoPoint(rectangle.getLatSouth(), rectangle.getLonWest()));
		return points;
	}

	/** Build a list of GeoPoint as a rectangle. 
	 * @param center of the rectangle
	 * @param lengthInMeters on longitude
	 * @param widthInMeters on latitude
	 * @return the list of 4 GeoPoint
	 */
	public static ArrayList<IGeoPoint> pointsAsRect(GeoPoint center, double lengthInMeters, double widthInMeters){
		ArrayList<IGeoPoint> points = new ArrayList<IGeoPoint>(4);
		GeoPoint east = center.destinationPoint(lengthInMeters*0.5, 90.0f);
		GeoPoint south = center.destinationPoint(widthInMeters*0.5, 180.0f);
		double westLon = center.getLongitude()*2 - east.getLongitude();
		double northLat = center.getLatitude()*2 - south.getLatitude();
		points.add(new GeoPoint(south.getLatitude(), east.getLongitude()));
		points.add(new GeoPoint(south.getLatitude(), westLon));
		points.add(new GeoPoint(northLat, westLon));
		points.add(new GeoPoint(northLat, east.getLongitude()));
		return points;
	}
	
	@Override public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		if (shadow) {
			return;
		}

		final Projection pj = mapView.getProjection();
		mPath.rewind();
		
		mOutline.buildPathPortion(pj);
		
		for (LinearRing hole:mHoles){
			hole.buildPathPortion(pj);
		}
		
		canvas.drawPath(mPath, mFillPaint);
		canvas.drawPath(mPath, mOutlinePaint);
	}
	
	/** Important note: this function returns correct results only if the Polygon has been drawn before, 
	 * and if the MapView positioning has not changed. 
	 * @param event
	 * @return true if the Polygon contains the event position. 
	 */
	public boolean contains(MotionEvent event){
		if (mPath.isEmpty())
			return false;
		RectF bounds = new RectF(); //bounds of the Path
		mPath.computeBounds(bounds, true);
		Region region = new Region();
		//Path has been computed in #draw (we assume that if it can be clicked, it has been drawn before). 
		region.setPath(mPath, new Region((int)bounds.left, (int)bounds.top, 
				(int) (bounds.right), (int) (bounds.bottom)));
		return region.contains((int)event.getX(), (int)event.getY());
	}
	
	@Override public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){
		if (mInfoWindow == null)
			//no support for tap:
			return false;
		boolean tapped = contains(event);
		if (tapped){
			Projection pj = mapView.getProjection();
			GeoPoint position = (GeoPoint)pj.fromPixels((int)event.getX(), (int)event.getY());
			mInfoWindow.open(this, position, 0, 0);
		}
		return tapped;
	}

	@Override
	public void onDetach(MapView mapView) {
		mOutline=null;
		mHoles.clear();
		onDestroy();
	}

}

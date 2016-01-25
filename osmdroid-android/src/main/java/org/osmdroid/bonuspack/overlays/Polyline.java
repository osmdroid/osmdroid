package org.osmdroid.bonuspack.overlays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.GeometryMath;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.util.constants.MathConstants;

import java.util.ArrayList;
import java.util.List;

import microsoft.mappoint.TileSystem;

/**
 * A polyline is a list of points, where line segments are drawn between consecutive points.
 * Mimics the Polyline class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support Z-Index: drawing order is the order in map overlays<br/>
 * - Supports InfoWindow (must be a BasicInfoWindow). <br/>
 * <p/>
 * Implementation: fork from osmdroid PathOverlay, adding Google API compatibility and Geodesic mode.
 *
 * @author M.Kergall
 * @see <a href="http://developer.android.com/reference/com/google/android/gms/maps/model/Polyline.html">Google Maps Polyline</a>
 */
public class Polyline extends OverlayWithIW {
	
	/** original GeoPoints */
	private int mOriginalPoints[][]; //as an array, to reduce object creation
	protected boolean mGeodesic;
	private final Path mPath = new Path();
	protected Paint mPaint = new Paint();
	/** points, converted to the map projection */
	private ArrayList<Point> mPoints;
	/** Number of points that have precomputed values */
	private int mPointsPrecomputed;
	public boolean mRepeatPath = false; /** if true: at low zoom level showing multiple maps, path will be drawn on all maps */

	/** bounding rectangle for the current line segment */
	private final Rect mLineBounds = new Rect();
	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	protected OnClickListener mOnClickListener;

	public Polyline(Context ctx){
		this(new DefaultResourceProxyImpl(ctx));
	}
	
	public Polyline(final ResourceProxy resourceProxy){
		super(resourceProxy);
		//default as defined in Google API:
		this.mPaint.setColor(Color.BLACK);
		this.mPaint.setStrokeWidth(10.0f);
		this.mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		this.clearPath();
		mOriginalPoints = new int[0][2];
		mGeodesic = false;
	}
	
	protected void clearPath() {
		this.mPoints = new ArrayList<Point>();
		this.mPointsPrecomputed = 0;
	}

	protected void addPoint(final GeoPoint aPoint) {
		addPoint(aPoint.getLatitudeE6(), aPoint.getLongitudeE6());
	}

	protected void addPoint(final int aLatitudeE6, final int aLongitudeE6) {
		mPoints.add(new Point(aLatitudeE6, aLongitudeE6));
	}

	/** @return a copy of the points. */
	public List<GeoPoint> getPoints(){
		List<GeoPoint> result = new ArrayList<GeoPoint>(mOriginalPoints.length);
		for (int i=0; i<mOriginalPoints.length; i++){
			GeoPoint gp = new GeoPoint(mOriginalPoints[i][0], mOriginalPoints[i][1]);
			result.add(gp);
		}
		return result;
	}
	
	public int getNumberOfPoints(){
		return mOriginalPoints.length;
	}
	
	public int getColor(){
		return mPaint.getColor();
	}
	
	public float getWidth(){
		return mPaint.getStrokeWidth();
	}
	
	/** @return the Paint used. This allows to set advanced Paint settings. */
	public Paint getPaint(){
		return mPaint;
	}
	
	public boolean isVisible(){
		return isEnabled();
	}
	
	public boolean isGeodesic(){
		return mGeodesic;
	}
	
	public void setColor(int color){
		mPaint.setColor(color);
	}
	
	public void setWidth(float width){
		mPaint.setStrokeWidth(width);
	}
	
	public void setVisible(boolean visible){
		setEnabled(visible);
	}

	public void setOnClickListener(OnClickListener listener){
		mOnClickListener = listener;
	}

	protected void addGreatCircle(final GeoPoint startPoint, final GeoPoint endPoint, final int numberOfPoints) {
		//	adapted from page http://compastic.blogspot.co.uk/2011/07/how-to-draw-great-circle-on-map-in.html
		//	which was adapted from page http://maps.forum.nu/gm_flight_path.html

		// convert to radians
		final double lat1 = startPoint.getLatitude() * MathConstants.DEG2RAD;
		final double lon1 = startPoint.getLongitude() * MathConstants.DEG2RAD;
		final double lat2 = endPoint.getLatitude() * MathConstants.DEG2RAD;
		final double lon2 = endPoint.getLongitude() * MathConstants.DEG2RAD;

		final double d = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2) / 2), 2) + Math.cos(lat1) * Math.cos(lat2)
				* Math.pow(Math.sin((lon1 - lon2) / 2), 2)));
		double bearing = Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2),
				Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2))
				/ -MathConstants.DEG2RAD;
		bearing = bearing < 0 ? 360 + bearing : bearing;
		
		for (int i = 1; i <= numberOfPoints; i++) {
			final double f = 1.0 * i / (numberOfPoints+1);
			final double A = Math.sin((1 - f) * d) / Math.sin(d);
			final double B = Math.sin(f * d) / Math.sin(d);
			final double x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
			final double y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
			final double z = A * Math.sin(lat1) + B * Math.sin(lat2);

			final double latN = Math.atan2(z, Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
			final double lonN = Math.atan2(y, x);
			addPoint((int) (latN * MathConstants.RAD2DEG * 1E6), (int) (lonN * MathConstants.RAD2DEG * 1E6));
		}
	}
	
	/** Set the points. 
	 * Note that a later change in the original points List will have no effect. 
	 * To add/remove/change points, you must call setPoints again. 
	 * If geodesic mode has been set, the long segments will follow the earth "great circle". */
	public void setPoints(List<GeoPoint> points){
		clearPath();
		int size = points.size();
		mOriginalPoints = new int[size][2];
		for (int i=0; i<size; i++){
			GeoPoint p = points.get(i);
			mOriginalPoints[i][0] = p.getLatitudeE6();
			mOriginalPoints[i][1] = p.getLongitudeE6();
			if (!mGeodesic){
				addPoint(p);
			} else {
				if (i>0){
					//add potential intermediate points:
					GeoPoint prev = points.get(i-1);
					final int greatCircleLength = prev.distanceTo(p);
					//add one point for every 100kms of the great circle path
					final int numberOfPoints = greatCircleLength/100000;
					addGreatCircle(prev, p, numberOfPoints);
				}
				addPoint(p);
			}
		}
	}
	
	/** Sets whether to draw each segment of the line as a geodesic or not. 
	 * Warning: it takes effect only if set before setting the points in the Polyline. */
	public void setGeodesic(boolean geodesic){
		mGeodesic = geodesic;
	}

	protected void precomputePoints(Projection pj){
		final int size = this.mPoints.size();
		while (this.mPointsPrecomputed < size) {
			final Point pt = this.mPoints.get(this.mPointsPrecomputed);
			pj.toProjectedPixels(pt.x, pt.y, pt);
			this.mPointsPrecomputed++;
		}
	}

	protected void drawOld(final Canvas canvas, final MapView mapView, final boolean shadow) {

		if (shadow) {
			return;
		}

		final int size = this.mPoints.size();
		if (size < 2) {
			// nothing to paint
			return;
		}

		final Projection pj = mapView.getProjection();

		// precompute new points to the intermediate projection.
		precomputePoints(pj);

		Point screenPoint0 = null; // points on screen
		Point screenPoint1;
		Point projectedPoint0; // points from the points list
		Point projectedPoint1;

		// clipping rectangle in the intermediate projection, to avoid performing projection.
		BoundingBoxE6 boundingBox = pj.getBoundingBox();
		Point topLeft = pj.toProjectedPixels(boundingBox.getLatNorthE6(),
				boundingBox.getLonWestE6(), null);
		Point bottomRight = pj.toProjectedPixels(boundingBox.getLatSouthE6(),
				boundingBox.getLonEastE6(), null);
		final Rect clipBounds = new Rect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
		// take into account map orientation:
		if (mapView.getMapOrientation() != 0.0f)
			GeometryMath.getBoundingBoxForRotatatedRectangle(clipBounds, mapView.getMapOrientation(), clipBounds);

		mPath.rewind();
		projectedPoint0 = this.mPoints.get(size - 1);
		mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

		for (int i = size - 2; i >= 0; i--) {
			// compute next points
			projectedPoint1 = this.mPoints.get(i);
			mLineBounds.union(projectedPoint1.x, projectedPoint1.y);

			if (!Rect.intersects(clipBounds, mLineBounds)) {
				// skip this line, move to next point
				projectedPoint0 = projectedPoint1;
				screenPoint0 = null;
				continue;
			}

			// the starting point may be not calculated, because previous segment was out of clip
			// bounds
			if (screenPoint0 == null) {
				screenPoint0 = pj.toPixelsFromProjected(projectedPoint0, this.mTempPoint1);
				mPath.moveTo(screenPoint0.x, screenPoint0.y);
			}

			screenPoint1 = pj.toPixelsFromProjected(projectedPoint1, this.mTempPoint2);

			// skip this point, too close to previous point
			if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				continue;
			}

			mPath.lineTo(screenPoint1.x, screenPoint1.y);

			// update starting point to next position
			projectedPoint0 = projectedPoint1;
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
			mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);
		}

		canvas.drawPath(mPath, mPaint);
	}

	@Override
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {

		if (shadow) {
			return;
		}

		final int size = mPoints.size();
		if (size < 2) {
			// nothing to paint
			return;
		}

		final Projection pj = mapView.getProjection();

		final int halfMapSize = TileSystem.MapSize(mapView.getProjection().getZoomLevel()) / 2; // 180° in longitude in pixels
		final int southLimit = pj.toPixelsFromMercator(0, halfMapSize * 2, null).y;            // southern Limit of the map in Pixels

		// precompute new points to the intermediate projection.
		precomputePoints(pj);

		Point projectedPoint0 = mPoints.get(0); // points from the points list

		Point screenPoint0 = pj.toPixelsFromProjected(projectedPoint0, mTempPoint1); // points on screen
		Point screenPoint1;

		mPath.rewind();
		mPath.moveTo(screenPoint0.x, screenPoint0.y);

		for (int i = 1; i < size; i++) {
			// compute next points
			Point projectedPoint1 = mPoints.get(i);
			screenPoint1 = pj.toPixelsFromProjected(projectedPoint1, this.mTempPoint2);

			if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				// skip this point, too close to previous point
				continue;
			}

			// check for lines exceeding 180° in longitude, or lines crossing to another map:
			// cut line into two segments
			if ((Math.abs(screenPoint1.x - screenPoint0.x) > halfMapSize)
					// check for lines crossing the southern limit
					|| (screenPoint1.y >= southLimit) != (screenPoint0.y >= southLimit)) {
				// handle x and y coordinates separately
				int x0 = screenPoint0.x;
				int y0 = screenPoint0.y;
				int x1 = screenPoint1.x;
				int y1 = screenPoint1.y;

				// first check x
				if (Math.abs(screenPoint1.x - screenPoint0.x) > halfMapSize) {// x has to be adjusted
					if (screenPoint1.x < mapView.getWidth() / 2) {
						// screenPoint1 is left of screenPoint0
						x1 += halfMapSize * 2; // set x1 360° east of screenPoint1
						x0 -= halfMapSize * 2; // set x0 360° west of screenPoint0
					} else {
						x1 -= halfMapSize * 2;
						x0 += halfMapSize * 2;
					}
				}

				// now check y
				if ((screenPoint1.y >= southLimit) != (screenPoint0.y >= southLimit)) {
					// line is crossing from one map to the other
					if (screenPoint1.y >= southLimit) {
						// screenPoint1 was switched to map below
						y1 -= halfMapSize * 2;  // set y1 into northern map
						y0 += halfMapSize * 2;  // set y0 into map below
					} else {
						y1 += halfMapSize * 2;
						y0 -= halfMapSize * 2;
					}
				}
				mPath.lineTo(x1, y1);
				mPath.moveTo(x0, y0);
			} // end of line break check

			mPath.lineTo(screenPoint1.x, screenPoint1.y);

			// update starting point to next position
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
		}

		canvas.drawPath(mPath, mPaint);

		if (mRepeatPath) {
			Path mPathCopy = new Path(mPath);
			mPathCopy.offset(-halfMapSize * 2, 0);                 // create left shifted copy of mPath
			if (halfMapSize * 2 < mapView.getWidth()) {
				mPathCopy.addPath(mPath, halfMapSize * 2, 0);      // add right shifted copy of mPath
			}
			if (halfMapSize * 2 < mapView.getHeight()) {
				mPathCopy.addPath(mPathCopy, 0, halfMapSize * 2); // duplicates mPathCopy one map south
				mPathCopy.addPath(mPath, 0, halfMapSize * 2);         // add right shifted copy of mPath
			}
			mPathCopy.addPath(mPath, 0, -halfMapSize * 2);         // add up shifted copy of mPath
			canvas.drawPath(mPathCopy, mPaint);
		}
	}
	
	/** Detection is done is screen coordinates. 
	 * @param point
	 * @param tolerance in pixels
	 * @return true if the Polyline is close enough to the point. 
	 */
	public boolean isCloseTo(GeoPoint point, double tolerance, MapView mapView) {
		final Projection pj = mapView.getProjection();
		precomputePoints(pj);
		Point p = pj.toPixels(point, null);
		int i = 0;
		boolean found = false;
		while (i < mPointsPrecomputed - 1 && !found) {
			Point projectedPoint1 = mPoints.get(i);
			if (i == 0){
				pj.toPixelsFromProjected(projectedPoint1, mTempPoint1);
			} else {
				//reuse last b:
				mTempPoint1.set(mTempPoint2.x, mTempPoint2.y);
			}
			Point projectedPoint2 = mPoints.get(i+1);
			pj.toPixelsFromProjected(projectedPoint2, mTempPoint2);
			found = (linePointDist(mTempPoint1, mTempPoint2, p, true) <= tolerance);
			//TODO: if found, compute and return the point ON the line. 
			i++;
		}
		return found;
	}
	
	// Compute the dot product AB x AC
	private double dot(Point A, Point B, Point C) {
		double AB_X = B.x - A.x;
		double AB_Y = B.y - A.y;
		double BC_X = C.x - B.x;
		double BC_Y = C.y - B.y;
		double dot = AB_X * BC_X + AB_Y * BC_Y;
		return dot;
	}

	// Compute the cross product AB x AC
	private double cross(Point A, Point B, Point C) {
		double AB_X = B.x - A.x;
		double AB_Y = B.y - A.y;
		double AC_X = C.x - A.x;
		double AC_Y = C.y - A.y;
		double cross = AB_X * AC_Y - AB_Y * AC_X;
		return cross;
	}

	// Compute the distance from A to B
	private double distance(Point A, Point B) {
		double dX = A.x - B.x;
		double dY = A.y - B.y;
		return Math.sqrt(dX * dX + dY * dY);
	}

	/** 
	 * @param A
	 * @param B
	 * @param C
	 * @param isSegment true if AB is a segment, not a line. 
	 * @return the distance from AB to C. 
	 */
	private double linePointDist(Point A, Point B, Point C, boolean isSegment) {
		double dAB = distance(A, B);
		if (dAB == 0.0)
			return distance(A, C);
		double dist = cross(A, B, C) / dAB;
		if (isSegment) {
			double dot1 = dot(A, B, C);
			if (dot1 > 0)
				return distance(B, C);
			double dot2 = dot(B, A, C);
			if (dot2 > 0)
				return distance(A, C);
		}
		return Math.abs(dist);
	}

	public void showInfoWindow(GeoPoint position){
		if (mInfoWindow == null)
			return;
		mInfoWindow.open(this, position, 0, 0);
	}

	@Override public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){
		final Projection pj = mapView.getProjection();
		GeoPoint eventPos = (GeoPoint) pj.fromPixels((int)event.getX(), (int)event.getY());
		double tolerance = mPaint.getStrokeWidth();
		boolean touched = isCloseTo(eventPos, tolerance, mapView);
		if (touched){
			if (mOnClickListener == null){
				return onClickDefault(this, mapView, eventPos);
			} else {
				return mOnClickListener.onClick(this, mapView, eventPos);
			}
		} else
			return touched;
	}

	//-- Polyline events listener interfaces ------------------------------------

	public interface OnClickListener{
		abstract boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos);
	}

	/** default behaviour when no click listener is set */
	protected boolean onClickDefault(Polyline polyline, MapView mapView, GeoPoint eventPos) {
		polyline.showInfoWindow(eventPos);
		return true;
	}

}

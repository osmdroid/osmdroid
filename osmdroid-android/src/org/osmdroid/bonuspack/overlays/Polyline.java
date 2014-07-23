package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;
import java.util.List;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.GeometryMath;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.util.constants.MathConstants;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * A polyline is a list of points, where line segments are drawn between consecutive points. 
 *  Mimics the Polyline class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support Z-Index: drawing order is the order in map overlays<br/>
 * 
 * Implementation: fork from osmdroid PathOverlay, adding Google API compatibility and Geodesic mode. 
 * 
 * @see <a href="http://developer.android.com/reference/com/google/android/gms/maps/model/Polyline.html">Google Maps Polyline</a>
 * @author M.Kergall
 */
public class Polyline extends Overlay /*NonAcceleratedOverlay*/ {
	
	/** original GeoPoints */
	private int mOriginalPoints[][]; //as an array, to reduce object creation
	protected boolean mGeodesic;
	private final Path mPath = new Path();
	protected Paint mPaint = new Paint();
	/** points, converted to the map projection */
	private ArrayList<Point> mPoints;
	/** Number of points that have precomputed values */
	private int mPointsPrecomputed;
	
	/** bounding rectangle for the current line segment */
	private final Rect mLineBounds = new Rect();
	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

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

	@Override protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {

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
		while (this.mPointsPrecomputed < size) {
			final Point pt = this.mPoints.get(this.mPointsPrecomputed);
			pj.toProjectedPixels(pt.x, pt.y, pt);

			this.mPointsPrecomputed++;
		}

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
	
}

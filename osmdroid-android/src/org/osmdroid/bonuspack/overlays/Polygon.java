package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;
import java.util.List;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;

/**
 * A polygon on the earth's surface. 
 * Mimics the Polygon class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support: holes, Z-Index, Geodesic mode<br/>
 * - Supports InfoWindow. 
 * 
 * @author Viesturs Zarins, Martin Pearman for efficient PathOverlay.draw method
 * @author M.Kergall: transformation from PathOverlay to Polygon
 */
public class Polygon extends Overlay {

	/** original GeoPoints */
	//private List<GeoPoint> mPoints;
	private int mOriginalPoints[][]; //as an array, to reduce object creation
	
	/** Stores points, converted to the map projection. */
	private List<Point> mConvertedPoints;

	/** Number of points that have precomputed values. */
	private int mPointsPrecomputed;

	/** Paint settings. */
	protected Paint mFillPaint;
	protected Paint mOutlinePaint;

	private final Path mPath = new Path();

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	//InfoWindow handling
	protected String mTitle, mSnippet;
	protected InfoWindow mBubble;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	public Polygon(final Context ctx) {
		this(new DefaultResourceProxyImpl(ctx));
	}

	public Polygon(final ResourceProxy resourceProxy) {
		super(resourceProxy);
		mFillPaint = new Paint();
		mFillPaint.setColor(Color.TRANSPARENT);
		mFillPaint.setStyle(Paint.Style.FILL);
		mOutlinePaint = new Paint();
		mOutlinePaint.setColor(Color.BLACK);
		mOutlinePaint.setStrokeWidth(10.0f);
		mOutlinePaint.setStyle(Paint.Style.STROKE);
		//mPoints = new ArrayList<GeoPoint>();
		mOriginalPoints = new int[0][3];
		mConvertedPoints = new ArrayList<Point>();
		mPointsPrecomputed = 0;
		mTitle = ""; 
		mSnippet = "";
		mBubble = null;
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
	
	/**
	 * @return a copy of the list of polygon's vertices. 
	 */
	public List<GeoPoint> getPoints(){
		List<GeoPoint> result = new ArrayList<GeoPoint>(mOriginalPoints.length);
		for (int i=0; i<mOriginalPoints.length; i++){
			GeoPoint gp = new GeoPoint(mOriginalPoints[i][0], mOriginalPoints[i][1], mOriginalPoints[i][2]);
			result.add(gp);
		}
		return result;
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
		int size = points.size();
		mOriginalPoints = new int[size][3];
		int i=0;
		for (GeoPoint p:points){
			mOriginalPoints[i][0] = p.getLatitudeE6();
			mOriginalPoints[i][1] = p.getLongitudeE6();
			mOriginalPoints[i][2] = p.getAltitude();
			mConvertedPoints.add(new Point(p.getLatitudeE6(), p.getLongitudeE6()));
			i++;
		}
		if (size>=2){
			GeoPoint first = points.get(0);
			if (!first.equals(points.get(size-1))){
				//last point is not same as first: close the polygon by adding first at the end
				mConvertedPoints.add(new Point(first.getLatitudeE6(), first.getLongitudeE6()));
			}
		}
	}

	public void setTitle(String title){
		mTitle = title;
	}
	
	public void setSnippet(String snippet){
		mSnippet = snippet;
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public String getSnippet(){
		return mSnippet;
	}

	/**
	 * @param layoutResId resource id of the layout to use. Set 0 for removing the infowindow. 
	 * @param mapView
	 */
	public void setInfoWindow(int layoutResId, MapView mapView){
		if (layoutResId != 0)
			mBubble = new DefaultInfoWindow(layoutResId, mapView);
		else 
			mBubble = null;
	}
	
	/**
	 * This method draws the polygon. Note - highly optimized to handle long paths, proceed with care.
	 * Should be fine up to 10K points.
	 */
	@Override protected void draw(Canvas canvas, MapView mapView, boolean shadow) {

		if (shadow) {
			return;
		}

		final int size = this.mConvertedPoints.size();
		if (size < 2) {
			// nothing to paint
			return;
		}

		final Projection pj = mapView.getProjection();

		// precompute new points to the intermediate projection.
		while (this.mPointsPrecomputed < size) {
			final Point pt = this.mConvertedPoints.get(this.mPointsPrecomputed);
			pj.toMapPixelsProjected(pt.x, pt.y, pt);
			this.mPointsPrecomputed++;
		}

		Point projectedPoint0 = this.mConvertedPoints.get(0); // points from the points list
		Point projectedPoint1;
		
		Point screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1); // points on screen
		Point screenPoint1;
		
		mPath.rewind();
		mPath.moveTo(screenPoint0.x, screenPoint0.y);

		for (int i=0; i<size; i++) {
			// compute next points
			projectedPoint1 = this.mConvertedPoints.get(i);
			screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);

			if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				// skip this point, too close to previous point
				continue;
			}

			mPath.lineTo(screenPoint1.x, screenPoint1.y);

			// update starting point to next position
			projectedPoint0 = projectedPoint1;
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
		}

		canvas.drawPath(mPath, mFillPaint);
		canvas.drawPath(mPath, mOutlinePaint);
	}
	
	public boolean contains(MotionEvent event, MapView mapView){
		if (mPath.isEmpty())
			return false;
		Projection pj = mapView.getProjection();
		Point point = pj.fromMapPixels((int)event.getX(), (int)event.getY(), null);
		RectF bounds = new RectF();
		mPath.computeBounds(bounds, true);
		Region region = new Region();
		region.setPath(mPath, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));
		return region.contains(point.x, point.y);
	}
	
	@Override public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){
		if (mBubble == null)
			//no support for tap:
			return false;
		boolean touched = contains(event, mapView);
		if (touched){
			Projection pj = mapView.getProjection();
			GeoPoint position = (GeoPoint)pj.fromPixels(event.getX(), event.getY());
			//as DefaultInfoWindow is expecting an ExtendedOverlayItem, build an ExtendedOverlayItem with needed information:
			ExtendedOverlayItem item = new ExtendedOverlayItem(mTitle, mSnippet, position, mapView.getContext());
			mBubble.open(item, item.getPoint(), 0, 0);
		}
		return touched;
	}
}

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
 * - Doesn't support: Z-Index, Geodesic mode<br/>
 * - Supports InfoWindow. 
 * 
 * @author Viesturs Zarins, Martin Pearman for efficient PathOverlay.draw method
 * @author M.Kergall: transformation from PathOverlay to Polygon
 */
public class Polygon extends Overlay {

	/** inner class holding one ring: the polygon outline, or a hole inside the polygon */
	class LinearRing {
		/** original GeoPoints */
		int mOriginalPoints[][]; //as an array, to reduce object creation
		
		/** Stores points, converted to the map projection. */
		ArrayList<Point> mConvertedPoints;

		/** is precomputation of points done or not */
		boolean mPrecomputed;
		
		LinearRing(){
			mOriginalPoints = new int[0][2];
			mConvertedPoints = new ArrayList<Point>(0);
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
			mOriginalPoints = new int[size][2];
			mConvertedPoints = new ArrayList<Point>(size);
			int i=0;
			for (GeoPoint p:points){
				mOriginalPoints[i][0] = p.getLatitudeE6();
				mOriginalPoints[i][1] = p.getLongitudeE6();
				mConvertedPoints.add(new Point(p.getLatitudeE6(), p.getLongitudeE6()));
				i++;
			}
			mPrecomputed = false;
		}
		
		/** 
		 * Note - highly optimized to handle long paths, proceed with care.
		 * Should be fine up to 10K points.
		 */
		protected void buildPathPortion(Projection pj){
			final int size = mConvertedPoints.size();
			if (size < 2) // nothing to paint
				return;

			// precompute new points to the intermediate projection.
			if (!mPrecomputed){
				for (int i=0; i<size; i++) {
					final Point pt = mConvertedPoints.get(i);
					pj.toMapPixelsProjected(pt.x, pt.y, pt);
				}
				mPrecomputed = true;
			}

			Point projectedPoint0 = mConvertedPoints.get(0); // points from the points list
			Point projectedPoint1;
			
			Point screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, mTempPoint1); // points on screen
			Point screenPoint1;
			
			mPath.moveTo(screenPoint0.x, screenPoint0.y);
			
			for (int i=0; i<size; i++) {
				// compute next points
				projectedPoint1 = mConvertedPoints.get(i);
				screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, mTempPoint2);

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
			mPath.close();
		}
		
	}

	private LinearRing mOutline;
	private ArrayList<LinearRing> mHoles;
	
	/** Paint settings. */
	protected Paint mFillPaint;
	protected Paint mOutlinePaint;

	private final Path mPath = new Path(); //Path drawn is kept for click detection
	private final RectF mBounds = new RectF(); //bounds of the Path

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	//InfoWindow handling
	protected String mTitle, mSnippet;
	protected InfoWindow mInfoWindow;
	
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
		mOutline = new LinearRing();
		mHoles = new ArrayList<LinearRing>(0);
		/*
		mTitle = null;
		mSnippet = null;
		mBubble = null;
		*/
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

	public List<ArrayList<GeoPoint>> getHoles(){
		ArrayList<ArrayList<GeoPoint>> result = new ArrayList<ArrayList<GeoPoint>>(mHoles.size());
		for (LinearRing hole:mHoles){
			result.add(hole.getPoints());
		}
		return result;
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

	/** By default, Polygon has no InfoWindow and do not react to a tap. 
	 * @param infoWindow the InfoWindow to be opened when tapping the Polygon. 
	 * Note that this InfoWindow will receive an ExtendedOverlayItem (not a Polygon) as an input, 
	 * so it MUST be able to handle ExtendedOverlayItem attributes. It will be typically a DefaultInfoWindow. 
	 * Set it to null to remove an existing InfoWindow. 
	 */
	public void setInfoWindow(InfoWindow infoWindow){
		mInfoWindow = infoWindow; //new DefaultInfoWindow(layoutResId, mapView);
	}
	
	@Override protected void draw(Canvas canvas, MapView mapView, boolean shadow) {

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
		
		//prepare mPath for click detection: 
		//As Region implementation (SkRegion.h) is based on "int32_t", it doesn't support values > 32768. 
		//(Path doesn't have this issue, it's based on SkScalar, with float values)
		//So we offset the Path to have (left, top) = (0, 0):
		mPath.computeBounds(mBounds, true);
		mPath.offset(-mBounds.left, -mBounds.top);
	}
	
	/** Important note: this function returns correct results only if the Polygon has been drawn before, 
	 * and if the MapView has not changed. 
	 * @param event
	 * @param mapView
	 * @return true if the Polygon contains the event position. 
	 */
	public boolean contains(MotionEvent event, MapView mapView){
		if (mPath.isEmpty())
			return false;
		Projection pj = mapView.getProjection();
		pj.fromMapPixels((int)event.getX(), (int)event.getY(), mTempPoint1);
		Region region = new Region();
		//Path has been computed in #draw (we assume that if it can be clicked, it has been drawn before). 
		//Then it has been offset to have (left, top) = (0,0)
		region.setPath(mPath, new Region(0, 0, 
				(int) (mBounds.right-mBounds.left), (int) (mBounds.bottom-mBounds.top)));
		return region.contains((int)(mTempPoint1.x-mBounds.left), (int)(mTempPoint1.y-mBounds.top));
	}
	
	@Override public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){
		if (mInfoWindow == null)
			//no support for tap:
			return false;
		boolean touched = contains(event, mapView);
		if (touched){
			Projection pj = mapView.getProjection();
			GeoPoint position = (GeoPoint)pj.fromPixels(event.getX(), event.getY());
			//as DefaultInfoWindow is expecting an ExtendedOverlayItem, build an ExtendedOverlayItem with needed information:
			ExtendedOverlayItem item = new ExtendedOverlayItem(mTitle, mSnippet, position);
			mInfoWindow.open(item, item.getPoint(), 0, 0);
		}
		return touched;
	}
}

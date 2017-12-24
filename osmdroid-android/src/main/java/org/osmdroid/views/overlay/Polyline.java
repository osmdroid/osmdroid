package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.RectL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.util.constants.MathConstants;

import java.util.List;

/**
 * A polyline is a list of points, where line segments are drawn between consecutive points.
 * Mimics the Polyline class from Google Maps Android API v2 as much as possible. Main differences:<br>
 * - Doesn't support Z-Index: drawing order is the order in map overlays<br>
 * - Supports InfoWindow (must be a BasicInfoWindow). <br>
 * <p></p>
 * Mimics the Polyline class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support Z-Index: drawing order is the order in map overlays<br/>
 * - Supports InfoWindow (must be a BasicInfoWindow). <br/>
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='src='./doc-files/marker-infowindow-classes.png' />
 *
 * @author M.Kergall
 * @see <a href="http://developer.android.com/reference/com/google/android/gms/maps/model/Polyline.html">Google Maps Polyline</a>
 */
public class Polyline extends OverlayWithIW {
	
	private boolean mGeodesic;
	private final Path mPath = new Path();
	private final Paint mPaint = new Paint();
	/** Bounding rectangle for view */
    private LinearRing mOutline = new LinearRing(mPath);
	private String id=null;
	private MilestoneManager mMilestoneManager;

	protected OnClickListener mOnClickListener;

	/** Use {@link #Polyline()} instead */
	@Deprecated
	public Polyline(Context ctx) {
		this();
	}

	public Polyline(){
		super();
		//default as defined in Google API:
		this.mPaint.setColor(Color.BLACK);
		this.mPaint.setStrokeWidth(10.0f);
		this.mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		this.clearPath();
		mGeodesic = false;
	}
	
	protected void clearPath() {
		mOutline.clearPath();
	}

	protected void addPoint(final GeoPoint aPoint) {
        mOutline.addPoint(aPoint);
	}

	@Deprecated
	protected void addPoint(final int aLatitudeE6, final int aLongitudeE6) {
		addPoint(new GeoPoint(aLatitudeE6, aLongitudeE6));
	}

	/** @return a copy of the points. */
	public List<GeoPoint> getPoints(){
		return mOutline.getPoints();
	}
	
	public int getNumberOfPoints(){
		return getPoints().size();
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
// TODO 0000		mOutline.setStrokeWidth(width);
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
			addPoint(new GeoPoint(latN * MathConstants.RAD2DEG, lonN * MathConstants.RAD2DEG));
		}
	}
	
	/** Set the points. 
	 * Note that a later change in the original points List will have no effect. 
	 * To add/remove/change points, you must call setPoints again. 
	 * If geodesic mode has been set, the long segments will follow the earth "great circle". */
	public void setPoints(List<GeoPoint> points){
		clearPath();
		int size = points.size();
		for (int i=0; i<size; i++){
			GeoPoint p = points.get(i);
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

	// TODO 0000 Polyline cf Polygon
	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {

		if (shadow) {
			return;
		}

        final Projection pj = mapView.getProjection();
        mPath.rewind();

        mOutline.setClipArea(mapView);
        mOutline.buildPathPortion(pj, false, null);
        if (mMilestoneManager != null) {
        	mMilestoneManager.init();
        	boolean first = true;
        	for (final RectL segment : mOutline.getSegments()) {
        		if (first) {
        			first = false;
        			mMilestoneManager.add(segment.left, segment.top);
				}
				mMilestoneManager.add(segment.right, segment.bottom);
			}
			mMilestoneManager.end();
		}

        canvas.drawPath(mPath, mPaint);

        if (mMilestoneManager != null) {
        	mMilestoneManager.draw(canvas);
		}
	}
	
	/** Detection is done is screen coordinates. 
	 * @param point
	 * @param tolerance in pixels
	 * @return true if the Polyline is close enough to the point. 
	 */
	public boolean isCloseTo(GeoPoint point, double tolerance, MapView mapView) {
		return mOutline.isCloseTo(point, tolerance, mapView.getProjection());
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

	/**
	 * @since 6.0.0
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @since 6.0.0
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
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

	@Override
	public void onDetach(MapView mapView) {
        mOutline = null;
		mOnClickListener=null;
		onDestroy();
	}

	/**
	 * @since 6.0.0
	 */
	public void setMilestoneManager(final MilestoneManager pMilestoneManager) {
		mMilestoneManager = pMilestoneManager;
	}
}

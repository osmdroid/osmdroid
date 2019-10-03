package org.osmdroid.views.overlay;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.List;

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
public class Polygon extends PolyOverlayWithIW {

	protected OnClickListener mOnClickListener;


	// ===========================================================
	// Constructors
	// ===========================================================

	public Polygon(){
		this(null);
	}

	public Polygon(MapView mapView) {
		super(mapView, true, true);
		mFillPaint = new Paint();
		mFillPaint.setColor(Color.TRANSPARENT);
		mFillPaint.setStyle(Paint.Style.FILL);
		mOutlinePaint.setColor(Color.BLACK);
		mOutlinePaint.setStrokeWidth(10.0f);
		mOutlinePaint.setStyle(Paint.Style.STROKE);
		mOutlinePaint.setAntiAlias(true);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * @deprecated Use {@link #getFillPaint()} instead
	 */
	@Deprecated
	public int getFillColor() {
		return mFillPaint.getColor();
	}

	/**
	 * @deprecated Use {@link #getOutlinePaint()} instead
	 */
	@Deprecated
	public int getStrokeColor() {
		return mOutlinePaint.getColor();
	}

	/**
	 * @deprecated Use {@link #getOutlinePaint()} instead
	 */
	@Deprecated
	public float getStrokeWidth() {
		return mOutlinePaint.getStrokeWidth();
	}

	/**
	 * @return the Paint used for the filling. This allows to set advanced Paint settings.
	 * @since 6.0.2
	 */
	public Paint getFillPaint() {
		return super.getFillPaint(); // public instead of protected
	}

	/**
	 * @return a copy of the list of polygon's vertices.
	 * Warning: changes on this list may cause strange results on the polygon display.
	 */
	public List<GeoPoint> getPoints(){
		//TODO This is completely wrong:
		// - this is not a copy, but a direct handler to the list itself.
		// - if geodesic, the outline points are not identical to original points.
		return mOutline.getPoints();
	}

	/**
	 * @deprecated Use {@link #getFillPaint()} instead
	 */
	@Deprecated
	public void setFillColor(final int fillColor) {
		mFillPaint.setColor(fillColor);
	}

	/**
	 * @deprecated Use {@link #getOutlinePaint()} instead
	 */
	@Deprecated
	public void setStrokeColor(final int color) {
		mOutlinePaint.setColor(color);
	}

	/**
	 * @deprecated Use {@link #getOutlinePaint()} instead
	 */
	@Deprecated
	public void setStrokeWidth(final float width) {
		mOutlinePaint.setStrokeWidth(width);
	}

	/**
	 * Set the points of the polygon outline.
	 * Note that a later change in the original points List will have no effect.
	 * To remove/change points, you must call setPoints again.
	 * If geodesic mode has been set, the long segments will follow the earth "great circle".
	 */
	public void setPoints(final List<GeoPoint> points) {
		mOutline.setPoints(points);
		setDefaultInfoWindowLocation();
		mBounds= BoundingBox.fromGeoPointsSafe(points);
	}

	/**
	 * Add the point at the end of the polygon outline.
	 * If geodesic mode has been set, the long segments will follow the earth "great circle".
	 */
	public void addPoint(GeoPoint p){
		mOutline.addPoint(p);
	}


	public void setHoles(List<? extends List<GeoPoint>> holes){
		mHoles = new ArrayList<LinearRing>(holes.size());
		for (List<GeoPoint> sourceHole:holes){
			LinearRing newHole = new LinearRing(mPath);
			newHole.setGeodesic(mOutline.isGeodesic());
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
			//TODO: completely wrong:
			// hole.getPoints() doesn't return a copy but a direct handler to the internal list.
			// - if geodesic, this is not the same points as the original list.
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

	/**
	 * Default listener for a single tap event on a Polygon:
	 * set the infowindow at the tapped position, and open the infowindow (if any).
	 * @param event
	 * @param mapView
	 * @return true if tapped
	 */
	@Override public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){
		Projection pj = mapView.getProjection();
		GeoPoint eventPos = (GeoPoint)pj.fromPixels((int)event.getX(), (int)event.getY());
		boolean tapped = contains(event);
		if (tapped) {
			if (mOnClickListener == null) {
				return onClickDefault(this, mapView, eventPos);
			} else {
				return mOnClickListener.onClick(this, mapView, eventPos);
			}
		} else
			return tapped;
	}

	@Override public void onDetach(MapView mapView) {
		super.onDetach(mapView);
		mOnClickListener=null;
	}



	//-- Polygon events listener interfaces ------------------------------------

	public interface OnClickListener {
		boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos);
	}

	/**
	 * default behaviour when no click listener is set
	 */
	public boolean onClickDefault(Polygon polygon, MapView mapView, GeoPoint eventPos) {
		polygon.setInfoWindowLocation(eventPos);
		polygon.showInfoWindow();
		return true;
	}

	/**
	 * @since 6.0.2
	 * @param listener
	 */
	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}
}

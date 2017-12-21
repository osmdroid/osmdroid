package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
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
public class Polygon extends OverlayWithIW {

	private final Path mPath = new Path(); //Path drawn is kept for click detection
	private ArrowsLinearRing mOutline = new ArrowsLinearRing(mPath);
	private ArrayList<LinearRing> mHoles = new ArrayList<>();
	private String id=null;
	
	/** Paint settings. */
	protected Paint mFillPaint;
	protected Paint mOutlinePaint;

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
		mOutline.setStrokeWidth(width);
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
			LinearRing newHole = new LinearRing(mPath);
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

		mOutline.setClipArea(mapView);
		final PointL offset = mOutline.buildPathPortion(pj, true, null);
		
		for (LinearRing hole:mHoles){
			hole.setClipArea(mapView);
			hole.buildPathPortion(pj, true, offset);
		}
		mPath.setFillType(Path.FillType.EVEN_ODD); //for correct support of holes

		canvas.drawPath(mPath, mFillPaint);
		canvas.drawPath(mPath, mOutlinePaint);

		mOutline.drawDirectionalArrows(canvas, mOutlinePaint, null);
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

	/**
	 * A directional arrow is a single arrow drawn in the middle of two points of a to
	 * provide a visual cue for direction of movement between the two points.
	 *
	 * By default the arrows always point towards the lower index as the list of GeoPoints are
	 * processed. The direction the arrows point can be inverted.
	 *
	 * @param drawDirectionalArrows enable or disable the feature. Cannot be null
	 */
	public void setDrawDirectionalArrows(boolean drawDirectionalArrows) {
		mOutline.setDrawDirectionalArrows(drawDirectionalArrows, null , mOutlinePaint.getStrokeWidth());
	}

	/**
	 * A directional arrow is a single arrow drawn in the middle of two points to
	 * provide a visual cue for direction of movement between the two points.
	 *
	 * By default the arrows always point towards the lower index as the list of GeoPoints are
	 * processed. The direction the arrows point can be inverted.
	 *
	 * @param drawDirectionalArrows enable or disable the feature. Cannot be null
	 * @param invertDirection invert the direction the arrows are drawn. Use null for default value
	 */
	public void setDrawDirectionalArrows(
			boolean drawDirectionalArrows, Boolean invertDirection) {
		mOutline.setDrawDirectionalArrows(drawDirectionalArrows, invertDirection, mOutlinePaint.getStrokeWidth());
	}
}

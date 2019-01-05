package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneManager;

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
	private LinearRing mOutline = new LinearRing(mPath);
	private ArrayList<LinearRing> mHoles = new ArrayList<>();
	protected OnClickListener mOnClickListener;
	/** Paint settings. */
	private Paint mFillPaint;
	private Paint mOutlinePaint;
	private List<MilestoneManager> mMilestoneManagers = new ArrayList<>();
	private GeoPoint mInfoWindowLocation;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Polygon(){
		this(null);
	}

	public Polygon(MapView mapView) {
		if (mapView != null) {
			setInfoWindow(mapView.getRepository().getDefaultPolygonInfoWindow());
		}
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
	 * @return the Paint used for the filling. This allows to set advanced Paint settings.
	 * @since 6.0.2
	 */
	public Paint getFillPaint() {
		return mFillPaint;
	}

	public void setGeodesic(boolean geodesic) {
		mOutline.setGeodesic(geodesic);
	}

	public boolean isGeodesic() {
		return mOutline.isGeodesic();
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

	/** Set the InfoWindow to be used.
	 * Default is a BasicInfoWindow, with the layout named "bonuspack_bubble".
	 * You can use this method either to use your own layout, or to use your own sub-class of InfoWindow.
	 * If you don't want any InfoWindow to open, you can set it to null. */
	public void setInfoWindow(InfoWindow infoWindow){
		if (mInfoWindow != null){
			if (mInfoWindow.getRelatedObject()==this)
				mInfoWindow.setRelatedObject(null);
		}
		mInfoWindow = infoWindow;
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
	
	@Override public void draw(Canvas canvas, Projection pj) {

		mPath.rewind();

		mOutline.setClipArea(pj);
		final PointL offset = mOutline.buildPathPortion(pj, null, mMilestoneManagers.size() > 0);
		for (final MilestoneManager milestoneManager : mMilestoneManagers) {
			milestoneManager.init();
			milestoneManager.setDistances(mOutline.getDistances());
			for (final PointL point : mOutline.getPointsForMilestones()) {
				milestoneManager.add(point.x, point.y);
			}
			milestoneManager.end();
		}

		for (LinearRing hole:mHoles){
			hole.setClipArea(pj);
			hole.buildPathPortion(pj, offset, mMilestoneManagers.size() > 0);
		}
		mPath.setFillType(Path.FillType.EVEN_ODD); //for correct support of holes

		canvas.drawPath(mPath, mFillPaint);
		canvas.drawPath(mPath, mOutlinePaint);

		for (final MilestoneManager milestoneManager : mMilestoneManagers) {
			milestoneManager.draw(canvas);
		}

		if (isInfoWindowOpen() && mInfoWindow!=null && mInfoWindow.getRelatedObject()==this) {
			mInfoWindow.draw();
		}
	}

	/**
	 * Show the infowindow, if any. It will be opened either at the latest location, if any,
	 * or to a default location computed by setDefaultInfoWindowLocation method.
	 * Note that you can manually set this location with: setInfoWindowLocation
	 */
	public void showInfoWindow(){
		if (mInfoWindow != null && mInfoWindowLocation != null)
			mInfoWindow.open(this, mInfoWindowLocation, 0, 0);
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

	/**
	 * @return the geopoint location where the infowindow should point at.
	 * Doesn't matter if the infowindow is currently opened or not.
	 * @since 6.0.0
	 */
	public GeoPoint getInfoWindowLocation() {
		return mInfoWindowLocation;
	}

	/** Internal method used to ensure that the infowindow will have a default position in all cases,
	 * so that the user can call showInfoWindow even if no tap occured before.
	 * Currently, set the position on the center of the polygon bounding box.
	 */
	protected void setDefaultInfoWindowLocation() {
		int s = mOutline.getPoints().size();
		if (s == 0){
			mInfoWindowLocation = new GeoPoint(0.0, 0.0);
			return;
		}
		//TODO: as soon as the polygon bounding box will be a class member, don't compute it again here.
		mInfoWindowLocation = mOutline.getCenter(null);
	}

	/**
	 * Sets the info window anchor point to a geopoint location
	 * @since 6.0.0
	 * @param location
	 */
	public void setInfoWindowLocation(GeoPoint location) {
		mInfoWindowLocation = location;
	}

	@Override public void onDetach(MapView mapView) {
		mOutline=null;
		mHoles.clear();
		mMilestoneManagers.clear();
		onDestroy();
	}

	/**
	 * @since 6.0.0
	 */
	public void setMilestoneManagers(final List<MilestoneManager> pMilestoneManagers) {
		if (pMilestoneManagers == null) {
			if (mMilestoneManagers.size() > 0) {
				mMilestoneManagers.clear();
			}
		} else {
			mMilestoneManagers = pMilestoneManagers;
		}
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

	/**
	 * @since 6.0.3
	 * @return aggregate distance (in meters)
	 */
	public double getDistance() {
		return mOutline.getDistance();
	}
}

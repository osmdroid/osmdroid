package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A polyline is a list of points, where line segments are drawn between consecutive points.
 * Mimics the Polyline class from Google Maps Android API v2 as much as possible. Main differences:<br>
 * - Doesn't support Z-Index: drawing order is the order in map overlays<br>
 * - Supports InfoWindow (must be a BasicInfoWindow). <br>
 * <p>
 * <img alt="Class diagram around Marker class" width="686" height="413" src='src='./doc-files/marker-infowindow-classes.png' />
 *
 * @author M.Kergall
 * @see <a href="http://developer.android.com/reference/com/google/android/gms/maps/model/Polyline.html">Google Maps Polyline</a>
 */
public class Polyline extends OverlayWithIW {

    private final Paint mPaint = new Paint();
    private final LineDrawer mLineDrawer = new LineDrawer(256);
    private LinearRing mOutline = new LinearRing(mLineDrawer);
    private List<MilestoneManager> mMilestoneManagers = new ArrayList<>();
    protected OnClickListener mOnClickListener;
    private GeoPoint mInfoWindowLocation;
    private float mDensity = 1.0f;
    private ArrayList<GeoPoint> mOriginalPoints = new ArrayList<>();

    /**
     * If MapView is not provided, infowindow popup will not function unless you set it yourself.
     */
    public Polyline() {
        this(null);
    }

    /**
     * If MapView is null, infowindow popup will not function unless you set it yourself.
     */
    public Polyline(MapView mapView) {
        if (mapView != null) {
            setInfoWindow(mapView.getRepository().getDefaultPolylineInfoWindow());
            mDensity = mapView.getContext().getResources().getDisplayMetrics().density;
        }
        //default as defined in Google API:
        this.mPaint.setColor(Color.BLACK);
        this.mPaint.setStrokeWidth(10.0f);
        this.mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mOutline.clearPath();
        mLineDrawer.setPaint(mPaint);
    }

    /**
     * @return a copy of the points.
     */
    public ArrayList<GeoPoint> getPoints() {
        ArrayList<GeoPoint> result = new ArrayList(mOriginalPoints.size());
        for (GeoPoint p:mOriginalPoints)
            result.add(p);
        return result;
    }

    public int getColor() {
        return mPaint.getColor();
    }

    public float getWidth() {
        return mPaint.getStrokeWidth();
    }

    /**
     * @return the Paint used. This allows to set advanced Paint settings.
     */
    public Paint getPaint() {
        return mPaint;
    }

    public boolean isVisible() {
        return isEnabled();
    }

    /**
     * Sets whether to draw each segment of the line as a geodesic or not.
     * Warning: it takes effect only if set before setting the points in the Polyline.
     */
    public void setGeodesic(boolean geodesic) {
        mOutline.setGeodesic(geodesic);
    }

    public boolean isGeodesic() {
        return mOutline.isGeodesic();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public void setWidth(float width) {
        mPaint.setStrokeWidth(width);
    }

    public void setVisible(boolean visible) {
        setEnabled(visible);
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    /**
     * Set the points.
     * Note that a later change in the original points List will have no effect.
     * To remove/change points, you must call setPoints again.
     * If geodesic mode has been set, the long segments will follow the earth "great circle".
     */
    public void setPoints(List<GeoPoint> points) {
        mOutline.clearPath();
        mOriginalPoints = new ArrayList<>(points.size());
        for (GeoPoint p:points) {
            mOriginalPoints.add(p);
        }
        mOutline.setPoints(points);
        setDefaultInfoWindowLocation();
    }

    /**
     * Add the point at the end.
     * If geodesic mode has been set, the long segments will follow the earth "great circle".
     */
    public void addPoint(GeoPoint p){
        mOriginalPoints.add(p);
        mOutline.addPoint(p);
    }

    @Override
    public void draw(final Canvas canvas, final Projection pj) {

        mLineDrawer.setCanvas(canvas);
        mOutline.setClipArea(pj);
        mOutline.buildLinePortion(pj, mMilestoneManagers.size() > 0);
        for (final MilestoneManager milestoneManager : mMilestoneManagers) {
            milestoneManager.init();
            milestoneManager.setDistances(mOutline.getDistances());
            for (final PointL point : mOutline.getPointsForMilestones()) {
                milestoneManager.add(point.x, point.y);
            }
            milestoneManager.end();
        }

        for (final MilestoneManager milestoneManager : mMilestoneManagers) {
            milestoneManager.draw(canvas);
        }
        if (isInfoWindowOpen() && mInfoWindow!=null && mInfoWindow.getRelatedObject()==this) {
            mInfoWindow.draw();
        }
    }

    /**
     * Detection is done is screen coordinates.
     *
     * @param point
     * @param tolerance in pixels
     * @return true if the Polyline is close enough to the point.
     */
    public boolean isCloseTo(GeoPoint point, double tolerance, MapView mapView) {
        return getCloseTo(point, tolerance, mapView) != null;
    }

    /**
     * @since 6.0.3
     * Detection is done is screen coordinates.
     *
     * @param point
     * @param tolerance in pixels
     * @return the first GeoPoint of the Polyline close enough to the point
     */
    public GeoPoint getCloseTo(GeoPoint point, double tolerance, MapView mapView) {
        return mOutline.getCloseTo(point, tolerance, mapView.getProjection(), false);
    }

    /**
     * Set the InfoWindow to be used.
     * Default is a BasicInfoWindow, with the layout named "bonuspack_bubble".
     * You can use this method either to use your own layout, or to use your own sub-class of InfoWindow.
     * If you don't want any InfoWindow to open, you can set it to null.
     */
    public void setInfoWindow(InfoWindow infoWindow) {
        if (mInfoWindow != null){
            if (mInfoWindow.getRelatedObject()==this)
                mInfoWindow.setRelatedObject(null);
        }
        mInfoWindow = infoWindow;
    }

    /**
     * Show the infowindow, if any. It will be opened either at the latest location, if any,
     * or to a default location computed by setDefaultInfoWindowLocation method.
     * Note that you can manually set this location with: setInfoWindowLocation
     */
    public void showInfoWindow() {
        if (mInfoWindow != null && mInfoWindowLocation != null)
            mInfoWindow.open(this, mInfoWindowLocation, 0, 0);
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
        final Projection pj = mapView.getProjection();
        GeoPoint eventPos = (GeoPoint) pj.fromPixels((int) event.getX(), (int) event.getY());
        double tolerance = mPaint.getStrokeWidth() * mDensity;
        final GeoPoint closest = getCloseTo(eventPos, tolerance, mapView);
        if (closest != null) {
            if (mOnClickListener == null) {
                return onClickDefault(this, mapView, closest);
            } else {
                return mOnClickListener.onClick(this, mapView, closest);
            }
        } else
            return false;
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
     * Currently, set the position on the "middle" point of the polyline.
     */
    protected void setDefaultInfoWindowLocation(){
        int s = mOriginalPoints.size();
        if (s > 0)
            mInfoWindowLocation = mOriginalPoints.get(s/2);
        else
            mInfoWindowLocation = new GeoPoint(0.0, 0.0);
    }

    /**
     * Sets the infowindow anchor point to a geopoint location
     * @since 6.0.0
     * @param location
     */
    public void setInfoWindowLocation(GeoPoint location) {
        mInfoWindowLocation = location;
    }

    //-- Polyline events listener interfaces ------------------------------------

    public interface OnClickListener {
        abstract boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos);
    }

    /**
     * default behaviour when no click listener is set
     */
    public boolean onClickDefault(Polyline polyline, MapView mapView, GeoPoint eventPos) {
        polyline.setInfoWindowLocation(eventPos);
        polyline.showInfoWindow();
        return true;
    }

    @Override
    public void onDetach(MapView mapView) {
        mOutline = null;
        mOnClickListener = null;
        mMilestoneManagers.clear();
        mOriginalPoints = null;
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

    /**
     * @since 6.0.3
     * @return aggregate distance (in meters)
     */
    public double getDistance() {
        return mOutline.getDistance();
    }
}

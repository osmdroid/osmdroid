package org.osmdroid.views.overlay;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

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
public class Polyline extends PolyOverlayWithIW {

    protected OnClickListener mOnClickListener;

    private GeoPoint mInfoWindowLocation;
    private float mDensity = 1.0f;
    private ArrayList<GeoPoint> mOriginalPoints = new ArrayList<>();
    private boolean mEnablePointReduction=false;


    private float mDensityMultiplier = 1.0f;


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
        this(mapView, false);
    }

    /**
     * @since 6.2.0
     */
    public Polyline(final MapView pMapView, final boolean pUsePath, final boolean pClosePath) {
        super(pMapView, pUsePath, pClosePath);
        //default as defined in Google API:
        mOutlinePaint.setColor(Color.BLACK);
        mOutlinePaint.setStrokeWidth(10.0f);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setAntiAlias(true);
    }

    /**
     * @since 6.1.0
     * @param pUsePath true if you want the drawing to use Path instead of Canvas.drawLines
     *                 Not recommended in all cases, given the performances.
     *                 Useful though if you want clean alpha vertices
     *                 cf. https://github.com/osmdroid/osmdroid/issues/1280
     */
    public Polyline(final MapView pMapView, final boolean pUsePath) {
        this(pMapView, pUsePath, false);
    }

    /**
     * @return a copy of the points.
     */
    public ArrayList<GeoPoint> getPoints() {
        ArrayList<GeoPoint> result = new ArrayList<>(mOriginalPoints.size());
        for (GeoPoint p:mOriginalPoints)
            result.add(p);
        return result;
    }

    /**
     * @deprecated Use {{@link #getOutlinePaint()}} instead
     */
    @Deprecated
    public int getColor() {
        return mOutlinePaint.getColor();
    }

    /**
     * @deprecated Use {{@link #getOutlinePaint()}} instead
     */
    @Deprecated
    public float getWidth() {
        return mOutlinePaint.getStrokeWidth();
    }

    /**
     * @deprecated Use {{@link #getOutlinePaint()}} instead
     */
    @Deprecated
    public Paint getPaint() {
        return getOutlinePaint();
    }

    /**
     * @deprecated Use {{@link #getOutlinePaint()}} instead
     */
    @Deprecated
    public void setColor(int color) {
        mOutlinePaint.setColor(color);
    }

    /**
     * @deprecated Use {{@link #getOutlinePaint()}} instead
     */
    @Deprecated
    public void setWidth(float width) {
        mOutlinePaint.setStrokeWidth(width);
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setDensityMultiplier(float multiplier) {
        mDensityMultiplier = multiplier;
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
        mBounds= BoundingBox.fromGeoPointsSafe(points);

    }

    /**
     * Add the point at the end.
     * If geodesic mode has been set, the long segments will follow the earth "great circle".
     */
    public void addPoint(GeoPoint p){
        mOriginalPoints.add(p);
        mOutline.addPoint(p);
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

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
        final Projection pj = mapView.getProjection();
        GeoPoint eventPos = (GeoPoint) pj.fromPixels((int) event.getX(), (int) event.getY());
        double tolerance = mOutlinePaint.getStrokeWidth() * mDensity * mDensityMultiplier;
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

    /** Internal method used to ensure that the infowindow will have a default position in all cases,
     * so that the user can call showInfoWindow even if no tap occured before.
     * Currently, set the position on the "middle" point of the polyline.
     */
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
        super.onDetach(mapView);
        mOnClickListener = null;
    }

    /**
     * @since 6.0.3
     * @return aggregate distance (in meters)
     */
    public double getDistance() {
        return mOutline.getDistance();
    }
}

package org.osmdroid.views.overlay;

import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

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
     * @param pUsePath true if you want the drawing to use Path instead of Canvas.drawLines
     *                 Not recommended in all cases, given the performances.
     *                 Useful though if you want clean alpha vertices
     *                 cf. https://github.com/osmdroid/osmdroid/issues/1280
     * @since 6.1.0
     */
    public Polyline(final MapView pMapView, final boolean pUsePath) {
        this(pMapView, pUsePath, false);
    }

    /**
     * @return a copy of the actual points
     * @deprecated Use {@link #getActualPoints()} instead; copy the list if necessary
     */
    @Deprecated
    public ArrayList<GeoPoint> getPoints() {
        return new ArrayList<>(getActualPoints());
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

    /**
     * Internal method used to ensure that the infowindow will have a default position in all cases,
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
     * @return aggregate distance (in meters)
     * @since 6.0.3
     */
    public double getDistance() {
        return mOutline.getDistance();
    }

    /**
     * @since 6.2.0
     */
    @Override
    protected boolean click(final MapView pMapView, final GeoPoint pEventPos) {
        if (mOnClickListener == null) {
            return onClickDefault(this, pMapView, pEventPos);
        }
        return mOnClickListener.onClick(this, pMapView, pEventPos);
    }
}

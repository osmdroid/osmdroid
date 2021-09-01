package org.osmdroid.samplefragments.events;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

/**
 * @author Marc Kurtz
 */
public class SampleLimitedScrollArea extends BaseSampleFragment {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String TITLE = "Limited scroll area";

    private final int MENU_LIMIT_SCROLLING_LAT_ID = Menu.FIRST;
    private final int MENU_LIMIT_SCROLLING_LNG_ID = Menu.FIRST + 1;

    private BoundingBox sCentralParkBoundingBox;

    public SampleLimitedScrollArea() {
        sCentralParkBoundingBox = new BoundingBox(40.796788,
                -73.949232, 40.768094, -73.981762);
    }

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    // ===========================================================
    // Constructors
    // ===========================================================
    //note that since we are not providing the mapview as a constructor parameter,
    //the infowindow bubble will not be available
    private final Polyline mNorthPolyline = new Polyline();
    private final Polyline mSouthPolyline = new Polyline();
    private final Polyline mWestPolyline = new Polyline();
    private final Polyline mEastPolyline = new Polyline();


    @Override
    protected void addOverlays() {
        super.addOverlays();

        final ArrayList<GeoPoint> list = new ArrayList<>();

        list.clear();
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualNorth(), -85));
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualNorth(), -65));
        mNorthPolyline.setPoints(list);
        mMapView.getOverlays().add(mNorthPolyline);

        list.clear();
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualSouth(), -85));
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualSouth(), -65));
        mSouthPolyline.setPoints(list);
        mMapView.getOverlays().add(mSouthPolyline);

        list.clear();
        list.add(new GeoPoint(45, sCentralParkBoundingBox.getLonWest()));
        list.add(new GeoPoint(35, sCentralParkBoundingBox.getLonWest()));
        mWestPolyline.setPoints(list);
        mMapView.getOverlays().add(mWestPolyline);

        list.clear();
        list.add(new GeoPoint(45, sCentralParkBoundingBox.getLonEast()));
        list.add(new GeoPoint(35, sCentralParkBoundingBox.getLonEast()));
        mEastPolyline.setPoints(list);
        mMapView.getOverlays().add(mEastPolyline);

        mMapView.getController().setZoom(13.);

        setHasOptionsMenu(true);

        mMapView.post(new Runnable() { // "post" because we need View.getWidth() to be set
            @Override
            public void run() {
                setLimitScrollingLatitude(true);
                setLimitScrollingLongitude(true);
            }
        });
    }

    /**
     * @since 6.0.0
     */
    private void setLimitScrollingLatitude(boolean pLimitScrolling) {
        mMapView.getOverlays().remove(mNorthPolyline);
        mMapView.getOverlays().remove(mSouthPolyline);
        if (pLimitScrolling) {
            mMapView.setScrollableAreaLimitLatitude(sCentralParkBoundingBox.getActualNorth(), sCentralParkBoundingBox.getActualSouth(), mMapView.getHeight() / 2);
            mMapView.setExpectedCenter(sCentralParkBoundingBox.getCenterWithDateLine());
            mMapView.getOverlays().add(mNorthPolyline);
            mMapView.getOverlays().add(mSouthPolyline);
        } else {
            mMapView.resetScrollableAreaLimitLatitude();
        }
        mMapView.invalidate();
    }

    /**
     * @since 6.0.0
     */
    private void setLimitScrollingLongitude(boolean pLimitScrolling) {
        mMapView.getOverlays().remove(mWestPolyline);
        mMapView.getOverlays().remove(mEastPolyline);
        if (pLimitScrolling) {
            mMapView.setScrollableAreaLimitLongitude(sCentralParkBoundingBox.getLonWest(), sCentralParkBoundingBox.getLonEast(), mMapView.getWidth() / 2);
            mMapView.setExpectedCenter(sCentralParkBoundingBox.getCenterWithDateLine());
            mMapView.getOverlays().add(mWestPolyline);
            mMapView.getOverlays().add(mEastPolyline);
        } else {
            mMapView.resetScrollableAreaLimitLongitude();
        }
        mMapView.invalidate();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_LIMIT_SCROLLING_LAT_ID, Menu.NONE, "Latitude: Limit scrolling").setCheckable(true);
        menu.add(0, MENU_LIMIT_SCROLLING_LNG_ID, Menu.NONE, "Longitude: Limit scrolling").setCheckable(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(MENU_LIMIT_SCROLLING_LAT_ID).setChecked(mMapView.isScrollableAreaLimitLatitude());
        menu.findItem(MENU_LIMIT_SCROLLING_LNG_ID).setChecked(mMapView.isScrollableAreaLimitLongitude());
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LIMIT_SCROLLING_LAT_ID:
                setLimitScrollingLatitude(!mMapView.isScrollableAreaLimitLatitude());
                return true;
            case MENU_LIMIT_SCROLLING_LNG_ID:
                setLimitScrollingLongitude(!mMapView.isScrollableAreaLimitLongitude());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package org.osmdroid.samplefragments.events;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * 
 * @author Marc Kurtz
 * 
 */
public class SampleLimitedScrollArea extends BaseSampleFragment {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final String TITLE = "Limited scroll area";

	private final int MENU_LIMIT_SCROLLING_ID = Menu.FIRST;

	private BoundingBox sCentralParkBoundingBox;

	public SampleLimitedScrollArea()
	{
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



	@Override
	protected void addOverlays() {
		super.addOverlays();

		final Polygon polygon = new Polygon();
		final ArrayList<GeoPoint> list = new ArrayList<>();
		list.add(new GeoPoint(sCentralParkBoundingBox.getLatNorth(), sCentralParkBoundingBox.getLonEast()));
		list.add(new GeoPoint(sCentralParkBoundingBox.getLatNorth(), sCentralParkBoundingBox.getLonWest()));
		list.add(new GeoPoint(sCentralParkBoundingBox.getLatSouth(), sCentralParkBoundingBox.getLonWest()));
		list.add(new GeoPoint(sCentralParkBoundingBox.getLatSouth(), sCentralParkBoundingBox.getLonEast()));
		polygon.setPoints(list);
		polygon.setFillColor(Color.argb(75, 255,0,0));
		mMapView.getOverlays().add(polygon);
		mMapView.getController().setZoom(13.);

		setLimitScrolling(true);
		setHasOptionsMenu(true);
	}

	protected void setLimitScrolling(boolean limitScrolling) {
		if (limitScrolling) {
			mMapView.setScrollableAreaLimitDouble(sCentralParkBoundingBox);
			mMapView.getController().animateTo(sCentralParkBoundingBox.getCenterWithDateLine());
			mMapView.invalidate();
		} else {
			mMapView.setScrollableAreaLimitDouble(null);
			mMapView.invalidate();
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, MENU_LIMIT_SCROLLING_ID, Menu.NONE, "Limit scrolling").setCheckable(true);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(MENU_LIMIT_SCROLLING_ID);
		item.setChecked(mMapView.getScrollableAreaLimit() != null);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LIMIT_SCROLLING_ID:
			setLimitScrolling(mMapView.getScrollableAreaLimit() == null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

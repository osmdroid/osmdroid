package org.osmdroid.samplefragments;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
	private Paint sPaint;

	// ===========================================================
	// Fields
	// ===========================================================

	private ShadeAreaOverlay mShadeAreaOverlay;

	public SampleLimitedScrollArea()
	{
		sCentralParkBoundingBox = new BoundingBox(40.796788,
			-73.949232, 40.768094, -73.981762);
		sPaint = new Paint();
		sPaint.setColor(Color.argb(50, 255, 0, 0));
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

		final Context context = getActivity();

		mShadeAreaOverlay = new ShadeAreaOverlay();
		mMapView.getOverlayManager().add(mShadeAreaOverlay);

		setLimitScrolling(true);
		setHasOptionsMenu(true);
	}

	protected void setLimitScrolling(boolean limitScrolling) {
		if (limitScrolling) {
			mMapView.getController().setZoom(15);
			mMapView.setScrollableAreaLimitDouble(sCentralParkBoundingBox);
			mMapView.setMinZoomLevel(15);
			mMapView.setMaxZoomLevel(18);
			mMapView.getController().animateTo(sCentralParkBoundingBox.getCenter());
			mShadeAreaOverlay.setEnabled(true);
			mMapView.invalidate();
		} else {
			mMapView.setScrollableAreaLimit(null);
			mMapView.setMinZoomLevel(null);
			mMapView.setMaxZoomLevel(null);
			mShadeAreaOverlay.setEnabled(false);
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
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	class ShadeAreaOverlay extends Overlay {

		final GeoPoint mTopLeft;
		final GeoPoint mBottomRight;
		final Point mTopLeftPoint = new Point();
		final Point mBottomRightPoint = new Point();
		Rect area=null;
		public ShadeAreaOverlay() {
			super();
			mTopLeft = new GeoPoint(sCentralParkBoundingBox.getLatNorth(),
					sCentralParkBoundingBox.getLonWest());
			mBottomRight = new GeoPoint(sCentralParkBoundingBox.getLatSouth(),
					sCentralParkBoundingBox.getLonEast());
		}

		@Override
		protected void draw(Canvas c, MapView osmv, boolean shadow) {
			if (shadow)
				return;

			final Projection proj = osmv.getProjection();

			if (area==null) {
				proj.toPixels(mTopLeft, mTopLeftPoint);
				proj.toPixels(mBottomRight, mBottomRightPoint);

				area = new Rect(mTopLeftPoint.x, mTopLeftPoint.y, mBottomRightPoint.x,
						mBottomRightPoint.y);
			}
			c.drawRect(area, sPaint);
		}
	}
}

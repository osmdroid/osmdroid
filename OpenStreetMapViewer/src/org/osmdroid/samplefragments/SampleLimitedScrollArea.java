// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid.samplefragments;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafePaint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class SampleLimitedScrollArea extends BaseSampleFragment {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final String TITLE = "Limited scroll area";

	private static final int MENU_LIMIT_SCROLLING_ID = Menu.FIRST;

	private static final BoundingBoxE6 sCentralParkBoundingBox;
	private static final SafePaint sPaint;

	// ===========================================================
	// Fields
	// ===========================================================

	private ShadeAreaOverlay mShadeAreaOverlay;

	static {
		sCentralParkBoundingBox = new BoundingBoxE6(40.796788,
			-73.949232, 40.768094, -73.981762);
		sPaint = new SafePaint();
		sPaint.setColor(Color.argb(50, 255, 0, 0));
	}
	@Override
	public String getSampleTitle() {
		return TITLE;
	}

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void addOverlays() {
		super.addOverlays();

		final Context context = getActivity();

		mShadeAreaOverlay = new ShadeAreaOverlay(context);
		mMapView.getOverlayManager().add(mShadeAreaOverlay);

		setLimitScrolling(true);
		setHasOptionsMenu(true);
	}

	protected void setLimitScrolling(boolean limitScrolling) {
		if (limitScrolling) {
			mMapView.getController().setZoom(15);
			mMapView.setScrollableAreaLimit(sCentralParkBoundingBox);
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
	class ShadeAreaOverlay extends SafeDrawOverlay {

		public ShadeAreaOverlay(Context ctx) {
			super(ctx);
		}

		@Override
		protected void drawSafe(ISafeCanvas c, MapView osmv, boolean shadow) {
			final Projection proj = osmv.getProjection();
			Rect area = proj.toPixels(sCentralParkBoundingBox);
			c.drawRect(area, sPaint);
		}
	}
}

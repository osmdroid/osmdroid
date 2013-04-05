// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid.samplefragments;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class SampleWithMinimapItemizedoverlayWithFocus extends BaseSampleFragment {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final String TITLE = "Itemized overlay w/focus";

	private static final int MENU_ZOOMIN_ID = Menu.FIRST;
	private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;

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

		/* Itemized Overlay */
		{
			/* Create a static ItemizedOverlay showing some Markers on various cities. */
			final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			items.add(new OverlayItem("Hannover", "Tiny SampleDescription", new GeoPoint(52370816,
					9735936))); // Hannover
			items.add(new OverlayItem("Berlin", "This is a relatively short SampleDescription.",
					new GeoPoint(52518333, 13408333))); // Berlin
			items.add(new OverlayItem(
					"Washington",
					"This SampleDescription is a pretty long one. Almost as long as a the great wall in china.",
					new GeoPoint(38895000, -77036667))); // Washington
			items.add(new OverlayItem("San Francisco", "SampleDescription", new GeoPoint(37779300,
					-122419200))); // San Francisco

			/* OnTapListener for the Markers, shows a simple Toast. */
			mMyLocationOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
					new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
						@Override
						public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
							Toast.makeText(
									context,
									"Item '" + item.mTitle + "' (index=" + index
											+ ") got single tapped up", Toast.LENGTH_LONG).show();
							return true;
						}

						@Override
						public boolean onItemLongPress(final int index, final OverlayItem item) {
							Toast.makeText(
									context,
									"Item '" + item.mTitle + "' (index=" + index
											+ ") got long pressed", Toast.LENGTH_LONG).show();
							return false;
						}
					}, mResourceProxy);
			mMyLocationOverlay.setFocusItemsOnTap(true);
			mMyLocationOverlay.setFocusedItem(0);

			mMapView.getOverlays().add(mMyLocationOverlay);
		}

		/* MiniMap */
		{
			MinimapOverlay miniMapOverlay = new MinimapOverlay(context,
					mMapView.getTileRequestCompleteHandler());
			mMapView.getOverlays().add(miniMapOverlay);
		}

		// Zoom and center on the focused item.
		mMapView.getController().setZoom(5);
		GeoPoint geoPoint = mMyLocationOverlay.getFocusedItem().mGeoPoint;
		mMapView.getController().animateTo(geoPoint);

		setHasOptionsMenu(true);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
		menu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ZOOMIN_ID:
			mMapView.getController().zoomIn();
			return true;

		case MENU_ZOOMOUT_ID:
			mMapView.getController().zoomOut();
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
}

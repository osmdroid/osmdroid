// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid.samples;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class SampleWithMinimapItemizedoverlay extends Activity {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MENU_ZOOMIN_ID = Menu.FIRST;
	private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private MapView mMapView;
	private ItemizedOverlay<OverlayItem> mMyLocationOverlay;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final RelativeLayout rl = new RelativeLayout(this);

		this.mMapView = new MapView(this);
		this.mMapView.setTilesScaledToDpi(true);
		rl.addView(this.mMapView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		/* Itemized Overlay */
		{
			/* Create a static ItemizedOverlay showing a some Markers on some cities. */
			final ArrayList<OverlayItem> items = new ArrayList<>();
			items.add(new OverlayItem("Hannover", "SampleDescription", new GeoPoint(52.370816, 9.735936)));
			items.add(new OverlayItem("Berlin", "SampleDescription", new GeoPoint(52.518333, 13.408333)));
			items.add(new OverlayItem("Washington", "SampleDescription", new GeoPoint(38.895000, -77.036667)));
			items.add(new OverlayItem("San Francisco", "SampleDescription", new GeoPoint(37.779300, -122.419200)));
			items.add(new OverlayItem("Tolaga Bay", "SampleDescription", new GeoPoint(-38.371000, 178.298000)));

			/* OnTapListener for the Markers, shows a simple Toast. */
			this.mMyLocationOverlay = new ItemizedIconOverlay<>(items,
					new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
						@Override
						public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
							Toast.makeText(
									SampleWithMinimapItemizedoverlay.this,
									"Item '" + item.getTitle() + "' (index=" + index
											+ ") got single tapped up", Toast.LENGTH_LONG).show();
							return true; // We 'handled' this event.
						}

						@Override
						public boolean onItemLongPress(final int index, final OverlayItem item) {
							Toast.makeText(
									SampleWithMinimapItemizedoverlay.this,
									"Item '" + item.getTitle() + "' (index=" + index
											+ ") got long pressed", Toast.LENGTH_LONG).show();
							return true;
						}
					}, getApplicationContext());
			this.mMapView.getOverlays().add(this.mMyLocationOverlay);
		}

		/* MiniMap */
		{
			final MinimapOverlay miniMapOverlay = new MinimapOverlay(this,
					mMapView.getTileRequestCompleteHandler());
			this.mMapView.getOverlays().add(miniMapOverlay);
		}

		/* list of items currently displayed */
		{
			final MapEventsReceiver mReceive = new MapEventsReceiver() {
				@Override
				public boolean singleTapConfirmedHelper(GeoPoint p) {
					return false;
				}

				@Override
				public boolean longPressHelper(final GeoPoint p) {
					final List<OverlayItem> displayed = mMyLocationOverlay.getDisplayedItems();
					final StringBuilder buffer = new StringBuilder();
					String sep = "";
					for (final OverlayItem item : displayed) {
						buffer.append(sep).append('\'').append(item.getTitle()).append('\'');
						sep = ", ";
					}
					Toast.makeText(
							SampleWithMinimapItemizedoverlay.this,
							"Currently displayed: " + buffer.toString(), Toast.LENGTH_LONG).show();
					return true;
				}
			};
			mMapView.getOverlays().add(new MapEventsOverlay(mReceive));

			final RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mMapView);
			rotationGestureOverlay.setEnabled(true);
			mMapView.getOverlays().add(rotationGestureOverlay);
		}

		this.setContentView(rl);

		// Default location and zoom level
		IMapController mapController = mMapView.getController();
		mapController.setZoom(5.);
		GeoPoint startPoint = new GeoPoint(50.936255, 6.957779);
		mapController.setCenter(startPoint);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		pMenu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
		pMenu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");

		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ZOOMIN_ID:
			this.mMapView.getController().zoomIn();
			return true;

		case MENU_ZOOMOUT_ID:
			this.mMapView.getController().zoomOut();
			return true;
		}
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void onPause(){
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onResume(){
		super.onResume();
		mMapView.onResume();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid.samples;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxyImpl;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.OpenStreetMapView;
import org.osmdroid.views.overlay.OpenStreetMapViewItemizedOverlay;
import org.osmdroid.views.overlay.OpenStreetMapViewItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OpenStreetMapViewOverlayItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class SampleWithMinimapItemizedoverlayWithFocus extends Activity {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MENU_ZOOMIN_ID = Menu.FIRST;
	private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private OpenStreetMapView mOsmv, mOsmvMinimap;
	private OpenStreetMapViewItemizedOverlayWithFocus<OpenStreetMapViewOverlayItem> mMyLocationOverlay;
	private ResourceProxy mResourceProxy;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResourceProxy = new ResourceProxyImpl(getApplicationContext());

		final RelativeLayout rl = new RelativeLayout(this);

		CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());

		this.mOsmv = new OpenStreetMapView(this, 256);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		/* Itemized Overlay */
		{
			/* Create a static ItemizedOverlay showing a some Markers on some cities. */
			final ArrayList<OpenStreetMapViewOverlayItem> items = new ArrayList<OpenStreetMapViewOverlayItem>();
			items.add(new OpenStreetMapViewOverlayItem("Hannover", "Tiny SampleDescription",
					new GeoPoint(52370816, 9735936))); // Hannover
			items.add(new OpenStreetMapViewOverlayItem("Berlin",
					"This is a relatively short SampleDescription.", new GeoPoint(52518333,
							13408333))); // Berlin
			items.add(new OpenStreetMapViewOverlayItem(
					"Washington",
					"This SampleDescription is a pretty long one. Almost as long as a the great wall in china.",
					new GeoPoint(38895000, -77036667))); // Washington
			items.add(new OpenStreetMapViewOverlayItem("San Francisco", "SampleDescription",
					new GeoPoint(37779300, -122419200))); // San Francisco

			/* OnTapListener for the Markers, shows a simple Toast. */
			this.mMyLocationOverlay = new OpenStreetMapViewItemizedOverlayWithFocus<OpenStreetMapViewOverlayItem>(
					this,
					items,
					new OpenStreetMapViewItemizedOverlay.OnItemGestureListener<OpenStreetMapViewOverlayItem>() {
						@Override
						public boolean onItemSingleTapUp(final int index,
								final OpenStreetMapViewOverlayItem item) {
							Toast.makeText(
									SampleWithMinimapItemizedoverlayWithFocus.this,
									"Item '" + item.mTitle + "' (index=" + index
											+ ") got single tapped up", Toast.LENGTH_LONG).show();
							return true;
						}

						@Override
						public boolean onItemLongPress(final int index,
								final OpenStreetMapViewOverlayItem item) {
							Toast.makeText(
									SampleWithMinimapItemizedoverlayWithFocus.this,
									"Item '" + item.mTitle + "' (index=" + index
											+ ") got long pressed", Toast.LENGTH_LONG).show();
							return false;
						}
					}, mResourceProxy);
			this.mMyLocationOverlay.setFocusItemsOnTap(true);
			this.mMyLocationOverlay.setFocusedItem(0);

			this.mOsmv.getOverlays().add(this.mMyLocationOverlay);
		}

		/* MiniMap */
		{
			/*
			 * Create another OpenStreetMapView, that will act as the MiniMap for the 'MainMap'.
			 * They will share the TileProvider.
			 */
			mOsmvMinimap = new OpenStreetMapView(this, this.mOsmv);
			final int aZoomDiff = 3; // Use OpenStreetMapViewConstants.NOT_SET to disable
										// autozooming of this
										// minimap
			this.mOsmv.setMiniMap(mOsmvMinimap, aZoomDiff);

			/*
			 * Create RelativeLayout.LayoutParams that position the MiniMap on the top-right corner
			 * of the RelativeLayout.
			 */
			final RelativeLayout.LayoutParams minimapParams = new RelativeLayout.LayoutParams(90,
					90);
			minimapParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			minimapParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			minimapParams.setMargins(5, 5, 5, 5);
			rl.addView(mOsmvMinimap, minimapParams);
		}

		this.setContentView(rl);
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
			this.mOsmv.getController().zoomIn();
			return true;

		case MENU_ZOOMOUT_ID:
			this.mOsmv.getController().zoomOut();
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

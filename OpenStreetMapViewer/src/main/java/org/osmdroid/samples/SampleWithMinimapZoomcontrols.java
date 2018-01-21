// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid.samples;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class SampleWithMinimapZoomcontrols extends Activity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private MapView mMapView;

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

		mMapView.setBuiltInZoomControls(true);

		/* MiniMap */
		{
			MinimapOverlay miniMapOverlay = new MinimapOverlay(this,
					mMapView.getTileRequestCompleteHandler());
			this.mMapView.getOverlays().add(miniMapOverlay);
		}

		this.setContentView(rl);

		// Default location and zoom level
		IMapController mapController = mMapView.getController();
		mapController.setZoom(5);
		GeoPoint startPoint = new GeoPoint(50.936255, 6.957779);
		mapController.setCenter(startPoint);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

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

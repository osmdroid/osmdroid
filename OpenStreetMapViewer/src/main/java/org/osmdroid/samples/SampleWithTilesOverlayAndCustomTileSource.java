package org.osmdroid.samples;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 *
 * @author Alex van der Linden
 *
 */
public class SampleWithTilesOverlayAndCustomTileSource extends Activity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	MapView mMapView;
	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup base map
		final RelativeLayout rl = new RelativeLayout(this);

		mMapView = new MapView(this);
		mMapView.setTilesScaledToDpi(true);
		rl.addView(mMapView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		mMapView.setBuiltInZoomControls(true);

		// zoom to the netherlands
		mMapView.getController().setZoom(7);
		mMapView.getController().setCenter(new GeoPoint(51500000, 5400000));

		// Add tiles layer with custom tile source
		final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
		final ITileSource tileSource = new XYTileSource("FietsRegionaal",  3, 18, 256, ".png",
				new String[] { "http://overlay.openstreetmap.nl/openfietskaart-rcn/" });
		tileProvider.setTileSource(tileSource);
		final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
		tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
		mMapView.getOverlays().add(tilesOverlay);

		this.setContentView(rl);
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

package org.osmdroid.samples;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 *
 * @author Alex van der Linden
 *
 */
public class SampleWithTilesOverlay extends Activity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private MapView mMapView;
	private TilesOverlay mTilesOverlay;
	private MapTileProviderBasic mProvider;
	private RelativeLayout rl;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup base map
		rl = new RelativeLayout(this);

		this.mMapView = new MapView(this);
		this.mMapView.setTilesScaledToDpi(true);
		rl.addView(this.mMapView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		this.mMapView.setBuiltInZoomControls(true);

		// zoom to the netherlands
		this.mMapView.getController().setZoom(7);
		this.mMapView.getController().setCenter(new GeoPoint(51500000, 5400000));

		// Add tiles layer
		mProvider = new MapTileProviderBasic(getApplicationContext());
		mProvider.setTileSource(TileSourceFactory.FIETS_OVERLAY_NL);
		this.mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());
		this.mMapView.getOverlays().add(this.mTilesOverlay);

		this.setContentView(rl);
	}


	@Override
	public void onDestroy(){
		super.onDestroy();
		if (mMapView !=null)
			mMapView.onDetach();
		mMapView =null;
		if (mProvider!=null)
			mProvider.detach();
		mProvider = null;
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

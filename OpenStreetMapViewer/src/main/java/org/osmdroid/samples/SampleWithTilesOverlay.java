package org.osmdroid.samples;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import static org.osmdroid.samplefragments.FragmentSamples.TAG;

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

	private MapView mOsmv;
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

		this.mOsmv = new MapView(this);
		this.mOsmv.setTilesScaledToDpi(true);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		this.mOsmv.setBuiltInZoomControls(true);

		// zoom to the netherlands
		this.mOsmv.getController().setZoom(7);
		this.mOsmv.getController().setCenter(new GeoPoint(51500000, 5400000));

		// Add tiles layer
		mProvider = new MapTileProviderBasic(getApplicationContext());
		mProvider.setTileSource(TileSourceFactory.FIETS_OVERLAY_NL);
		this.mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());
		this.mOsmv.getOverlays().add(this.mTilesOverlay);

		this.setContentView(rl);
	}


	@Override
	public void onDestroy(){
		super.onDestroy();
		if (mOsmv!=null)
			mOsmv.onDetach();
		mOsmv=null;
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

package org.andnav.osm.samples;

import org.andnav.osm.tileprovider.IRegisterReceiver;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapTilesOverlay;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory;
import org.andnav.osm.views.util.OpenStreetMapTileProviderDirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
	private static final Logger logger = LoggerFactory.getLogger(SampleWithTilesOverlay.class);

	// ===========================================================
	// Fields
	// ===========================================================

	private OpenStreetMapView mOsmv;
	private OpenStreetMapTilesOverlay mTilesOverlay;
	private OpenStreetMapTileProviderDirect mProvider;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup base map
		final RelativeLayout rl = new RelativeLayout(this);
		this.mOsmv = new OpenStreetMapView(this);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		this.mOsmv.setBuiltInZoomControls(true);

		// zoom to the netherlands
		this.mOsmv.getController().setZoom(7);
		this.mOsmv.getController().setCenter(new GeoPoint(51500000, 5400000));

		// Add tiles layer
		IRegisterReceiver registerReceiver = new IRegisterReceiver() {
			@Override
			public Intent registerReceiver(final BroadcastReceiver aReceiver, final IntentFilter aFilter) {
				return null;
			}
			@Override
			public void unregisterReceiver(final BroadcastReceiver aReceiver) {
			}
		};
		mProvider = new OpenStreetMapTileProviderDirect(new Handler(), "key", registerReceiver);
		this.mTilesOverlay = new OpenStreetMapTilesOverlay (this.mOsmv, OpenStreetMapRendererFactory.FIETS_OVERLAY_NL, mProvider, this.getBaseContext());
		this.mOsmv.getOverlays().add(this.mTilesOverlay);

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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

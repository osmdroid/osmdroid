// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid.samples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
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

	private MapView mOsmv;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final RelativeLayout rl = new RelativeLayout(this);

		this.mOsmv = new MapView(this);
		this.mOsmv.setTilesScaledToDpi(true);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		/* ZoomControls */
		{
			/* Create a ImageView with a zoomIn-Icon. */
			final ImageView ivZoomIn = new ImageView(this);
			ivZoomIn.setImageResource(org.osmdroid.R.drawable.zoom_in);
			/* Create RelativeLayoutParams, that position in in the top right corner. */
			final RelativeLayout.LayoutParams zoominParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			zoominParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			zoominParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			rl.addView(ivZoomIn, zoominParams);

			ivZoomIn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					SampleWithMinimapZoomcontrols.this.mOsmv.getController().zoomIn();
				}
			});

			/* Create a ImageView with a zoomOut-Icon. */
			final ImageView ivZoomOut = new ImageView(this);
			ivZoomOut.setImageResource(org.osmdroid.R.drawable.zoom_out);

			/* Create RelativeLayoutParams, that position in in the top left corner. */
			final RelativeLayout.LayoutParams zoomoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			rl.addView(ivZoomOut, zoomoutParams);

			ivZoomOut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					SampleWithMinimapZoomcontrols.this.mOsmv.getController().zoomOut();
				}
			});
		}

		/* MiniMap */
		{
			MinimapOverlay miniMapOverlay = new MinimapOverlay(this,
					mOsmv.getTileRequestCompleteHandler());
			this.mOsmv.getOverlays().add(miniMapOverlay);
		}

		this.setContentView(rl);

		// Default location and zoom level
		IMapController mapController = mOsmv.getController();
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

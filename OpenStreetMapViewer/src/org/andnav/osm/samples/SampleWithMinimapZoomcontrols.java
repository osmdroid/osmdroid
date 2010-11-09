// Created by plusminus on 00:23:14 - 03.10.2008
package org.andnav.osm.samples;

import org.andnav.osm.R;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class SampleWithMinimapZoomcontrols extends Activity {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final Logger logger = LoggerFactory.getLogger(SampleWithMinimapZoomcontrols.class);

	// ===========================================================
	// Fields
	// ===========================================================

	private OpenStreetMapView mOsmv, mOsmvMinimap;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final RelativeLayout rl = new RelativeLayout(this);

        this.mOsmv = new OpenStreetMapView(this);
        rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));


        /* ZoomControls */
        {
	        /* Create a ImageView with a zoomIn-Icon. */
	        final ImageView ivZoomIn = new ImageView(this);
	        ivZoomIn.setImageResource(R.drawable.zoom_in);
	        /* Create RelativeLayoutParams, that position in in the top right corner. */
	        final RelativeLayout.LayoutParams zoominParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        zoominParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	        zoominParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	        rl.addView(ivZoomIn, zoominParams);

	        ivZoomIn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					SampleWithMinimapZoomcontrols.this.mOsmv.getController().zoomIn();
				}
	        });


	        /* Create a ImageView with a zoomOut-Icon. */
	        final ImageView ivZoomOut = new ImageView(this);
	        ivZoomOut.setImageResource(R.drawable.zoom_out);

	        /* Create RelativeLayoutParams, that position in in the top left corner. */
	        final RelativeLayout.LayoutParams zoomoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	        rl.addView(ivZoomOut, zoomoutParams);

	        ivZoomOut.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					SampleWithMinimapZoomcontrols.this.mOsmv.getController().zoomOut();
				}
	        });
        }

        /* MiniMap */
        {
	        /* Create another OpenStreetMapView, that will act as the MiniMap for the 'MainMap'. They will share the TileProvider. */
	        mOsmvMinimap = new OpenStreetMapView(this, OpenStreetMapRendererFactory.CLOUDMADESTANDARDTILES, this.mOsmv);
	        final int aZoomDiff = 3; // Use OpenStreetMapViewConstants.NOT_SET to disable autozooming of this minimap
	        this.mOsmv.setMiniMap(mOsmvMinimap, aZoomDiff);


	        /* Create RelativeLayout.LayoutParams that position the MiniMap on the top-right corner of the RelativeLayout. */
	        RelativeLayout.LayoutParams minimapParams = new RelativeLayout.LayoutParams(90, 90);
	        minimapParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	        minimapParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	        minimapParams.setMargins(5,5,5,5);
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

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

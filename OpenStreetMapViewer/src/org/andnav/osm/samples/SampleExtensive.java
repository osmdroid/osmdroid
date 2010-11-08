package org.andnav.osm.samples;

import org.andnav.osm.OpenStreetMapActivity;
import org.andnav.osm.R;
import org.andnav.osm.ResourceProxy;
import org.andnav.osm.ResourceProxyImpl;
import org.andnav.osm.constants.OpenStreetMapConstants;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapViewController;
import org.andnav.osm.views.overlay.OpenStreetMapViewSimpleLocationOverlay;
import org.andnav.osm.views.overlay.ScaleBarOverlay;
import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
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
public class SampleExtensive extends OpenStreetMapActivity implements OpenStreetMapConstants{
	// ===========================================================
	// Constants
	// ===========================================================
	public static final Logger logger = LoggerFactory.getLogger(SampleExtensive.class);
	
	private static final int MENU_ZOOMIN_ID = Menu.FIRST;
	private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
	private static final int MENU_RENDERER_ID = MENU_ZOOMOUT_ID + 1;
	private static final int MENU_ANIMATION_ID = MENU_RENDERER_ID + 1;
	private static final int MENU_MINIMAP_ID = MENU_ANIMATION_ID + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private OpenStreetMapView mOsmv, mOsmvMinimap;
	private OpenStreetMapViewController mOsmvController;
	private OpenStreetMapViewSimpleLocationOverlay mMyLocationOverlay;
	private ResourceProxy mResourceProxy;
	private ScaleBarOverlay mScaleBarOverlay;

	// ===========================================================
	// Constructors
	// ===========================================================

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, false); // Pass true here to actually contribute to OSM!

        mResourceProxy = new ResourceProxyImpl(getApplicationContext());

        final RelativeLayout rl = new RelativeLayout(this);

        this.mOsmv = new OpenStreetMapView(this);
        this.mOsmvController = this.mOsmv.getController();
        rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        /* Scale Bar Overlay */
        {
        	this.mScaleBarOverlay = new ScaleBarOverlay(this.getBaseContext(), mResourceProxy);
        	this.mOsmv.getOverlays().add(mScaleBarOverlay);
        	// Scale bar tries to draw as 1-inch, so to put it in the top center, set x offset to half screen width, minus half an inch.
        	this.mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels/2 - getResources().getDisplayMetrics().xdpi/2, 10);
        }
        
        /* SingleLocation-Overlay */
        {
	        /* Create a static Overlay showing a single location. (Gets updated in onLocationChanged(Location loc)! */
	        this.mMyLocationOverlay = new OpenStreetMapViewSimpleLocationOverlay(this, mResourceProxy);
	        this.mOsmv.getOverlays().add(mMyLocationOverlay);
        }

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
					SampleExtensive.this.mOsmvController.zoomIn();
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
					SampleExtensive.this.mOsmvController.zoomOut();
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

	@Override
	public void onLocationChanged(final Location pLoc) {
		this.mMyLocationOverlay.setLocation(new GeoPoint(pLoc));
	}

	@Override
	public void onLocationLost() {
		// We'll do nothing here.
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		pMenu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
		pMenu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");

		final SubMenu subMenu = pMenu.addSubMenu(0, MENU_RENDERER_ID, Menu.NONE, "Choose Renderer");
		{
			for(IOpenStreetMapRendererInfo renderer : OpenStreetMapRendererFactory.getRenderers()) {
				subMenu.add(0, 1000 + renderer.ordinal(), Menu.NONE, renderer.localizedName(mResourceProxy));
			}
		}

		pMenu.add(0, MENU_ANIMATION_ID, Menu.NONE, "Run Animation");
		pMenu.add(0, MENU_MINIMAP_ID, Menu.NONE, "Toggle Minimap");

		return true;
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()){
			case MENU_ZOOMIN_ID:
				this.mOsmvController.zoomIn();
				return true;

			case MENU_ZOOMOUT_ID:
				this.mOsmvController.zoomOut();
				return true;

			case MENU_RENDERER_ID:
				this.mOsmv.invalidate();
				return true;

			case MENU_MINIMAP_ID:
				switch(this.mOsmv.getOverrideMiniMapVisiblity()){
					case View.VISIBLE:
						this.mOsmv.setOverrideMiniMapVisiblity(View.INVISIBLE);
						break;
					case NOT_SET:
					case View.INVISIBLE:
					case View.GONE:
						this.mOsmv.setOverrideMiniMapVisiblity(View.VISIBLE);
						break;
				}
				return true;

			case MENU_ANIMATION_ID:
				this.mOsmv.getController().animateTo(52370816, 9735936, OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED, OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT); // Hannover
				// Stop the Animation after 500ms  (just to show that it works)
//				new Handler().postDelayed(new Runnable(){
//					@Override
//					public void run() {
//						SampleExtensive.this.mOsmv.getController().stopAnimation(false);
//					}
//				}, 500);
				return true;

			default:
				this.mOsmv.setRenderer(OpenStreetMapRendererFactory.getRenderer(item.getItemId() - 1000));
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

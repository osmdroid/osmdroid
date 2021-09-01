package org.osmdroid.samples;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import org.osmdroid.R;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

/**
 * @author Alex van der Linden
 */
public class SampleWithTilesOverlay extends AppCompatActivity {

    private MapView mMapView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup base map
        setContentView(R.layout.activity_samplewithtilesoverlay);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final LinearLayout mapContainer = findViewById(R.id.map_container);

        mMapView = new MapView(this);
        mMapView.setTilesScaledToDpi(true);
        mapContainer.addView(this.mMapView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mMapView.getZoomController().setVisibility(
                CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);

        //Copyright overlay
        String copyrightNotice = mMapView.getTileProvider().getTileSource().getCopyrightNotice();
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(this);
        copyrightOverlay.setCopyrightNotice(copyrightNotice);
        mMapView.getOverlays().add(copyrightOverlay);

        // zoom to the netherlands
        mMapView.getController().setZoom(8.);
        mMapView.getController().setCenter(new GeoPoint(53.6, 5.3));

        // Add tiles layer
        MapTileProviderBasic provider = new MapTileProviderBasic(getApplicationContext());
        provider.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
        TilesOverlay tilesOverlay = new TilesOverlay(provider, this.getBaseContext());
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mMapView.getOverlays().add(tilesOverlay);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
}

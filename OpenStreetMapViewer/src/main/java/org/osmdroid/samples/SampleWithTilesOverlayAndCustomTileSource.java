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
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

/**
 * @author Alex van der Linden
 */
public class SampleWithTilesOverlayAndCustomTileSource extends AppCompatActivity {

    private MapView mMapView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup base map
        setContentView(R.layout.activity_samplewithtilesoverlayandcustomtilesource);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final LinearLayout mapContainer = findViewById(R.id.map_container);

        mMapView = new MapView(this);
        mMapView.setTilesScaledToDpi(true);

        //Copyright overlay
        String copyrightNotice = mMapView.getTileProvider().getTileSource().getCopyrightNotice();
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(this);
        copyrightOverlay.setCopyrightNotice(copyrightNotice);
        mMapView.getOverlays().add(copyrightOverlay);

        mapContainer.addView(mMapView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mMapView.getZoomController().setVisibility(
                CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);

        // zoom to the netherlands
        mMapView.getController().setZoom(7D);
        mMapView.getController().setCenter(new GeoPoint(51.5D, 5.4D));

        // Add tiles layer with custom tile source
        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
        final ITileSource tileSource = new XYTileSource("FietsRegionaal", 3, 18, 256, ".png",
                new String[]{"http://overlay.openstreetmap.nl/openfietskaart-rcn/"});
        tileProvider.setTileSource(tileSource);
        tileProvider.getTileRequestCompleteHandlers().add(mMapView.getTileRequestCompleteHandler());
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
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

package org.osmdroid.google.sample;

import android.app.Activity;
import android.os.Bundle;

import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.views.MapView;

import java.util.Locale;

/**
 * Created by alex on 1/25/16.
 */
public class OsmMapActivity extends Activity {

    MapView mapView;
    @Override
    public void onCreate(Bundle instance){
        super.onCreate(instance);
        mapView = new MapView(this);
        setContentView(mapView);

        //this gets the key from the manifest
        BingMapTileSource.retrieveBingKey(this);
        BingMapTileSource source = new BingMapTileSource(null);

        mapView.setTileSource(source);
        mapView.setTilesScaledToDpi(true);
    }
}

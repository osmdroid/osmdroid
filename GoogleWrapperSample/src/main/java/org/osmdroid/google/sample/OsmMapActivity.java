package org.osmdroid.google.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.views.MapView;

import java.text.DecimalFormat;

/**
 * Created by alex on 1/25/16.
 */
public class OsmMapActivity extends Activity {

    public static final DecimalFormat df = new DecimalFormat("#.000000");
    TextView textViewCurrentLocation;
    MapView mMapView;
    @Override
    public void onCreate(Bundle instance){
        super.onCreate(instance);

        setContentView(R.layout.map_with_locationbox);
        mMapView = (MapView) findViewById(R.id.mapview);
        textViewCurrentLocation = (TextView) findViewById(R.id.textViewCurrentLocation);

        //this gets the key from the manifest
        BingMapTileSource.retrieveBingKey(this);
        BingMapTileSource source = new BingMapTileSource(null);
        mMapView.setMaxZoomLevel(19);

        mMapView.setTileSource(source);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," +event.getY() );
                //Toast.makeText(getActivity(), "onScroll", Toast.LENGTH_SHORT).show();
                updateInfo();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                updateInfo();
                return true;
            }
        });
    }

    private void updateInfo(){
        IGeoPoint mapCenter = mMapView.getMapCenter();
        textViewCurrentLocation.setText(df.format(mapCenter.getLatitude())+","+
                df.format(mapCenter.getLongitude())
                +","+mMapView.getZoomLevel());


    }
}

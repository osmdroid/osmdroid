package org.osmdroid.bugtestfragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

import java.text.DecimalFormat;

/**
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class Bug523 extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Issue 523 Bounding box when wrapping vertically";
    }
    TextView textViewCurrentLocation;
    public static final DecimalFormat df = new DecimalFormat("#.00000");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_with_locationbox, container,false);

        mMapView = (MapView) root.findViewById(R.id.mapview);
        textViewCurrentLocation = (TextView) root.findViewById(R.id.textViewCurrentLocation);
        return root;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        updateInfo();

        mMapView.setTileSource(TileSourceFactory.USGS_SAT);
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
        BoundingBox boundingBox = mMapView.getBoundingBox();
        textViewCurrentLocation.setText("N:" + df.format(boundingBox.getLatNorth())+"\n"+
            "S:" + df.format(boundingBox.getLatSouth())+"\n"+
            "E:" + df.format(boundingBox.getLonEast())+"\n"+
            "W:" + df.format(boundingBox.getLonWest())+"\n");

    }
}

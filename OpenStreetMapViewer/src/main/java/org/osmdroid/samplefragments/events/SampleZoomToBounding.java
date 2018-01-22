package org.osmdroid.samplefragments.events;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 10/4/16.
 */
public class SampleZoomToBounding extends BaseSampleFragment implements View.OnClickListener {

    private static final int border = 10;

    private Polygon polygon;
    Button btnCache;
    @Override
    public String getSampleTitle() {
        return "Zoom to Bounding Box";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container,false);

        mMapView = (MapView) root.findViewById(R.id.mapview);
        polygon = new Polygon(mMapView);
        btnCache = (Button) root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Zoom to bounds");

        polygon.setStrokeColor(Color.parseColor("#990000FF"));
        polygon.setStrokeWidth(2);
        polygon.setFillColor(Color.parseColor("#330000FF"));
        mMapView.getOverlays().add(polygon);

        return root;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCache:
                boolean ok = false;
                while (!ok) {
                    final double south = getRandomLatitude(TileSystem.MinLatitude);
                    final double north = getRandomLatitude(south);
                    final double west = getRandomLongitude();
                    double east = getRandomLongitude();
                    final BoundingBox boundingBox = new BoundingBox(north, east, south, west);
                    final double zoom = TileSystem.getBoundingBoxZoom(boundingBox, mMapView.getWidth() - 2 * border, mMapView.getHeight() - 2 * border);
                    ok = zoom >= mMapView.getMinZoomLevel() && zoom <= mMapView.getMaxZoomLevel();
                    if (ok) {
                        final String text = "with a border of " + border + " the computed zoom is " + zoom + " for box " + boundingBox;
                        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                        final List<GeoPoint> points = new ArrayList<>();
                        if (west > east) {
                            east += 2 * TileSystem.MaxLongitude;
                        }
                        addPoints(points, north, west, north, east);
                        addPoints(points, north, east, south, east);
                        addPoints(points, south, east, south, west);
                        addPoints(points, south, west, north, west);
                        polygon.setPoints(points);
                        mMapView.invalidate();
                        mMapView.zoomToBoundingBox(boundingBox, false, border);
                    }
                }
                break;

        }
    }

    /**
     * Add a succession of GeoPoint's, separated by an increment,
     * taken from the segment between two GeoPoint's
     * @since 6.0.0
     */
    private void addPoints(final List<GeoPoint> pPoints,
                           final double pBeginLat, final double pBeginLon,
                           final double pEndLat, final double pEndLon) {
        final double increment = 10; // in degrees
        pPoints.add(new GeoPoint(pBeginLat, pBeginLon));
        double lat = pBeginLat;
        double lon = pBeginLon;
        double incLat = pBeginLat == pEndLat ? 0 : pBeginLat < pEndLat ? increment : -increment;
        double incLon = pBeginLon == pEndLon ? 0 : pBeginLon < pEndLon ? increment : -increment;
        while (true) {
            if (incLat != 0) {
                lat += incLat;
                if (incLat < 0) {
                    if (lat < pEndLat) {
                        break;
                    }
                } else {
                    if (lat > pEndLat) {
                        break;
                    }
                }
            }
            if (incLon != 0) {
                lon += incLon;
                if (incLon < 0) {
                    if (lon < pEndLon) {
                        break;
                    }
                } else {
                    if (lon > pEndLon) {
                        break;
                    }
                }
            }
            pPoints.add(new GeoPoint(lat, lon));
        }
        pPoints.add(new GeoPoint(pEndLat, pEndLon));
    }

    private double getRandomLongitude() {
        return Math.random() * (TileSystem.MaxLongitude - TileSystem.MinLongitude) + TileSystem.MinLongitude;
    }

    private double getRandomLatitude(final double pMinLatitude) {
        return Math.random() * (TileSystem.MaxLatitude - pMinLatitude) + pMinLatitude;
    }
}

package org.osmdroid.samplefragments.data;

import android.graphics.drawable.Drawable;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * An example on using osmbonuspack's Marker class by following the tutorial at
 * https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0
 * https://github.com/MKergall/osmbonuspack/wiki/Tutorial_1
 *
 * created on 12/29/2016.
 *
 * @author Alex O'Ree
 */

public class SampleMarker extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Marker";
    }

    @Override
    public void addOverlays(){
        super.addOverlays();

        final List<GeoPoint> points = new ArrayList<>();
        final Drawable drawable = getResources().getDrawable(R.drawable.marker_default);

        GeoPoint startPoint = new GeoPoint(38.8977, -77.0365);  //white house
        points.add(startPoint);
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(drawable);
        startMarker.setTitle("White House");
        startMarker.setSnippet("The White House is the official residence and principal workplace of the President of the United States.");
        startMarker.setSubDescription("1600 Pennsylvania Ave NW, Washington, DC 20500");
        mMapView.getOverlays().add(startMarker);

        startPoint = new GeoPoint(38.8719, -77.0563);
        points.add(startPoint);
        startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(drawable);
        startMarker.setTitle("Pentagon");
        startMarker.setSnippet("The Pentagon.");
        startMarker.setSubDescription("The Pentagon is the headquarters of the United States Department of Defense.");
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.showInfoWindow();
                return true;
            }
        });
        mMapView.getOverlays().add(startMarker);



        startPoint = new GeoPoint(38.8895, -77.0353);
        points.add(startPoint);
        startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(drawable);
        startMarker.setTitle("Washington Monument");
        startMarker.setSnippet("Washington Monument.");
        startMarker.setSubDescription("Washington Monument.");
        startMarker.setRotation(45); // for demo purposes
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Toast.makeText(getContext(), marker.getTitle() + " was clicked", Toast.LENGTH_LONG).show();
                marker.showInfoWindow();
                return true;
            }
        });
        //startMarker.setInfoWindow(new MarkerInfoWindow());
        mMapView.getOverlays().add(startMarker);


        final BoundingBox boundingBox = BoundingBox.fromGeoPoints(points);
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.zoomToBoundingBox(boundingBox, false, drawable.getIntrinsicWidth());
            }
        });
    }
}

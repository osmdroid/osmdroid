package org.osmdroid.bugtestfragments;

import android.graphics.Color;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 8/25/16.
 */

public class Bug382Crash extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Bug 382 Crash while scrolling";
    }

    private Polygon polygon;
    private Polyline polyline;

    @Override
    protected void addOverlays() {
        super.addOverlays();
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(new GeoPoint(26.0, 113.5));
        geoPoints.add(new GeoPoint(26.0, 114.5));
        geoPoints.add(new GeoPoint(27.0, 114.5));
        geoPoints.add(new GeoPoint(26.0, 115.0));
        geoPoints.add(new GeoPoint(26.0, 116.0));
        geoPoints.add(new GeoPoint(27.0, 115.0));


        polygon = new Polygon(mMapView);
        polygon.setPoints(geoPoints.subList(0, 3));
        polygon.getFillPaint().setColor(0x96FF8200);
        polygon.getOutlinePaint().setColor(Color.RED);
        polygon.getOutlinePaint().setStrokeWidth(4);
        polygon.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView));
        polygon.setTitle("Polygon tapped!");
        mMapView.getOverlays().add(polygon);
        mMapView.invalidate();

        polyline = new Polyline(mMapView);
        polyline.setPoints(geoPoints.subList(3, 6));
        polyline.getOutlinePaint().setColor(Color.YELLOW);
        polyline.getOutlinePaint().setStrokeWidth(8);
        polyline.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView));
        polyline.setTitle("Polyline tapped!");
        mMapView.getOverlays().add(polyline);
        mMapView.invalidate();


    }
}

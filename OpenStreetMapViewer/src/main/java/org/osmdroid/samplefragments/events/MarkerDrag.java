package org.osmdroid.samplefragments.events;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

/**
 * Based on osmbonuspacks tutorial for dragging markers.
 * <p>
 * Long press to drag the marker, once you yet go, the new location added to a polyline to show it's
 * relative path
 * created on 1/14/2018.
 *
 * @author Alex O'Ree
 */

public class MarkerDrag extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Dragging a Marker";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        //0. Using the Marker overlay
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(new GeoPoint(0.0, 0.0));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Start point");
        startMarker.setDraggable(true);
        startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());
        mMapView.getOverlays().add(startMarker);

    }

    //0. Using the Marker and Polyline overlays - advanced options
    class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener {
        ArrayList<GeoPoint> mTrace;
        Polyline mPolyline;

        OnMarkerDragListenerDrawer() {
            mTrace = new ArrayList<GeoPoint>(100);
            mPolyline = new Polyline(mMapView);
            mPolyline.getOutlinePaint().setColor(0xAA0000FF);
            mPolyline.getOutlinePaint().setStrokeWidth(2.0f);
            mPolyline.setGeodesic(true);
            mMapView.getOverlays().add(mPolyline);
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            //mTrace.add(marker.getPosition());
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            mTrace.add(marker.getPosition());
            mPolyline.setPoints(mTrace);
            mMapView.invalidate();
        }

        @Override
        public void onMarkerDragStart(Marker marker) {
            //mTrace.add(marker.getPosition());
        }
    }

}

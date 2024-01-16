package org.osmdroid.samplefragments.events;

import androidx.annotation.NonNull;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

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
        mMapView.getOverlayManager().add(startMarker);

    }

    //0. Using the Marker and Polyline overlays - advanced options
    class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener {
        List<GeoPoint> mTrace;
        Polyline mPolyline;

        OnMarkerDragListenerDrawer() {
            mTrace = new ArrayList<GeoPoint>(100);
            mPolyline = new Polyline(mMapView);
            mPolyline.getOutlinePaint().setColor(0xAA0000FF);
            mPolyline.getOutlinePaint().setStrokeWidth(2.0f);
            mPolyline.setGeodesic(true);
            mMapView.getOverlayManager().add(mPolyline);
        }

        @Override
        public void onMarkerDrag(@NonNull Marker marker) {
            //mTrace.add(marker.getPosition());
        }

        @Override
        public void onMarkerDragEnd(@NonNull Marker marker) {
            mTrace.add(marker.getPosition());
            mPolyline.setPoints(mTrace);
            mMapView.invalidate();
        }

        @Override
        public void onMarkerDragStart(@NonNull Marker marker) {
            //mTrace.add(marker.getPosition());
        }
    }

}

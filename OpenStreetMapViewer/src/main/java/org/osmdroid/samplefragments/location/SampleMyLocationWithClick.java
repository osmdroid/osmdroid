package org.osmdroid.samplefragments.location;

import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * created on 1/13/2017.
 *
 * @author Alex O'Ree
 */

public class SampleMyLocationWithClick extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "MyLocationNewOverlay with Click";
    }

    @Override
    public void addOverlays(){
        super.addOverlays();

        MyLocationOverlayWithClick overlay = new MyLocationOverlayWithClick(mMapView);
        overlay.enableFollowLocation();
        overlay.enableMyLocation();
        mMapView.getOverlayManager().add(overlay);

    }

    public static class MyLocationOverlayWithClick extends MyLocationNewOverlay{

        public MyLocationOverlayWithClick(MapView mapView) {
            super(mapView);
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, MapView map) {
            if (getLastFix() != null)
                Toast.makeText(map.getContext(), "Tap! I am at " + getLastFix().getLatitude() + "," + getLastFix().getLongitude(), Toast.LENGTH_LONG).show();
            return true;

        }
    }
}

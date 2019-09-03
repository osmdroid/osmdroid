package org.osmdroid.samplefragments.location;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
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
    public void addOverlays() {
        super.addOverlays();

        final MyLocationOverlayWithClick overlay = new MyLocationOverlayWithClick(mMapView);
        overlay.enableFollowLocation();
        overlay.enableMyLocation();
        overlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "I was ran on the first fix");
                FragmentActivity activity = SampleMyLocationWithClick.this.getActivity();
                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GeoPoint myLocation = overlay.getMyLocation();
                            if (myLocation != null)
                                Toast.makeText(SampleMyLocationWithClick.this.getContext(), "GPS fix acquired at " + myLocation.toDoubleString(), Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(SampleMyLocationWithClick.this.getContext(), "GPS fix acquired (null)", Toast.LENGTH_LONG).show();
                        }
                    });

            }
        });
        mMapView.getOverlayManager().add(overlay);

    }

    public static class MyLocationOverlayWithClick extends MyLocationNewOverlay {

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

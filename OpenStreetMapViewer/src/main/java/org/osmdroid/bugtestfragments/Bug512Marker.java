package org.osmdroid.bugtestfragments;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

/**
 * created on 1/7/2017.
 *
 * @author Alex O'Ree
 */

public class Bug512Marker extends BaseSampleFragment {


    Marker marker = null;

    @Override
    public String getSampleTitle() {
        return "Bug 512 Marker infowindow leaks";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        marker = new Marker(mMapView);
        marker.setSnippet("Hello world, bug 512 part 1");
        marker.setPosition(new GeoPoint(-40d, -74d));
        mMapView.getController().setCenter(marker.getPosition());
        mMapView.getOverlayManager().add(marker);
    }

    @Override
    public boolean skipOnCiTests() {
        return true;
    }

    @Override
    public void runTestProcedures() throws Exception {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker.showInfoWindow();
            }
        });
        Thread.sleep(500);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker.closeInfoWindow();
                mMapView.getOverlayManager().remove(marker);
                marker.onDetach(mMapView);

                marker = new Marker(mMapView);
                marker.setSnippet("Hello world, bug 512 part 2");
                marker.setPosition(new GeoPoint(-40d, -74d));
                mMapView.getController().setCenter(marker.getPosition());
                mMapView.getOverlayManager().add(marker);

            }
        });

        Thread.sleep(500);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker.showInfoWindow();
            }
        });
        Thread.sleep(500);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker.closeInfoWindow();
                mMapView.getOverlayManager().remove(marker);
                marker.onDetach(mMapView);
            }
        });
    }
}

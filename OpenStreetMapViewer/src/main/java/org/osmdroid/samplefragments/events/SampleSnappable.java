package org.osmdroid.samplefragments.events;

import android.graphics.Point;

import org.osmdroid.api.IMapView;
import org.osmdroid.api.IProjection;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class SampleSnappable extends BaseSampleFragment {

    // cf. https://en.wikipedia.org/wiki/Gare_de_Perpignan
    private final GeoPoint MAP_CENTER = new GeoPoint(42.696111, 2.879444);

    @Override
    public String getSampleTitle() {
        return "Snappable";
    }

    class MyOverlay extends Overlay implements Overlay.Snappable {

        @Override
        public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
            final IProjection projection = mapView.getProjection();
            projection.toPixels(MAP_CENTER, snapPoint);
            return true;
        }
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        mMapView.getOverlayManager().add(new MyOverlay());
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(14.);
                mMapView.setExpectedCenter(MAP_CENTER);
            }
        });
    }
}

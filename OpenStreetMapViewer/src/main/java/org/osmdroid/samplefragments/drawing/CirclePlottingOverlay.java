package org.osmdroid.samplefragments.drawing;

import android.util.Log;
import android.view.MotionEvent;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

/**
 * created on 12/27/2017.
 *
 * @author Alex O'Ree
 */

public class CirclePlottingOverlay extends Overlay {

    float distanceKm;

    public CirclePlottingOverlay(float distanceKm) {
        super();
        this.distanceKm = distanceKm;
    }

    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {

        if (Configuration.getInstance().isDebugMapView()) {
            Log.d(IMapView.LOGTAG, "CirclePlottingOverlay onLongPress");
        }
        GeoPoint pt = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY(), null);
        /*
         * <b>Note</b></b: when plotting a point off the map, the conversion from
         * screen coordinates to map coordinates will return values that are invalid from a latitude,longitude
         * perspective. Sometimes this is a wanted behavior and sometimes it isn't. We are leaving it up to you,
         * the developer using osmdroid to decide on what is right for your application. See
         * <a href="https://github.com/osmdroid/osmdroid/pull/722">https://github.com/osmdroid/osmdroid/pull/722</a>
         * for more information and the discussion associated with this.
         */

        //just in case the point is off the map, let's fix the coordinates
        if (pt.getLongitude() < -180)
            pt.setLongitude(pt.getLongitude() + 360);
        if (pt.getLongitude() > 180)
            pt.setLongitude(pt.getLongitude() - 360);
        //latitude is a bit harder. see https://en.wikipedia.org/wiki/Mercator_projection
        if (pt.getLatitude() > 85.05112877980659)
            pt.setLatitude(85.05112877980659);
        if (pt.getLatitude() < -85.05112877980659)
            pt.setLatitude(-85.05112877980659);

        List<GeoPoint> circle = Polygon.pointsAsCircle(pt, distanceKm);
        Polygon p = new Polygon(mapView);
        p.setPoints(circle);
        p.setTitle("A circle");
        mapView.getOverlayManager().add(p);
        mapView.invalidate();
        return true;

    }
}

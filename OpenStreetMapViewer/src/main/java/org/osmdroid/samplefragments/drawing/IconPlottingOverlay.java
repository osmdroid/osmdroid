package org.osmdroid.samplefragments.drawing;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

/**
 * A super simple overlay to plot a marker when the user long presses on the map.
 * <p>
 * It does not draw anything on screen but does intercept long press events then adds
 * a hardcoded Marker to the map
 * created on 11/19/2017.
 *
 * @author Alex O'Ree
 * @since 6.0.0
 */

public class IconPlottingOverlay extends Overlay {
    Drawable markerIcon;

    public IconPlottingOverlay(Drawable m) {
        super();
        markerIcon = m;

    }

    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        if (markerIcon != null) {
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
            if (pt.getLatitude() > mapView.getTileSystem().getMaxLatitude())
                pt.setLatitude(mapView.getTileSystem().getMaxLatitude());
            if (pt.getLatitude() < mapView.getTileSystem().getMinLatitude())
                pt.setLatitude(mapView.getTileSystem().getMinLatitude());

            Marker m = new Marker(mapView);
            m.setPosition(pt);
            m.setIcon(markerIcon);
            m.setImage(markerIcon);
            m.setTitle("A demo title");
            m.setSubDescription("A demo sub description\n" + pt.getLatitude() + "," + pt.getLongitude());
            m.setSnippet("a snippet of information");
            mapView.getOverlayManager().add(m);
            mapView.invalidate();
            return true;
        }
        return false;
    }
}

package org.osmdroid.samplefragments.drawing;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

/**
 * A super simple overlay to plot a marker when the user long presses on the map.
 *
 * It does not draw anything on screen but does intercept long press events then adds
 * a hardcoded Marker to the map
 * created on 11/19/2017.
 * @since 6.0.0
 * @author Alex O'Ree
 */

public class IconPlottingOverlay extends Overlay {
    Drawable  markerIcon;

    public IconPlottingOverlay(Drawable m ) {
        super();
        markerIcon =m;

    }
    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {

    }

    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        if (markerIcon!=null) {
            IGeoPoint pt = mapView.getProjection().fromPixels((int)e.getX(), (int)e.getY(), null);
            Marker m = new Marker(mapView);
            m.setPosition((GeoPoint) pt);
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

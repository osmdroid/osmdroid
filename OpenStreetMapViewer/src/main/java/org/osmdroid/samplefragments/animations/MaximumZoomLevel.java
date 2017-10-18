package org.osmdroid.samplefragments.animations;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay;

/**
 * Demonstrates a one way to move an icon in an animation.
 * It's dirty but it works
 * created on 7/29/2017.
 * https://github.com/osmdroid/osmdroid/issues/636
 *
 * @author Alex O'Ree
 */

public class MaximumZoomLevel extends BaseSampleFragment implements MapListener {

    @Override
    public String getSampleTitle() {
        return "Maximum Zoom Level";
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        mMapView.setMaxZoomLevel(5);
        mMapView.setMapListener(this);
    }

    @Override
    public boolean onScroll(ScrollEvent scrollEvent) {
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent zoomEvent) {
        String zoomLevel = String.format(Locale.getDefault(), "%.2f", zoomEvent.getZoomLevel());
        Toast.makeText(getContext(), "Zoom to "+zoomLevel, Toast.LENGTH_SHORT).show();
        return false;
    }

}

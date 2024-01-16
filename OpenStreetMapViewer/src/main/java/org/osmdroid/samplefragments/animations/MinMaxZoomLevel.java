package org.osmdroid.samplefragments.animations;

import android.graphics.Rect;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.osmdroid.events.MapAdapter;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;

import java.util.Locale;

/**
 * Demonstrates interaction of double tab zoom with maximum zoom level
 * created on 10/18/2017.
 * https://github.com/osmdroid/osmdroid/issues/743
 *
 * @author Maradox
 * @since 6.0.0
 */

public class MinMaxZoomLevel extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Minimum and Maximum Zoom Level";
    }

    private final MapAdapter mMapAdapter = new MapAdapter() {
        @Override
        public boolean onZoom(ZoomEvent event) {
            String zoomLevel = String.format(Locale.getDefault(), "%.2f", event.getZoomLevel());
            Toast.makeText(getContext(), "Zoom to " + zoomLevel, Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    @Override
    protected void addOverlays() {
        super.addOverlays();
        mMapView.setMinZoomLevel(1.5);
        mMapView.setMaxZoomLevel(5.5);
        mMapView.addMapListener(mMapAdapter);
        mMapView.getController().zoomTo(2.5);
    }

}

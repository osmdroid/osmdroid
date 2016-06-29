package org.osmdroid.bugtestfragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

/**
 * See https://github.com/osmdroid/osmdroid/issues/82#issuecomment-229413838
 *
 * Created by alex on 6/29/16.
 */
public class Bug82WinDeath extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Bug #82 WinDeath";
    }
    protected void addOverlays() {
        //
        MapOverlay overlay = new MapOverlay(getActivity());
        mMapView.getOverlayManager().add(overlay);
        mMapView.getController().setCenter(new GeoPoint(-19279750, -11004483 ));
        mMapView.getController().setZoom(17);
    }



    public static class MapOverlay extends Overlay {

        private Paint innerPaint;


        public MapOverlay(Context context) {

            super(context);
            this.innerPaint = new Paint();
            this.innerPaint.setColor(Color.argb(0x80, 0x43, 0x24, 0xa0));
            this.innerPaint.setStrokeWidth(2.0f);
            this.innerPaint.setStyle(Paint.Style.FILL);

        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (!shadow && mapView.getZoomLevel() > 15) {
                canvas.drawCircle(-19279750, -11004483, 8.0f, innerPaint);
            }

        }
    }
}

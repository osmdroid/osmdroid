package org.osmdroid.samplefragments.drawing;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;
import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;

/**
 * Created by Maradox on 11/26/17.
 */

public class SampleDrawPolylineWithoutWrapping extends BaseSampleFragment implements View.OnClickListener {
    ImageButton painting,panning;

    CustomPaintingSurface paint;
    @Override
    public String getSampleTitle() {
        return "Draw a polyline on screen without wrapping";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_drawlines, null);
        mMapView = (MapView) v.findViewById(R.id.mapview);
        panning = (ImageButton) v.findViewById(R.id.enablePanning);
        panning.setOnClickListener(this);
        panning.setBackgroundColor(Color.BLACK);
        painting = (ImageButton) v.findViewById(R.id.enablePainting);
        painting.setOnClickListener(this);
        paint = (CustomPaintingSurface) v.findViewById(R.id.paintingSurface);
        paint.init(mMapView);
        paint.setMode(CustomPaintingSurface.Mode.Polyline);
        return v;

    }

    @Override
    public void addOverlays() {
        this.mMapView.setMapRepetitionEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enablePanning:
                paint.setVisibility(View.GONE);
                panning.setBackgroundColor(Color.BLACK);
                painting.setBackgroundColor(Color.TRANSPARENT);
                break;
            case R.id.enablePainting:
                paint.setVisibility(View.VISIBLE);
                painting.setBackgroundColor(Color.BLACK);
                panning.setBackgroundColor(Color.TRANSPARENT);
                break;
        }
    }
}

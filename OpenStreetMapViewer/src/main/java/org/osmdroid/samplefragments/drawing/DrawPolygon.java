package org.osmdroid.samplefragments.drawing;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.MapView;

/**
 * created on 8/26/2017.
 *
 * @author Alex O'Ree
 */

public class DrawPolygon extends BaseSampleFragment implements View.OnClickListener {
    ImageButton painting,panning;

    CustomPaintingSurface paint;
    @Override
    public String getSampleTitle() {
        return "Draw a polygon on screen";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_drawlines, null);
        mMapView = (MapView) v.findViewById(org.osmdroid.R.id.mapview);
        panning = (ImageButton) v.findViewById(R.id.enablePanning);
        panning.setOnClickListener(this);
        panning.setBackgroundColor(Color.BLACK);
        painting = (ImageButton) v.findViewById(R.id.enablePainting);
        painting.setOnClickListener(this);
        paint = (CustomPaintingSurface) v.findViewById(R.id.paintingSurface);
        paint.init(mMapView);
        paint.setMode(CustomPaintingSurface.Mode.Polygon);
        return v;

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

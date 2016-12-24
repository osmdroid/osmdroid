package org.osmdroid.samplefragments.data;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of SimpleFastPointOverlay
 * Created by Miguel Porto on 12-11-2016.
 */

public class SampleSimpleFastPointOverlay extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Simple Fast Point Overlay with 10k points";
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        // create 10k labelled points
        // in most cases, there will be no problems of displaying >100k points, feel free to try
        List<IGeoPoint> points = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            points.add(new LabelledGeoPoint(37 + Math.random() * 5, -8 + Math.random() * 5
                    , "Point #" + i));
        }

        // wrap them in a theme
        SimplePointTheme pt = new SimplePointTheme(points, true);

        // create label style
        Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setColor(Color.parseColor("#0000ff"));
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(24);

        // set some visual options for the overlay
        // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(true).setCellSize(15).setTextStyle(textStyle);

        // create the overlay with the theme
        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);

        // onClick callback
        sfpo.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                Toast.makeText(mMapView.getContext()
                        , "You clicked " + ((LabelledGeoPoint) points.get(point)).getLabel()
                        , Toast.LENGTH_SHORT).show();
            }
        });

        // add overlay
        mMapView.getOverlays().add(sfpo);

        // zoom to its bounding box
        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {

            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                if(mMapView != null && mMapView.getController() != null) {
                    mMapView.getController().zoomTo(6);
                    mMapView.zoomToBoundingBox(sfpo.getBoundingBox(), true);
                }

            }
        });

    }

}


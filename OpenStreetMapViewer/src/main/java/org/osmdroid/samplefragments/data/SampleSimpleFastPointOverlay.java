package org.osmdroid.samplefragments.data;

import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;
import org.osmdroid.views.overlay.simplefastpoint.StyledLabelledGeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of SimpleFastPointOverlay
 * Created by Miguel Porto on 12-11-2016.
 */

public class SampleSimpleFastPointOverlay extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Simple Fast Point Overlay with 60k points";
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        // **********************************************
        // Create 30k labelled points sharing same style
        // **********************************************
        // in most cases, there will be no problems of displaying >100k points, feel free to try
        List<IGeoPoint> points = new ArrayList<>();
        for (int i = 0; i < 30000; i++) {
            points.add(new LabelledGeoPoint(37 + Math.random() * 5, -8 + Math.random() * 5
                    , "Point #" + i));
        }

        // wrap them in a theme
        SimplePointTheme pointTheme = new SimplePointTheme(points);

        // create label style
        Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setColor(Color.parseColor("#0000ff"));
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(24);

        // set some visual options for the overlay
        // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setSymbol(SimpleFastPointOverlayOptions.Shape.SQUARE)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(true).setCellSize(12).setTextStyle(textStyle)
                .setMinZoomShowLabels(10);

        // create the overlay with the theme
        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pointTheme, opt);

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

        // *****************************************************
        // Now add another layer with points individually styled
        // *****************************************************
        // create 30k labelled points
        List<IGeoPoint> individualStyledPoints = new ArrayList<>();
        Paint indPointStyle, indTextStyle;

        for (int i = 0; i < 30000; i++) {
            // create random colored style for each point
            indPointStyle = new Paint();
            indPointStyle.setStyle(Paint.Style.FILL);
            indPointStyle.setColor(Color.rgb((int) Math.floor(Math.random() * 255)
                    , (int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255)));

            // create style with random color and text size for each point label
            indTextStyle = new Paint();
            indTextStyle.setTextSize((int) (10 + Math.random() * 30));
            indTextStyle.setTextAlign(Paint.Align.CENTER);
            indTextStyle.setColor(Color.rgb((int) Math.floor(Math.random() * 255)
                    , (int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255)));
            indTextStyle.setStyle(Paint.Style.FILL);

            individualStyledPoints.add(new StyledLabelledGeoPoint(
                    37 + Math.random() * 5, -3 + Math.random() * 5
                    , "Point #" + i, indPointStyle, indTextStyle));
        }

        // wrap point list in a theme
        SimplePointTheme individualStyledPointTheme = new SimplePointTheme(individualStyledPoints);

        // set some visual options for the theme
        opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setSymbol(SimpleFastPointOverlayOptions.Shape.SQUARE)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setCellSize(12).setMinZoomShowLabels(10);

        // create the overlay with the theme
        final SimpleFastPointOverlay sfpo1 = new SimpleFastPointOverlay(individualStyledPointTheme, opt);

        // add overlay
        mMapView.getOverlays().add(sfpo1);

        // zoom to both themes' bounding box
        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mMapView != null && mMapView.getController() != null
                        && mMapView.getIntrinsicScreenRect(null).height() > 0)
                    mMapView.zoomToBoundingBox(sfpo.getBoundingBox().concat(sfpo1.getBoundingBox()), false);
            }
        }, 500L);

    }

}


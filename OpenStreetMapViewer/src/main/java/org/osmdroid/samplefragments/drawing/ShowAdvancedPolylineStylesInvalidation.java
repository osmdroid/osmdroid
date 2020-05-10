package org.osmdroid.samplefragments.drawing;

import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingForScalarContainer;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationHue;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList;

import java.util.ArrayList;

/**
 * Simple example to show scalar mapping invalidation.
 * @author Matthias Dittmer
 */
public class ShowAdvancedPolylineStylesInvalidation extends BaseSampleFragment implements View.OnClickListener {

    /*
     * Example data
     */
    private boolean mLineExtended = false;
    private TextView textViewCurrentLocation;
    private Button btnProceed;
    private Polyline mPolyline = null;
    private ColorMappingVariationHue mMapping = null;
    private ColorMappingForScalarContainer mContainer = null;
    private ArrayList<GeoPoint> points = new ArrayList<>();
    private ArrayList<Float> scalars = new ArrayList<>();

    @Override
    public String getSampleTitle() {
        return "Show advanced polyline (with invalidation)";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        setupLine();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.layout_advanced_polyline_invalidate, null);

        // setup UI references
        mMapView = v.findViewById(R.id.mapview);
        textViewCurrentLocation = v.findViewById(R.id.textInformation);
        btnProceed = v.findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(this);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        centerToLine();
    }

    /*
     * Creates new line with an initial mapping.
     */
    private void setupLine() {

        // remove previous data
        if(mPolyline != null) {
            mMapView.getOverlayManager().remove(mPolyline);
            mPolyline = null;
            mMapping = null;
            mContainer = null;
        }

        // fill list with points
        points.clear();
        points.add(new GeoPoint(37.0, -11.0));
        points.add(new GeoPoint(37.5, -11.5));
        points.add(new GeoPoint(38.0, -11.0));
        points.add(new GeoPoint(38.5, -11.5));
        points.add(new GeoPoint(39.0, -11.0));
        points.add(new GeoPoint(39.5, -11.5));

        // create new polyline
        mPolyline = new Polyline(mMapView, false, false);

        // setup a black border
        final Paint paintBorder = new Paint();
        paintBorder.setColor(Color.BLACK);
        paintBorder.setAntiAlias(true);
        paintBorder.setStrokeWidth(25);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeJoin(Paint.Join.ROUND);
        paintBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBorder.setAntiAlias(true);
        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintBorder));

        // add points to line
        mPolyline.setPoints(points);

        // fill list with scalars
        scalars.clear();
        scalars.add(0.0f);
        scalars.add(20.0f);
        scalars.add(10.0f);
        scalars.add(30.0f);
        scalars.add(50.0f);
        scalars.add(25.0f);

        // setup mapping objects
        mMapping = new ColorMappingVariationHue(0,50, 0,120, 1.0f, 0.5f);
        mContainer = new ColorMappingForScalarContainer(mMapping);

        // add scalars to mapping and container
        // you need to add the scalars to both lists
        for (final float scalar : scalars) {
            mMapping.add(scalar);
            mContainer.add(scalar);
        }

        // setup the mapping
        final Paint paintMapping = new Paint();
        paintMapping.setAntiAlias(true);
        paintMapping.setStrokeWidth(20);
        paintMapping.setStyle(Paint.Style.FILL_AND_STROKE);
        paintMapping.setStrokeJoin(Paint.Join.ROUND);
        paintMapping.setStrokeCap(Paint.Cap.ROUND);
        paintMapping.setAntiAlias(true);
        mPolyline.getOutlinePaintLists().add(new PolychromaticPaintList(paintMapping, mMapping, true));

        // update UI
        mMapView.getOverlayManager().add(mPolyline);
        // force a redraw (normally triggered when map is moved for example)
        mMapView.invalidate();
        textViewCurrentLocation.setText("Scalar range from 0 to 50\nfor hue ranging from 0 to 120.");
        btnProceed.setText("Extend Polyline");
    }

    /*
     * Extends the line and invalidates the mapping with a new range.
     */
    private void extendAndInvalidateLine() {

        // add new points
        mPolyline.addPoint(new GeoPoint(40.0, -11.0));
        mPolyline.addPoint(new GeoPoint(40.5, -11.5));
        mPolyline.addPoint(new GeoPoint(41.0, -11.0));
        mPolyline.addPoint(new GeoPoint(41.5, -11.5));
        // add scalars with higher values to mapping and container
        mMapping.add(80.f);
        mMapping.add(60.f);
        mMapping.add(100.f);
        mMapping.add(100.f);
        mContainer.add(80.f);
        mContainer.add(60.f);
        mContainer.add(100.f);
        mContainer.add(100.f);

        // update mapping with scalar end updated from 50 to 100
        mMapping.init(0,100, 0,120);
        // call refresh to update line
        mContainer.refresh();
        // force a redraw (normally triggered when map is moved for example)
        mMapView.invalidate();

        // update UI
        textViewCurrentLocation.setText("New scalar range from 0 to 100\nfor hue ranging from 0 to 120.");
        btnProceed.setText("Reset Polyline");

    }

    void centerToLine() {
        mMapView.getController().setCenter(new GeoPoint(38.5, -11.5));
        mMapView.getController().zoomTo(6.0f);
    }

    @Override
    public void onClick(View view) {
        // simple toggle logic
        if (view.getId() == R.id.btnProceed) {
            if (mLineExtended) {
                setupLine();
                mLineExtended = false;
            } else {
                extendAndInvalidateLine();
                mLineExtended = true;
            }
        }
    }
}

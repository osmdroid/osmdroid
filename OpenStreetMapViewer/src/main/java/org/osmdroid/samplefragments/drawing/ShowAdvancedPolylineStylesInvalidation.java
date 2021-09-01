package org.osmdroid.samplefragments.drawing;

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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingForScalarContainer;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationHue;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList;

import java.util.ArrayList;


/**
 * Simple example to show scalar mapping invalidation.
 *
 * @author Matthias Dittmer
 */
public class ShowAdvancedPolylineStylesInvalidation extends BaseSampleFragment implements View.OnClickListener {

    /*
     * Example data
     */
    private boolean mLineExtended = false;
    private TextView textInformation;
    private Button btnProceed;
    private Polyline mPolyline = null;
    private ColorMappingVariationHue mMapping = null;
    private ColorMappingForScalarContainer mContainer = null;
    private String mInformation = "Scalar range from %d to %d\n" +
            "for hue ranging from %d to %d.\n" +
            "Showing speed from red (slow) to green (fast).";
    private String sProceed = "Extend Polyline";
    private String sReset = "Reset Polyline";
    private final Paint paintBorder = new Paint();
    private final Paint paintMapping = new Paint();

    // min / max values used in the example
    // scalar meaning is "speed" in this example with no unit
    static final int MIN_SCALAR = 0;
    static final int MAX_SCALAR = 50;
    static final int MAX_SCALAR_EXTENDED = 100;
    // hue range from red for "slow" to green for "fast"
    static final int MIN_HUE = 0;       // red
    static final int MAX_HUE = 120;     // green
    static final float SAT = 1.0f;
    static final float LUM = 0.5f;

    // Simple wrapper class to group a point and scalar together
    // No getters and setters
    static class PointWithScalar {
        GeoPoint mPoint;
        float mScalar;

        PointWithScalar(GeoPoint pPoint, float scalar) {
            mPoint = pPoint;
            mScalar = scalar;
        }
    }

    // list holding the initial and extended data
    private ArrayList<PointWithScalar> mInitialData;
    private ArrayList<PointWithScalar> mExtendedData;

    @Override
    public String getSampleTitle() {
        return "Show advanced polyline (with invalidation)";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        initialSetupForLine();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.layout_advanced_polyline_invalidate, null);

        // setup UI references
        mMapView = v.findViewById(R.id.mapview);
        textInformation = v.findViewById(R.id.textInformation);
        btnProceed = v.findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(this);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // create border paint
        paintBorder.setColor(Color.BLACK);
        paintBorder.setAntiAlias(true);
        paintBorder.setStrokeWidth(25);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeJoin(Paint.Join.ROUND);
        paintBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBorder.setAntiAlias(true);

        // create mapping paint
        paintMapping.setAntiAlias(true);
        paintMapping.setStrokeWidth(20);
        paintMapping.setStyle(Paint.Style.FILL_AND_STROKE);
        paintMapping.setStrokeJoin(Paint.Join.ROUND);
        paintMapping.setStrokeCap(Paint.Cap.ROUND);
        paintMapping.setAntiAlias(true);

        // setup initial data
        mInitialData = new ArrayList<>();
        mInitialData.add(new PointWithScalar(new GeoPoint(37.0, -11.0), 10));
        mInitialData.add(new PointWithScalar(new GeoPoint(37.0, -11.0), 0.0f));
        mInitialData.add(new PointWithScalar(new GeoPoint(37.5, -11.5), 20.0f));
        mInitialData.add(new PointWithScalar(new GeoPoint(38.0, -11.0), 10.0f));
        mInitialData.add(new PointWithScalar(new GeoPoint(38.5, -11.5), 30.0f));
        mInitialData.add(new PointWithScalar(new GeoPoint(39.0, -11.0), 50.0f));
        mInitialData.add(new PointWithScalar(new GeoPoint(39.5, -11.5), 25.0f));

        // setup extended data
        // please note: the last scalar is not used, N points use N - 1 scalars
        mExtendedData = new ArrayList<>();
        mExtendedData.add(new PointWithScalar(new GeoPoint(40.0, -11.0), 80.0f));
        mExtendedData.add(new PointWithScalar(new GeoPoint(40.5, -11.5), 60.f));
        mExtendedData.add(new PointWithScalar(new GeoPoint(41.0, -11.0), 100.0f));
        mExtendedData.add(new PointWithScalar(new GeoPoint(41.5, -11.5), 100.0f));

        // center to line once here
        centerToLine();
    }

    /*
     * Creates initial line.
     */
    private void initialSetupForLine() {

        // remove previous data
        if (mPolyline != null) {
            // remove polyline
            mMapView.getOverlayManager().remove(mPolyline);
            mPolyline = null;
            mMapping = null;
            mContainer = null;
        }

        // create polyline
        mPolyline = new Polyline(mMapView, false, false);

        // setup border
        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintBorder));

        // setup mapping objects
        mMapping = new ColorMappingVariationHue(MIN_SCALAR, MAX_SCALAR, MIN_HUE, MAX_HUE, SAT, LUM);
        mContainer = new ColorMappingForScalarContainer(mMapping);

        // add initial data to polyline
        addDataToPolyline(mInitialData);

        // setup the mapping
        mPolyline.getOutlinePaintLists().add(new PolychromaticPaintList(paintMapping, mMapping, true));

        // update UI
        mMapView.getOverlayManager().add(mPolyline);
        // force a redraw (normally triggered when map is moved for example)
        mMapView.invalidate();
        textInformation.setText(String.format(mInformation, MIN_SCALAR, MAX_SCALAR, MIN_HUE, MAX_HUE));
        btnProceed.setText(sProceed);
    }

    /*
     * Extends the line and invalidates the mapping with a new scalar range.
     */
    private void extendAndInvalidateLine() {

        // extend data of polyline
        addDataToPolyline(mExtendedData);

        // update mapping with scalar end updated from 50 to 100
        // new "top speed" of "100"
        mMapping.init(MIN_SCALAR, MAX_SCALAR_EXTENDED, MIN_HUE, MAX_HUE);

        // call refresh to update line
        mContainer.refresh();

        // force a redraw (normally triggered when map is moved for example)
        mMapView.invalidate();

        // update UI
        textInformation.setText(String.format(mInformation, MIN_SCALAR, MAX_SCALAR_EXTENDED, MIN_HUE, MAX_HUE));
        btnProceed.setText(sReset);

    }

    // add geopoint to polyline and scalar to container from provided list
    private void addDataToPolyline(ArrayList<PointWithScalar> pData) {
        for (final PointWithScalar element : pData) {
            mPolyline.addPoint(element.mPoint);
            mContainer.add(element.mScalar);
        }
    }

    // centers roughly to line
    private void centerToLine() {
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setCenter(new GeoPoint(38.5, -11.5));
                mMapView.getController().zoomTo(6.0f);
            }
        });
    }

    @Override
    public void onClick(View view) {
        // simple toggle logic
        if (view.getId() == R.id.btnProceed) {
            if (mLineExtended) {
                initialSetupForLine();
                mLineExtended = false;
            } else {
                extendAndInvalidateLine();
                mLineExtended = true;
            }
        }
    }
}

package org.osmdroid.samplefragments.drawing;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.advancedpolyline.ColorMapping;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingCycle;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingForScalar;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingPlain;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingRanges;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationHue;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationLuminance;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationSaturation;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Showing all modes of advanced polyline styles with example data.
 *
 * @author Matthias Dittmer
 */
public class ShowAdvancedPolylineStyles extends BaseSampleFragment implements View.OnClickListener {

    /**
     * List with all examples.
     */
    ArrayList<AdvancedPolylineExample> mListExamples = new ArrayList<>();

    /**
     * JSON object holding the complete example data.
     */
    private final String JSON_EXAMPLE_DATA = "example_data_advanced_polyline.json";
    JSONObject mData = null;

    @Override
    public String getSampleTitle() {
        return "Show advanced polyline styles";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        addSamplePolylines();
        recenter(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container, false);

        mMapView = new MapView(getActivity());
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        final Button btnCache = root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Next example");

        return root;
    }

    @Override
    public void onClick(View view) {
        recenter(1);
    }

    public void addSamplePolylines() {

        // load JSON data
        loadJSONDataFromAssets();

        // setup all examples
        setupExamples();

        // add all examples from array
        for (AdvancedPolylineExample example : mListExamples) {
            mMapView.getOverlayManager().add(example.getPolyline());
            // show info window so line is easy to spot for user
            //example.getPolyline().showInfoWindow();
        }
    }

    private void recenter(final int pIndex) {
        mListExamples.get(mIndex).mPolyline.closeInfoWindow();
        mIndex += pIndex;
        mIndex = mIndex % mListExamples.size();
        getmMapView().post(new Runnable() {
            @Override
            public void run() {
                final AdvancedPolylineExample example = mListExamples.get(mIndex);
                getmMapView().zoomToBoundingBox(example.mBoundingBox, false);
                example.getPolyline().showInfoWindow();
            }
        });
    }

    private int mIndex;

    /**
     * Class to hold on example.
     */
    class AdvancedPolylineExample {

        private Polyline mPolyline;
        private BoundingBox mBoundingBox;

        public AdvancedPolylineExample(final String title, final String description,
                                       final ColorMapping mapping, final boolean gradient,
                                       final Integer borderColor, final boolean pClosePath,
                                       final List<GeoPoint> points, final List<Float> pScalars) {
            // setup polyline
            mPolyline = new Polyline(mMapView, false, pClosePath);

            if (borderColor != null) {
                final Paint paint = new Paint();
                paint.setColor(borderColor);
                paint.setAntiAlias(true);
                paint.setStrokeWidth(25);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setAntiAlias(true);
                mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paint));
            }

            // add points and scalar
            mPolyline.setPoints(points);
            if (mapping instanceof ColorMappingForScalar) {
                final ColorMappingForScalar mappingForScalar = (ColorMappingForScalar) mapping;
                for (final float scalar : pScalars) {
                    mappingForScalar.add(scalar);
                }
            }

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(20);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);
            mPolyline.getOutlinePaintLists().add(new PolychromaticPaintList(paint, mapping, gradient));

            // set a bounding box from points, plus 1.2f scaled
            mBoundingBox = BoundingBox.fromGeoPoints(points).increaseByScale(1.2f);

            // add infowindow
            final InfoWindowExample infoWindow;
            infoWindow = new InfoWindowExample(R.layout.bonuspack_bubble, mMapView);
            infoWindow.setText(title, description);
            mPolyline.setInfoWindow(infoWindow);
        }

        public final Polyline getPolyline() {
            return mPolyline;
        }
    }

    /**
     * Infowindow
     */
    class InfoWindowExample extends InfoWindow {

        InfoWindowExample(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    close();
                }
            });
        }

        public void setText(String title, String description) {
            ((TextView) getView().findViewById(R.id.bubble_title)).setText(title);
            ((TextView) getView().findViewById(R.id.bubble_description)).setText(description);
        }

        @Override
        public void onOpen(Object item) {

        }

        @Override
        public void onClose() {

        }
    }

    private void setupExamples() {
        // Plain example
        mListExamples.add(new AdvancedPolylineExample("Sailing", "Plain colored polyline showing a sailing track from Sicily to Sardinia.",
                new ColorMappingPlain(Color.WHITE),
                false, Color.BLACK, false,
                getPoints("sailing"), null));

        // Cycle example
        mListExamples.add(new AdvancedPolylineExample("Coast", "Cycle polyline showing border of Italy coast line.\n\nColor cycle: GREEN, WHITE, RED.",
                new ColorMappingCycle(new int[]{Color.GREEN, Color.WHITE, Color.RED}),
                true, Color.BLACK, false,
                getPoints("border_coast_italy"), null));

        // Ranges example
        SortedMap<Float, Integer> mColorRanges = new TreeMap<>();
        mColorRanges.put(5.0f, Color.RED);
        mColorRanges.put(7.5f, Color.YELLOW);
        mColorRanges.put(10.0f, Color.GREEN);
        mListExamples.add(new AdvancedPolylineExample("Tram", "Ranges polyline with border showing a tram ride between airport and main train station.\n\nBorders: 5 m/s RED, 7.5 m/s YELLOW, 10.0 m/s GREEN.",
                new ColorMappingRanges(mColorRanges, true),
                false, Color.BLACK, false,
                getPoints("tram"), getScalars("tram")));

        // Hue example
        mListExamples.add(new AdvancedPolylineExample("Flight", "Hue variation polyline for speed of plane from Paris to Philadelphia.\n\nHue from 0.0f to 120.0f for speed range 0 km/h to 1000 km/h.",
                new ColorMappingVariationHue(0.0f, 1000.0f, 0.0f, 120.0f, 1.0f, 0.5f),
                false, Color.BLACK, false,
                getPoints("flight_paris_phil"), getScalars("flight_paris_phil")));

        // Saturation example
        mListExamples.add(new AdvancedPolylineExample("Flight", "Saturation variation polyline for speed of plane from Frankfurt to Bangkok.\n\nSaturation from 0.0f to 1.0f for speed range 0 km/h to 1100 km/h.",
                new ColorMappingVariationSaturation(0.0f, 1100.0f, 0.0f, 1.0f, 160.0f, 0.5f),
                false, Color.BLACK, false,
                getPoints("flight_fra_bkk"), getScalars("flight_fra_bkk")));

        // Luminance example
        mListExamples.add(new AdvancedPolylineExample("Hiking", "Luminance variation polyline for height of hiking track in Nepal Himalayas.\n\nLuminance from 0.0f to 1.0f for height range 1800 m to 6000 m.",
                new ColorMappingVariationLuminance(1800.0f, 6000.0f, 0.0f, 1.0f, 0.0f, 0.0f),
                false, Color.BLACK, false,
                getPoints("nepal_himalayas"), getScalars("nepal_himalayas")));

        // Loop example
        final List<GeoPoint> hexagon = new ArrayList<>();
        hexagon.add(new GeoPoint(51.038333, 2.377500)); // Dunkerque
        hexagon.add(new GeoPoint(48.573333, 7.752200)); // Strasbourg
        hexagon.add(new GeoPoint(43.695833, 7.271389)); // Nice
        hexagon.add(new GeoPoint(42.698611, 2.895556)); // Perpignan
        hexagon.add(new GeoPoint(43.481617, -1.556111)); // Biarritz
        hexagon.add(new GeoPoint(48.390833, -4.468889)); // Brest
        final ColorMappingCycle colorMappingCycle = new ColorMappingCycle(new int[]{ // rainbow
                Color.RED,
                Color.rgb(0xFF, 0x7f, 0), // orange
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.rgb(0x7F, 0, 0xFF) // violet
        });
        colorMappingCycle.setGeoPointNumber(hexagon.size());
        mListExamples.add(new AdvancedPolylineExample("Loop", "Test about closed Polylines",
                colorMappingCycle,
                true, Color.BLACK, true,
                hexagon, null));
    }

    private void loadJSONDataFromAssets() {
        try {
            InputStream inputStream = getContext().getAssets().open(JSON_EXAMPLE_DATA);
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(inputStream, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }

            // parse into JSON object
            mData = new JSONObject(out.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<GeoPoint> getPoints(String identifier) {
        ArrayList<GeoPoint> points = new ArrayList<>();
        try {
            JSONObject example = (JSONObject) mData.get(identifier);
            JSONArray array = example.getJSONArray("geopoints");

            for (int i = 0; i < array.length(); i += 2) {
                final double lat = array.getDouble(i);
                final double lon = array.getDouble(i + 1);
                points.add(new GeoPoint(lat, lon));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return points;
    }

    private ArrayList<Float> getScalars(String identifier) {
        ArrayList<Float> scalars = new ArrayList<>();
        try {
            JSONObject example = (JSONObject) mData.get(identifier);
            JSONArray array = example.getJSONArray("scalars");

            for (int i = 0; i < array.length(); i++) {
                final double scalar = array.getDouble(i);
                scalars.add((float) scalar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scalars;
    }
}


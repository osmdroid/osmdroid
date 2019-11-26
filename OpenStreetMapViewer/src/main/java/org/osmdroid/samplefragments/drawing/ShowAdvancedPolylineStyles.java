package org.osmdroid.samplefragments.drawing;

import android.graphics.Color;
import android.view.View;
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
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingPlain;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingRanges;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationHue;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationLuminance;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationSaturation;
import org.osmdroid.views.overlay.advancedpolyline.PolylineStyle;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Showing all modes of advanced polyline styles with example data.
 * @author Matthias Dittmer
 */
public class ShowAdvancedPolylineStyles extends BaseSampleFragment {

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
            example.getPolyline().showInfoWindow();
        }
    }

    /**
     * Class to hold on example.
     */
    class AdvancedPolylineExample {

        private Polyline mPolyline;
        private PolylineStyle mPolylineStyle;
        private InfoWindowExample mInfoWindow;
        private BoundingBox mBoundingBox;

        public AdvancedPolylineExample(final String title, final String description,
                                       final ColorMapping mapping, final Boolean gradient,
                                       final Boolean border, final Integer borderColor,
                                       ArrayList<GeoPoint> points, ArrayList<Float> scalar) {
            // setup polyline
            mPolyline = new Polyline();
            mPolyline.setWidth(20.0f);

            // setup style
            mPolylineStyle = new PolylineStyle(mapping, gradient);
            if (border) {
                mPolylineStyle.setBorder(25.0f, borderColor);
            }
            mPolyline.setStyle(mPolylineStyle);

            // add points and scalar
            mPolyline.setPoints(points, scalar);

            // set a bounding box from points, plus 1.2f scaled
            mBoundingBox = BoundingBox.fromGeoPoints(points).increaseByScale(1.2f);

            // add infowindow
            mInfoWindow = new InfoWindowExample(R.layout.bonuspack_bubble, mMapView);
            mInfoWindow.setText(title, description);
            mPolyline.setInfoWindow(mInfoWindow);
        }

        public final Polyline getPolyline() {
            return mPolyline;
        }
    }

    /**
     * Infowindow
     */
    class InfoWindowExample extends InfoWindow {

        public InfoWindowExample(int layoutResId, MapView mapView) {
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
                new ColorMappingPlain(Color.WHITE), false, true, Color.BLACK, getPoints("sailing"), getScalars("sailing")));

        // Cycle example
        ArrayList<Integer> mColors = new ArrayList<>();
        mColors.add( Color.GREEN);
        mColors.add( Color.WHITE);
        mColors.add(Color.RED);
        mListExamples.add(new AdvancedPolylineExample("Coast", "Cycle polyline showing border of Italy coast line.\n\nColor cycle: GREEN, WHITE, RED.",
                new ColorMappingCycle(mColors), false, true, Color.BLACK, getPoints("border_coast_italy"), getScalars("border_coast_italy")));

        // Ranges example
        SortedMap<Float, Integer> mColorRanges = new TreeMap<>();
        mColorRanges.put(5.0f, Color.RED);
        mColorRanges.put(7.5f, Color.YELLOW);
        mColorRanges.put(10.0f, Color.GREEN);
        mListExamples.add(new AdvancedPolylineExample("Tram", "Ranges polyline with border showing a tram ride between airport and main train station.\n\nBorders: 5 m/s RED, 7.5 m/s YELLOW, 10.0 m/s GREEN.",
                new ColorMappingRanges(mColorRanges), false, true, Color.BLACK, getPoints("tram"), getScalars("tram")));

        // Hue example
        mListExamples.add(new AdvancedPolylineExample("Flight", "Hue variation polyline for speed of plane from Paris to Philadelphia.\n\nHue from 0.0f to 120.0f for speed range 0 km/h to 1000 km/h.",
                new ColorMappingVariationHue(0.0f, 1000.0f, 0.0f, 120.0f, 1.0f, 0.5f), false, true, Color.BLACK, getPoints("flight_paris_phil"), getScalars("flight_paris_phil")));

        // Saturation example
        mListExamples.add(new AdvancedPolylineExample("Flight", "Saturation variation polyline for speed of plane from Frankfurt to Bangkok.\n\nSaturation from 0.0f to 1.0f for speed range 0 km/h to 1100 km/h.",
                new ColorMappingVariationSaturation(0.0f, 1100.0f, 0.0f, 1.0f, 160.0f, 0.5f), false, true, Color.BLACK, getPoints("flight_fra_bkk"), getScalars("flight_fra_bkk")));

        // Luminance example
        mListExamples.add(new AdvancedPolylineExample("Hiking", "Luminance variation polyline for height of hiking track in Nepal Himalayas.\n\nLuminance from 0.0f to 1.0f for height range 1800 m to 6000 m.",
                new ColorMappingVariationLuminance(1800.0f, 6000.0f, 0.0f, 1.0f, 0.0f, 0.0f), false, true, Color.BLACK, getPoints("nepal_himalayas"), getScalars("nepal_himalayas")));
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
                Double lat = array.getDouble(i);
                Double lon = array.getDouble(i + 1);
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
                Double scalar = array.getDouble(i);
                scalars.add(scalar.floatValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scalars;
    }
}


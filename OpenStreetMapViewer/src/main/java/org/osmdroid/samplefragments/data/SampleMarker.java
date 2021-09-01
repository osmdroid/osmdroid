package org.osmdroid.samplefragments.data;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.ScaleDiskOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * An example on using osmbonuspack's Marker class by following the tutorial at
 * https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0
 * https://github.com/MKergall/osmbonuspack/wiki/Tutorial_1
 * <p>
 * created on 12/29/2016.
 *
 * @author Alex O'Ree
 */

public class SampleMarker extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Marker";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();

        final GeoPoint whiteHouse = new GeoPoint(38.8977, -77.0365);
        final GeoPoint pentagon = new GeoPoint(38.8719, -77.0563);
        final GeoPoint washington = new GeoPoint(38.8895, -77.0353);

        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        final ScaleDiskOverlay scaleDiskOverlayWhiteHouse = new ScaleDiskOverlay(getContext(), whiteHouse, 2000, GeoConstants.UnitOfMeasure.foot);
        final Paint circlePaint = new Paint();
        circlePaint.setColor(Color.rgb(128, 128, 128));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(2);
        scaleDiskOverlayWhiteHouse.setCirclePaint2(circlePaint);
        final Paint diskPaint = new Paint();
        diskPaint.setColor(Color.argb(128, 128, 128, 128));
        diskPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        scaleDiskOverlayWhiteHouse.setCirclePaint1(diskPaint);
        final Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(10 * displayMetrics.density);
        scaleDiskOverlayWhiteHouse.setTextPaint(textPaint);
        scaleDiskOverlayWhiteHouse.setLabelOffsetBottom((int) (-2 * displayMetrics.density));
        scaleDiskOverlayWhiteHouse.setLabelOffsetTop((int) (2 * displayMetrics.density));
        scaleDiskOverlayWhiteHouse.setLabelOffsetLeft((int) (2 * displayMetrics.density));
        scaleDiskOverlayWhiteHouse.setLabelOffsetRight((int) (-2 * displayMetrics.density));
        scaleDiskOverlayWhiteHouse.setDisplaySizeMin(100);
        scaleDiskOverlayWhiteHouse.setDisplaySizeMax(800);
        mMapView.getOverlays().add(scaleDiskOverlayWhiteHouse);

        final ScaleDiskOverlay scaleDiskOverlayPentagon = new ScaleDiskOverlay(getContext(), pentagon, 1, GeoConstants.UnitOfMeasure.statuteMile);
        final Paint diskPaint2 = new Paint();
        diskPaint2.setColor(Color.argb(32, 255, 0, 0));
        diskPaint2.setStyle(Paint.Style.FILL);
        scaleDiskOverlayPentagon.setCirclePaint1(diskPaint2);
        final Paint textPaint2 = new Paint();
        textPaint2.setAntiAlias(true);
        textPaint2.setColor(Color.RED);
        textPaint2.setTextSize(20 * displayMetrics.density);
        scaleDiskOverlayPentagon.setTextPaint(textPaint2);
        scaleDiskOverlayPentagon.setLabelOffsetTop((int) (2 * displayMetrics.density));
        scaleDiskOverlayPentagon.setDisplaySizeMin(100);
        scaleDiskOverlayPentagon.setDisplaySizeMax(800);
        mMapView.getOverlays().add(scaleDiskOverlayPentagon);

        final ScaleDiskOverlay scaleDiskOverlayWashington = new ScaleDiskOverlay(getContext(), washington, 2000, GeoConstants.UnitOfMeasure.foot);
        final Paint circlePaint2 = new Paint();
        circlePaint2.setColor(Color.CYAN);
        circlePaint2.setStyle(Paint.Style.STROKE);
        circlePaint2.setStrokeWidth(2);
        scaleDiskOverlayWashington.setCirclePaint2(circlePaint2);
        scaleDiskOverlayWashington.setDisplaySizeMin(100);
        scaleDiskOverlayWashington.setDisplaySizeMax(800);
        mMapView.getOverlays().add(scaleDiskOverlayWashington);

        mMapView.getOverlays().add(new ScaleBarOverlay(mMapView));

        final List<GeoPoint> points = new ArrayList<>();
        final Drawable drawable = getResources().getDrawable(R.drawable.marker_default);

        GeoPoint startPoint = new GeoPoint(whiteHouse);
        points.add(startPoint);
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(drawable);
        startMarker.setTitle("White House");
        startMarker.setSnippet("The White House is the official residence and principal workplace of the President of the United States.");
        startMarker.setSubDescription("1600 Pennsylvania Ave NW, Washington, DC 20500");
        mMapView.getOverlays().add(startMarker);

        startPoint = new GeoPoint(pentagon);
        points.add(startPoint);
        startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(drawable);
        startMarker.setTitle("Pentagon");
        startMarker.setSnippet("The Pentagon.");
        startMarker.setSubDescription("The Pentagon is the headquarters of the United States Department of Defense.");
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.showInfoWindow();
                return true;
            }
        });
        mMapView.getOverlays().add(startMarker);

        startPoint = new GeoPoint(washington);
        points.add(startPoint);
        startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(drawable);
        startMarker.setTitle("Washington Monument");
        startMarker.setSnippet("Washington Monument.");
        startMarker.setSubDescription("Washington Monument.");
        startMarker.setRotation(45); // for demo purposes
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Toast.makeText(getContext(), marker.getTitle() + " was clicked", Toast.LENGTH_LONG).show();
                marker.showInfoWindow();
                return true;
            }
        });
        //startMarker.setInfoWindow(new MarkerInfoWindow());
        mMapView.getOverlays().add(startMarker);


        final BoundingBox boundingBox = BoundingBox.fromGeoPoints(points);
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.zoomToBoundingBox(boundingBox, false, drawable.getIntrinsicWidth());
            }
        });
    }
}

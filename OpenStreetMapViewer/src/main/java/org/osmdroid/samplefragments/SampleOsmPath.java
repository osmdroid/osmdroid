package org.osmdroid.samplefragments;

import org.osmdroid.R;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * An example using osmbonuspacks polyline class for a simple box on around centra park, nyc
 * @author Marc Kurtz
 * 
 */
public class SampleOsmPath extends BaseSampleFragment {

	public static final String TITLE = "OsmPath drawing";

	private BoundingBox sCentralParkBoundingBox;
	private Paint sPaint;

	public SampleOsmPath() {
		sCentralParkBoundingBox = new BoundingBox(40.796788, -73.949232, 40.768094, -73.981762);

		sPaint = new Paint();
		sPaint.setColor(Color.argb(175, 255, 0, 0));
		sPaint.setStyle(Style.FILL);
	}
	@Override
	public String getSampleTitle() {
		return TITLE;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		mMapView.getController().setZoom(13);
		mMapView.getController().setCenter(sCentralParkBoundingBox.getCenter());

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void addOverlays() {
		super.addOverlays();

		final Context context = getActivity();

		//mOsmPathOverlay = new OsmPathOverlay(context);
		//mMapView.getOverlayManager().add(mOsmPathOverlay);
		Polyline line = new Polyline();
		line.setTitle("Central Park, NYC");
		line.setSubDescription(Polyline.class.getCanonicalName());
		line.setWidth(20f);
		List<GeoPoint> pts = new ArrayList<>();
		//here, we create a polygon, note that you need 5 points in order to make a closed polygon (rectangle)

		pts.add(new GeoPoint(40.796788, -73.949232));
		pts.add(new GeoPoint(40.796788, -73.981762));
		pts.add(new GeoPoint(40.768094, -73.981762));
		pts.add(new GeoPoint(40.768094, -73.949232));
		pts.add(new GeoPoint(40.796788, -73.949232));
		line.setPoints(pts);
		line.setGeodesic(true);
		line.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView));
		//Note, the info window will not show if you set the onclick listener
		//line can also attach click listeners to the line
		/*
		line.setOnClickListener(new Polyline.OnClickListener() {
			@Override
			public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
				Toast.makeText(context, "Hello world!", Toast.LENGTH_LONG).show();
				return false;
			}
		});*/
		mMapView.getOverlayManager().add(line);


		Marker marker = new Marker(mMapView);
		marker.setDraggable(false);
		marker.setTitle("Central Park");
		marker.setPosition(new GeoPoint(((40.796788-40.768094)/2)+40.768094,
				((-73.949232- -73.981762)/2) +  -73.981762));
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		marker.setIcon(getResources().getDrawable(R.drawable.sfgpuci));
		marker.setTitle("Start point");
		marker.setDraggable(true);
		mMapView.getOverlays().add(marker);


		//here, we create a polygon using polygon class, note that you need 4 points in order to make a rectangle
		Polygon polygon = new Polygon();
		polygon.setTitle("This is a polygon");
		polygon.setSubDescription(Polygon.class.getCanonicalName());
		polygon.setFillColor(Color.RED);
		polygon.setVisible(true);
		polygon.setStrokeColor(Color.BLACK);
		polygon.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView));


		pts = new ArrayList<>();
		pts.add(new GeoPoint(40.886788, -73.959232));
		pts.add(new GeoPoint(40.886788, -73.971762));
		pts.add(new GeoPoint(40.878094, -73.971762));
		pts.add(new GeoPoint(40.878094, -73.959232));
		polygon.setPoints(pts);
		mMapView.getOverlays().add(polygon);

	}
}

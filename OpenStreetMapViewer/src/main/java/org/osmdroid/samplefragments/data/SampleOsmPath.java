package org.osmdroid.samplefragments.data;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.R;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.models.MyMapItem;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.util.ArrayList;
import java.util.List;


/**
 * An example using osmbonuspacks polyline class for a simple box on around centra park, nyc
 *
 * @author Marc Kurtz
 */
public class SampleOsmPath extends BaseSampleFragment implements MapListener {

    public static final String TITLE = "OsmPath drawing";

    private BoundingBox sCentralParkBoundingBox;

    public SampleOsmPath() {
        sCentralParkBoundingBox = new BoundingBox(40.796788, -73.949232, 40.768094, -73.981762);
    }

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mMapView.getController().setZoom(13.);
        mMapView.getController().setCenter(sCentralParkBoundingBox.getCenterWithDateLine());

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        //we override this to force zoom to 22, even though mapnik dooesn't do that deep
        OnlineTileSourceBase mapnik = new XYTileSource("Mapnik",
                0, 22, 256, ".png", new String[]{
                "https://a.tile.openstreetmap.org/",
                "https://b.tile.openstreetmap.org/",
                "https://c.tile.openstreetmap.org/"});
        mMapView.getTileProvider().setTileSource(mapnik);


        Polyline line = new Polyline(mMapView);
        line.setTitle("Central Park, NYC");
        line.setSubDescription(Polyline.class.getCanonicalName());
        line.getOutlinePaint().setStrokeWidth(20f);
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
        mMapView.setMaxZoomLevel(22.0);


        Marker marker = new Marker(mMapView);
        marker.setDraggable(false);
        marker.setTitle("Central Park");
        marker.setPosition(new GeoPoint(((40.796788 - 40.768094) / 2) + 40.768094,
                ((-73.949232 - -73.981762) / 2) + -73.981762));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.sfgpuci));
        marker.setTitle("Start point");
        marker.setDraggable(true);
        mMapView.getOverlays().add(marker);


        //here, we create a polygon using polygon class, note that you need 4 points in order to make a rectangle
        Polygon polygon = new Polygon(mMapView);
        polygon.setTitle("This is a polygon");
        polygon.setSubDescription(Polygon.class.getCanonicalName());
        polygon.getFillPaint().setColor(Color.RED);
        polygon.setVisible(true);
        polygon.getOutlinePaint().setColor(Color.BLACK);
        polygon.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView));


        pts = new ArrayList<>();
        pts.add(new GeoPoint(40.886788, -73.959232));
        pts.add(new GeoPoint(40.886788, -73.971762));
        pts.add(new GeoPoint(40.878094, -73.971762));
        pts.add(new GeoPoint(40.878094, -73.959232));
        polygon.setPoints(pts);
        mMapView.getOverlays().add(polygon);


        Marker m = new Marker(mMapView);
        m.setPosition(new GeoPoint(51.7875, 6.135278));
        m.setImage(getResources().getDrawable(R.drawable.icon));
        line = new Polyline(mMapView);
        line.setTitle("TEST");
        line.setSubDescription(Polyline.class.getCanonicalName());
        line.getOutlinePaint().setStrokeWidth(20f);
        pts = new ArrayList<>();
        //here, we create a polygon, note that you need 5 points in order to make a closed polygon (rectangle)

        pts.add(new GeoPoint(51.7875, 6.135278));
        pts.add(new GeoPoint(51.7875, 6.135288));
        pts.add(new GeoPoint(51.7874, 6.135288));
        pts.add(new GeoPoint(51.7874, 6.135288));
        pts.add(new GeoPoint(51.7875, 6.135278));
        line.setPoints(pts);
        line.setGeodesic(true);
        line.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView));

        mMapView.getOverlayManager().add(m);
        mMapView.getOverlayManager().add(line);


        List<MyMapItem> list = new ArrayList<>();
        list.add(new MyMapItem("title", "description", new GeoPoint(51.7875, 6.135278)));
        ItemizedIconOverlay<MyMapItem> layer = new ItemizedIconOverlay<>(list, getResources().getDrawable(R.drawable.shgpuci), new ItemizedIconOverlay.OnItemGestureListener<MyMapItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, MyMapItem item) {
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, MyMapItem item) {
                return false;
            }
        }, getActivity());

        mMapView.getOverlayManager().add(layer);
        mMapView.addMapListener(this);

    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        return false;
    }

    @Override
    public boolean onZoom(final ZoomEvent event) {
        Activity act = getActivity();
        if (act != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("Zoomer", "zoom event triggered " + event.getZoomLevel());
                        //Toast.makeText(getActivity(), "Zoom is " + event.getZoomLevel(), Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        return true;
    }

    @Override
    public boolean skipOnCiTests() {
        return true;
    }

    @Override
    public void runTestProcedures() {
        final GeoPoint geoPoint = new GeoPoint(40.886788, -73.959232);
        while (mMapView.getZoomLevelDouble() < mMapView.getMaxZoomLevel()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMapView.getController().animateTo(geoPoint);
                    mMapView.getController().zoomIn();
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        final GeoPoint geoPoint2 = new GeoPoint(40.796788, -73.949232);
        while (mMapView.getZoomLevelDouble() < mMapView.getMaxZoomLevel()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMapView.getController().animateTo(geoPoint2);
                    mMapView.getController().zoomIn();
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

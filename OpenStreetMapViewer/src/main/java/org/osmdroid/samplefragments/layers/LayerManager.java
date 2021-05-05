package org.osmdroid.samplefragments.layers;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Views the current layers in a navigation drawer layout
 * created on 2/18/2018.
 *
 * @author Alex O'Ree
 */

public class LayerManager extends BaseSampleFragment {
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    public String getSampleTitle() {
        return "Layer Manager";
    }

    TextView textViewCurrentLocation;
    public static final DecimalFormat df = new DecimalFormat("#.000000");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.layermanage_drawer, container, false);

        mMapView = root.findViewById(R.id.mapview);
        textViewCurrentLocation = root.findViewById(R.id.textViewCurrentLocation);


        mPlanetTitles = new String[]{"Layer 1", "Layer 2"};
        mDrawerLayout = root.findViewById(R.id.drawer_layout);
        mDrawerList = root.findViewById(R.id.left_drawer);
        final OverlayAdapter adapter = new OverlayAdapter(getContext(), mMapView.getOverlayManager());
        // Set the adapter for the list view
        mDrawerList.setAdapter(adapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Overlay overlay = adapter.getItem(position);
                if (overlay instanceof Marker) {
                    ((Marker) overlay).showInfoWindow();
                    mMapView.getController().animateTo(((Marker) overlay).getPosition());

                } else if (overlay instanceof Polygon) {
                    ((Polygon) overlay).showInfoWindow();
                    mMapView.getController().animateTo(((Polygon) overlay).getInfoWindowLocation());

                } else if (overlay instanceof Polyline) {
                    ((Polyline) overlay).showInfoWindow();
                    mMapView.getController().animateTo(((Polyline) overlay).getInfoWindowLocation());

                } else {
                    BoundingBox bounds = overlay.getBounds();
                    mMapView.getController().animateTo(new GeoPoint(bounds.getCenterLatitude(), bounds.getCenterLongitude()));

                    //mMapView.getController().zoomToSpan(bounds.getLatitudeSpan(), bounds.getLongitudeSpan());
                }
                //TODO center map on location
            }
        });
        mDrawerList.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO prompt for confirmation, then remove it from the overlay manager
                return false;
            }
        });


        return root;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        updateInfo();

        mMapView.setTileSource(TileSourceFactory.USGS_SAT);
        mMapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
                //Toast.makeText(getActivity(), "onScroll", Toast.LENGTH_SHORT).show();
                updateInfo();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                updateInfo();
                return true;
            }
        });


        //add some simple markers, lines and polygons just to have something to populate the list
        GeoPoint startPoint = new GeoPoint(38.8977, -77.0365);  //white house
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.icon));
        startMarker.setTitle("White House");
        startMarker.setSnippet("The White House is the official residence and principal workplace of the President of the United States.");
        startMarker.setSubDescription("1600 Pennsylvania Ave NW, Washington, DC 20500");
        mMapView.getOverlays().add(startMarker);

        startPoint = new GeoPoint(38.8719, -77.0563);
        startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.icon));
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


        startPoint = new GeoPoint(38.8895, -77.0353);
        startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.icon));
        startMarker.setTitle("Washington Monument");
        startMarker.setSnippet("Washington Monument.");
        startMarker.setSubDescription("Washington Monument.");
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
        Polyline mNorthPolyline = new Polyline();
        Polyline mSouthPolyline = new Polyline();
        Polyline mWestPolyline = new Polyline();
        Polyline mEastPolyline = new Polyline();


        ArrayList<GeoPoint> list = new ArrayList<>();
        BoundingBox sCentralParkBoundingBox = new BoundingBox(40.796788,
                -73.949232, 40.768094, -73.981762);
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualNorth(), -85));
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualNorth(), -65));
        mNorthPolyline.setPoints(list);
        mMapView.getOverlays().add(mNorthPolyline);

        list.clear();
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualSouth(), -85));
        list.add(new GeoPoint(sCentralParkBoundingBox.getActualSouth(), -65));
        mSouthPolyline.setPoints(list);
        mMapView.getOverlays().add(mSouthPolyline);

        list.clear();
        list.add(new GeoPoint(45, sCentralParkBoundingBox.getLonWest()));
        list.add(new GeoPoint(35, sCentralParkBoundingBox.getLonWest()));
        mWestPolyline.setPoints(list);
        mMapView.getOverlays().add(mWestPolyline);

        list.clear();
        list.add(new GeoPoint(45, sCentralParkBoundingBox.getLonEast()));
        list.add(new GeoPoint(35, sCentralParkBoundingBox.getLonEast()));
        mEastPolyline.setPoints(list);
        mMapView.getOverlays().add(mEastPolyline);

        mMapView.invalidate();
        Toast.makeText(this.mMapView.getContext(), "Swipe from the right", Toast.LENGTH_LONG).show();
    }

    private void updateInfo() {
        IGeoPoint mapCenter = mMapView.getMapCenter();
        textViewCurrentLocation.setText(df.format(mapCenter.getLatitude()) + "," +
                df.format(mapCenter.getLongitude())
                + ",zoom=" + mMapView.getZoomLevelDouble());

    }
}

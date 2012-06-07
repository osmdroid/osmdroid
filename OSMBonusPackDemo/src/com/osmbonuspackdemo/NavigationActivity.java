package com.osmbonuspackdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.GeocoderNominatim;
import org.osmdroid.bonuspack.GoogleRoadManager;
import org.osmdroid.bonuspack.MapEventsOverlay;
import org.osmdroid.bonuspack.MapEventsReceiver;
import org.osmdroid.bonuspack.OSRMRoadManager;
import org.osmdroid.bonuspack.MapQuestRoadManager;
import org.osmdroid.bonuspack.Road;
import org.osmdroid.bonuspack.RoadNode;
import org.osmdroid.bonuspack.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NavigationActivity 
	extends Activity implements MapEventsReceiver
{
	protected MapView map;
	protected PathOverlay roadOverlay;
	protected MyItemizedOverlayWithBubble markerOverlays;
	MyItemizedOverlayWithBubble roadNodes;
	protected GeoPoint startPoint, destinationPoint;
	protected ExtendedOverlayItem markerStart, markerDestination;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        MapController mapController = map.getController();
        mapController.setZoom(9);
        
		//To use MapEventsReceiver methods, you must add a MapEventsOverlay:
		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		map.getOverlays().add(overlay);
		
		startPoint = new GeoPoint(48.131174, -1.637256);
		mapController.setCenter(startPoint);
		
		// Test MyItemizedOverlayWithBubble:
		markerOverlays = new MyItemizedOverlayWithBubble(map, this);
		map.getOverlays().add(markerOverlays.getOverlay());
		markerStart = putMarkerItem(null, startPoint, "Start", 
				R.drawable.marker_a, R.drawable.rogger_rabbit);
		
		destinationPoint = new GeoPoint(48.4, -1.9);
		markerDestination = putMarkerItem(null, destinationPoint, "Destination", 
				R.drawable.marker_b, R.drawable.jessica);

    	roadNodes = new MyItemizedOverlayWithBubble(map, this);
		map.getOverlays().add(roadNodes.getOverlay());

		//Test road service:
		putRoadOverlay(startPoint, destinationPoint);
		
		//Handle Search Destination button:
		Button searchButton = (Button)findViewById(R.id.buttonSearch);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchLocationButton();
			}
		});
	}

    /**
     * Test MyItemizedOverlay object
     */
    public void putMyItemizedOverlay(GeoPoint p){
		Drawable markerA = getResources().getDrawable(R.drawable.marker_a);
		MyItemizedOverlay myOverlays = new MyItemizedOverlay(markerA, this);
		OverlayItem overlayItem = new OverlayItem("Home Sweet Home", "This is the place I live", p);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		myOverlays.addItem(overlayItem);
		map.getOverlays().add(myOverlays.getOverlay());    	
		map.invalidate();
    }

    /** 
     * Reverse Geocoding
     */
    public String getAddress(GeoPoint p){
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		String theAddress;
		try {
			double dLatitude = p.getLatitudeE6() * 1E-6;
			double dLongitude = p.getLongitudeE6() * 1E-6;
			List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
			StringBuilder sb = new StringBuilder(); 
			if (addresses.size() > 0) { 
				Address address = addresses.get(0); 
				int n = address.getMaxAddressLineIndex();
				for (int i=0; i<=n; i++) {
					if (i!=0) 
						sb.append(", ");
					sb.append(address.getAddressLine(i));
				}
				theAddress = new String(sb.toString());
			} else {
				theAddress = null;
			}
		} catch (IOException e) {
			theAddress = null;
		}
		if (theAddress != null) {
			//Toast.makeText(this, theAddress, Toast.LENGTH_LONG).show();
			return theAddress;
		} else {
			//Toast.makeText(this, "Issue on Geocoding", Toast.LENGTH_LONG).show();
			return "";
		}
    }
    
    public ExtendedOverlayItem putMarkerItem(ExtendedOverlayItem item, GeoPoint p, String title, 
    		int markerResId, int iconResId) {
		if (item != null){
			markerOverlays.removeItem(item);
		}
		Drawable marker = getResources().getDrawable(markerResId);
		ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(title, "", p);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		overlayItem.setMarker(marker);
		overlayItem.setImage(getResources().getDrawable(iconResId));
		markerOverlays.addItem(overlayItem);
		map.invalidate();
		//Start geocoding task to update the description of the marker with its address:
		new GeocodingTask().execute(overlayItem);
		return overlayItem;
    }
    
    private void putRoadNodes(Road road){
		roadNodes.removeAllItems();
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
    	for (int i=0; i<n; i++){
    		RoadNode node = road.mNodes.get(i);
    		String instructions = (node.mInstructions==null ? "" : node.mInstructions);
    		ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(
    				"" + (i+1) + ". " + instructions, 
    				road.getLengthDurationText(node.mLength, node.mDuration),
    				node.mLocation);
    		nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
    		nodeMarker.setMarker(marker);
    		roadNodes.addItem(nodeMarker);
    	}
    }
    
	/**
	 * Task to get the road in a separate thread. 
	 */
	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
		protected Road doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
			//GoogleRoadManager roadManager = new GoogleRoadManager();
			//OSRMRoadManager roadManager = new OSRMRoadManager();
			MapQuestRoadManager roadManager = new MapQuestRoadManager();
			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) {
			List<Overlay> mapOverlays = map.getOverlays();
			if (roadOverlay != null){
				mapOverlays.remove(roadOverlay);
			}
			roadOverlay = RoadManager.buildRoadOverlay(result, map.getContext());
			Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
				//we set the road overlay at the "bottom", just above MapEventsOverlay,
				//to avoid covering the other overlays. 
			mapOverlays.add(removedOverlay);
			putRoadNodes(result);
			map.invalidate();
			//Set route info in the text view:
			((TextView)findViewById(R.id.routeInfo)).setText(result.getLengthDurationText(-1));
		}
	}
	
	//Task to Reverse-Geocode the marker position in a separate thread:
	private class GeocodingTask extends AsyncTask<Object, Void, String> {
		ExtendedOverlayItem marker;
		protected String doInBackground(Object... params) {
			marker = (ExtendedOverlayItem)params[0];
			return getAddress(marker.getPoint());
		}
		protected void onPostExecute(String result) {
			marker.setDescription(result);
		}
	}
	
    public void putRoadOverlay(GeoPoint start, GeoPoint destination){
    	ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
    	waypoints.add(start);
    	//waypoints.add(new GeoPoint(...)); 
    		//intermediate waypoints can be added here.
    	waypoints.add(destination);
		new UpdateRoadTask().execute(waypoints);
    }

    /**
     * Geocoding of the destination address
     */
	public void handleSearchLocationButton(){
		EditText destinationEdit = (EditText)findViewById(R.id.editDestination);
		String destinationAddress = destinationEdit.getText().toString();
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		try {
			List<Address> foundAdresses = geocoder.getFromLocationName(destinationAddress, 1);
			if (foundAdresses.size() == 0) { //if no address found, display an error
				Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
			} else {
				Address address = foundAdresses.get(0); //get first address
				GeoPoint addressPosition = new GeoPoint(
						address.getLatitude(), 
						address.getLongitude());
				longPressHelper(addressPosition);
				map.getController().setCenter(addressPosition);
			}
		} catch (Exception e) {
			Toast.makeText(this, "Geocoding error", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override public boolean longPressHelper(IGeoPoint p) {
		//On long-press, we update the trip destination:
		destinationPoint = new GeoPoint((GeoPoint)p);
		markerDestination = putMarkerItem(markerDestination, destinationPoint, 
	    		"Destination", R.drawable.marker_b, R.drawable.jessica);
		putRoadOverlay(startPoint, destinationPoint);
		return true;
	}

	@Override public boolean singleTapUpHelper(IGeoPoint p) {
		return false;
	}

}

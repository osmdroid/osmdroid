package com.osmbonuspackdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;
import org.osmdroid.DefaultResourceProxyImpl;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Demo Activity using osmdroid and osmbonuspack
 * @see http://code.google.com/p/osmbonuspack/
 * @author M.Kergall
 *
 */
public class MapActivity extends Activity implements MapEventsReceiver, LocationListener {
	protected MapView map;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> markerOverlays;
	protected GeoPoint startPoint, destinationPoint, currentPoint;
	protected ExtendedOverlayItem markerStart, markerDestination, markerCurrent;
	SimpleLocationOverlay myLocationOverlay;

	protected PathOverlay roadOverlay;
	protected Road mRoad;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes;
	protected static final int ROUTE_REQUEST = 1;
	
	ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
	AutoCompleteTextView poiTagText;
	ArrayList<POI> mPOIs;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        MapController mapController = map.getController();
        
		//To use MapEventsReceiver methods, we add a MapEventsOverlay:
		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		map.getOverlays().add(overlay);
		
		LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 100.0f, this);
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100.0f, this);
		
		if (savedInstanceState == null){
			Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (l != null) {
				startPoint = new GeoPoint(l.getLatitude(), l.getLongitude());
			} else {
				//we put a hard-coded start
				startPoint = new GeoPoint(48.13, -1.63);
			}
			destinationPoint = null;
	        mapController.setZoom(9);
			mapController.setCenter(startPoint);
		} else {
			startPoint = savedInstanceState.getParcelable("start");
			destinationPoint = savedInstanceState.getParcelable("destination");
			mapController.setZoom(savedInstanceState.getInt("zoom_level"));
			mapController.setCenter((GeoPoint)savedInstanceState.getParcelable("map_center"));
		}
		
		myLocationOverlay = new SimpleLocationOverlay(this, new DefaultResourceProxyImpl(this));
		map.getOverlays().add(myLocationOverlay);
		myLocationOverlay.setLocation(startPoint);

		// Start and destination markers:
		final ArrayList<ExtendedOverlayItem> waypointsItems = new ArrayList<ExtendedOverlayItem>();
		markerOverlays = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
				waypointsItems, 
				map, new DefaultInfoWindow(R.layout.bonuspack_bubble_black, map));
		map.getOverlays().add(markerOverlays);
		markerStart = putMarkerItem(null, startPoint, "Start", 
				R.drawable.marker_a, R.drawable.rogger_rabbit);
		
		markerDestination = putMarkerItem(null, destinationPoint, "Destination", 
				R.drawable.marker_b, R.drawable.jessica);

		Button searchButton = (Button)findViewById(R.id.buttonSearch);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchLocationButton();
			}
		});
		
		registerForContextMenu(searchButton);
		//context menu for clicking on the map is registered on this button. 
		//(if registered on mapView, it catches map drag events)
		
		//Route and Directions
		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
    	roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, map);
		map.getOverlays().add(roadNodes);
		
		Button routeButton = (Button)findViewById(R.id.buttonRoute);
		routeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), RouteActivity.class);
				myIntent.putExtra("ROAD", mRoad);
				myIntent.putExtra("NODE_ID", roadNodes.getBubbledItemId());
				startActivityForResult(myIntent, ROUTE_REQUEST);
			}
		});
		
		if (savedInstanceState == null){
			//Test road service at first launch:
			getRoadAsync(startPoint, destinationPoint);
		} else {
			mRoad = savedInstanceState.getParcelable("road");
			updateUIWithRoad(mRoad);
		}
		
		//POIs:
        //POI search interface:
        String[] poiTags = getResources().getStringArray(R.array.poi_tags);
        poiTagText = (AutoCompleteTextView) findViewById(R.id.poiTag);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, poiTags);
        poiTagText.setAdapter(adapter);
        Button setPOITagButton = (Button) findViewById(R.id.buttonSetPOITag);
        setPOITagButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getPOIAsync(poiTagText.getText().toString());
			}
		});
        //POI markers:
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
				poiItems, map, new POIInfoWindow(map));
		map.getOverlays().add(poiMarkers);
		if (savedInstanceState == null){
			//test at first launch: get cinemas:
			//getPOIAsync(destinationPoint, "cinema");
		} else {
			mPOIs = savedInstanceState.getParcelableArrayList("poi");
			updateUIWithPOI(mPOIs);
		}
	}

	/**
	 * callback to store activity status before a restart (orientation change for instance)
	 */
	@Override protected void onSaveInstanceState (Bundle outState){
		outState.putParcelable("start", startPoint);
		outState.putParcelable("destination", destinationPoint);
		outState.putParcelable("road", mRoad);
		outState.putInt("zoom_level", map.getZoomLevel());
		GeoPoint c = (GeoPoint) map.getMapCenter();
		outState.putParcelable("map_center", c);
		outState.putParcelableArrayList("poi", mPOIs);
	}
	
	@Override protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case ROUTE_REQUEST : 
			if (resultCode == RESULT_OK) {
				int nodeId = intent.getIntExtra("NODE_ID", 0);
				map.getController().setCenter(mRoad.mNodes.get(nodeId).mLocation);
				roadNodes.showBubbleOnItem(nodeId, map);
			}
			break;
		default: 
			break;
		}
	}
	
    /**
     * Test MyItemizedOverlay object
     */
    public void putMyItemizedOverlay(GeoPoint p){
		ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
		MyItemizedOverlay myOverlays = new MyItemizedOverlay(this, list);
		OverlayItem overlayItem = new OverlayItem("Home Sweet Home", "This is the place I live", p);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		Drawable marker = getResources().getDrawable(R.drawable.marker_a);
		overlayItem.setMarker(marker);
		myOverlays.addItem(overlayItem);
		map.getOverlays().add(myOverlays);    	
		map.invalidate();
    }

    //------------- Geocoding and Reverse Geocoding
    
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
			return theAddress;
		} else {
			return "";
		}
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
				destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
				markerDestination = putMarkerItem(markerDestination, destinationPoint, 
			    		"Destination", R.drawable.marker_b, R.drawable.jessica);
				getRoadAsync(startPoint, destinationPoint);
				map.getController().setCenter(destinationPoint);
			}
		} catch (Exception e) {
			Toast.makeText(this, "Geocoding error", Toast.LENGTH_SHORT).show();
		}
	}
	
	//Async task to reverse-geocode the marker position in a separate thread:
	private class GeocodingTask extends AsyncTask<Object, Void, String> {
		ExtendedOverlayItem marker;
		protected String doInBackground(Object... params) {
			marker = (ExtendedOverlayItem)params[0];
			return getAddress(marker.getPoint());
		}
		protected void onPostExecute(String result) {
			marker.setDescription(result);
			//markerOverlays.showBubbleOnItem(1, map); //open bubble on item 1 = destination
		}
	}
	
    public ExtendedOverlayItem putMarkerItem(ExtendedOverlayItem item, GeoPoint p, String title, 
    		int markerResId, int iconResId) {
		if (item != null){
			markerOverlays.removeItem(item);
		}
		if (p != null){
			Drawable marker = getResources().getDrawable(markerResId);
			ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(title, "", p, this);
			overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
			overlayItem.setMarker(marker);
			overlayItem.setImage(getResources().getDrawable(iconResId));
			markerOverlays.addItem(overlayItem);
			map.invalidate();
			//Start geocoding task to update the description of the marker with its address:
			new GeocodingTask().execute(overlayItem);
			return overlayItem;
		} else
			return null;
    }
    
    //------------ Route and Directions
    
    private void putRoadNodes(Road road){
		roadNodes.removeAllItems();
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
    	for (int i=0; i<n; i++){
    		RoadNode node = road.mNodes.get(i);
    		String instructions = (node.mInstructions==null ? "" : node.mInstructions);
    		ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(
    				"Step " + (i+1), instructions, 
    				node.mLocation, this);
    		nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
    		nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
    		nodeMarker.setMarker(marker);
    		int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
    		if (iconId != R.drawable.ic_empty){
	    		Drawable icon = getResources().getDrawable(iconId);
	    		nodeMarker.setImage(icon);
    		}
    		roadNodes.addItem(nodeMarker);
    	}
    }
    
    void updateUIWithRoad(Road road){
		List<Overlay> mapOverlays = map.getOverlays();
		if (roadOverlay != null){
			mapOverlays.remove(roadOverlay);
		}
		if (road.mStatus == Road.STATUS_DEFAULT)
			Toast.makeText(map.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
		Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
			//we set the road overlay at the "bottom", just above the MapEventsOverlay,
			//to avoid covering the other overlays. 
		mapOverlays.add(removedOverlay);
		putRoadNodes(road);
		map.invalidate();
		//Set route info in the text view:
		((TextView)findViewById(R.id.routeInfo)).setText(road.getLengthDurationText(-1));
    }
    
	/**
	 * Async task to get the road in a separate thread. 
	 */
	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
		protected Road doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
			//RoadManager roadManager = new GoogleRoadManager();
			RoadManager roadManager = new OSRMRoadManager();
			/*
			RoadManager roadManager = new MapQuestRoadManager();
			Locale locale = Locale.getDefault();
			roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
			*/
			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) {
			mRoad = result;
			updateUIWithRoad(result);
			getPOIAsync(poiTagText.getText().toString());
		}
	}
	
	public void getRoadAsync(GeoPoint start, GeoPoint destination){
		if (destination == null)
			return;
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
		waypoints.add(start);
		//intermediate waypoints can be added here:
		//waypoints.add(new GeoPoint(48.226, -1.9456)); 
		waypoints.add(destination);
		new UpdateRoadTask().execute(waypoints);
	}

	//----------------- POIs
	
	void updateUIWithPOI(ArrayList<POI> pois){
		if (pois != null){
			for (POI poi:pois){
				ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(
					poi.mType, poi.mDescription, 
					poi.mLocation, this);
				Drawable marker = null;
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM){
					marker = getResources().getDrawable(R.drawable.marker_poi_default);
				} else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA){
					if (poi.mRank < 90)
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_16);
					else
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_32);
				} else if (poi.mServiceId == POI.POI_SERVICE_FLICKR){
					marker = getResources().getDrawable(R.drawable.marker_poi_flickr);
				}
				poiMarker.setMarker(marker);
				poiMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
				//thumbNail loading moved in POIInfoWindow.onOpen for better performances. 
				poiMarker.setRelatedObject(poi);
				poiMarkers.addItem(poiMarker);
			}
		}
		map.invalidate();
	}
	
	private class POITask extends AsyncTask<Object, Void, ArrayList<POI>> {
		String mTag;
		protected ArrayList<POI> doInBackground(Object... params) {
			mTag = (String)params[0];
			
			if (mTag == null || mTag.equals("")){
				return null;
			} else if (mTag.equals("wikipedia")){
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
				//ArrayList<POI> pois = poiProvider.getPOICloseTo(point, 75, 20.0);
				//Get POI inside the bounding box of the current map view:
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 75);
				return pois;
			} else if (mTag.equals("flickr")){
				FlickrPOIProvider poiProvider = new FlickrPOIProvider("c39be46304a6c6efda8bc066c185cd7e");
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 50);
				return pois;
			} else {
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				poiProvider.setService(NominatimPOIProvider.MAPQUEST_POI_SERVICE);
				ArrayList<POI> pois = poiProvider.getPOIAlong(
						mRoad.getRouteLow(), mTag, 100, 2.0);
				//ArrayList<POI> pois = poiProvider.getPOICloseTo(
				//		point, mTag, 100, 2.0);
				return pois;
			}
		}
		protected void onPostExecute(ArrayList<POI> pois) {
			mPOIs = pois;
			if (mTag.equals("")){
				//no search, no message
			} else if (mPOIs == null){
				Toast.makeText(getApplicationContext(), "Technical issue when getting "+mTag+ " POI.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), ""+mPOIs.size()+" "+mTag+ " entries found", Toast.LENGTH_LONG).show();
			}
			updateUIWithPOI(mPOIs);
		}
	}
	
	void getPOIAsync(String tag){
		poiMarkers.removeAllItems();
		new POITask().execute(tag);
	}
	
	//------------ MapEventsReceiver implementation

	GeoPoint tempClickedGeoPoint; //any other way to pass the position to the menu ???
	
	@Override public boolean longPressHelper(IGeoPoint p) {
		tempClickedGeoPoint = new GeoPoint((GeoPoint)p);
		Button searchButton = (Button)findViewById(R.id.buttonSearch);
		openContextMenu(searchButton); //menu is hooked on the "Search" button
		return true;
	}

	@Override public boolean singleTapUpHelper(IGeoPoint p) {
		return false;
	}

	//----------- Context Menu when clicking on the map
	@Override public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_departure:
			startPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerStart = putMarkerItem(markerStart, startPoint, 
		    		"Destination", R.drawable.marker_a, R.drawable.rogger_rabbit);
			getRoadAsync(startPoint, destinationPoint);
			return true;
		case R.id.menu_arrival:
			destinationPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerDestination = putMarkerItem(markerDestination, destinationPoint, 
		    		"Destination", R.drawable.marker_b, R.drawable.jessica);
			getRoadAsync(startPoint, destinationPoint);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	//------------ LocationListener implementation
	@Override public void onLocationChanged(final Location pLoc) {
            myLocationOverlay.setLocation(new GeoPoint(pLoc));
    }

	@Override public void onProviderDisabled(String provider) {
	}

	@Override public void onProviderEnabled(String provider) {
	}

	@Override public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}

package com.osmbonuspackdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.GoogleRoadManager;
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
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.DefaultResourceProxyImpl;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
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
	
	protected GeoPoint startPoint, destinationPoint;
	protected ArrayList<GeoPoint> viaPoints;
	protected static int START_INDEX=-2, DEST_INDEX=-1;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> itineraryMarkers; 
		//for departure, destination and viapoints
	protected ExtendedOverlayItem markerStart, markerDestination;
	SimpleLocationOverlay myLocationOverlay;

	protected PathOverlay polygonOverlay; //enclosing polygon of destination location - experimental
	
	protected Road mRoad;
	protected PathOverlay roadOverlay;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;
	protected static final int ROUTE_REQUEST = 1;
	
	ArrayList<POI> mPOIs;
	ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
	AutoCompleteTextView poiTagText;
	protected static final int POIS_REQUEST = 2;
	
	enum RouteProviderType {OSRM, MAPQUEST_FASTEST, MAPQUEST_BICYCLE, MAPQUEST_PEDESTRIAN, GOOGLE_FASTEST};
	RouteProviderType whichRouteProvider = RouteProviderType.OSRM;
	
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
			viaPoints = new ArrayList<GeoPoint>();
	        mapController.setZoom(9);
			mapController.setCenter(startPoint);
		} else {
			startPoint = savedInstanceState.getParcelable("start");
			destinationPoint = savedInstanceState.getParcelable("destination");
			viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
			mapController.setZoom(savedInstanceState.getInt("zoom_level"));
			mapController.setCenter((GeoPoint)savedInstanceState.getParcelable("map_center"));
		}
		
		DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(this);
		myLocationOverlay = new SimpleLocationOverlay(this, resourceProxy);
		map.getOverlays().add(myLocationOverlay);
		myLocationOverlay.setLocation(startPoint);
		
		ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this, resourceProxy);
		map.getOverlays().add(scaleBarOverlay);
		
		// Itinerary markers:
		final ArrayList<ExtendedOverlayItem> waypointsItems = new ArrayList<ExtendedOverlayItem>();
		itineraryMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
				waypointsItems, 
				map, new ViaPointInfoWindow(R.layout.itinerary_bubble, map));
		map.getOverlays().add(itineraryMarkers);
		updateUIWithItineraryMarkers();
		
		Button searchButton = (Button)findViewById(R.id.buttonSearch);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchLocationButton();
			}
		});
		
		registerForContextMenu(searchButton);
		//context menu for clicking on the map is registered on this button. 
		//(a little bit strange, but if we register it on mapView, it will catch map drag events)
		
		//Route and Directions
		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
    	roadNodeMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, map);
		map.getOverlays().add(roadNodeMarkers);
		
		if (savedInstanceState != null){
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
				//Hide the soft keyboard:
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(poiTagText.getWindowToken(), 0);
				//Start search:
				getPOIAsync(poiTagText.getText().toString());
			}
		});
        //POI markers:
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
				poiItems, map, new POIInfoWindow(map));
		map.getOverlays().add(poiMarkers);
		if (savedInstanceState != null){
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
		outState.putParcelableArrayList("viapoints", viaPoints);
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
				roadNodeMarkers.showBubbleOnItem(nodeId, map, true);
			}
			break;
		case POIS_REQUEST:
			if (resultCode == RESULT_OK) {
				int id = intent.getIntExtra("ID", 0);
				map.getController().setCenter(mPOIs.get(id).mLocation);
				poiMarkers.showBubbleOnItem(id, map, true);
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
		Drawable marker = getResources().getDrawable(R.drawable.marker_departure);
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
		//Hide the soft keyboard:
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(destinationEdit.getWindowToken(), 0);
		
		String destinationAddress = destinationEdit.getText().toString();
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		geocoder.setOptions(true); //ask for enclosing polygon (if any)
		try {
			List<Address> foundAdresses = geocoder.getFromLocationName(destinationAddress, 1);
			if (foundAdresses.size() == 0) { //if no address found, display an error
				Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
			} else {
				Address address = foundAdresses.get(0); //get first address
				destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
			    		R.string.destination, R.drawable.marker_destination, -1);
				getRoadAsync();
				map.getController().setCenter(destinationPoint);
				//get and display enclosing polygon:
				Bundle extras = address.getExtras();
				if (extras != null && extras.containsKey("polygonpoints")){
					ArrayList<GeoPoint> polygon = extras.getParcelableArrayList("polygonpoints");
					//Log.d("DEBUG", "polygon:"+polygon.size());
					updateUIWithPolygon(polygon);
				} else {
					updateUIWithPolygon(null);
				}
			}
		} catch (Exception e) {
			Toast.makeText(this, "Geocoding error", Toast.LENGTH_SHORT).show();
		}
	}
	
	//add or replace the polygon overlay
	public void updateUIWithPolygon(ArrayList<GeoPoint> polygon){
		List<Overlay> mapOverlays = map.getOverlays();
		int location = -1;
		if (polygonOverlay != null)
			location = mapOverlays.indexOf(polygonOverlay);
		Paint paint = new Paint();
		paint.setColor(0x80FF0080);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		polygonOverlay = new PathOverlay(0, this);
		polygonOverlay.setPaint(paint);
		if (polygon != null){
			for (GeoPoint p:polygon)
				polygonOverlay.addPoint(p);
		}
		if (location != -1)
			mapOverlays.set(location, polygonOverlay);
		else
			mapOverlays.add(polygonOverlay);
		map.invalidate();
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
			//itineraryMarkers.showBubbleOnItem(???, map); //open bubble on the item
		}
	}
	
    //------------ Itinerary markers
	
	/** add (or replace) an item in markerOverlays. p position. 
	 */
    public ExtendedOverlayItem putMarkerItem(ExtendedOverlayItem item, GeoPoint p, int index,
    		int titleResId, int markerResId, int iconResId) {
		if (item != null){
			itineraryMarkers.removeItem(item);
		}
		Drawable marker = getResources().getDrawable(markerResId);
		String title = getResources().getString(titleResId);
		ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(title, "", p, this);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		overlayItem.setMarker(marker);
		if (iconResId != -1)
			overlayItem.setImage(getResources().getDrawable(iconResId));
		overlayItem.setRelatedObject(index);
		itineraryMarkers.addItem(overlayItem);
		map.invalidate();
		//Start geocoding task to update the description of the marker with its address:
		new GeocodingTask().execute(overlayItem);
		return overlayItem;
	}

	public void addViaPoint(GeoPoint p){
		viaPoints.add(p);
		putMarkerItem(null, p, viaPoints.size()-1,
			R.string.viapoint, R.drawable.marker_via, -1);
	}
    
	public void removePoint(int index){
		if (index == START_INDEX)
			startPoint = null;
		else if (index == DEST_INDEX)
			destinationPoint = null;
		else 
			viaPoints.remove(index);
		getRoadAsync();
		updateUIWithItineraryMarkers();
	}
	
	public void updateUIWithItineraryMarkers(){
		itineraryMarkers.removeAllItems();
		//Start marker:
		if (startPoint != null){
			markerStart = putMarkerItem(null, startPoint, START_INDEX, 
				R.string.departure, R.drawable.marker_departure, -1);
		}
		//Via-points markers if any:
		for (int index=0; index<viaPoints.size(); index++){
			putMarkerItem(null, viaPoints.get(index), index, 
				R.string.viapoint, R.drawable.marker_via, -1);
		}
		//Destination marker if any:
		if (destinationPoint != null){
			markerDestination = putMarkerItem(null, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
		}
	}
	
    //------------ Route and Directions
    
    private void putRoadNodes(Road road){
		roadNodeMarkers.removeAllItems();
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
    		roadNodeMarkers.addItem(nodeMarker);
    	}
    }
    
	void updateUIWithRoad(Road road){
		roadNodeMarkers.removeAllItems();
		TextView textView = (TextView)findViewById(R.id.routeInfo);
		textView.setText("");
		List<Overlay> mapOverlays = map.getOverlays();
		if (roadOverlay != null){
			mapOverlays.remove(roadOverlay);
		}
		if (road == null)
			return;
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
		textView.setText(road.getLengthDurationText(-1));
    }
    
	/**
	 * Async task to get the road in a separate thread. 
	 */
	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
		protected Road doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
			RoadManager roadManager = null;
			Locale locale = Locale.getDefault();
			switch (whichRouteProvider){
			case OSRM:
				roadManager = new OSRMRoadManager();
				break;
			case MAPQUEST_FASTEST:
				roadManager = new MapQuestRoadManager();
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				break;
			case MAPQUEST_BICYCLE:
				roadManager = new MapQuestRoadManager();
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				roadManager.addRequestOption("routeType=bicycle");
				break;
			case MAPQUEST_PEDESTRIAN:
				roadManager = new MapQuestRoadManager();
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				roadManager.addRequestOption("routeType=pedestrian");
				break;
			case GOOGLE_FASTEST:
				roadManager = new GoogleRoadManager();
				//roadManager.addRequestOption("mode=driving"); //default
				break;
			default:
				return null;
			}
			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) {
			mRoad = result;
			updateUIWithRoad(result);
			getPOIAsync(poiTagText.getText().toString());
		}
	}
	
	public void getRoadAsync(){
		mRoad = null;
		if (startPoint == null || destinationPoint == null){
			updateUIWithRoad(mRoad);
			return;
		}
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
		waypoints.add(startPoint);
		//add intermediate via points:
		for (GeoPoint p:viaPoints){
			waypoints.add(p); 
		}
		waypoints.add(destinationPoint);
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
				} else if (poi.mServiceId == POI.POI_SERVICE_PICASA){
					marker = getResources().getDrawable(R.drawable.marker_poi_picasa_24);
					poiMarker.setSubDescription(poi.mCategory);
				}
				poiMarker.setMarker(marker);
				poiMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
				//thumbnail loading moved in POIInfoWindow.onOpen for better performances. 
				poiMarker.setRelatedObject(poi);
				poiMarkers.addItem(poiMarker);
			}
		}
		map.invalidate();
	}
	
	ExecutorService mThreadPool = Executors.newFixedThreadPool(5);
	/** Loads all thumbnails in background */
	void startAsyncThumbnailsLoading(ArrayList<POI> pois){
		/* Try to stop existing threads:
		 * not sure it has any effect... 
		if (mThreadPool != null){
			//Stop threads if any:
			mThreadPool.shutdownNow();
		}
		mThreadPool = Executors.newFixedThreadPool(5);
		*/
		for (int i=0; i<pois.size(); i++){
			final int index = i;
			final POI poi = pois.get(index);
			mThreadPool.submit(new Runnable(){
				@Override public void run(){
					Bitmap b = poi.getThumbnail();
					if (b != null){
						/*
						//Change POI marker:
						ExtendedOverlayItem item = poiMarkers.getItem(index);
						b = Bitmap.createScaledBitmap(b, 48, 48, true);
						BitmapDrawable bd = new BitmapDrawable(getResources(), b);
						item.setMarker(bd);
						*/
					}
				}
			});
		}
	}
	
	private class POITask extends AsyncTask<Object, Void, ArrayList<POI>> {
		String mTag;
		protected ArrayList<POI> doInBackground(Object... params) {
			mTag = (String)params[0];
			
			if (mTag == null || mTag.equals("")){
				return null;
			} else if (mTag.equals("wikipedia")){
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
				//ArrayList<POI> pois = poiProvider.getPOICloseTo(point, 30, 20.0);
				//Get POI inside the bounding box of the current map view:
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.equals("flickr")){
				FlickrPOIProvider poiProvider = new FlickrPOIProvider("c39be46304a6c6efda8bc066c185cd7e");
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.startsWith("picasa")){
				PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
				BoundingBoxE6 bb = map.getBoundingBox();
				//allow to search for keywords among picasa photos:
				String q = mTag.substring("picasa".length());
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30, q);
				return pois;
			} else {
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				//poiProvider.setService(NominatimPOIProvider.MAPQUEST_POI_SERVICE);
				ArrayList<POI> pois;
				if (mRoad == null){
					BoundingBoxE6 bb = map.getBoundingBox();
					pois = poiProvider.getPOIInside(bb, mTag, 100);
				} else {
					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mTag, 100, 2.0);
				}
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
				if (mTag.equals("flickr")||mTag.startsWith("picasa")||mTag.equals("wikipedia"))
					startAsyncThumbnailsLoading(mPOIs);
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
			markerStart = putMarkerItem(markerStart, startPoint, START_INDEX,
				R.string.departure, R.drawable.marker_departure, -1);
			getRoadAsync();
			return true;
		case R.id.menu_destination:
			destinationPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
			getRoadAsync();
			return true;
		case R.id.menu_viapoint:
			GeoPoint viaPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			addViaPoint(viaPoint);
			getRoadAsync();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	//------------ Option Menu implementation
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}
	
	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		if (mRoad != null && mRoad.mNodes.size()>0)
			menu.findItem(R.id.menu_itinerary).setEnabled(true);
		else 
			menu.findItem(R.id.menu_itinerary).setEnabled(false);
		if (mPOIs != null && mPOIs.size()>0)
			menu.findItem(R.id.menu_pois).setEnabled(true);
		else 
			menu.findItem(R.id.menu_pois).setEnabled(false);
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent;
		switch (item.getItemId()) {
		case R.id.menu_itinerary:
			myIntent = new Intent(this, RouteActivity.class);
			myIntent.putExtra("ROAD", mRoad);
			myIntent.putExtra("NODE_ID", roadNodeMarkers.getBubbledItemId());
			startActivityForResult(myIntent, ROUTE_REQUEST);
			return true;
		case R.id.menu_pois:
			myIntent = new Intent(this, POIActivity.class);
			myIntent.putParcelableArrayListExtra("POI", mPOIs);
			myIntent.putExtra("ID", poiMarkers.getBubbledItemId());
			startActivityForResult(myIntent, POIS_REQUEST);
			return true;
		case R.id.menu_my_position:
			GeoPoint myPosition = myLocationOverlay.getMyLocation();
			map.getController().animateTo(myPosition);
			return true;
		case R.id.menu_route_osrm:
			whichRouteProvider = RouteProviderType.OSRM;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_fastest:
			whichRouteProvider = RouteProviderType.MAPQUEST_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_bicycle:
			whichRouteProvider = RouteProviderType.MAPQUEST_BICYCLE;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_pedestrian:
			whichRouteProvider = RouteProviderType.MAPQUEST_PEDESTRIAN;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_google:
			whichRouteProvider = RouteProviderType.GOOGLE_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_tile_mapnik:
			map.setTileSource(TileSourceFactory.MAPNIK);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapquest_osm:
			map.setTileSource(TileSourceFactory.MAPQUESTOSM);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapquest_aerial:
			map.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
			item.setChecked(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	//------------ LocationListener implementation
	@Override public void onLocationChanged(final Location pLoc) {
		myLocationOverlay.setLocation(new GeoPoint(pLoc));
		//myLocationOverlay.setBearing(0.0f);
	}

	@Override public void onProviderDisabled(String provider) {
	}

	@Override public void onProviderEnabled(String provider) {
	}

	@Override public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}

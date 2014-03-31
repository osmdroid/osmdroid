package com.osmbonuspackdemo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.GridMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Demo Activity using osmdroid and OSMBonusPack
 * @see http://code.google.com/p/osmbonuspack/
 * @author M.Kergall
 *
 */
public class MapActivity extends Activity implements MapEventsReceiver, LocationListener, SensorEventListener {
	protected MapView map;
	
	protected GeoPoint startPoint, destinationPoint;
	protected ArrayList<GeoPoint> viaPoints;
	protected static int START_INDEX=-2, DEST_INDEX=-1;
	protected FolderOverlay itineraryMarkers; 
		//for departure, destination and viapoints
	protected Marker markerStart, markerDestination;
	protected ViaPointInfoWindow mViaPointInfoWindow;
	protected DirectedLocationOverlay myLocationOverlay;
	//MyLocationNewOverlay myLocationNewOverlay;
	protected LocationManager locationManager;
	//protected SensorManager mSensorManager;
	//protected Sensor mOrientation;

	protected boolean mTrackingMode;
	Button mTrackingModeButton;
	float mAzimuthAngleSpeed = 0.0f;

	protected Polygon mDestinationPolygon; //enclosing polygon of destination location
	
	public static Road mRoad; //made static to pass between activities
	protected Polyline roadOverlay;
	protected FolderOverlay roadNodeMarkers;
	protected static final int ROUTE_REQUEST = 1;
	static final int OSRM=0, MAPQUEST_FASTEST=1, MAPQUEST_BICYCLE=2, MAPQUEST_PEDESTRIAN=3, GOOGLE_FASTEST=4;
	int whichRouteProvider;
	
	public static ArrayList<POI> mPOIs; //made static to pass between activities
	GridMarkerClusterer poiMarkers;
	AutoCompleteTextView poiTagText;
	protected static final int POIS_REQUEST = 2;
	
	protected FolderOverlay mKmlOverlay; //root container of overlays from KML reading
	protected static final int KML_TREE_REQUEST = 3;
	public static KmlDocument mKmlDocument; //made static to pass between activities
	public static Stack<KmlFeature> mKmlStack; //passed between activities, top is the current KmlFeature to edit. 
	public static KmlFolder mKmlClipboard; //passed between activities. Folder for multiple items selection. 
	
	static String SHARED_PREFS_APPKEY = "OSMNavigator";
	static String PREF_LOCATIONS_KEY = "PREF_LOCATIONS";
	
	OnlineTileSourceBase MAPBOXSATELLITELABELLED;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		
		MapBoxTileSource.retrieveMapBoxMapId(this);
		MAPBOXSATELLITELABELLED = new MapBoxTileSource("MapBoxSatelliteLabelled", ResourceProxy.string.mapquest_aerial, 1, 19, 256, ".png");
		TileSourceFactory.addTileSource(MAPBOXSATELLITELABELLED);
		
		map = (MapView) findViewById(R.id.map);
		
		String tileProviderName = prefs.getString("TILE_PROVIDER", "Mapnik");
		try {
			ITileSource tileSource = TileSourceFactory.getTileSource(tileProviderName);
			map.setTileSource(tileSource);
		} catch (IllegalArgumentException e) {
			map.setTileSource(TileSourceFactory.MAPNIK);
		}
		
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		IMapController mapController = map.getController();
		
		//To use MapEventsReceiver methods, we add a MapEventsOverlay:
		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		map.getOverlays().add(overlay);
		
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		//mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		//mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		//map prefs:
		mapController.setZoom(prefs.getInt("MAP_ZOOM_LEVEL", 5));
		mapController.setCenter(new GeoPoint((double)prefs.getFloat("MAP_CENTER_LAT", 48.5f), 
				(double)prefs.getFloat("MAP_CENTER_LON", 2.5f)));
		
		myLocationOverlay = new DirectedLocationOverlay(this);
		map.getOverlays().add(myLocationOverlay);

		if (savedInstanceState == null){
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null)
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				//location known:
				onLocationChanged(location);
			} else {
				//no location known: hide myLocationOverlay
				myLocationOverlay.setEnabled(false);
			}
			startPoint = null;
			destinationPoint = null;
			viaPoints = new ArrayList<GeoPoint>();
		} else {
			myLocationOverlay.setLocation((GeoPoint)savedInstanceState.getParcelable("location"));
			//TODO: restore other aspects of myLocationOverlay...
			startPoint = savedInstanceState.getParcelable("start");
			destinationPoint = savedInstanceState.getParcelable("destination");
			viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
		}
		
		//ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
		//map.getOverlays().add(scaleBarOverlay);
		
		// Itinerary markers:
		itineraryMarkers = new FolderOverlay(this);
		map.getOverlays().add(itineraryMarkers);
		mViaPointInfoWindow = new ViaPointInfoWindow(R.layout.itinerary_bubble, map);
		updateUIWithItineraryMarkers();
		
		//Tracking system:
		mTrackingModeButton = (Button)findViewById(R.id.buttonTrackingMode);
		mTrackingModeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mTrackingMode = !mTrackingMode;
				updateUIWithTrackingMode();
			}
		});
		if (savedInstanceState != null){
			mTrackingMode = savedInstanceState.getBoolean("tracking_mode");
			updateUIWithTrackingMode();
		} else 
			mTrackingMode = false;
		
		AutoCompleteOnPreferences departureText = (AutoCompleteOnPreferences) findViewById(R.id.editDeparture);
		departureText.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
		
		Button searchDepButton = (Button)findViewById(R.id.buttonSearchDep);
		searchDepButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchButton(START_INDEX, R.id.editDeparture);
			}
		});
		
		AutoCompleteOnPreferences destinationText = (AutoCompleteOnPreferences) findViewById(R.id.editDestination);
		destinationText.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
		
		Button searchDestButton = (Button)findViewById(R.id.buttonSearchDest);
		searchDestButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchButton(DEST_INDEX, R.id.editDestination);
			}
		});
		
		View expander = (View)findViewById(R.id.expander);
		expander.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				View searchPanel = (View)findViewById(R.id.search_panel);
				if (searchPanel.getVisibility() == View.VISIBLE){
					searchPanel.setVisibility(View.GONE);
				} else {
					searchPanel.setVisibility(View.VISIBLE);
				}
			}
		});
		View searchPanel = (View)findViewById(R.id.search_panel);
		searchPanel.setVisibility(prefs.getInt("PANEL_VISIBILITY", View.VISIBLE));

		registerForContextMenu(searchDestButton);
		//context menu for clicking on the map is registered on this button. 
		//(a little bit strange, but if we register it on mapView, it will catch map drag events)
		
		//Route and Directions
		whichRouteProvider = prefs.getInt("ROUTE_PROVIDER", OSRM);
		
    	roadNodeMarkers = new FolderOverlay(this);
		map.getOverlays().add(roadNodeMarkers);
		
		if (savedInstanceState != null){
			//STATIC mRoad = savedInstanceState.getParcelable("road");
			updateUIWithRoad(mRoad);
		}
		
		//POIs:
		//POI search interface:
		String[] poiTags = getResources().getStringArray(R.array.poi_tags);
		poiTagText = (AutoCompleteTextView) findViewById(R.id.poiTag);
		ArrayAdapter<String> poiAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, poiTags);
		poiTagText.setAdapter(poiAdapter);
		Button setPOITagButton = (Button) findViewById(R.id.buttonSetPOITag);
		setPOITagButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Hide the soft keyboard:
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(poiTagText.getWindowToken(), 0);
				//Start search:
				String feature = poiTagText.getText().toString();
				if (!feature.equals(""))
					Toast.makeText(v.getContext(), "Searching:\n"+feature, Toast.LENGTH_LONG).show();
				getPOIAsync(feature);
			}
		});
		//POI markers:
		poiMarkers = new GridMarkerClusterer(this);
		Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_poi_cluster);
		Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
		poiMarkers.setIcon(clusterIcon);
		poiMarkers.mAnchorV = Marker.ANCHOR_BOTTOM;
		poiMarkers.mTextAnchorU = 0.70f;
		poiMarkers.mTextAnchorV = 0.27f;
		poiMarkers.getTextPaint().setTextSize(12.0f);
		map.getOverlays().add(poiMarkers);
		if (savedInstanceState != null){
			//STATIC - mPOIs = savedInstanceState.getParcelableArrayList("poi");
			updateUIWithPOI(mPOIs);
		}

		//KML handling:
		mKmlOverlay = null;
		if (savedInstanceState != null){
			//STATIC - mKmlDocument = savedInstanceState.getParcelable("kml");
			updateUIWithKml();
		} else { //first launch: 
			mKmlDocument = new KmlDocument();
			mKmlStack = new Stack<KmlFeature>();
			mKmlClipboard = new KmlFolder();
			//check if intent has been passed with a kml URI to load (url or file)
			Intent onCreateIntent = getIntent();
			if (onCreateIntent.getAction().equals(Intent.ACTION_VIEW)){
				String uri = onCreateIntent.getDataString();
				openFile(uri, true);
			}
		}
	}

	void setViewOn(BoundingBoxE6 bb){
		if (bb != null){
			map.zoomToBoundingBox(bb);
		}
	}
	
	void savePrefs(){
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt("MAP_ZOOM_LEVEL", map.getZoomLevel());
		GeoPoint c = (GeoPoint) map.getMapCenter();
		ed.putFloat("MAP_CENTER_LAT", c.getLatitudeE6()*1E-6f);
		ed.putFloat("MAP_CENTER_LON", c.getLongitudeE6()*1E-6f);
		MapTileProviderBase tileProvider = map.getTileProvider();
		String tileProviderName = tileProvider.getTileSource().name();
		View searchPanel = (View)findViewById(R.id.search_panel);
		ed.putInt("PANEL_VISIBILITY", searchPanel.getVisibility());
		ed.putString("TILE_PROVIDER", tileProviderName);
		ed.putInt("ROUTE_PROVIDER", whichRouteProvider);
		ed.commit();
	}
	
	/**
	 * callback to store activity status before a restart (orientation change for instance)
	 */
	@Override protected void onSaveInstanceState (Bundle outState){
		outState.putParcelable("location", myLocationOverlay.getLocation());
		outState.putBoolean("tracking_mode", mTrackingMode);
		outState.putParcelable("start", startPoint);
		outState.putParcelable("destination", destinationPoint);
		outState.putParcelableArrayList("viapoints", viaPoints);
		//STATIC - outState.putParcelable("road", mRoad);
		//STATIC - outState.putParcelableArrayList("poi", mPOIs);
		//STATIC - outState.putParcelable("kml", mKmlDocument);
		
		savePrefs();
	}
	
	@Override protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case ROUTE_REQUEST : 
			if (resultCode == RESULT_OK) {
				int nodeId = intent.getIntExtra("NODE_ID", 0);
				map.getController().setCenter(mRoad.mNodes.get(nodeId).mLocation);
				Marker roadMarker = (Marker)roadNodeMarkers.getItems().get(nodeId);
				roadMarker.showInfoWindow();
			}
			break;
		case POIS_REQUEST:
			if (resultCode == RESULT_OK) {
				int id = intent.getIntExtra("ID", 0);
				map.getController().setCenter(mPOIs.get(id).mLocation);
				Marker poiMarker = (Marker)poiMarkers.getItem(id);
				poiMarker.showInfoWindow();
			}
			break;
		case KML_TREE_REQUEST:
			KmlFolder result = (KmlFolder)mKmlStack.pop();
			if (resultCode == RESULT_OK) {
				//use the object which has been modified in KmlTreeActivity:
				mKmlDocument.mKmlRoot = result; //intent.getParcelableExtra("KML");
				updateUIWithKml();
			}
			break;
		default: 
			break;
		}
	}
	
	/* String getBestProvider(){
		String bestProvider = null;
		//bestProvider = locationManager.getBestProvider(new Criteria(), true); // => returns "Network Provider"! 
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			bestProvider = LocationManager.GPS_PROVIDER;
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			bestProvider = LocationManager.NETWORK_PROVIDER;
		return bestProvider;
	} */
	
	boolean startLocationUpdates(){
		boolean result = false;
		for (final String provider : locationManager.getProviders(true)) {
			locationManager.requestLocationUpdates(provider, 2*1000, 0.0f, this);
			result = true;
		}
		return result;
	}

	@Override protected void onResume() {
		super.onResume();
		boolean isOneProviderEnabled = startLocationUpdates();
		myLocationOverlay.setEnabled(isOneProviderEnabled);
		//TODO: not used currently
		//mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
			//sensor listener is causing a high CPU consumption...
	}

	@Override protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		//TODO: mSensorManager.unregisterListener(this);
		savePrefs();
	}

    void updateUIWithTrackingMode(){
		if (mTrackingMode){
			mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_on);
			if (myLocationOverlay.isEnabled()&& myLocationOverlay.getLocation() != null){
				map.getController().animateTo(myLocationOverlay.getLocation());
			}
			map.setMapOrientation(-mAzimuthAngleSpeed);
			mTrackingModeButton.setKeepScreenOn(true);
		} else {
			mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_off);
			map.setMapOrientation(0.0f);
			mTrackingModeButton.setKeepScreenOn(false);
		}
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
     * Geocoding of the departure or destination address
     */
	public void handleSearchButton(int index, int editResId){
		EditText locationEdit = (EditText)findViewById(editResId);
		//Hide the soft keyboard:
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(locationEdit.getWindowToken(), 0);
		
		String locationAddress = locationEdit.getText().toString();
		
		if (locationAddress.equals("")){
			removePoint(index);
			map.invalidate();
			return;
		}
		
		Toast.makeText(this, "Searching:\n"+locationAddress, Toast.LENGTH_LONG).show();
		AutoCompleteOnPreferences.storePreference(this, locationAddress, SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		geocoder.setOptions(true); //ask for enclosing polygon (if any)
		try {
			List<Address> foundAdresses = geocoder.getFromLocationName(locationAddress, 1);
			if (foundAdresses.size() == 0) { //if no address found, display an error
				Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
			} else {
				Address address = foundAdresses.get(0); //get first address
				if (index == START_INDEX){
					startPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerStart = updateItineraryMarker(markerStart, startPoint, START_INDEX,
						R.string.departure, R.drawable.marker_departure, -1);
					map.getController().setCenter(startPoint);
				} else if (index == DEST_INDEX){
					destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
						R.string.destination, R.drawable.marker_destination, -1);
					map.getController().setCenter(destinationPoint);
				}
				getRoadAsync();
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
		if (mDestinationPolygon != null)
			location = mapOverlays.indexOf(mDestinationPolygon);
		mDestinationPolygon = new Polygon(this);
		mDestinationPolygon.setFillColor(0x30FF0080);
		mDestinationPolygon.setStrokeColor(0x800000FF);
		mDestinationPolygon.setStrokeWidth(5.0f);
		BoundingBoxE6 bb = null;
		if (polygon != null){
			mDestinationPolygon.setPoints(polygon);
			bb = BoundingBoxE6.fromGeoPoints(polygon);
		}
		if (location != -1)
			mapOverlays.set(location, mDestinationPolygon);
		else
			mapOverlays.add(mDestinationPolygon);
		if (bb != null)
			setViewOn(bb);
		map.invalidate();
	}
	
	//Async task to reverse-geocode the marker position in a separate thread:
	private class GeocodingTask extends AsyncTask<Object, Void, String> {
		Marker marker;
		protected String doInBackground(Object... params) {
			marker = (Marker)params[0];
			return getAddress(marker.getPosition());
		}
		protected void onPostExecute(String result) {
			marker.setSnippet(result);
			marker.showInfoWindow();
		}
	}
	
    //------------ Itinerary markers

	class OnItineraryMarkerDragListener implements OnMarkerDragListener {
		@Override public void onMarkerDrag(Marker marker) {}
		@Override public void onMarkerDragEnd(Marker marker) {
			int index = (Integer)marker.getRelatedObject();
			if (index == START_INDEX)
				startPoint = marker.getPosition();
			else if (index == DEST_INDEX)
				destinationPoint = marker.getPosition();
			else 
				viaPoints.set(index, marker.getPosition());
			//update location:
			new GeocodingTask().execute(marker);
			//update route:
			getRoadAsync();
		}
		@Override public void onMarkerDragStart(Marker marker) {}		
	}
	
	final OnItineraryMarkerDragListener mItineraryListener = new OnItineraryMarkerDragListener();
	
	/** Update (or create if null) a marker in itineraryMarkers. */
    public Marker updateItineraryMarker(Marker item, GeoPoint p, int index,
    		int titleResId, int markerResId, int imageResId) {
		Drawable icon = getResources().getDrawable(markerResId);
		String title = getResources().getString(titleResId);
		if (item == null){
			item = new Marker(map);
			item.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			item.setInfoWindow(mViaPointInfoWindow);
			item.setDraggable(true);
			item.setOnMarkerDragListener(mItineraryListener);
			itineraryMarkers.add(item);
		}
		item.setTitle(title);
		item.setPosition(p);
		item.setIcon(icon);
		if (imageResId != -1)
			item.setImage(getResources().getDrawable(imageResId));
		item.setRelatedObject(index);
		map.invalidate();
		//Start geocoding task to update the description of the marker with its address:
		new GeocodingTask().execute(item);
		return item;
	}

	public void addViaPoint(GeoPoint p){
		viaPoints.add(p);
		updateItineraryMarker(null, p, viaPoints.size()-1,
			R.string.viapoint, R.drawable.marker_via, -1);
	}
    
	public void removePoint(int index){
		if (index == START_INDEX){
			startPoint = null;
			if (markerStart != null){
				itineraryMarkers.remove(markerStart);
				markerStart = null;
			}
		} else if (index == DEST_INDEX){
			destinationPoint = null;
			if (markerDestination != null){
				itineraryMarkers.remove(markerDestination);
				markerDestination = null;
			}
		} else {
			viaPoints.remove(index);
			updateUIWithItineraryMarkers();
		}
		getRoadAsync();
	}
	
	public void updateUIWithItineraryMarkers(){
		itineraryMarkers.getItems().clear();
		//Start marker:
		if (startPoint != null){
			markerStart = updateItineraryMarker(null, startPoint, START_INDEX, 
				R.string.departure, R.drawable.marker_departure, -1);
		}
		//Via-points markers if any:
		for (int index=0; index<viaPoints.size(); index++){
			updateItineraryMarker(null, viaPoints.get(index), index, 
				R.string.viapoint, R.drawable.marker_via, -1);
		}
		//Destination marker if any:
		if (destinationPoint != null){
			markerDestination = updateItineraryMarker(null, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
		}
	}
	
    //------------ Route and Directions
    
    private void putRoadNodes(Road road){
		roadNodeMarkers.getItems().clear();
		Drawable icon = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
		MarkerInfoWindow infoWindow = new MarkerInfoWindow(R.layout.bonuspack_bubble, map);
		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
    	for (int i=0; i<n; i++){
    		RoadNode node = road.mNodes.get(i);
    		String instructions = (node.mInstructions==null ? "" : node.mInstructions);
    		Marker nodeMarker = new Marker(map);
    		nodeMarker.setTitle("Step " + (i+1));
    		nodeMarker.setSnippet(instructions);
    		nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
    		nodeMarker.setPosition(node.mLocation);
    		nodeMarker.setIcon(icon);
    		nodeMarker.setInfoWindow(infoWindow); //use a shared infowindow. 
    		int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
    		if (iconId != R.drawable.ic_empty){
	    		Drawable image = getResources().getDrawable(iconId);
	    		nodeMarker.setImage(image);
    		}
    		roadNodeMarkers.add(nodeMarker);
    	}
    	iconIds.recycle();
    }
    
	void updateUIWithRoad(Road road){
		roadNodeMarkers.getItems().clear();
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
				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				break;
			case MAPQUEST_BICYCLE:
				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				roadManager.addRequestOption("routeType=bicycle");
				break;
			case MAPQUEST_PEDESTRIAN:
				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
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
		GeoPoint roadStartPoint = null;
		if (startPoint != null){
			roadStartPoint = startPoint;
		} else if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null){
			//use my current location as itinerary start point:
			roadStartPoint = myLocationOverlay.getLocation();
		}
		if (roadStartPoint == null || destinationPoint == null){
			updateUIWithRoad(mRoad);
			return;
		}
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
		waypoints.add(roadStartPoint);
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
			POIInfoWindow poiInfoWindow = new POIInfoWindow(map);
			for (POI poi:pois){
				Marker poiMarker = new Marker(map);
				poiMarker.setTitle(poi.mType);
				poiMarker.setSnippet(poi.mDescription);
				poiMarker.setPosition(poi.mLocation);
				Drawable icon = null;
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM){
					icon = getResources().getDrawable(R.drawable.marker_poi);
				} else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA){
					if (poi.mRank < 90)
						icon = getResources().getDrawable(R.drawable.marker_poi_wikipedia_16);
					else
						icon = getResources().getDrawable(R.drawable.marker_poi_wikipedia_32);
				} else if (poi.mServiceId == POI.POI_SERVICE_FLICKR){
					icon = getResources().getDrawable(R.drawable.marker_poi_flickr);
				} else if (poi.mServiceId == POI.POI_SERVICE_PICASA){
					icon = getResources().getDrawable(R.drawable.marker_poi_picasa_24);
					poiMarker.setSubDescription(poi.mCategory);
				}
				poiMarker.setIcon(icon);
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM){
					poiMarker.setAnchor(Marker.ANCHOR_CENTER, 1.0f);
				}
				poiMarker.setRelatedObject(poi);
				poiMarker.setInfoWindow(poiInfoWindow);
				//thumbnail loading moved in async task for better performances. 
				poiMarkers.add(poiMarker);
			}
		}
		poiMarkers.invalidate();
		map.invalidate();
	}
	
	void setMarkerIconAsPhoto(Marker marker, Bitmap thumbnail){
		int borderSize = 2;
		thumbnail = Bitmap.createScaledBitmap(thumbnail, 48, 48, true);
	    Bitmap withBorder = Bitmap.createBitmap(thumbnail.getWidth() + borderSize * 2, thumbnail.getHeight() + borderSize * 2, thumbnail.getConfig());
	    Canvas canvas = new Canvas(withBorder);
	    canvas.drawColor(Color.WHITE);
	    canvas.drawBitmap(thumbnail, borderSize, borderSize, null);
		BitmapDrawable icon = new BitmapDrawable(getResources(), withBorder);
		marker.setIcon(icon);
	}
	
	ExecutorService mThreadPool = Executors.newFixedThreadPool(3);
	
	class ThumbnailLoaderTask implements Runnable {
		POI mPoi; Marker mMarker;
		ThumbnailLoaderTask(POI poi, Marker marker){
			mPoi = poi; mMarker = marker;
		}
		@Override public void run(){
			Bitmap thumbnail = mPoi.getThumbnail();
			if (thumbnail != null){
				setMarkerIconAsPhoto(mMarker, thumbnail);
			}
		}
	}
	
	/** Loads all thumbnails in background */
	void startAsyncThumbnailsLoading(ArrayList<POI> pois){
		if (pois == null)
			return;
		//Try to stop existing threads:
		mThreadPool.shutdownNow();
		mThreadPool = Executors.newFixedThreadPool(3);
		for (int i=0; i<pois.size(); i++){
			final POI poi = pois.get(i);
			final Marker marker = (Marker)poiMarkers.getItem(i);
			mThreadPool.submit(new ThumbnailLoaderTask(poi, marker));
		}
	}
	
	private class POILoadingTask extends AsyncTask<Object, Void, ArrayList<POI>> {
		String mTag;
		protected ArrayList<POI> doInBackground(Object... params) {
			mTag = (String)params[0];
			
			if (mTag == null || mTag.equals("")){
				return null;
			} else if (mTag.equals("wikipedia")){
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
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
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 50, q);
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
			}
			updateUIWithPOI(mPOIs);
			if (mTag.equals("flickr")||mTag.startsWith("picasa")||mTag.equals("wikipedia"))
				startAsyncThumbnailsLoading(mPOIs);
		}
	}
	
	void getPOIAsync(String tag){
		poiMarkers.getItems().clear();
		new POILoadingTask().execute(tag);
	}
	
	//------------ KML handling

	boolean mDialogForOpen;
	String mLocalFileName = "current.kml";
	
	void openLocalFileDialog(boolean open){
		mDialogForOpen = open;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("File (.kml or .json)");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setText(mLocalFileName);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				mLocalFileName = input.getText().toString();
				dialog.cancel();
				if (mDialogForOpen){
					File file = mKmlDocument.getDefaultPathForAndroid(mLocalFileName);
					openFile("file:/"+file.toString(), false);
				} else 
					saveFile(mLocalFileName);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	void openUrlDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("KML url");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String defaultUri = "http://mapsengine.google.com/map/kml?mid=z6IJfj90QEd4.kUUY9FoHFRdE";
		//String defaultUri = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=52.215676&flon=5.963946&tlat=52.2573&tlon=6.1799";
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		String uri = prefs.getString("KML_URI", defaultUri);
		input.setText(uri);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			@Override public void onClick(DialogInterface dialog, int which) {
				String uri = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				SharedPreferences.Editor ed = prefs.edit();
				ed.putString("KML_URI", uri);
				ed.commit();
				dialog.cancel();
				openFile(uri, false);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	ProgressDialog createSpinningDialog(String title){
		ProgressDialog pd = new ProgressDialog(map.getContext());
		pd.setTitle(title);
		pd.setMessage("Please wait.");
		pd.setCancelable(false);
		pd.setIndeterminate(true);
		return pd;
	}
	
	class KmlLoadingTask extends AsyncTask<Object, Void, Boolean>{
		String mUri;
		boolean mOnCreate;
		ProgressDialog mPD;
		String mMessage;
		KmlLoadingTask(String message){
			super();
			mMessage = message;
		}
		@Override protected void onPreExecute() {
			mPD = createSpinningDialog(mMessage);
			mPD.show();
		}
		@Override protected Boolean doInBackground(Object... params) {
			mUri = (String)params[0];
			mOnCreate = (Boolean)params[1];
			mKmlDocument = new KmlDocument();
			boolean ok = false;
			if (mUri.startsWith("file:/")){
				mUri = mUri.substring("file:/".length());
				File file = new File(mUri);
				if (mUri.endsWith(".json"))
					ok = mKmlDocument.parseGeoJSON(file);
				else //assume KML
					ok = mKmlDocument.parseFile(file);
			} else if (mUri.startsWith("http")) {
				ok = mKmlDocument.parseUrl(mUri);
			}
			return ok;
		}
		@Override protected void onPostExecute(Boolean ok) {
			if (mPD != null)
				mPD.dismiss();
			if (!ok)
				Toast.makeText(getApplicationContext(), "Sorry, unable to read "+mUri, Toast.LENGTH_SHORT).show();
			updateUIWithKml();
			if (mKmlDocument.mKmlRoot != null && mKmlDocument.mKmlRoot.mBB != null){
					if (!mOnCreate)
						setViewOn(mKmlDocument.mKmlRoot.mBB); 
					else  //KO in onCreate - Workaround:
						map.getController().setCenter(new GeoPoint(
								mKmlDocument.mKmlRoot.mBB.getLatSouthE6()+mKmlDocument.mKmlRoot.mBB.getLatitudeSpanE6()/2, 
								mKmlDocument.mKmlRoot.mBB.getLonWestE6()+mKmlDocument.mKmlRoot.mBB.getLongitudeSpanE6()/2));
			}
		}
	}
	
	void openFile(String uri, boolean onCreate){
		//Toast.makeText(this, "Loading "+uri, Toast.LENGTH_SHORT).show();
		new KmlLoadingTask("Loading "+uri).execute(uri, onCreate);
	}
	
	/** save fileName locally, as KML or GeoJSON depending on the extension */
	void saveFile(String fileName){
		boolean result;
		File file = mKmlDocument.getDefaultPathForAndroid(fileName);
		if (fileName.endsWith(".json"))
			result = mKmlDocument.saveAsGeoJSON(file);
		else
			result = mKmlDocument.saveAsKML(file);
		if (result)
			Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
		else 
			Toast.makeText(this, "Unable to save "+fileName, Toast.LENGTH_SHORT).show();
	}
	
	Style buildDefaultStyle(){
		Drawable defaultKmlMarker = getResources().getDrawable(R.drawable.marker_kml_point);
		Bitmap bitmap = ((BitmapDrawable)defaultKmlMarker).getBitmap();
		Style defaultStyle = new Style(bitmap, 0x901010AA, 3.0f, 0x20AA1010);
		return defaultStyle;
	}
	
	void updateUIWithKml(){
		if (mKmlOverlay != null)
			map.getOverlays().remove(mKmlOverlay);
		if (mKmlDocument.mKmlRoot != null){
			mKmlOverlay = (FolderOverlay)mKmlDocument.mKmlRoot.buildOverlay(map, buildDefaultStyle(), null, mKmlDocument);
			map.getOverlays().add(mKmlOverlay);
		}
		map.invalidate();
	}
	
	void insertOverlaysInKml(){
		//Ensure the root exist:
		if (mKmlDocument.mKmlRoot == null){
			mKmlDocument.mKmlRoot = new KmlFolder();
		}
		KmlFolder root = mKmlDocument.mKmlRoot;
		//Insert relevant overlays inside:
		if (itineraryMarkers.getItems().size()>0)
			root.addOverlay(itineraryMarkers, mKmlDocument);
		root.addOverlay(roadOverlay, mKmlDocument);
		if (roadNodeMarkers.getItems().size()>0)
			root.addOverlay(roadNodeMarkers, mKmlDocument);
		root.addOverlay(mDestinationPolygon, mKmlDocument);
		if (poiMarkers.getItems().size()>0)
			root.addOverlay(poiMarkers, mKmlDocument);
	}
	
	void addKmlPoint(GeoPoint position){
		//Ensure the root exist:
		if (mKmlDocument.mKmlRoot == null){
			mKmlDocument.mKmlRoot = new KmlFolder();
		}
		KmlFeature kmlPoint = new KmlPlacemark(position);
		mKmlDocument.mKmlRoot.add(kmlPoint);
		updateUIWithKml();
	}
	
	//------------ MapEventsReceiver implementation

	GeoPoint mClickedGeoPoint; //any other way to pass the position to the menu ???
	
	@Override public boolean longPressHelper(GeoPoint p) {
		mClickedGeoPoint = p;
		Button searchButton = (Button)findViewById(R.id.buttonSearchDest);
		openContextMenu(searchButton); 
			//menu is hooked on the "Search Destination" button, as it must be hooked somewhere. 
		return true;
	}

	@Override public boolean singleTapConfirmedHelper(GeoPoint p) {
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
			startPoint = new GeoPoint(mClickedGeoPoint);
			markerStart = updateItineraryMarker(markerStart, startPoint, START_INDEX,
				R.string.departure, R.drawable.marker_departure, -1);
			getRoadAsync();
			return true;
		case R.id.menu_destination:
			destinationPoint = new GeoPoint(mClickedGeoPoint);
			markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
			getRoadAsync();
			return true;
		case R.id.menu_viapoint:
			GeoPoint viaPoint = new GeoPoint(mClickedGeoPoint);
			addViaPoint(viaPoint);
			getRoadAsync();
			return true;
		case R.id.menu_kmlpoint:
			GeoPoint kmlPoint = new GeoPoint(mClickedGeoPoint);
			addKmlPoint(kmlPoint);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	//------------ Option Menu implementation
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		
		switch (whichRouteProvider){
		case OSRM: 
			menu.findItem(R.id.menu_route_osrm).setChecked(true);
			break;
		case MAPQUEST_FASTEST:
			menu.findItem(R.id.menu_route_mapquest_fastest).setChecked(true);
			break;
		case MAPQUEST_BICYCLE:
			menu.findItem(R.id.menu_route_mapquest_bicycle).setChecked(true);
			break;
		case MAPQUEST_PEDESTRIAN:
			menu.findItem(R.id.menu_route_mapquest_pedestrian).setChecked(true);
			break;
		case GOOGLE_FASTEST:
			menu.findItem(R.id.menu_route_google).setChecked(true);
			break;
		}
		
		if (map.getTileProvider().getTileSource() == TileSourceFactory.MAPNIK)
			menu.findItem(R.id.menu_tile_mapnik).setChecked(true);
		else if (map.getTileProvider().getTileSource() == TileSourceFactory.MAPQUESTOSM)
			menu.findItem(R.id.menu_tile_mapquest_osm).setChecked(true);
		else if (map.getTileProvider().getTileSource() == MAPBOXSATELLITELABELLED)
			menu.findItem(R.id.menu_tile_mapbox_satellite).setChecked(true);
		
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
			myIntent.putExtra("NODE_ID", -1 /*TODO - default to roadNodeMarkers.getBubbledItemId()*/);
			startActivityForResult(myIntent, ROUTE_REQUEST);
			return true;
		case R.id.menu_pois:
			myIntent = new Intent(this, POIActivity.class);
			myIntent.putExtra("ID", -1 /*TODO - default to poiMarkers.getBubbledItemId()*/);
			startActivityForResult(myIntent, POIS_REQUEST);
			return true;
		case R.id.menu_kml_url:
			openUrlDialog();
			return true;
		case R.id.menu_open_file:
			openLocalFileDialog(true);
			return true;
		case R.id.menu_kml_get_overlays:
			insertOverlaysInKml();
			updateUIWithKml();
			return true;
		case R.id.menu_kml_tree:
			if (mKmlDocument.mKmlRoot==null)
				return false;
			myIntent = new Intent(this, KmlTreeActivity.class);
			//myIntent.putExtra("KML", mKmlDocument.kmlRoot);
			mKmlStack.push(mKmlDocument.mKmlRoot.clone());
			startActivityForResult(myIntent, KML_TREE_REQUEST);
			return true;
		case R.id.menu_save_file:
			openLocalFileDialog(false);
			return true;
		case R.id.menu_kml_clear:
			mKmlDocument = new KmlDocument();
			updateUIWithKml();
			return true;
		case R.id.menu_route_osrm:
			whichRouteProvider = OSRM;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_fastest:
			whichRouteProvider = MAPQUEST_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_bicycle:
			whichRouteProvider = MAPQUEST_BICYCLE;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_pedestrian:
			whichRouteProvider = MAPQUEST_PEDESTRIAN;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_google:
			whichRouteProvider = GOOGLE_FASTEST;
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
		case R.id.menu_tile_mapbox_satellite:
			map.setTileSource(MAPBOXSATELLITELABELLED);
			item.setChecked(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	//------------ LocationListener implementation
	private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
	long mLastTime = 0; // milliseconds
	double mSpeed = 0.0; // km/h
	@Override public void onLocationChanged(final Location pLoc) {
		long currentTime = System.currentTimeMillis();
		if (mIgnorer.shouldIgnore(pLoc.getProvider(), currentTime))
            return;
		double dT = currentTime - mLastTime;
		if (dT < 100.0){
			//Toast.makeText(this, pLoc.getProvider()+" dT="+dT, Toast.LENGTH_SHORT).show();
			return;
		}
		mLastTime = currentTime;
		
		GeoPoint newLocation = new GeoPoint(pLoc);
		if (!myLocationOverlay.isEnabled()){
			//we get the location for the first time:
			myLocationOverlay.setEnabled(true);
			map.getController().animateTo(newLocation);
		}
		
		GeoPoint prevLocation = myLocationOverlay.getLocation();
		myLocationOverlay.setLocation(newLocation);
		myLocationOverlay.setAccuracy((int)pLoc.getAccuracy());

		if (prevLocation != null && pLoc.getProvider().equals(LocationManager.GPS_PROVIDER)){
			/*
			double d = prevLocation.distanceTo(newLocation);
			mSpeed = d/dT*1000.0; // m/s
			mSpeed = mSpeed * 3.6; //km/h
			*/
			mSpeed = pLoc.getSpeed() * 3.6;
			long speedInt = Math.round(mSpeed);
			TextView speedTxt = (TextView)findViewById(R.id.speed);
			speedTxt.setText(speedInt + " km/h");
			
			//TODO: check if speed is not too small
			if (mSpeed >= 0.1){
				//mAzimuthAngleSpeed = (float)prevLocation.bearingTo(newLocation);
				mAzimuthAngleSpeed = (float)pLoc.getBearing();
				myLocationOverlay.setBearing(mAzimuthAngleSpeed);
			}
		}
		
		if (mTrackingMode){
			//keep the map view centered on current location:
			map.getController().animateTo(newLocation);
			map.setMapOrientation(-mAzimuthAngleSpeed);
		} else {
			//just redraw the location overlay:
			map.invalidate();
		}
	}

	@Override public void onProviderDisabled(String provider) {}

	@Override public void onProviderEnabled(String provider) {}

	@Override public void onStatusChanged(String provider, int status, Bundle extras) {}

	//------------ SensorEventListener implementation
	@Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
		myLocationOverlay.setAccuracy(accuracy);
		map.invalidate();
	}

	static float mAzimuthOrientation = 0.0f;
	@Override public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()){
			case Sensor.TYPE_ORIENTATION: 
				if (mSpeed < 0.1){
					/* TODO Filter to implement...
					float azimuth = event.values[0];
					if (Math.abs(azimuth-mAzimuthOrientation)>2.0f){
						mAzimuthOrientation = azimuth;
						myLocationOverlay.setBearing(mAzimuthOrientation);
						if (mTrackingMode)
							map.setMapOrientation(-mAzimuthOrientation);
						else
							map.invalidate();
					}
					*/
				}
				//at higher speed, we use speed vector, not phone orientation. 
				break;
			default:
				break;
		}
	}
}

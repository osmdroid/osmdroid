package com.osmnavigator;

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
import android.graphics.DashPathEffect;
import android.graphics.Paint;
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
import android.os.Environment;
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

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.cachemanager.CacheManager;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.mapsforge.GenericMapView;
import org.osmdroid.bonuspack.mapsforge.MapsForgeTileProvider;
import org.osmdroid.bonuspack.overlays.BasicInfoWindow;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.ManifestUtil;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple and general-purpose map/navigation Android application, including a KML viewer and editor. 
 * It is based on osmdroid and OSMBonusPack
 * @see https://github.com/MKergall/osmbonuspack
 * @author M.Kergall
 *
 */
public class MapActivity extends Activity implements MapEventsReceiver, LocationListener, SensorEventListener {
	protected MapView map;
	
	protected GeoPoint startPoint, destinationPoint;
	protected ArrayList<GeoPoint> viaPoints;
	protected static int START_INDEX=-2, DEST_INDEX=-1;
	protected FolderOverlay mItineraryMarkers;
		//for departure, destination and viapoints
	protected Marker markerStart, markerDestination;
	protected ViaPointInfoWindow mViaPointInfoWindow;
	protected DirectedLocationOverlay myLocationOverlay;
	//MyLocationNewOverlay myLocationNewOverlay;
	protected LocationManager mLocationManager;
	//protected SensorManager mSensorManager;
	//protected Sensor mOrientation;

	protected boolean mTrackingMode;
	Button mTrackingModeButton;
	float mAzimuthAngleSpeed = 0.0f;

	protected Polygon mDestinationPolygon; //enclosing polygon of destination location
	
	public static Road[] mRoads;  //made static to pass between activities
	protected int mSelectedRoad;
	protected Polyline[] mRoadOverlays;
	protected FolderOverlay mRoadNodeMarkers;
	protected static final int ROUTE_REQUEST = 1;
	static final int OSRM=0, GRAPHHOPPER_FASTEST=1, GRAPHHOPPER_BICYCLE=2, GRAPHHOPPER_PEDESTRIAN=3, GOOGLE_FASTEST=4;
	int mWhichRouteProvider;
	
	public static ArrayList<POI> mPOIs; //made static to pass between activities
	RadiusMarkerClusterer mPoiMarkers;
	AutoCompleteTextView poiTagText;
	protected static final int POIS_REQUEST = 2;
	
	protected FolderOverlay mKmlOverlay; //root container of overlays from KML reading
	public static KmlDocument mKmlDocument; //made static to pass between activities
	public static Stack<KmlFeature> mKmlStack; //passed between activities, top is the current KmlFeature to edit. 
	public static KmlFolder mKmlClipboard; //passed between activities. Folder for multiple items selection. 
	
	static String SHARED_PREFS_APPKEY = "OSMNavigator";
	static String PREF_LOCATIONS_KEY = "PREF_LOCATIONS";
	
	OnlineTileSourceBase MAPBOXSATELLITELABELLED;

	static final String userAgent = "OsmNavigator/1.0";

	static String graphHopperApiKey;
	static String mapQuestApiKey;
	static String flickrApiKey;
	static String geonamesAccount;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		
		MapBoxTileSource.retrieveMapBoxMapId(this);
		MAPBOXSATELLITELABELLED = new MapBoxTileSource("MapBoxSatelliteLabelled", ResourceProxy.string.mapquest_aerial, 1, 19, 256, ".png");
		TileSourceFactory.addTileSource(MAPBOXSATELLITELABELLED);

		graphHopperApiKey = ManifestUtil.retrieveKey(this, "GRAPHHOPPER_API_KEY");
		mapQuestApiKey = ManifestUtil.retrieveKey(this, "MAPQUEST_API_KEY");
		flickrApiKey = ManifestUtil.retrieveKey(this, "FLICKR_API_KEY");
		geonamesAccount = ManifestUtil.retrieveKey(this, "GEONAMES_ACCOUNT");

		//map = (MapView) findViewById(R.id.map);
		GenericMapView genericMap = (GenericMapView) findViewById(R.id.map);
		MapTileProviderBasic bitmapProvider = new MapTileProviderBasic(getApplicationContext());
		genericMap.setTileProvider(bitmapProvider);
		map = genericMap.getMapView();
		
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
		
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		//mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		//mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		//map prefs:
		mapController.setZoom(prefs.getInt("MAP_ZOOM_LEVEL", 5));
		mapController.setCenter(new GeoPoint((double)prefs.getFloat("MAP_CENTER_LAT", 48.5f), 
				(double)prefs.getFloat("MAP_CENTER_LON", 2.5f)));
		
		myLocationOverlay = new DirectedLocationOverlay(this);
		map.getOverlays().add(myLocationOverlay);

		if (savedInstanceState == null){
			Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null)
				location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
		
		ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
		map.getOverlays().add(scaleBarOverlay);
		
		// Itinerary markers:
		mItineraryMarkers = new FolderOverlay(this);
		mItineraryMarkers.setName(getString(R.string.itinerary_markers_title));
		map.getOverlays().add(mItineraryMarkers);
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
		mWhichRouteProvider = prefs.getInt("ROUTE_PROVIDER", OSRM);
		
		mRoadNodeMarkers = new FolderOverlay(this);
		mRoadNodeMarkers.setName("Route Steps");
		map.getOverlays().add(mRoadNodeMarkers);
		
		if (savedInstanceState != null){
			//STATIC mRoad = savedInstanceState.getParcelable("road");
			updateUIWithRoads(mRoads);
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
		mPoiMarkers = new RadiusMarkerClusterer(this);
		Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_poi_cluster);
		Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
		mPoiMarkers.setIcon(clusterIcon);
		mPoiMarkers.mAnchorV = Marker.ANCHOR_BOTTOM;
		mPoiMarkers.mTextAnchorU = 0.70f;
		mPoiMarkers.mTextAnchorV = 0.27f;
		mPoiMarkers.getTextPaint().setTextSize(12.0f);
		map.getOverlays().add(mPoiMarkers);
		if (savedInstanceState != null){
			//STATIC - mPOIs = savedInstanceState.getParcelableArrayList("poi");
			updateUIWithPOI(mPOIs, "");
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
				openFile(uri, true, false);
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
		ed.putFloat("MAP_CENTER_LAT", (float)c.getLatitude());
		ed.putFloat("MAP_CENTER_LON", (float)c.getLongitude());
		View searchPanel = (View)findViewById(R.id.search_panel);
		ed.putInt("PANEL_VISIBILITY", searchPanel.getVisibility());
		MapTileProviderBase tileProvider = map.getTileProvider();
		String tileProviderName = tileProvider.getTileSource().name();
		ed.putString("TILE_PROVIDER", tileProviderName);
		ed.putInt("ROUTE_PROVIDER", mWhichRouteProvider);
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
				map.getController().setCenter(mRoads[mSelectedRoad].mNodes.get(nodeId).mLocation);
				Marker roadMarker = (Marker)mRoadNodeMarkers.getItems().get(nodeId);
				roadMarker.showInfoWindow();
			}
			break;
		case POIS_REQUEST:
			if (resultCode == RESULT_OK) {
				int id = intent.getIntExtra("ID", 0);
				map.getController().setCenter(mPOIs.get(id).mLocation);
				Marker poiMarker = mPoiMarkers.getItem(id);
				poiMarker.showInfoWindow();
			}
			break;
		case KmlTreeActivity.KML_TREE_REQUEST:
			mKmlStack.pop();
			updateUIWithKml();
			if (intent == null)
				break;
			KmlFeature selectedFeature = intent.getParcelableExtra("KML_FEATURE");
			if (selectedFeature == null)
				break;
			BoundingBoxE6 bb = selectedFeature.getBoundingBox();
			if (bb == null)
				break;
			map.zoomToBoundingBox(bb);
			break;
		case KmlStylesActivity.KML_STYLES_REQUEST:
			updateUIWithKml();
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
		for (final String provider : mLocationManager.getProviders(true)) {
			mLocationManager.requestLocationUpdates(provider, 2*1000, 0.0f, this);
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
		mLocationManager.removeUpdates(this);
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
		GeocoderNominatim geocoder = new GeocoderNominatim(this, userAgent);
		String theAddress;
		try {
			double dLatitude = p.getLatitude();
			double dLongitude = p.getLongitude();
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
				theAddress = sb.toString();
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

	private class GeocodingTask extends AsyncTask<Object, Void, List<Address>> {
		int mIndex;
		protected List<Address> doInBackground(Object... params) {
			String locationAddress = (String)params[0];
			mIndex = (Integer)params[1];
			GeocoderNominatim geocoder = new GeocoderNominatim(getApplicationContext(), userAgent);
			geocoder.setOptions(true); //ask for enclosing polygon (if any)
			try {
				BoundingBoxE6 viewbox = map.getBoundingBox();
				List<Address> foundAdresses = geocoder.getFromLocationName(locationAddress, 1,
						viewbox.getLatSouthE6()*1E-6, viewbox.getLonEastE6()*1E-6,
						viewbox.getLatNorthE6()*1E-6, viewbox.getLonWestE6()*1E-6, false);
				return foundAdresses;
			} catch (Exception e) {
				return null;
			}
		}
		protected void onPostExecute(List<Address> foundAdresses) {
			if (foundAdresses == null) {
				Toast.makeText(getApplicationContext(), "Geocoding error", Toast.LENGTH_SHORT).show();
			} else if (foundAdresses.size() == 0) { //if no address found, display an error
				Toast.makeText(getApplicationContext(), "Address not found.", Toast.LENGTH_SHORT).show();
			} else {
				Address address = foundAdresses.get(0); //get first address
				String addressDisplayName = address.getExtras().getString("display_name");
				if (mIndex == START_INDEX){
					startPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerStart = updateItineraryMarker(markerStart, startPoint, START_INDEX,
							R.string.departure, R.drawable.marker_departure, -1, addressDisplayName);
					map.getController().setCenter(startPoint);
				} else if (mIndex == DEST_INDEX){
					destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
							R.string.destination, R.drawable.marker_destination, -1, addressDisplayName);
					map.getController().setCenter(destinationPoint);
				}
				getRoadAsync();
				//get and display enclosing polygon:
				Bundle extras = address.getExtras();
				if (extras != null && extras.containsKey("polygonpoints")){
					ArrayList<GeoPoint> polygon = extras.getParcelableArrayList("polygonpoints");
					//Log.d("DEBUG", "polygon:"+polygon.size());
					updateUIWithPolygon(polygon, addressDisplayName);
				} else {
					updateUIWithPolygon(null, "");
				}
			}
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
		new GeocodingTask().execute(locationAddress, index);
	}
	
	//add or replace the polygon overlay
	public void updateUIWithPolygon(ArrayList<GeoPoint> polygon, String name){
		List<Overlay> mapOverlays = map.getOverlays();
		int location = -1;
		if (mDestinationPolygon != null)
			location = mapOverlays.indexOf(mDestinationPolygon);
		mDestinationPolygon = new Polygon(this);
		mDestinationPolygon.setFillColor(0x15FF0080);
		mDestinationPolygon.setStrokeColor(0x800000FF);
		mDestinationPolygon.setStrokeWidth(5.0f);
		mDestinationPolygon.setTitle(name);
		BoundingBoxE6 bb = null;
		if (polygon != null){
			mDestinationPolygon.setPoints(polygon);
			bb = BoundingBoxE6.fromGeoPoints(polygon);
		}
		if (location != -1)
			mapOverlays.set(location, mDestinationPolygon);
		else
			mapOverlays.add(1, mDestinationPolygon); //insert just above the MapEventsOverlay. 
		setViewOn(bb);
		map.invalidate();
	}

	//Async task to reverse-geocode the marker position in a separate thread:
	private class ReverseGeocodingTask extends AsyncTask<Object, Void, String> {
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
			new ReverseGeocodingTask().execute(marker);
			//update route:
			getRoadAsync();
		}
		@Override public void onMarkerDragStart(Marker marker) {}		
	}
	
	final OnItineraryMarkerDragListener mItineraryListener = new OnItineraryMarkerDragListener();
	
	/** Update (or create if null) a marker in itineraryMarkers. */
    public Marker updateItineraryMarker(Marker marker, GeoPoint p, int index,
    		int titleResId, int markerResId, int imageResId, String address) {
		Drawable icon = getResources().getDrawable(markerResId);
		String title = getResources().getString(titleResId);
		if (marker == null){
			marker = new Marker(map);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			marker.setInfoWindow(mViaPointInfoWindow);
			marker.setDraggable(true);
			marker.setOnMarkerDragListener(mItineraryListener);
			mItineraryMarkers.add(marker);
		}
		marker.setTitle(title);
		marker.setPosition(p);
		marker.setIcon(icon);
		if (imageResId != -1)
			marker.setImage(getResources().getDrawable(imageResId));
		marker.setRelatedObject(index);
		map.invalidate();
		if (address != null)
			marker.setSnippet(address);
		else
			//Start geocoding task to get the address and update the Marker description:
			new ReverseGeocodingTask().execute(marker);
		return marker;
	}

	public void addViaPoint(GeoPoint p){
		viaPoints.add(p);
		updateItineraryMarker(null, p, viaPoints.size() - 1,
				R.string.viapoint, R.drawable.marker_via, -1, null);
	}
    
	public void removePoint(int index){
		if (index == START_INDEX){
			startPoint = null;
			if (markerStart != null){
				markerStart.closeInfoWindow();
				mItineraryMarkers.remove(markerStart);
				markerStart = null;
			}
		} else if (index == DEST_INDEX){
			destinationPoint = null;
			if (markerDestination != null){
				markerDestination.closeInfoWindow();
				mItineraryMarkers.remove(markerDestination);
				markerDestination = null;
			}
		} else {
			viaPoints.remove(index);
			updateUIWithItineraryMarkers();
		}
		getRoadAsync();
	}
	
	public void updateUIWithItineraryMarkers(){
		mItineraryMarkers.closeAllInfoWindows();
		mItineraryMarkers.getItems().clear();
		//Start marker:
		if (startPoint != null){
			markerStart = updateItineraryMarker(null, startPoint, START_INDEX, 
				R.string.departure, R.drawable.marker_departure, -1, null);
		}
		//Via-points markers if any:
		for (int index=0; index<viaPoints.size(); index++){
			updateItineraryMarker(null, viaPoints.get(index), index, 
				R.string.viapoint, R.drawable.marker_via, -1, null);
		}
		//Destination marker if any:
		if (destinationPoint != null){
			markerDestination = updateItineraryMarker(null, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1, null);
		}
	}
	
    //------------ Route and Directions
    
    private void putRoadNodes(Road road){
		mRoadNodeMarkers.getItems().clear();
		Drawable icon = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
		MarkerInfoWindow infoWindow = new MarkerInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map);
		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
    	for (int i=0; i<n; i++){
    		RoadNode node = road.mNodes.get(i);
    		String instructions = (node.mInstructions==null ? "" : node.mInstructions);
    		Marker nodeMarker = new Marker(map);
    		nodeMarker.setTitle(getString(R.string.step)+ " " + (i+1));
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
    		mRoadNodeMarkers.add(nodeMarker);
    	}
    	iconIds.recycle();
	}

	void selectRoad(int roadIndex){
		mSelectedRoad = roadIndex;
		putRoadNodes(mRoads[roadIndex]);
		//Set route info in the text view:
		TextView textView = (TextView)findViewById(R.id.routeInfo);
		textView.setText(mRoads[roadIndex].getLengthDurationText(-1));
		for (int i=0; i<mRoadOverlays.length; i++){
			Paint p = mRoadOverlays[i].getPaint();
			if (i == roadIndex)
				p.setColor(0x800000FF); //blue
			else
				p.setColor(0x90666666); //grey
		}
		map.invalidate();
	}

	class RoadOnClickListener implements Polyline.OnClickListener{
		@Override public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos){
			int selectedRoad = (Integer)polyline.getRelatedObject();
			selectRoad(selectedRoad);
			polyline.showInfoWindow(eventPos);
			return true;
		}
	};

	void updateUIWithRoads(Road[] roads){
		mRoadNodeMarkers.getItems().clear();
		TextView textView = (TextView)findViewById(R.id.routeInfo);
		textView.setText("");
		List<Overlay> mapOverlays = map.getOverlays();
		if (mRoadOverlays != null){
			for (int i=0; i<mRoadOverlays.length; i++)
				mapOverlays.remove(mRoadOverlays[i]);
			mRoadOverlays = null;
		}
		if (roads == null)
			return;
		if (roads[0].mStatus == Road.STATUS_TECHNICAL_ISSUE)
			Toast.makeText(map.getContext(), "Technical issue when getting the route", Toast.LENGTH_SHORT).show();
		else if (roads[0].mStatus > Road.STATUS_TECHNICAL_ISSUE) //functional issues
			Toast.makeText(map.getContext(), "No possible route here", Toast.LENGTH_SHORT).show();
		mRoadOverlays = new Polyline[roads.length];
		for (int i=0; i<roads.length; i++) {
			Polyline roadPolyline = RoadManager.buildRoadOverlay(roads[i], this);
			mRoadOverlays[i] = roadPolyline;
			if (mWhichRouteProvider == GRAPHHOPPER_BICYCLE || mWhichRouteProvider == GRAPHHOPPER_PEDESTRIAN) {
				Paint p = roadPolyline.getPaint();
				p.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
			}
			String routeDesc = roads[i].getLengthDurationText(-1);
			roadPolyline.setTitle(getString(R.string.route) + " - " + routeDesc);
			roadPolyline.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
			roadPolyline.setRelatedObject(i);
			roadPolyline.setOnClickListener(new RoadOnClickListener());
			mapOverlays.add(1, roadPolyline);
			//we insert the road overlays at the "bottom", just above the MapEventsOverlay,
			//to avoid covering the other overlays. 
		}
		selectRoad(0);
    }
    
	/**
	 * Async task to get the road in a separate thread. 
	 */
	private class UpdateRoadTask extends AsyncTask<Object, Void, Road[]> {
		protected Road[] doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
			RoadManager roadManager;
			Locale locale = Locale.getDefault();
			switch (mWhichRouteProvider){
			case OSRM:
				roadManager = new OSRMRoadManager();
				break;
			case GRAPHHOPPER_FASTEST:
				roadManager = new GraphHopperRoadManager(graphHopperApiKey);
				roadManager.addRequestOption("locale="+locale.getLanguage());
				//roadManager = new MapQuestRoadManager(mapQuestApiKey);
				//roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				break;
			case GRAPHHOPPER_BICYCLE:
				roadManager = new GraphHopperRoadManager(graphHopperApiKey);
				roadManager.addRequestOption("locale="+locale.getLanguage());
				roadManager.addRequestOption("vehicle=bike");
				//((GraphHopperRoadManager)roadManager).setElevation(true);
				break;
			case GRAPHHOPPER_PEDESTRIAN:
				roadManager = new GraphHopperRoadManager(graphHopperApiKey);
				roadManager.addRequestOption("locale="+locale.getLanguage());
				roadManager.addRequestOption("vehicle=foot");
				//((GraphHopperRoadManager)roadManager).setElevation(true);
				break;
			case GOOGLE_FASTEST:
				roadManager = new GoogleRoadManager();
				break;
				default:
				return null;
			}
			return roadManager.getRoads(waypoints);
		}

		protected void onPostExecute(Road[] result) {
			mRoads = result;
			updateUIWithRoads(result);
			getPOIAsync(poiTagText.getText().toString());
		}
	}
	
	public void getRoadAsync(){
		mRoads = null;
		GeoPoint roadStartPoint = null;
		if (startPoint != null){
			roadStartPoint = startPoint;
		} else if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null){
			//use my current location as itinerary start point:
			roadStartPoint = myLocationOverlay.getLocation();
		}
		if (roadStartPoint == null || destinationPoint == null){
			updateUIWithRoads(mRoads);
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
	
	void updateUIWithPOI(ArrayList<POI> pois, String featureTag){
		if (pois != null){
			POIInfoWindow poiInfoWindow = new POIInfoWindow(map);
			for (POI poi:pois){
				Marker poiMarker = new Marker(map);
				poiMarker.setTitle(poi.mType);
				poiMarker.setSnippet(poi.mDescription);
				poiMarker.setPosition(poi.mLocation);
				Drawable icon = null;
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM || poi.mServiceId == POI.POI_SERVICE_OVERPASS_API){
					icon = getResources().getDrawable(R.drawable.marker_poi);
					poiMarker.setAnchor(Marker.ANCHOR_CENTER, 1.0f);
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
				poiMarker.setRelatedObject(poi);
				poiMarker.setInfoWindow(poiInfoWindow);
				//thumbnail loading moved in async task for better performances. 
				mPoiMarkers.add(poiMarker);
			}
		}
		mPoiMarkers.setName(featureTag);
		mPoiMarkers.invalidate();
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
			final Marker marker = mPoiMarkers.getItem(i);
			mThreadPool.submit(new ThumbnailLoaderTask(poi, marker));
		}
	}
	
	/**
	 * Convert human readable feature to an OSM tag. 
	 * @param humanReadableFeature
	 * @return OSM tag string: "k=v"
	 */
	String getOSMTag(String humanReadableFeature){
		HashMap<String,String> map = BonusPackHelper.parseStringMapResource(getApplicationContext(), R.array.osm_poi_tags);
		return map.get(humanReadableFeature.toLowerCase(Locale.getDefault()));
	}
	
	private class POILoadingTask extends AsyncTask<Object, Void, ArrayList<POI>> {
		String mFeatureTag;
		String message;
		protected ArrayList<POI> doInBackground(Object... params) {
			mFeatureTag = (String)params[0];
			
			if (mFeatureTag == null || mFeatureTag.equals("")){
				return null;
			} else if (mFeatureTag.equals("wikipedia")){
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider(geonamesAccount);
				//Get POI inside the bounding box of the current map view:
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mFeatureTag.equals("flickr")){
				FlickrPOIProvider poiProvider = new FlickrPOIProvider(flickrApiKey);
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mFeatureTag.startsWith("picasa")){
				PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
				BoundingBoxE6 bb = map.getBoundingBox();
				//allow to search for keywords among picasa photos:
				String q = mFeatureTag.substring("picasa".length());
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 50, q);
				return pois;
			} else {
				/*
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				ArrayList<POI> pois;
				if (mRoad == null){
					pois = poiProvider.getPOIInside(map.getBoundingBox(), mFeatureTag, 100);
				} else {
					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mFeatureTag, 100, 2.0);
				}
				*/
				OverpassAPIProvider overpassProvider = new OverpassAPIProvider();
				String osmTag = getOSMTag(mFeatureTag);
				if (osmTag == null){
					message = mFeatureTag + " is not a valid feature.";
					return null;
				}
				String oUrl = overpassProvider.urlForPOISearch(osmTag, map.getBoundingBox(), 100, 10);
				ArrayList<POI> pois = overpassProvider.getPOIsFromUrl(oUrl);
				return pois;
			}
		}
		protected void onPostExecute(ArrayList<POI> pois) {
			mPOIs = pois;
			if (mFeatureTag == null || mFeatureTag.equals("")){
				//no search, no message
			} else if (mPOIs == null){
				if (message != null)
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				else 
					Toast.makeText(getApplicationContext(), "Technical issue when getting "+mFeatureTag+ " POI.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), mFeatureTag+ " found:"+mPOIs.size(), Toast.LENGTH_LONG).show();
			}
			updateUIWithPOI(mPOIs, mFeatureTag);
			if (mFeatureTag.equals("flickr")||mFeatureTag.startsWith("picasa")||mFeatureTag.equals("wikipedia"))
				startAsyncThumbnailsLoading(mPOIs);
		}
	}
	
	void getPOIAsync(String tag){
		mPoiMarkers.getItems().clear();
		new POILoadingTask().execute(tag);
	}
	
	//------------ KML handling

	boolean mDialogForOpen;
	
	void openLocalFileDialog(boolean open){
		mDialogForOpen = open;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.file_kml_open));
		builder.setMessage("" + mKmlDocument.getDefaultPathForAndroid(""));
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String localFileName = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE).getString("KML_LOCAL_FILE", "current.kml");
		input.setText(localFileName);
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				String localFileName = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				prefs.edit().putString("KML_LOCAL_FILE", localFileName).commit();
				dialog.cancel();
				if (mDialogForOpen){
					File file = mKmlDocument.getDefaultPathForAndroid(localFileName);
					openFile("file:/"+file.toString(), false, false);
				} else 
					saveFile(localFileName);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	void openUrlDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.url_kml_open));
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String defaultUri = "http://gmaps-samples.googlecode.com/svn/trunk/ggeoxml/cta.kml";
		String uri = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE).getString("KML_URI", defaultUri);
		input.setText(uri);
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() { 
			@Override public void onClick(DialogInterface dialog, int which) {
				String uri = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				prefs.edit().putString("KML_URI", uri).commit();
				dialog.cancel();
				openFile(uri, false, false);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	void openOverpassAPIWizard(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Overpass API Wizard");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String query = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE).getString("OVERPASS_QUERY", "amenity=cinema");
		input.setText(query);
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String query = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				prefs.edit().putString("OVERPASS_QUERY", query).commit();
				dialog.cancel();
				openFile(query, false, true);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	boolean getKMLFromOverpass(String query){
		OverpassAPIProvider overpassProvider = new OverpassAPIProvider();
		String oUrl = overpassProvider.urlForTagSearchKml(query, map.getBoundingBox(), 500, 30);
		return overpassProvider.addInKmlFolder(mKmlDocument.mKmlRoot, oUrl);
	}

	ProgressDialog createSpinningDialog(String title){
		ProgressDialog pd = new ProgressDialog(map.getContext());
		pd.setTitle(title);
		pd.setMessage(getString(R.string.wait));
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
			boolean isOverpassRequest = (Boolean)params[2];
			mKmlDocument = new KmlDocument();
			boolean ok = false;
			if (isOverpassRequest){
				//mUri contains the query
				ok = getKMLFromOverpass(mUri);
			} else if (mUri.startsWith("file:/")){
				mUri = mUri.substring("file:/".length());
				File file = new File(mUri);
				if (mUri.endsWith(".json"))
					ok = mKmlDocument.parseGeoJSON(file);
				else if (mUri.endsWith(".kmz"))
					ok = mKmlDocument.parseKMZFile(file);
				else //assume KML
					ok = mKmlDocument.parseKMLFile(file);
			} else if (mUri.startsWith("http")) {
				ok = mKmlDocument.parseKMLUrl(mUri);
			}
			return ok;
		}
		@Override protected void onPostExecute(Boolean ok) {
			if (mPD != null)
				mPD.dismiss();
			if (!ok)
				Toast.makeText(getApplicationContext(), "Sorry, unable to read "+mUri, Toast.LENGTH_SHORT).show();
			updateUIWithKml();
			if (ok){
				BoundingBoxE6 bb = mKmlDocument.mKmlRoot.getBoundingBox();
				if (bb != null){
					if (!mOnCreate)
						setViewOn(bb);
					else  //KO in onCreate (osmdroid bug) - Workaround:
						map.getController().setCenter(bb.getCenter());
				}
			}
		}
	}
	
	void openFile(String uri, boolean onCreate, boolean isOverpassRequest){
		//Toast.makeText(this, "Loading "+uri, Toast.LENGTH_SHORT).show();
		new KmlLoadingTask(getString(R.string.loading)+" "+uri).execute(uri, onCreate, isOverpassRequest);
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
		if (mKmlOverlay != null){
			mKmlOverlay.closeAllInfoWindows();
			map.getOverlays().remove(mKmlOverlay);
		}
		mKmlOverlay = (FolderOverlay)mKmlDocument.mKmlRoot.buildOverlay(map, buildDefaultStyle(), null, mKmlDocument);
		map.getOverlays().add(mKmlOverlay);
		map.invalidate();
	}
	
	void insertOverlaysInKml(){
		KmlFolder root = mKmlDocument.mKmlRoot;
		//Insert relevant overlays inside:
		if (mItineraryMarkers.getItems().size()>0)
			root.addOverlay(mItineraryMarkers, mKmlDocument);
		if (mRoadOverlays != null){
			for (int i=0; i<mRoadOverlays.length; i++)
				root.addOverlay(mRoadOverlays[i], mKmlDocument);
		}
		if (mRoadNodeMarkers.getItems().size()>0)
			root.addOverlay(mRoadNodeMarkers, mKmlDocument);
		root.addOverlay(mDestinationPolygon, mKmlDocument);
		if (mPoiMarkers.getItems().size()>0){
			root.addOverlay(mPoiMarkers, mKmlDocument);
		}
	}

	//Async task to reverse-geocode the KML point in a separate thread:
	private class KMLGeocodingTask extends AsyncTask<Object, Void, String> {
		KmlPlacemark kmlPoint;
		protected String doInBackground(Object... params) {
			kmlPoint = (KmlPlacemark)params[0];
			return getAddress(((KmlPoint) kmlPoint.mGeometry).getPosition());
		}
		protected void onPostExecute(String result) {
			kmlPoint.mName = result;
			updateUIWithKml();
			// marker.showInfoWindow();
		}
	}

	void addKmlPoint(GeoPoint position){
		KmlFeature kmlPoint = new KmlPlacemark(position);
		mKmlDocument.mKmlRoot.add(kmlPoint);
		new KMLGeocodingTask().execute(kmlPoint);
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
		InfoWindow.closeAllInfoWindowsOn(map);
		return true;
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
				R.string.departure, R.drawable.marker_departure, -1, null);
			getRoadAsync();
			return true;
		case R.id.menu_destination:
			destinationPoint = new GeoPoint(mClickedGeoPoint);
			markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1, null);
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
		
		switch (mWhichRouteProvider){
		case OSRM: 
			menu.findItem(R.id.menu_route_osrm).setChecked(true);
			break;
		case GRAPHHOPPER_FASTEST:
			menu.findItem(R.id.menu_route_graphhopper_fastest).setChecked(true);
			break;
		case GRAPHHOPPER_BICYCLE:
			menu.findItem(R.id.menu_route_graphhopper_bicycle).setChecked(true);
			break;
		case GRAPHHOPPER_PEDESTRIAN:
			menu.findItem(R.id.menu_route_graphhopper_pedestrian).setChecked(true);
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
		if (mRoads != null && mRoads[mSelectedRoad].mNodes.size()>0)
			menu.findItem(R.id.menu_itinerary).setEnabled(true);
		else
			menu.findItem(R.id.menu_itinerary).setEnabled(false);
		if (mPOIs != null && mPOIs.size()>0)
			menu.findItem(R.id.menu_pois).setEnabled(true);
		else 
			menu.findItem(R.id.menu_pois).setEnabled(false);
		return true;
	}

	/** return the index of the first Marker having its bubble opened, -1 if none */
	int getIndexOfBubbledMarker(AbstractList<? extends Overlay> list){
		for (int i=0; i<list.size(); i++){
			Overlay item = list.get(i);
			if (item instanceof Marker){
				Marker marker = (Marker)item;
				if (marker.isInfoWindowShown())
					return i;
			}
		}
		return -1;
	}
	
	void setStdTileProvider(){
		if (!(map.getTileProvider() instanceof MapTileProviderBasic)){
			GenericMapView genericMap = (GenericMapView) findViewById(R.id.map);
			MapTileProviderBasic bitmapProvider = new MapTileProviderBasic(this);
			genericMap.setTileProvider(bitmapProvider);
			map = genericMap.getMapView();
		}
	}
	
	boolean setMapsForgeTileProvider(){
		String path = Environment.getExternalStorageDirectory().getPath()+"/mapsforge/";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null)
			return false;
		File mapFile = null;
		for (File file:listOfFiles){
			if (file.isFile() && file.getName().endsWith(".map")){
				mapFile = file;
			}
		}
		if (mapFile == null)
			return false;
		MapsForgeTileProvider mfProvider = new MapsForgeTileProvider(new SimpleRegisterReceiver(this), mapFile);
		GenericMapView genericMap = (GenericMapView) findViewById(R.id.map);
		genericMap.setTileProvider(mfProvider);
		map = genericMap.getMapView();
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent;
		switch (item.getItemId()) {
		case R.id.menu_itinerary:
			myIntent = new Intent(this, RouteActivity.class);
			int currentNodeId = getIndexOfBubbledMarker(mRoadNodeMarkers.getItems());
			myIntent.putExtra("SELECTED_ROAD", mSelectedRoad);
			myIntent.putExtra("NODE_ID", currentNodeId);
			startActivityForResult(myIntent, ROUTE_REQUEST);
			return true;
		case R.id.menu_pois:
			myIntent = new Intent(this, POIActivity.class);
			myIntent.putExtra("ID", getIndexOfBubbledMarker(mPoiMarkers.getItems()));
			startActivityForResult(myIntent, POIS_REQUEST);
			return true;
		case R.id.menu_kml_url:
			openUrlDialog();
			return true;
		case R.id.menu_open_file:
			openLocalFileDialog(true);
			return true;
		case R.id.menu_overpass_api:
			openOverpassAPIWizard();
			return true;
		case R.id.menu_kml_get_overlays:
			insertOverlaysInKml();
			updateUIWithKml();
			return true;
		case R.id.menu_kml_tree:
			myIntent = new Intent(this, KmlTreeActivity.class);
			//myIntent.putExtra("KML", mKmlDocument.kmlRoot);
			mKmlStack.push(mKmlDocument.mKmlRoot);
			startActivityForResult(myIntent, KmlTreeActivity.KML_TREE_REQUEST);
			return true;
		case R.id.menu_kml_styles:
			myIntent = new Intent(this, KmlStylesActivity.class);
			startActivityForResult(myIntent, KmlStylesActivity.KML_STYLES_REQUEST);
			return true;
		case R.id.menu_save_file:
			openLocalFileDialog(false);
			return true;
		case R.id.menu_kml_clear:
			mKmlDocument = new KmlDocument();
			updateUIWithKml();
			return true;
		case R.id.menu_route_osrm:
			mWhichRouteProvider = OSRM;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_graphhopper_fastest:
			mWhichRouteProvider = GRAPHHOPPER_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_graphhopper_bicycle:
			mWhichRouteProvider = GRAPHHOPPER_BICYCLE;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_graphhopper_pedestrian:
			mWhichRouteProvider = GRAPHHOPPER_PEDESTRIAN;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_google:
			mWhichRouteProvider = GOOGLE_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_tile_mapnik:
			setStdTileProvider();
			map.setTileSource(TileSourceFactory.MAPNIK);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapquest_osm:
			setStdTileProvider();
			map.setTileSource(TileSourceFactory.MAPQUESTOSM);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapbox_satellite:
			setStdTileProvider();
			map.setTileSource(MAPBOXSATELLITELABELLED);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapsforge:
			boolean result = setMapsForgeTileProvider();
			if (result)
				item.setChecked(true);
			else 
				Toast.makeText(this, "No MapsForge map found", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_download_view_area:{
			CacheManager cacheManager = new CacheManager(map);
			int zoomMin = map.getZoomLevel();
			int zoomMax = map.getZoomLevel()+4;
			cacheManager.downloadAreaAsync(this, map.getBoundingBox(), zoomMin, zoomMax);
			return true;
			}
		case R.id.menu_clear_view_area:{
			CacheManager cacheManager = new CacheManager(map);
			int zoomMin = map.getZoomLevel();
			int zoomMax = map.getZoomLevel()+7;
			cacheManager.cleanAreaAsync(this, map.getBoundingBox(), zoomMin, zoomMax);
			return true;
			}
		case R.id.menu_cache_usage:{
			CacheManager cacheManager = new CacheManager(map);
			long cacheUsage = cacheManager.currentCacheUsage()/(1024*1024);
			long cacheCapacity = cacheManager.cacheCapacity()/(1024*1024);
			float percent = 100.0f*cacheUsage/cacheCapacity;
			String message = "Cache usage:\n"+cacheUsage+" Mo / "+cacheCapacity+" Mo = "+(int)percent + "%";
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			return true;
			}
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

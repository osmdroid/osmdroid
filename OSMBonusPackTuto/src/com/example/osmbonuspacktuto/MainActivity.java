package com.example.osmbonuspacktuto;

import java.io.File;
import java.util.ArrayList;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * This is the implementation of OSMBonusPack tutorials. 
 * Sections of code can be commented/uncommented depending on the progress in the tutorials. 
 * @see http://code.google.com/p/osmbonuspack/
 * @author M.Kergall
 *
 */
public class MainActivity extends Activity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		
		//Introduction
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MapView map = (MapView) findViewById(R.id.map);
		map.setTileSource(TileSourceFactory.MAPNIK);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		
		GeoPoint startPoint = new GeoPoint(48.13, -1.63);
		IMapController mapController = map.getController();
		mapController.setZoom(9);
		mapController.setCenter(startPoint);
		
		//0. Using the Marker overlay:
		Marker startMarker = new Marker(map);
		startMarker.setPosition(startPoint);
		startMarker.setAnchor(Marker.ANCHOR_CENTER, 1.0f);
		startMarker.setIcon(getResources().getDrawable(R.drawable.ic_launcher).mutate());
		startMarker.setTitle("Start point");
		startMarker.setImage(getResources().getDrawable(R.drawable.ic_launcher).mutate());
		startMarker.setDraggable(true);
		startMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble_black, map));
		map.getOverlays().add(startMarker);
		
		//1. "Hello, Routing World"
		RoadManager roadManager = new OSRMRoadManager();
		//or: 
		//roadManager roadManager = new MapQuestRoadManager("YOUR_API_KEY");
		//roadManager.addRequestOption("routeType=bicycle");
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
		waypoints.add(startPoint);
		GeoPoint endPoint = new GeoPoint(48.4, -1.9);
		waypoints.add(endPoint);
		Road road = roadManager.getRoad(waypoints);
		Polyline roadOverlay = RoadManager.buildRoadOverlay(road, this);
		map.getOverlays().add(roadOverlay);
		map.invalidate();
		
		//3. Showing the Route steps on the map
		Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
		FolderOverlay roadMarkers = new FolderOverlay(this);
		map.getOverlays().add(roadMarkers);
		for (int i=0; i<road.mNodes.size(); i++){
			RoadNode node = road.mNodes.get(i);
			Marker nodeMarker = new Marker(map);
			nodeMarker.setPosition(node.mLocation);
			nodeMarker.setIcon(nodeIcon);
			
			//4. Filling the bubbles
			nodeMarker.setTitle("Step "+i);
			nodeMarker.setSnippet(node.mInstructions);
			nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
			Drawable iconContinue = getResources().getDrawable(R.drawable.ic_continue);
			nodeMarker.setImage(iconContinue);
			//4. end
			
			roadMarkers.add(nodeMarker);
		}
		
		//5. OpenStreetMap POIs with Nominatim
		
		/* 5. 
		NominatimPOIProvider poiProvider = new NominatimPOIProvider();
		ArrayList<POI> pois = poiProvider.getPOICloseTo(startPoint, "cinema", 50, 0.1);
		//or : ArrayList<POI> pois = poiProvider.getPOIAlong(road.getRouteLow(), "fuel", 50, 2.0);
		*/
		
		/* 6. Wikipedia POIs with GeoNames 
		GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
		//BoundingBoxE6 bb = map.getBoundingBox(); 
		//ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
		//=> not possible in onCreate, as map bounding box is not correct until a draw occurs (osmdroid issue). 
		ArrayList<POI> pois = poiProvider.getPOICloseTo(startPoint, 30, 20.0);
		*/
		
		/*8. Quick overview of the Flickr and Picasa POIs */
		PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
		BoundingBoxE6 bb = BoundingBoxE6.fromGeoPoints(waypoints);
		ArrayList<POI> pois = poiProvider.getPOIInside(bb, 20, null);
		
		if (pois != null) {
            Drawable poiIcon = getResources().getDrawable(R.drawable.marker_poi_default);
			for (POI poi:pois){
	            Marker poiMarker = new Marker(map);
	            poiMarker.setTitle(poi.mType);
	            poiMarker.setSnippet(poi.mDescription);
	            poiMarker.setPosition(poi.mLocation);
	            poiMarker.setIcon(poiIcon);
	            if (poi.mThumbnail != null){
	            	poiMarker.setImage(new BitmapDrawable(poi.mThumbnail));
	            }
				/* 7.*/ 
				poiMarker.setInfoWindow(new CustomInfoWindow(map, poi));
	            map.getOverlays().add(poiMarker);
			}
		}
		
		//10. Loading KML content
		String url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=48.13&flon=-1.63&tlat=48.1&tlon=-1.26";
		KmlDocument kmlDocument = new KmlDocument();
		boolean ok = kmlDocument.parseUrl(url);
		//File file = kmlDocument.getDefaultPathForAndroid("Province_test.kml");
		//boolean ok = kmlDocument.parseFile(file);
		Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_kml_point);
		if (ok){
			FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(this, map, defaultMarker, kmlDocument, false);
			map.getOverlays().add(kmlOverlay);
			if (kmlDocument.kmlRoot.mBB != null){
				//map.zoomToBoundingBox(kmlRoot.mBB); => not working in onCreate - this is a well-known osmdroid bug. 
				//Workaround:
				map.getController().setCenter(new GeoPoint(
						kmlDocument.kmlRoot.mBB.getLatSouthE6()+kmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
						kmlDocument.kmlRoot.mBB.getLonWestE6()+kmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2));
			}
		}
		
		//11. Grab overlays in KML structure, save KML document locally
		if (kmlDocument.kmlRoot != null){
			kmlDocument.kmlRoot.addOverlay(roadOverlay, kmlDocument);
			kmlDocument.kmlRoot.addOverlay(roadMarkers, kmlDocument);
			File localFile = kmlDocument.getDefaultPathForAndroid("my_route.kml");
			kmlDocument.saveAsKML(localFile);
		}
		
		//12. Using GroundOverlay
		GroundOverlay myGroundOverlay = new GroundOverlay(this);
		myGroundOverlay.setPosition(endPoint);
		myGroundOverlay.setImage(getResources().getDrawable(R.drawable.ic_launcher).mutate());
		myGroundOverlay.setTransparency(0.5f);
		myGroundOverlay.setBearing(45.0f);
		myGroundOverlay.setDimensions(2000.0f);
		map.getOverlays().add(myGroundOverlay);
	}
	
	/*
	@Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.map_menu, menu);
	}
	*/
	
	//7. Customizing the bubble behaviour
	class CustomInfoWindow extends MarkerInfoWindow {
		POI mSelectedPoi;
		public CustomInfoWindow(MapView mapView, POI selectedPoi) {
			super(R.layout.bonuspack_bubble, mapView);
			mSelectedPoi = selectedPoi;
			/*
			//open a context menu when clicking on the bubble:
			((Activity) mapView.getContext()).registerForContextMenu(mView);
			mView.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent e) {
					if (e.getAction() == MotionEvent.ACTION_UP){
						((Activity) mapView.getContext()).openContextMenu(mView);
					}
					return true;
				}
			});
			*/
			Button btn = (Button)(mView.findViewById(R.id.bubble_moreinfo));
			btn.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View view) {
			        if (mSelectedPoi.mUrl != null){
			            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSelectedPoi.mUrl));
			            view.getContext().startActivity(myIntent);
			        } else {
			        	Toast.makeText(view.getContext(), "Button clicked", Toast.LENGTH_LONG).show();
			        }
			    }
			});
		}
		@Override public void onOpen(Object item){
			super.onOpen(item);
			mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.VISIBLE);
			
			//8. put thumbnail image in bubble, fetching the thumbnail in background:
			if (mSelectedPoi.mThumbnailPath != null){
				ImageView imageView = (ImageView)mView.findViewById(R.id.bubble_image);
				mSelectedPoi.fetchThumbnailOnThread(imageView);
			}
		}	
	}
	
}

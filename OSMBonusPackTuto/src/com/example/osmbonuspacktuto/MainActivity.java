package com.example.osmbonuspacktuto;

import java.util.ArrayList;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;

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
		
		//1. "Hello, Routing World"
		RoadManager roadManager = new OSRMRoadManager();
		//or: 
		//roadManager roadManager = new MapQuestRoadManager();
		//roadManager.addRequestOption("routeType=bicycle");
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
		waypoints.add(startPoint);
		waypoints.add(new GeoPoint(48.4, -1.9)); //end point
		Road road = roadManager.getRoad(waypoints);
		PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road, this);
		map.getOverlays().add(roadOverlay);
		map.invalidate();
		
		//3. Showing the Route steps on the map
		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
		ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = 
				new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, map);
		map.getOverlays().add(roadNodes);
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		for (int i=0; i<road.mNodes.size(); i++){
			RoadNode node = road.mNodes.get(i);
			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step "+i, "", node.mLocation, this);
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
			
			//4. Filling the bubbles
			nodeMarker.setDescription(node.mInstructions);
			nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
			Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
			nodeMarker.setImage(icon);
			//4. end
			
			roadNodes.addItem(nodeMarker);
		}
		
		//5. OpenStreetMap POIs with Nominatim
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
		                                poiItems, map);
		map.getOverlays().add(poiMarkers);
		NominatimPOIProvider poiProvider = new NominatimPOIProvider();
		ArrayList<POI> pois = poiProvider.getPOICloseTo(startPoint, "cinema", 50, 0.1);
		//or : ArrayList<POI> pois = poiProvider.getPOIAlong(road.getRouteLow(), "fuel", 50, 2.0);
		for (POI poi:pois){
            ExtendedOverlayItem poiItem = new ExtendedOverlayItem(
                                    poi.mType, poi.mDescription, 
                                    poi.mLocation, map.getContext());
            Drawable poiMarker = getResources().getDrawable(R.drawable.marker_poi_default);
            poiItem.setMarker(poiMarker);
            poiItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            if (poi.mThumbnail != null){
            	poiItem.setImage(new BitmapDrawable(poi.mThumbnail));
            }
            poiMarkers.addItem(poiItem);
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

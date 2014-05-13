package com.example.osmbonuspacktuto;

import java.io.File;
import java.util.ArrayList;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.GridMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
public class MainActivity extends Activity implements MapEventsReceiver {

	MapView map;
	KmlDocument mKmlDocument;
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		
		//Introduction
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		map = (MapView) findViewById(R.id.map);
		//map.setTileSource(TileSourceFactory.MAPNIK);
		/* or using local file archives (zip, sqlite or mbtiles) built with MOBAC:
		map.setTileSource(new XYTileSource("MapQuest",
				ResourceProxy.string.mapquest_osm, 9, 13, 256, ".jpg", new String[] {
				"http://otile1.mqcdn.com/tiles/1.0.0/map/",
				"http://otile2.mqcdn.com/tiles/1.0.0/map/",
				"http://otile3.mqcdn.com/tiles/1.0.0/map/",
				"http://otile4.mqcdn.com/tiles/1.0.0/map/" }));
		map.setUseDataConnection(false);
		map.setScrollableAreaLimit(new BoundingBoxE6(47.4, -1.05, 46.9, -1.93));
		*/
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		GeoPoint startPoint = new GeoPoint(48.13, -1.63);
		IMapController mapController = map.getController();
		mapController.setZoom(10);
		mapController.setCenter(startPoint);

		//0. Using the Marker overlay
		Marker startMarker = new Marker(map);
		startMarker.setPosition(startPoint);
		startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		startMarker.setTitle("Start point");
		//startMarker.setIcon(getResources().getDrawable(R.drawable.marker_kml_point).mutate());
		//startMarker.setImage(getResources().getDrawable(R.drawable.ic_launcher));
		//startMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble_black, map));
		startMarker.setDraggable(true);
		startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());
		map.getOverlays().add(startMarker);
		
		//1. "Hello, Routing World"
		RoadManager roadManager = new OSRMRoadManager();
		//2. Playing with the RoadManager
		//roadManager roadManager = new MapQuestRoadManager("YOUR_API_KEY");
		//roadManager.addRequestOption("routeType=bicycle");
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
		waypoints.add(startPoint);
		GeoPoint endPoint = new GeoPoint(48.4, -1.9);
		waypoints.add(endPoint);
		Road road = roadManager.getRoad(waypoints);
		if (road.mStatus != Road.STATUS_OK)
			Toast.makeText(this, "Error when loading the road - status="+road.mStatus, Toast.LENGTH_SHORT).show();
		
		Polyline roadOverlay = RoadManager.buildRoadOverlay(road, this);
		map.getOverlays().add(roadOverlay);
		
		//3. Showing the Route steps on the map
		FolderOverlay roadMarkers = new FolderOverlay(this);
		map.getOverlays().add(roadMarkers);
		Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
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
		NominatimPOIProvider poiProvider = new NominatimPOIProvider();
		ArrayList<POI> pois = poiProvider.getPOICloseTo(startPoint, "cinema", 50, 0.1);
		//or : ArrayList<POI> pois = poiProvider.getPOIAlong(road.getRouteLow(), "fuel", 50, 2.0);
		
		//6. Wikipedia POIs with GeoNames 
		/*
		GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
		//BoundingBoxE6 bb = map.getBoundingBox(); 
		//ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
		//=> not possible in onCreate, as map bounding box is not correct until a draw occurs (osmdroid issue). 
		ArrayList<POI> pois = poiProvider.getPOICloseTo(startPoint, 30, 20.0);
		*/
		
		//8. Quick overview of the Flickr and Picasa POIs */
		/*
		PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
		BoundingBoxE6 bb = BoundingBoxE6.fromGeoPoints(waypoints);
		ArrayList<POI> pois = poiProvider.getPOIInside(bb, 20, null);
		*/
		
		//FolderOverlay poiMarkers = new FolderOverlay(this);
		//10. Marker Clustering
		GridMarkerClusterer poiMarkers = new GridMarkerClusterer(this);
		//Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
		Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_poi_cluster);
		Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
		poiMarkers.setIcon(clusterIcon);
		//end of 10.
		//11. Customizing the clusters design
		poiMarkers.getTextPaint().setTextSize(12.0f);
		poiMarkers.mAnchorV = Marker.ANCHOR_BOTTOM;
		poiMarkers.mTextAnchorU = 0.70f;
		poiMarkers.mTextAnchorV = 0.27f;
		//end of 11.
		map.getOverlays().add(poiMarkers);
        Drawable poiIcon = getResources().getDrawable(R.drawable.marker_poi_default);
		if (pois != null) {
			for (POI poi:pois){
	            Marker poiMarker = new Marker(map);
	            poiMarker.setTitle(poi.mType);
	            poiMarker.setSnippet(poi.mDescription);
	            poiMarker.setPosition(poi.mLocation);
	            poiMarker.setIcon(poiIcon);
	            if (poi.mThumbnail != null){
	            	poiMarker.setImage(new BitmapDrawable(poi.mThumbnail));
	            }
				// 7.
				poiMarker.setInfoWindow(new CustomInfoWindow(map));
	            poiMarker.setRelatedObject(poi);
	            poiMarkers.add(poiMarker);
			}
		}

		/* the "Too Many Markers" test:
		for (int i=0; i<5000; i++){
			Marker poiMarker = new Marker(map);
			poiMarker.setPosition(new GeoPoint(Math.random()*5+45, Math.random()*5));
			poiMarker.setIcon(poiIcon);
			poiMarkers.add(poiMarker);
		}
		*/

		//12. Loading KML content
		String url = "http://mapsengine.google.com/map/kml?mid=z6IJfj90QEd4.kUUY9FoHFRdE";
		//String url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=48.13&flon=-1.63&tlat=48.1&tlon=-1.26";
		mKmlDocument = new KmlDocument();
		boolean ok = mKmlDocument.parseUrl(url);
		if (ok){
			//13.1 Simple styling
			Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_kml_point);
			Bitmap defaultBitmap = ((BitmapDrawable)defaultMarker).getBitmap();
			Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);
			//13.2 Advanced styling with Styler
			KmlFeature.Styler styler = new MyKmlStyler(defaultStyle);
			
			FolderOverlay kmlOverlay = (FolderOverlay)mKmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, styler, mKmlDocument);
			map.getOverlays().add(kmlOverlay);
			BoundingBoxE6 bb = mKmlDocument.mKmlRoot.getBoundingBox();
			if (bb != null){
				//map.zoomToBoundingBox(bb); => not working in onCreate - this is a well-known osmdroid bug. 
				//Workaround:
				map.getController().setCenter(new GeoPoint(
						bb.getLatSouthE6()+bb.getLatitudeSpanE6()/2, 
						bb.getLonWestE6()+bb.getLongitudeSpanE6()/2));
			}
		} else
			Toast.makeText(this, "Error when loading KML", Toast.LENGTH_SHORT).show();
		
		//14. Grab overlays in KML structure, save KML document locally
		if (mKmlDocument.mKmlRoot != null){
			KmlFolder root = (KmlFolder)mKmlDocument.mKmlRoot;
			root.addOverlay(roadOverlay, mKmlDocument);
			root.addOverlay(roadMarkers, mKmlDocument);
			mKmlDocument.saveAsKML(mKmlDocument.getDefaultPathForAndroid("my_route.kml"));
			//15. Loading and saving of GeoJSON content
			mKmlDocument.saveAsGeoJSON(mKmlDocument.getDefaultPathForAndroid("my_route.json"));
		}
		
		//16. Handling Map events
		MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
		Overlay removed = map.getOverlays().set(0, mapEventsOverlay);
		map.getOverlays().add(removed);
	}
	
	//0. Using the Marker and Polyline overlays - advanced options
	class OnMarkerDragListenerDrawer implements OnMarkerDragListener {
		ArrayList<GeoPoint> mTrace;
		Polyline mPolyline;
		OnMarkerDragListenerDrawer() {
			mTrace = new ArrayList<GeoPoint>(100);
			mPolyline = new Polyline(map.getContext());
			mPolyline.setColor(0xAA0000FF);
			mPolyline.setWidth(2.0f);
			mPolyline.setGeodesic(true);
			map.getOverlays().add(mPolyline);
		}
		@Override public void onMarkerDrag(Marker marker) {
			//mTrace.add(marker.getPosition());
		}
		@Override public void onMarkerDragEnd(Marker marker) {
			mTrace.add(marker.getPosition());
			mPolyline.setPoints(mTrace);
			map.invalidate();
		}
		@Override public void onMarkerDragStart(Marker marker) {
			//mTrace.add(marker.getPosition());
		}
	}
	
	//7. Customizing the bubble behaviour
	class CustomInfoWindow extends MarkerInfoWindow {
		POI mSelectedPoi;
		public CustomInfoWindow(MapView mapView) {
			super(R.layout.bonuspack_bubble, mapView);
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
			Marker marker = (Marker)item;
			mSelectedPoi = (POI)marker.getRelatedObject();
			
			//8. put thumbnail image in bubble, fetching the thumbnail in background:
			if (mSelectedPoi.mThumbnailPath != null){
				ImageView imageView = (ImageView)mView.findViewById(R.id.bubble_image);
				mSelectedPoi.fetchThumbnailOnThread(imageView);
			}
		}	
	}
	
	//11. Customizing the clusters design - and beyond
	class CirclesGridMarkerClusterer extends GridMarkerClusterer{

		public CirclesGridMarkerClusterer(Context ctx) {
			super(ctx);
		}
		
		@Override public Marker buildClusterMarker(StaticCluster cluster, MapView mapView){
			Marker m = new Marker(mapView);
			m.setPosition(cluster.getPosition());
			m.setInfoWindow(null);
			m.setAnchor(0.5f, 0.5f);
			int radius = (int) Math.sqrt(cluster.getSize()*3);
			radius = Math.max(radius, 10);
			radius = Math.min(radius, 30);
			Bitmap finalIcon = Bitmap.createBitmap(radius*2, radius*2, mClusterIcon.getConfig());
			Canvas iconCanvas = new Canvas(finalIcon);
			Paint circlePaint = new Paint();
			if (cluster.getSize() < 20)
				circlePaint.setColor(Color.BLUE);
			else 
				circlePaint.setColor(Color.RED);
			circlePaint.setAlpha(200);
			iconCanvas.drawCircle(radius, radius, radius, circlePaint);
			String text = ""+cluster.getSize();
		    int textHeight = (int) (mTextPaint.descent() + mTextPaint.ascent());
			iconCanvas.drawText(text, 
					mTextAnchorU*finalIcon.getWidth(), 
					mTextAnchorV*finalIcon.getHeight() - textHeight/2, 
					mTextPaint);
			m.setIcon(new BitmapDrawable(mapView.getContext().getResources(), finalIcon));
			return m;
		}
	}

	//13.2 Loading KML content - Advanced styling with Styler
	class MyKmlStyler implements KmlFeature.Styler {
		Style mDefaultStyle;
		
		MyKmlStyler(Style defaultStyle){
			mDefaultStyle = defaultStyle;
		}
		
		@Override public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
			//Custom styling:
			polyline.setColor(Color.GREEN);
			polyline.setWidth(Math.max(kmlLineString.mCoordinates.size()/200.0f, 3.0f));
		}
		@Override public void onPolygon(Polygon polygon, KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {
			//Keeping default styling:
			kmlPolygon.applyDefaultStyling(polygon, mDefaultStyle, kmlPlacemark, mKmlDocument, map);
		}
		@Override public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
			//Styling based on ExtendedData properties: 
			if ("panda_area".equals(kmlPlacemark.getExtendedData("category")))
				kmlPlacemark.mStyle = "panda_area";
			else if ("gorilla_area".equals(kmlPlacemark.getExtendedData("category")))
				kmlPlacemark.mStyle = "gorilla_area";
			kmlPoint.applyDefaultStyling(marker, mDefaultStyle, kmlPlacemark, mKmlDocument, map);
		}
		@Override public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
			//If nothing to do, do nothing. 
		}
	}

	//16. Handling Map events
	@Override public boolean singleTapConfirmedHelper(GeoPoint p) {
		Toast.makeText(this, "Tap on ("+p.getLatitude()+","+p.getLongitude()+")", Toast.LENGTH_SHORT).show();
		InfoWindow.closeAllInfoWindowsOn(map);
		return true;
	}
	float mGroundOverlayBearing = 0.0f;
	@Override public boolean longPressHelper(GeoPoint p) {
		//Toast.makeText(this, "Long press", Toast.LENGTH_SHORT).show();
		//17. Using Polygon, defined as a circle:
		Polygon circle = new Polygon(this);
		circle.setPointsAsCircle(p, 10000.0);
		circle.setFillColor(0x12121212);
		circle.setStrokeColor(Color.RED);
		circle.setStrokeWidth(2);
		map.getOverlays().add(circle);
		
		//18. Using GroundOverlay
		GroundOverlay myGroundOverlay = new GroundOverlay(this);
		myGroundOverlay.setPosition(p);
		myGroundOverlay.setImage(getResources().getDrawable(R.drawable.ic_launcher).mutate());
		myGroundOverlay.setDimensions(2000.0f);
		myGroundOverlay.setTransparency(0.25f);
		myGroundOverlay.setBearing(mGroundOverlayBearing);
		mGroundOverlayBearing += 20.0f;
		map.getOverlays().add(myGroundOverlay);
		
		map.invalidate();
		return true;
	}
	
}

package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Java representation of a KML Feature. 
 * It currently supports: Folder, Document, and Placemarks with the following Geometry: Point, LineString, and Polygon. <br>
 * Each KmlFeature has a type: mObjectType. <br>
 * 	- Folder feature object type = FOLDER. <br>
 * 	- Document feature is handled exactly like a Folder (object type = FOLDER). <br>
 * 	- For a Placemark, the KmlFeature has the object type of its geometry: POINT, LINE_STRING, POLYGON or GROUND_OVERLAY. 
 * 	  It contains both the Placemark attributes and the Geometry attributes. <br>
 * 	- UNKNOWN object type is reserved for default, issues/errors, or unsupported features/geometries. 
 * 
 * @see KmlDocument
 * @see https://developers.google.com/kml/documentation/kmlreference
 * 
 * @author M.Kergall
 */
public class KmlFeature implements Parcelable, Cloneable {
	/** possible KML object type */
	public static final int UNKNOWN=0, POINT=1, LINE_STRING=2, POLYGON=3, GROUND_OVERLAY=4, FOLDER=5;
	
	/** KML object type */
	public int mObjectType;
	/** feature id attribute, if any. Null if none. */
	public String mId;
	/** name tag */
	public String mName;
	/** description tag */
	public String mDescription;
	/** if this is a Folder, list of features it contains */
	public ArrayList<KmlFeature> mItems;
	/** visibility tag */
	public boolean mVisibility;
	/** open tag */
	public boolean mOpen;
	/** coordinates of the geometry. If Point, one and only one entry. */
	public ArrayList<GeoPoint> mCoordinates;
	/** Polygon holes (can be null) */
	public ArrayList<ArrayList<GeoPoint>> mHoles;
	/** Overlay Icon (can be null) */
	public String mIconHref;
	public Bitmap mIcon;
	/** Overlay color */
	public int mColor;
	/** GroundOverlay rotation - default = 0 */
	public float mRotation;
	/** styleUrl (without the #) */
	public String mStyle;
	/** ExtendedData, as a HashMap of (name, value). 
	 * Can be null if the feature has no ExtendedData. 
	 * The KML displayName is not handled. 
	 * value is always stored as a Java String. */
	public HashMap<String, String> mExtendedData;
	/** bounding box - null if no geometry (means empty) */
	public BoundingBoxE6 mBB;
		
	/** default constructor: create an UNKNOWN object */
	public KmlFeature(){
		mObjectType = UNKNOWN;
		mVisibility=true;
		mOpen=true;
	}
	
	public void createAsFolder(){
		mObjectType = FOLDER;
		mItems = new ArrayList<KmlFeature>();
	}
	
	public void createFromOverlayItem(ExtendedOverlayItem marker){
		mObjectType = POINT;
		mName = marker.getTitle();
		mDescription = marker.getDescription();
		mCoordinates = new ArrayList<GeoPoint>(1);
		GeoPoint p = marker.getPoint();
		mCoordinates.add(p);
		mBB = new BoundingBoxE6(p.getLatitudeE6(), p.getLongitudeE6(), p.getLatitudeE6(), p.getLongitudeE6());
	}

	public void createFromPolygon(Polygon polygon, KmlDocument kmlDoc){
		mObjectType = POLYGON;
		mName = polygon.getTitle();
		mDescription = polygon.getSnippet();
		mCoordinates = (ArrayList<GeoPoint>)polygon.getPoints();
		mHoles = (ArrayList<ArrayList<GeoPoint>>)polygon.getHoles();
		mBB = BoundingBoxE6.fromGeoPoints(mCoordinates);
		mVisibility = polygon.isEnabled();
		//Style:
		Style style = new Style();
		style.fillColorStyle = new ColorStyle(polygon.getFillColor());
		style.outlineColorStyle = new ColorStyle(polygon.getStrokeColor());
		style.outlineWidth = polygon.getStrokeWidth();
		mStyle = kmlDoc.addStyle(style);
	}

	public void createFromPolyline(Polyline polyline, KmlDocument kmlDoc){
		mObjectType = LINE_STRING;
		mName = "LineString - "+polyline.getNumberOfPoints()+" points";
		mCoordinates = (ArrayList<GeoPoint>)polyline.getPoints();
		mBB = BoundingBoxE6.fromGeoPoints(mCoordinates);
		mVisibility = polyline.isEnabled();
		//Style:
		Style style = new Style();
		style.outlineColorStyle = new ColorStyle(polyline.getColor());
		style.outlineWidth = polyline.getWidth();
		mStyle = kmlDoc.addStyle(style);
	}
	
	/** 
	 * Assuming this is a Folder, converts the overlay to a KmlFeature and add it inside. 
	 * If there is no available conversion from this Overlay class to a KmlFeature, add nothing. 
	 * @param overlay to convert and add
	 * @param kmlDoc for style handling. 
	 * @return true if OK, false if the overlay has not been added. 
	 */
	public boolean addOverlay(Overlay overlay, KmlDocument kmlDoc){
		if (overlay == null || mObjectType != FOLDER)
			return false;
		KmlFeature kmlItem = new KmlFeature();
		kmlItem.createFromOverlay(overlay, kmlDoc);
		if (kmlItem.mObjectType != UNKNOWN){
			mItems.add(kmlItem);
			updateBoundingBoxWith(kmlItem.mBB);
			return true;		
		} else
			return false;
	}
	
	/** 
	 * Assuming this is a Folder, adds all overlays inside, converting them in KmlFeatures. 
	 * @param overlays to add
	 * @param kmlDoc
	 */
	public void addOverlays(List<Overlay> overlays, KmlDocument kmlDoc){
		if (overlays != null){
			for (Overlay item:overlays){
				addOverlay(item, kmlDoc);
			}
		}
	}
	
	/** Set-up the KmlFeature from an overlay. 
	 * Conversion from Overlay subclasses to KML Features is as follow: <br>
	 *   FolderOverlay => Folder<br>
	 *   ItemizedOverlayWithBubble => Point if 1 point, Folder of Points if multiple points, UNKNOWN if no point<br>
	 *   Polygon => Polygon<br>
	 *   Polyline => LineString<br>
	 * 	 For all other Overlay subclasses => not supported, creates a NO_SHAPE object. 
	 * @param overlay
	 * @param kmlDoc for style handling
	 */
	public void createFromOverlay(Overlay overlay, KmlDocument kmlDoc){
		if (overlay.getClass() == FolderOverlay.class){
			FolderOverlay folderOverlay = (FolderOverlay)overlay;
			createAsFolder();
			addOverlays(folderOverlay.getItems(), kmlDoc);
			mName = folderOverlay.getName();
			mDescription = folderOverlay.getDescription();
			mVisibility = folderOverlay.isEnabled();
		} else if (overlay.getClass() == ItemizedOverlayWithBubble.class){
			ItemizedOverlayWithBubble<ExtendedOverlayItem> markers = (ItemizedOverlayWithBubble<ExtendedOverlayItem>)overlay;
			if (markers.size()==1){ //only 1 item => we can create it as a KML Point:
				ExtendedOverlayItem marker = markers.getItem(0);
				createFromOverlayItem(marker);
			} else if (markers.size()==0){
				//if empty list, ignore => create an UNKNOWN object. 
				mObjectType = UNKNOWN;
			} else { //we have multiple points => we must create a KML Folder, and put items inside as Points:
				createAsFolder();
				mName = "Points - " + markers.size();
				for (int j=0; j<markers.size(); j++){
					ExtendedOverlayItem marker = markers.getItem(j);
					KmlFeature kmlItem = new KmlFeature();
					kmlItem.createFromOverlayItem(marker);
					mItems.add(kmlItem);
					updateBoundingBoxWith(kmlItem.mBB);
				}
			}
		} else if (overlay.getClass() == Polygon.class){
			Polygon polygon = (Polygon)overlay;
			createFromPolygon(polygon, kmlDoc);
		} else if (overlay.getClass() == Polyline.class){
			Polyline polyline = (Polyline)overlay;
			createFromPolyline(polyline, kmlDoc);
		} else { //unsupported overlay - create an UNKNOWN:
			mObjectType = UNKNOWN;
			mName = "Unknown object - " + overlay.getClass().getName();
		}
	}
	
	/**
	 * Increase the bounding box of the feature to include an other bounding box. 
	 * Typically needed when adding an item in the feature. 
	 * @param itemBB the bounding box to "add". null means empty. 
	 */
	public void updateBoundingBoxWith(BoundingBoxE6 itemBB){
		if (itemBB != null){
			if (mBB == null){
				mBB = new BoundingBoxE6(
						itemBB.getLatNorthE6(), 
						itemBB.getLonEastE6(), 
						itemBB.getLatSouthE6(), 
						itemBB.getLonWestE6());
			} else {
				mBB = new BoundingBoxE6(
						Math.max(itemBB.getLatNorthE6(), mBB.getLatNorthE6()), 
						Math.max(itemBB.getLonEastE6(), mBB.getLonEastE6()),
						Math.min(itemBB.getLatSouthE6(), mBB.getLatSouthE6()),
						Math.min(itemBB.getLonWestE6(), mBB.getLonWestE6()));
			}
		}
	}

	/** add an item in the Folder 
	 * @return false if not a Folder
	 * */
	public boolean add(KmlFeature item){
		if (mObjectType != FOLDER)
			return false;
		mItems.add(item);
		updateBoundingBoxWith(item.mBB);
		return true;
	}
	
	/** 
	 * Set this name/value pair in the ExtendedData of the feature. 
	 * If there is already a pair with this name, it will be replaced by the new one. 
	 * @param name
	 * @param value always as a String. 
	 */
	public void setExtendedData(String name, String value){
		if (mExtendedData == null)
			mExtendedData = new HashMap<String,String>();
		mExtendedData.put(name, value);
	}
	
	/** from a Placemark feature having a Point geometry, build a Marker overlay (ItemizedOverlayWithBubble)
	 * @param map
	 * @param defaultMarker default marker to be used if no style or no icon specified
	 * @param kmlDocument
	 * @param supportVisibility
	 * @return the marker overlay
	 * @see buildOverlays
	 */
	public Overlay buildMarkerFromPoint(MapView map, Drawable defaultMarker, KmlDocument kmlDocument, 
			boolean supportVisibility){
		Context context = map.getContext();
		ExtendedOverlayItem item = new ExtendedOverlayItem(mName, mDescription, mCoordinates.get(0), context);
		item.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		Style style = kmlDocument.getStyle(mStyle);
		if (style != null && style.mIcon != null){
			BitmapDrawable marker = style.getFinalIcon(context);
			item.setMarker(marker);
		} else {
			item.setMarker(defaultMarker);
		}
		ArrayList<ExtendedOverlayItem> kmlPointsItems = new ArrayList<ExtendedOverlayItem>(1);
		ItemizedOverlayWithBubble<ExtendedOverlayItem> kmlPointsOverlay = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(context, 
				kmlPointsItems, map);
		kmlPointsOverlay.addItem(item);
		if (supportVisibility && !mVisibility)
			kmlPointsOverlay.setEnabled(mVisibility);
		return kmlPointsOverlay;
	}
	
	public Overlay buildPolylineFromLineString(MapView map, KmlDocument kmlDocument, boolean supportVisibility){
		Context context = map.getContext();
		Polyline lineStringOverlay = new Polyline(context);
		Style style = kmlDocument.getStyle(mStyle);
		if (style != null){
			lineStringOverlay.setPaint(style.getOutlinePaint());
		} else { 
			//set default:
			lineStringOverlay.setColor(0x90101010);
			lineStringOverlay.setWidth(5.0f);
		}
		lineStringOverlay.setPoints(mCoordinates);
		if (supportVisibility && !mVisibility)
			lineStringOverlay.setEnabled(mVisibility);
		return lineStringOverlay;
	}
	
	public Overlay buildPolygonFromPolygon(MapView map, KmlDocument kmlDocument, boolean supportVisibility){
		Context context = map.getContext();
		Polygon polygonOverlay = new Polygon(context);
		Style style = kmlDocument.getStyle(mStyle);
		Paint outlinePaint = null;
		int fillColor = 0x20101010; //default
		if (style != null){
			outlinePaint = style.getOutlinePaint();
			fillColor = style.fillColorStyle.getFinalColor();
		}
		if (outlinePaint == null){ 
			//set default:
			outlinePaint = new Paint();
			outlinePaint.setColor(0x90101010);
			outlinePaint.setStrokeWidth(5);
		}
		polygonOverlay.setFillColor(fillColor);
		polygonOverlay.setStrokeColor(outlinePaint.getColor());
		polygonOverlay.setStrokeWidth(outlinePaint.getStrokeWidth());
		polygonOverlay.setPoints(mCoordinates);
		if (mHoles != null)
			polygonOverlay.setHoles(mHoles);
		polygonOverlay.setTitle(mName);
		polygonOverlay.setSnippet(mDescription);
		if ((mName!=null && !"".equals(mName)) || (mDescription!=null && !"".equals(mDescription))){
			//TODO: cache layoutResId retrieval. 
			String packageName = context.getPackageName();
			int layoutResId = context.getResources().getIdentifier("layout/bonuspack_bubble", null, packageName);
			polygonOverlay.setInfoWindow(layoutResId, map);
		}
		if (supportVisibility && !mVisibility)
			polygonOverlay.setEnabled(mVisibility);
		return polygonOverlay;
	}
	
	public Overlay buildGroundOverlay(MapView map, KmlDocument kmlDocument){
		Context context = map.getContext();
		GroundOverlay overlay = new GroundOverlay(context);
		if (mCoordinates.size()==2){
			GeoPoint pNW = mCoordinates.get(0);
			GeoPoint pSE = mCoordinates.get(1);
			overlay.setPosition(GeoPoint.fromCenterBetween(pNW, pSE));
			GeoPoint pNE = new GeoPoint(pNW.getLatitude(), pSE.getLongitude());
			int width = pNE.distanceTo(pNW);
			GeoPoint pSW = new GeoPoint(pSE.getLatitude(), pNW.getLongitude());
			int height = pSW.distanceTo(pNW);
			overlay.setDimensions((float)width, (float)height);
		}
		//TODO: 
		//else if size=4, nonrectangular quadrilateral
		//else, error
		
		if (mIcon != null)
			overlay.setImage(new BitmapDrawable(mIcon));
		else {
			/* TODO: currently filling the canvas. 
			ColorDrawable rect = new ColorDrawable(mColor);
			rect.setAlpha(255); //transparency will be applied below. 
			overlay.setImage(rect);
			*/
		}
		
		float transparency = 1.0f - Color.alpha(mColor)/255.0f;
			//Even if not documented, KML transparency is the transparency part of "color" element. 
		overlay.setTransparency(transparency);
		overlay.setBearing(-mRotation); //from KML counterclockwise to Google Maps API which is clockwise
		overlay.setEnabled(mVisibility);
		return overlay;
	}
	
	/**
	 * Build the overlay related to this KML object. If this is a Folder, recursively build overlays from folder items. 
	 * @param context
	 * @param map
	 * @param defaultMarker to use for Points if no icon specified
	 * @param kmlDocument for styles
	 * @param supportVisibility if true, set overlays visibility according to KML visibility. If false, always set overlays as visible. 
	 * @return the overlay, depending on the KML object type: <br>
	 * 		Folder=>FolderOverlay, Point=>ItemizedOverlayWithBubble, Polygon=>Polygon, LineString=>Polyline, GroundOverlay=>GroundOverlay
	 * 		and return null if object type is UNKNOWN. 
	 */
	public Overlay buildOverlays(Context context, MapView map, Drawable defaultMarker, KmlDocument kmlDocument, 
			boolean supportVisibility){
		switch (mObjectType){
		case FOLDER:{
			FolderOverlay folderOverlay = new FolderOverlay(context);
			for (KmlFeature k:mItems){
				Overlay overlay = k.buildOverlays(context, map, defaultMarker, kmlDocument, supportVisibility);
				folderOverlay.add(overlay);
			}
			if (supportVisibility && !mVisibility)
				folderOverlay.setEnabled(false);
			return folderOverlay;
		}
		case POINT:{
			return buildMarkerFromPoint(map, defaultMarker, kmlDocument, supportVisibility);
		}
		case LINE_STRING:{
			return buildPolylineFromLineString(map, kmlDocument, supportVisibility);
		}
		case POLYGON:{
			return buildPolygonFromPolygon(map, kmlDocument, supportVisibility);
		}
		case GROUND_OVERLAY:{
			return buildGroundOverlay(map, kmlDocument);
		}
		default:
			return null;	
		}
	}
	
	/** 
	 * remove the item at itemPosition. No check for bad usage (not a Folder, or itemPosition out of rank)
	 * @param itemPosition position of the item, starting from 0. 
	 * @return item removed
	 */
	public KmlFeature removeItem(int itemPosition){
		KmlFeature removed = mItems.remove(itemPosition);
		//refresh bounding box from scratch:
		mBB = null;
		for (KmlFeature item:mItems) {
			updateBoundingBoxWith(item.mBB);
		}
		return removed;
	}
	
	protected boolean writeKMLExtendedData(Writer writer){
		if (mExtendedData == null)
			return true;
		try {
			writer.write("<ExtendedData>\n");
			for (HashMap.Entry<String, String> entry : mExtendedData.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				writer.write("<Data name=\""+name+"\"><value>"+value+"</value></Data>\n");
			}
			writer.write("</ExtendedData>\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write a list of coordinates in KML format. 
	 * @param writer
	 * @param coordinates
	 * @return false if error
	 */
	public boolean writeKMLCoordinates(Writer writer, ArrayList<GeoPoint> coordinates){
		try {
			writer.write("<coordinates>");
			for (GeoPoint coord:coordinates){
				writer.write(coord.toInvertedDoubleString());
				writer.write(' ');
			}
			writer.write("</coordinates>\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** write elements specific to GroundOverlay in KML format */
	protected void saveKMLGroundOverlay(Writer writer){
		try {
			writer.write("<color>"+ColorStyle.colorAsKMLString(mColor)+"</color>\n");
			writer.write("<Icon><href>"+mIconHref+"</href></Icon>\n");
			writer.write("<LatLonBox>");
			GeoPoint pNW = mCoordinates.get(0);
			GeoPoint pSE = mCoordinates.get(1);
			writer.write("<north>"+pNW.getLatitude()+"</north>");
			writer.write("<south>"+pSE.getLatitude()+"</south>");
			writer.write("<east>"+pSE.getLongitude()+"</east>");
			writer.write("<west>"+pNW.getLongitude()+"</west>");
			writer.write("<rotation>"+mRotation+"</rotation>");
			writer.write("</LatLonBox>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write the object in KML text format (= save as a KML text file)
	 * @param writer on which the object is written. 
	 * @param isDocument true is this feature is the root of the whole hierarchy (the "Document" folder). 
	 * @param kmlDocument containing the shared styles, that will also be written if isDocument is true. 
	 * @return false if error
	 */
	public boolean writeAsKML(Writer writer, boolean isDocument, KmlDocument kmlDocument){
		try {
			String objectType;
			String geometry = null;
			switch (mObjectType){
			case FOLDER:
				if (isDocument)
					objectType = "Document";
				else 
					objectType = "Folder";
				break;
			case POINT:
				objectType = "Placemark";
				geometry = "Point";
				break;
			case LINE_STRING:
				objectType = "Placemark";
				geometry = "LineString";
				break;
			case POLYGON:
				objectType = "Placemark";
				geometry = "Polygon";
				break;
			case GROUND_OVERLAY:
				objectType = "GroundOverlay";
				break;
			default:
				objectType = "Unknown"; //TODO - not a good error handling
				break;
			}
			writer.write('<'+objectType);
			if (mId != null)
				writer.write(" id=\"mId\"");
			writer.write(">\n");
			if (mStyle != null){
				writer.write("<styleUrl>#"+mStyle+"</styleUrl>\n");
				//TODO: if styleUrl is external, don't add the '#'
			}
			if (mName != null)
				writer.write("<name>"+mName+"</name>\n");
			if (mDescription != null)
				writer.write("<description><![CDATA["+mDescription+"]]></description>\n");
			if (!mVisibility)
				writer.write("<visibility>0</visibility>\n");
			if (geometry != null){
				writer.write("<"+geometry+">\n");
				if (mObjectType == POLYGON)
					writer.write("<outerBoundaryIs>\n<LinearRing>\n");
				if (mCoordinates != null){
					writeKMLCoordinates(writer, mCoordinates);
				}
				if (mObjectType == POLYGON){
					writer.write("</LinearRing>\n</outerBoundaryIs>\n");
					if (mHoles != null){
						for (ArrayList<GeoPoint> hole:mHoles){
							writer.write("<innerBoundaryIs>\n<LinearRing>\n");
							writeKMLCoordinates(writer, hole);
							writer.write("</LinearRing>\n</innerBoundaryIs>\n");
						}
					}
				}
				writer.write("</"+geometry+">\n");
			} else if (mObjectType == FOLDER){
				if (!mOpen)
					writer.write("<open>0</open>\n");
				for (KmlFeature item:mItems){
					item.writeAsKML(writer, false, null);
				}
			} else if (mObjectType == GROUND_OVERLAY){
				saveKMLGroundOverlay(writer);
			}
			writeKMLExtendedData(writer);
			if (isDocument){
				kmlDocument.writeKMLStyles(writer);
			}
			writer.write("</"+objectType+">\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	protected boolean writeGeoJSONProperties(Writer writer, boolean isRoot){
		try {
			writer.write("\"properties\":{");
			boolean isFirstProp = true; //handling of "," between properties
			if (mName != null){
				writer.write("\"name\":\""+mName+"\"");
				isFirstProp = false;
			}
			if (mExtendedData != null){
				for (HashMap.Entry<String, String> entry : mExtendedData.entrySet()) {
					String name = entry.getKey();
					String value = entry.getValue();
					if (isFirstProp)
						isFirstProp = false;
					else 
						writer.write(", ");
					writer.write("\""+name+"\":\""+value+"\"");
				}
			}
			writer.write("}\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** mapping with object types values */
	protected static String[] GeoJSONTypes = {"Unknown", "Point", "LineString", "Polygon", "FeatureCollection"};
	
	/** write the object on writer in GeoJSON format
	 * @return false if error
	 * @see http://geojson.org
	 */
	public boolean writeAsGeoJSON(Writer writer, boolean isRoot){
		try {
			writer.write('{');
			if (mObjectType == FOLDER){
				writer.write("\"type\": \"FeatureCollection\",\n");
				writer.write("\"features\": [\n");
				Iterator<KmlFeature> it = mItems.iterator();
				while(it.hasNext()) {
					KmlFeature item = it.next();
					if (!item.writeAsGeoJSON(writer, false))
						return false;
					if (it.hasNext())
						writer.write(',');
				}
				writer.write("],\n");
			} else if (mObjectType == POLYGON || mObjectType == POINT || mObjectType == LINE_STRING){
				writer.write("\"type\": \"Feature\",\n");
				writer.write("\"geometry\": {\n");
				writer.write("\"type\": \""+GeoJSONTypes[mObjectType]+"\",\n");
				writer.write("\"coordinates\":\n");
				if (mObjectType == LINE_STRING)
					writer.write("[");
				else if (mObjectType == POLYGON)
					writer.write("[[");
				Iterator<GeoPoint> it = mCoordinates.iterator();
				while(it.hasNext()) {
					GeoPoint coord = it.next();
					writer.write("["+coord.getLongitude()+","+coord.getLatitude()/*+","+coord.getAltitude()*/+"]");
						//don't add altitude, as OpenLayers doesn't supports it... (vertigo?)
					if (it.hasNext())
						writer.write(',');
				}
				if (mObjectType == LINE_STRING)
					writer.write("]");
				else if (mObjectType == POLYGON){
					writer.write("]]");
					//TODO: write polygon holes if any
				}
				writer.write("\n},\n");
			}
			if (!writeGeoJSONProperties(writer, isRoot))
				return false;
			if (isRoot){
				writer.write(", \"crs\":{\"type\":\"name\", \"properties\":{\"name\":\"urn:ogc:def:crs:OGC:1.3:CRS84\"}}\n");
			}
			writer.write("}\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//Cloneable implementation ------------------------------------

	public ArrayList<GeoPoint> cloneArrayOfGeoPoint(ArrayList<GeoPoint> coords){
		ArrayList<GeoPoint> result = new ArrayList<GeoPoint>(coords.size());
		for (GeoPoint p:coords)
			result.add((GeoPoint)p.clone());
		return result;
	}
	
	/** the mandatory tribute to this monument of Java stupidity */
	public KmlFeature clone(){
		KmlFeature kmlFeature = null;
		try {
			kmlFeature = (KmlFeature)super.clone();
		} catch (CloneNotSupportedException e){
			e.printStackTrace();
			return null;
		}
		if (mItems != null){
			kmlFeature.mItems = new ArrayList<KmlFeature>(mItems.size());
			for (KmlFeature item:mItems)
				kmlFeature.mItems.add(item.clone());
		}
		if (mCoordinates != null){
			kmlFeature.mCoordinates = cloneArrayOfGeoPoint(mCoordinates);
		}
		if (mHoles != null){
			kmlFeature.mHoles = new ArrayList<ArrayList<GeoPoint>>(mHoles.size());
			for (ArrayList<GeoPoint> hole:mHoles){
				kmlFeature.mHoles.add(cloneArrayOfGeoPoint(hole));
			}
		}
		if (mExtendedData != null){
			kmlFeature.mExtendedData = new HashMap<String,String>(mExtendedData.size());
			kmlFeature.mExtendedData.putAll(mExtendedData);
		}
		if (mBB != null)
			kmlFeature.mBB = new BoundingBoxE6(mBB.getLatNorthE6(), mBB.getLonEastE6(), 
				mBB.getLatSouthE6(), mBB.getLonWestE6());
		return kmlFeature;
	}

	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mObjectType);
		out.writeString(mId);
		out.writeString(mName);
		out.writeString(mDescription);
		out.writeList(mItems);
		out.writeInt(mVisibility?1:0);
		out.writeInt(mOpen?1:0);
		out.writeList(mCoordinates);
		out.writeString(mStyle);
		//TODO: mExtendedData, mHoles
		out.writeParcelable(mBB, flags);
	}
	
	public static final Parcelable.Creator<KmlFeature> CREATOR = new Parcelable.Creator<KmlFeature>() {
		@Override public KmlFeature createFromParcel(Parcel source) {
			return new KmlFeature(source);
		}
		@Override public KmlFeature[] newArray(int size) {
			return new KmlFeature[size];
		}
	};
	
	public KmlFeature(Parcel in){
		mObjectType = in.readInt();
		mId = in.readString();
		mName = in.readString();
		mDescription = in.readString();
		mItems = in.readArrayList(KmlFeature.class.getClassLoader());
		mVisibility = (in.readInt()==1);
		mOpen = (in.readInt()==1);
		mCoordinates = in.readArrayList(GeoPoint.class.getClassLoader());
		mStyle = in.readString();
		//TODO: mExtendedData, mHoles
		mBB = in.readParcelable(BoundingBoxE6.class.getClassLoader());
	}

}

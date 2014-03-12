package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML Placemark. Support the following Geometry: Point, LineString, and Polygon. 
 * @author M.Kergall
 */
public class KmlPlacemark extends KmlFeature implements Cloneable, Parcelable {
	
	/** the KML Geometry of the Placemark. Null if none. */
	public KmlGeometry mGeometry;

	/** constructs a Placemark of unknown Geometry */
	public KmlPlacemark(){
		super();
		mObjectType = PLACEMARK;
	}
	
	/**
	 * Create the KmlFeature as a KML Point.  
	 * @param position position of the point
	 */
	public KmlPlacemark(GeoPoint position){
		this();
		mGeometry = new KmlPoint(position);
		mBB = new BoundingBoxE6(position.getLatitudeE6(), position.getLongitudeE6(), position.getLatitudeE6(), position.getLongitudeE6());
	}
	
	/** Create the KML Placemark from a Marker, as a KML Point */
	public KmlPlacemark(Marker marker){
		this(marker.getPosition());
		mName = marker.getTitle();
		mDescription = marker.getSnippet();
		mVisibility = marker.isEnabled();
		//TODO: Style / IconStyle => transparency, hotspot, bearing. 
	}

	/** Create the KML Placemark from a Polygon overlay, as a KML Polygon */
	public KmlPlacemark(Polygon polygon, KmlDocument kmlDoc){
		this();
		mName = polygon.getTitle();
		mDescription = polygon.getSnippet();
		mGeometry = new KmlPolygon();
		mGeometry.mCoordinates = (ArrayList<GeoPoint>)polygon.getPoints();
		((KmlPolygon)mGeometry).mHoles = (ArrayList<ArrayList<GeoPoint>>)polygon.getHoles();
		mBB = BoundingBoxE6.fromGeoPoints(mGeometry.mCoordinates);
		mVisibility = polygon.isEnabled();
		//Style:
		Style style = new Style();
		style.mPolyStyle = new ColorStyle(polygon.getFillColor());
		style.mLineStyle = new LineStyle(polygon.getStrokeColor(), polygon.getStrokeWidth());
		mStyle = kmlDoc.addStyle(style);
	}

	/** Create the KML Placemark from a Polyline overlay, as a KML LineString */
	public KmlPlacemark(Polyline polyline, KmlDocument kmlDoc){
		this();
		mName = "LineString - "+polyline.getNumberOfPoints()+" points";
		mGeometry = new KmlLineString();
		mGeometry.mCoordinates = (ArrayList<GeoPoint>)polyline.getPoints();
		mBB = BoundingBoxE6.fromGeoPoints(mGeometry.mCoordinates);
		mVisibility = polyline.isEnabled();
		//Style:
		Style style = new Style();
		style.mLineStyle = new LineStyle(polyline.getColor(), polyline.getWidth());
		mStyle = kmlDoc.addStyle(style);
	}

	/** GeoJSON constructor */
	public KmlPlacemark(JSONObject json){
		this();
		mId = json.optString("id");
		JSONObject geometry = json.optJSONObject("geometry");
		if (geometry != null) {
			mGeometry = KmlGeometry.parseGeoJSON(geometry);
        }
		//Parse properties:
		JSONObject properties = json.optJSONObject("properties");
		Iterator<?> keys = properties.keys();
		while (keys.hasNext()){
			String key = (String)keys.next();
			String value = properties.optString(key);
			if (key!=null && value!=null)
				setExtendedData(key, value);
		}
		//Put "name" property in standard KML format:
		if (mExtendedData!=null && mExtendedData.containsKey("name")){
			mName = mExtendedData.get("name");
			mExtendedData.remove("name");
		}
	}
	
	/** default listener for dragging a marker built from a KML Point */
	public class OnKMLMarkerDragListener implements OnMarkerDragListener {
		@Override public void onMarkerDrag(Marker marker) {}
		@Override public void onMarkerDragEnd(Marker marker) {
			KmlFeature feature = (KmlFeature)marker.getRelatedObject();
			if (feature != null && feature.isA(PLACEMARK)){
				KmlPlacemark placemark = (KmlPlacemark)feature;
				if (placemark.isA(KmlGeometry.POINT)){
					KmlPoint point = (KmlPoint)placemark.mGeometry;
					point.setPosition(marker.getPosition());
				}
			}
		}
		@Override public void onMarkerDragStart(Marker marker) {}		
	}
	
	/** from a Placemark feature having a Point geometry, build a Marker overlay
	 * @param map
	 * @param defaultIcon default icon to be used if no style or no icon specified. If null, default osmdroid marker icon will be used. 
	 * @param kmlDocument
	 * @param supportVisibility
	 * @return the Marker overlay
	 * @see buildOverlay
	 */
	public Overlay buildMarkerFromPoint(MapView map, Drawable defaultIcon, KmlDocument kmlDocument, 
			boolean supportVisibility){
		Context context = map.getContext();
		Marker marker = new Marker(map);
		marker.setTitle(mName);
		marker.setSnippet(mDescription);
		marker.setPosition(((KmlPoint)mGeometry).getPosition());
		Style style = kmlDocument.getStyle(mStyle);
		if (style != null && style.mIconStyle != null){
			style.mIconStyle.styleMarker(marker, context);
		} else {
			if (defaultIcon != null)
				marker.setIcon(defaultIcon);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		}
		//keep the link from the marker to the KML feature:
		marker.setRelatedObject(this);
		//allow marker drag, acting on KML Point:
		marker.setDraggable(true);
		marker.setOnMarkerDragListener(new OnKMLMarkerDragListener());
		if (supportVisibility && !mVisibility)
			marker.setEnabled(mVisibility);
		return marker;
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
		lineStringOverlay.setPoints(mGeometry.mCoordinates);
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
			fillColor = style.mPolyStyle.getFinalColor();
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
		polygonOverlay.setPoints(mGeometry.mCoordinates);
		if (((KmlPolygon)mGeometry).mHoles != null)
			polygonOverlay.setHoles(((KmlPolygon)mGeometry).mHoles);
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
	
	@Override public Overlay buildOverlay(MapView map, Drawable defaultIcon, KmlDocument kmlDocument, 
			boolean supportVisibility){
		switch (mGeometry.mType){
			case KmlGeometry.POINT:
				return buildMarkerFromPoint(map, defaultIcon, kmlDocument, supportVisibility);
			case KmlGeometry.LINE_STRING:
				return buildPolylineFromLineString(map, kmlDocument, supportVisibility);
			case KmlGeometry.POLYGON:
				return buildPolygonFromPolygon(map, kmlDocument, supportVisibility);
			default:
				return null;
		}
	}
	
	@Override public void writeKMLSpecifics(Writer writer){
		if (mGeometry != null)
			mGeometry.saveAsKML(writer);
	}
	
	protected JSONObject geoJSONProperties(){
		try {
			JSONObject json = new JSONObject();
			if (mName != null){
				json.put("name", mName);
			}
			if (mExtendedData != null){
				for (HashMap.Entry<String, String> entry : mExtendedData.entrySet()) {
					String name = entry.getKey();
					String value = entry.getValue();
					json.put(name, value);
				}
			}
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** @return this as a GeoJSON object. */
	@Override public JSONObject asGeoJSON(boolean isRoot){
		JSONObject json = new JSONObject();
		try {
			json.put("type", "Feature");
			if (mId != null)
				json.put("id", mId);
			json.put("geometry", mGeometry.asGeoJSON());
			json.put("properties", geoJSONProperties());
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Cloneable implementation ------------------------------------

	@Override public KmlPlacemark clone(){
		KmlPlacemark kmlPlacemark = (KmlPlacemark)super.clone();
		if (mGeometry != null)
			kmlPlacemark.mGeometry = mGeometry.clone();
		return kmlPlacemark;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeParcelable(mGeometry, flags);
	}
	
	public static final Parcelable.Creator<KmlPlacemark> CREATOR = new Parcelable.Creator<KmlPlacemark>() {
		@Override public KmlPlacemark createFromParcel(Parcel source) {
			return new KmlPlacemark(source);
		}
		@Override public KmlPlacemark[] newArray(int size) {
			return new KmlPlacemark[size];
		}
	};
	
	public KmlPlacemark(Parcel in){
		super(in);
		in.readParcelable(KmlGeometry.class.getClassLoader());
	}
}

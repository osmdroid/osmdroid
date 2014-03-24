package org.osmdroid.bonuspack.kml;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
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
	}
	
	/**
	 * Create the KmlFeature as a KML Point.  
	 * @param position position of the point
	 */
	public KmlPlacemark(GeoPoint position){
		this();
		mGeometry = new KmlPoint(position);
		mBB = mGeometry.getBoundingBox();
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
		mBB = mGeometry.getBoundingBox();
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
		mBB = mGeometry.getBoundingBox();
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
			if (mGeometry != null)
				mBB = mGeometry.getBoundingBox();
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
	
	@Override public Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlDocument kmlDocument){
		if (mGeometry != null)
			return mGeometry.buildOverlay(map, defaultStyle, styler, this, kmlDocument);
		else 
			return null;
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

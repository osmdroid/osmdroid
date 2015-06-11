package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import org.osmdroid.bonuspack.kml.KmlFeature.Styler;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML Geometry. This is an abstract class. 
 * Supported subclasses are: KmlPoint, KmlLineString, KmlPolygon and KmlMultiGeometry
 * @author M.Kergall
 *
 */
public abstract class KmlGeometry implements Cloneable, Parcelable {
	
	/** KML id attribute, if any. Null if none. */
	public String mId;
	/** coordinates of the geometry. If Point, one and only one entry. */
	public ArrayList<GeoPoint> mCoordinates;
	
	//-----------------------------------------------------
	// abstract methods
	public abstract void saveAsKML(Writer writer);
	public abstract JsonObject asGeoJSON();
	public abstract Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlPlacemark kmlPlacemark, KmlDocument kmlDocument);
	public abstract BoundingBoxE6 getBoundingBox();

	//-----------------------------------------------------
	
	public KmlGeometry(){
	}

	/**
	 * Write a list of coordinates in KML format. 
	 * @param writer
	 * @param coordinates
	 * @return false if error
	 */
	public static boolean writeKMLCoordinates(Writer writer, ArrayList<GeoPoint> coordinates){
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

	/**
	 * Build a Position in GeoJSON format. 
	 * @param position
	 * @return the GeoJSON position. 
	 */
	public static JsonArray geoJSONPosition(GeoPoint position){
		JsonArray json = new JsonArray();
		json.add(new JsonPrimitive(position.getLongitude()));
		json.add(new JsonPrimitive(position.getLatitude()));
		//json.add(new JsonPrimitive(coord.getAltitude())); //don't add altitude, as OpenLayers doesn't supports it... (vertigo?)
		return json;
	}
	
	/**
	 * Build an array of Positions in GeoJSON format. 
	 * @param coordinates
	 * @return the GeoJSON array of Positions. 
	 */
	public static JsonArray geoJSONCoordinates(ArrayList<GeoPoint> coordinates){
		JsonArray json = new JsonArray();
		Iterator<GeoPoint> it = coordinates.iterator();
		while(it.hasNext()) {
			GeoPoint position = it.next();
			json.add(KmlGeometry.geoJSONPosition(position));
		}
		return json;
	}
	
	public static ArrayList<GeoPoint> cloneArrayOfGeoPoint(ArrayList<GeoPoint> coords){
		ArrayList<GeoPoint> result = new ArrayList<GeoPoint>(coords.size());
		for (GeoPoint p:coords)
			result.add((GeoPoint)p.clone());
		return result;
	}
	
	/** parse a GeoJSON Position: [longitude, latitude, altitude(optional)] */
	public static GeoPoint parseGeoJSONPosition(JsonArray json){
		double alt = (json.size()>=3 ? json.get(2).getAsDouble() : 0.0);
		return new GeoPoint(json.get(1).getAsDouble(), 
				json.get(0).getAsDouble(), 
				alt);
	}
	
	/** parse a GeoJSON array of Positions: [ [lon, lat, alt],... [lon, lat, alt] ] */
	public static ArrayList<GeoPoint> parseGeoJSONPositions(JsonArray json){
		if (json == null)
			return null;
		ArrayList<GeoPoint> coordinates = new  ArrayList<GeoPoint>(json.size());
		for (int i=0; i<json.size(); i++){
			JsonArray position = json.get(i).getAsJsonArray();
			GeoPoint p = KmlGeometry.parseGeoJSONPosition(position);
			if (p != null)
				coordinates.add(p);
		}
		return coordinates;
	}
	
	/** parse a GeoJSON Geometry. 
	 * Supports: Point, LineString, Polygon, GeometryCollection and MultiPoint. 
	 * @return the corresponding KmlGeometry, or null for not supported Geometry. 
	 */
	public static KmlGeometry parseGeoJSON(JsonObject json){
		if (json == null)
			return null;
		String type = json.get("type").getAsString();
		if ("Point".equals(type)){
			return new KmlPoint(json);
		} else if ("LineString".equals(type)){
			return new KmlLineString(json);
		} else if ("Polygon".equals(type)){
			return new KmlPolygon(json);
		} else if ("GeometryCollection".equals(type) || "MultiPoint".equals(type)){
			return new KmlMultiGeometry(json);
		} else 
			return null;
	}
	
	//Cloneable implementation ------------------------------------
	
	@Override public KmlGeometry clone(){
		KmlGeometry kmlGeometry = null;
		try {
			kmlGeometry = (KmlGeometry)super.clone();
		} catch (CloneNotSupportedException e){
			e.printStackTrace();
			return null;
		}
		if (mCoordinates != null)
			kmlGeometry.mCoordinates = cloneArrayOfGeoPoint(mCoordinates);
		return kmlGeometry;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeString(mId);
		out.writeList(mCoordinates);
	}
	
	public KmlGeometry(Parcel in){
		mId = in.readString();
		mCoordinates = in.readArrayList(GeoPoint.class.getClassLoader());
	}
}

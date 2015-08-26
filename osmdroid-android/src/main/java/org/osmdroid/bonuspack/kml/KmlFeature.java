package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import com.google.gson.JsonObject;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Java representation of a KML Feature. 
 * It currently supports: Folder, Document, GroundOverlay, and Placemark with the following Geometry: Point, LineString, or Polygon. <br>
 * This is an abstract class, actual Features must use sub-classes:
 * 	- Folder feature is a KmlFolder. <br>
 * 	- Document feature is a KmlFolder, handled exactly like a Folder. <br>
 * 	- GroundOverlay feature is a KmlGroundOverlay. 
 * 	- Placemark feature is a KmlPlacemark. <br>
 * 
 * @see KmlDocument
 * @see <a href="https://developers.google.com/kml/documentation/kmlreference">KML Reference</a>
 * 
 * @author M.Kergall
 */
public abstract class KmlFeature implements Parcelable, Cloneable {

	/** feature id attribute, if any. Null if none. */
	public String mId;
	/** name tag */
	public String mName;
	/** description tag */
	public String mDescription;
	/** visibility tag */
	public boolean mVisibility;
	/** open tag */
	public boolean mOpen;
	/** styleUrl (without the #) */
	public String mStyle;
	/** ExtendedData, as a HashMap of (name, value). 
	 * Can be null if the feature has no ExtendedData. 
	 * The KML displayName is not handled. 
	 * value is always stored as a Java String. */
	public HashMap<String, String> mExtendedData;
	
	//-----------------------------------------------------
	//abstract methods
	
	/** @return the bounding box of all contained geometries - null if no geometry */
	public abstract BoundingBoxE6 getBoundingBox();
	
	/**
	 * Build the Overlay related to this KML object. If this is a Folder, recursively build overlays from Folder items. 
	 * Styling strategy is following this order of priority: 
	 *  1) the styler 2) or if null, style of the Feature, 3) or if not defined, defaultStyle 4) or if null, hard-coded default values. 
	 * @param map
	 * @param defaultStyle to apply when an Feature has no Style defined. 
	 * @param styler Styler that will be applied to Features and Geometries. 
	 * @param kmlDocument for styles
	 * @return the Overlay related to this KML object class. 
	 */
	public abstract Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlDocument kmlDocument);

	/** 
	 * When building Overlays, a custom Styler can be defined to perform specific actions on specific objects. 
	 * Each method is called just after Overlay creation. 
	 * If a Styler is defined, no styling is applied by default. 
	 * Note that an applyDefaultStyling method is available, to perform default styling if needed. */
	 public interface Styler {
		/** called on each KmlFeature, except KmlPlacemark */
		abstract void onFeature(Overlay overlay, KmlFeature kmlFeature);
		/** called on each KmlPoint */
		abstract void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint);
		/** called on each KmlPoint */
		abstract void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString);
		/** called on each KmlPoint */
		abstract void onPolygon(Polygon polygon, KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon);
	}
	
	/** write KML content specific to its type */
	public abstract void writeKMLSpecifics(Writer writer);

	/** return this as GeoJSON object */
	public abstract JsonObject asGeoJSON(boolean isRoot);
	
	//-----------------------------------------------------
	
	/** default constructor */
	public KmlFeature(){
		mVisibility = true;
		mOpen = true;
	}

	/**
	 * @param C KmlGeometry subclass to compare
	 * @return true if this a KML Placemark containing a KML Geometry of class C. 
	 */
	public boolean hasGeometry(Class<? extends KmlGeometry> C){
		if (!(this instanceof KmlPlacemark))
			return false;
		KmlPlacemark placemark = (KmlPlacemark)this;
		KmlGeometry geometry = placemark.mGeometry;
		if (geometry == null)
			return false;
		return C.isInstance(geometry);
	}
	
	/**
	 * @param name
	 * @return the value associated to this name, or null if none. 
	 */
	public String getExtendedData(String name){
		if (mExtendedData == null)
			return null;
		else 
			return mExtendedData.get(name);
	}

	/**
	 * @return Extended Data as a list of lines: "name=value". Return null if none. 
	 */
	public String getExtendedDataAsText(){
		if (mExtendedData == null)
			return null;
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> entry : mExtendedData.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			result.append(name+"="+value+"<br>\n");
		}
		if (result.length() > 0)
			return result.toString();
		else 
			return null;
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

	protected boolean writeKMLExtendedData(Writer writer){
		if (mExtendedData == null)
			return true;
		try {
			writer.write("<ExtendedData>\n");
			for (Map.Entry<String, String> entry : mExtendedData.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				writer.write("<Data name=\""+name+"\"><value>"+StringEscapeUtils.escapeXml10(value)+"</value></Data>\n");
			}
			writer.write("</ExtendedData>\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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
			//TODO: push this code in each subclass
			String objectType;
			if (this instanceof KmlFolder){
				if (isDocument)
					objectType = "Document";
				else 
					objectType = "Folder";
			} else if (this instanceof KmlPlacemark)
				objectType = "Placemark";
			else if (this instanceof KmlGroundOverlay)
				objectType = "GroundOverlay";
			else
				return false;
			writer.write('<'+objectType);
			if (mId != null)
				writer.write(" id=\"mId\"");
			writer.write(">\n");
			if (mStyle != null){
				writer.write("<styleUrl>#"+mStyle+"</styleUrl>\n");
				//TODO: if styleUrl is external, don't add the '#'
			}
			if (mName != null){
				writer.write("<name>"+StringEscapeUtils.escapeXml10(mName)+"</name>\n");
			}
			if (mDescription != null){
				writer.write("<description><![CDATA["+mDescription+"]]></description>\n");
			}
			if (!mVisibility){
				writer.write("<visibility>0</visibility>\n");
			}
			writeKMLSpecifics(writer);
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
	
	public static KmlFeature parseGeoJSON(JsonObject json){
		if (json == null)
			return null;
		String type = json.get("type").getAsString();
		if ("FeatureCollection".equals(type)){
			return new KmlFolder(json);
		} else if ("Feature".equals(type)){
			return new KmlPlacemark(json);
		} else 
			return null;
	}
	
	//Cloneable implementation ------------------------------------

	/** the mandatory tribute to this monument of Java stupidity */
	public KmlFeature clone(){
		KmlFeature kmlFeature = null;
		try {
			kmlFeature = (KmlFeature)super.clone();
		} catch (CloneNotSupportedException e){
			e.printStackTrace();
			return null;
		}
		if (mExtendedData != null){
			kmlFeature.mExtendedData = new HashMap<String,String>(mExtendedData.size());
			kmlFeature.mExtendedData.putAll(mExtendedData);
		}
		return kmlFeature;
	}

	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeString(mId);
		out.writeString(mName);
		out.writeString(mDescription);
		out.writeInt(mVisibility?1:0);
		out.writeInt(mOpen?1:0);
		out.writeString(mStyle);
		//TODO: mExtendedData
	}
	
	public KmlFeature(Parcel in){
		mId = in.readString();
		mName = in.readString();
		mDescription = in.readString();
		mVisibility = (in.readInt()==1);
		mOpen = (in.readInt()==1);
		mStyle = in.readString();
		//TODO: mExtendedData
	}

}

package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Java representation of a KML Feature. 
 * It currently supports: Folder, Document, GroundOverlay, and Placemark with the following Geometry: Point, LineString, or Polygon. <br>
 * This is an abstract class, real Features must use sub-classes. 
 * Each KmlFeature has an object type (mObjectType). <br>
 * 	- Folder feature is a KmlFolder, and its object type = FOLDER. <br>
 * 	- Document feature is handled exactly like a Folder (KmlFolder, object type = FOLDER). <br>
 * 	- GroundOverlay feature is a KmlGroundOverlay, and its object type = GROUND_OVERLAY. 
 * 	- Placemark feature is a KmlPlacemark, its object type is according to its geometry: POINT, LINE_STRING, or POLYGON. 
 * 	  It contains both the Placemark attributes and the Geometry attributes. <br>
 * 	- UNKNOWN object type is reserved for default, issues/errors, or unsupported features/geometries. 
 * 
 * @see KmlDocument
 * @see https://developers.google.com/kml/documentation/kmlreference
 * 
 * @author M.Kergall
 */
public abstract class KmlFeature implements Parcelable, Cloneable {
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
	/** bounding box - null if no geometry (means empty) */
	public BoundingBoxE6 mBB;
	
	/** default constructor: create an UNKNOWN object */
	public KmlFeature(){
		mObjectType = UNKNOWN;
		mVisibility=true;
		mOpen=true;
	}
	
	public boolean isA(int objectType){
		return(mObjectType == objectType);
	}
	
	public boolean isAPlacemark(){
		return (isA(POLYGON) || isA(POINT) || isA(LINE_STRING));
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

	/**
	 * Build the overlay related to this KML object. If this is a Folder, recursively build overlays from folder items. 
	 * @param map
	 * @param defaultIcon to use for Points if no icon specified. If null, default osmdroid marker icon will be used. 
	 * @param kmlDocument for styles
	 * @param supportVisibility if true, set overlays visibility according to KML visibility. If false, always set overlays as visible. 
	 * @return the overlay, depending on the KML object type: <br>
	 * 		Folder=>FolderOverlay, Point=>Marker, Polygon=>Polygon, LineString=>Polyline, GroundOverlay=>GroundOverlay
	 * 		and return null if object type is UNKNOWN. 
	 */
	public abstract Overlay buildOverlay(MapView map, Drawable defaultIcon, KmlDocument kmlDocument, boolean supportVisibility);
	
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

	/** write KML content specific to its type */
	abstract void saveKMLSpecifics(Writer writer);
	
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
			switch (mObjectType){
			case FOLDER:
				if (isDocument)
					objectType = "Document";
				else 
					objectType = "Folder";
				break;
			case POINT:
			case LINE_STRING:
			case POLYGON:
				objectType = "Placemark";
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
			saveKMLSpecifics(writer);
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
	
	abstract public boolean writeGeoJSONSpecifics(Writer writer);
	
	/** write the object on writer in GeoJSON format
	 * @return false if error
	 * @see http://geojson.org
	 */
	public boolean writeAsGeoJSON(Writer writer, boolean isRoot){
		try {
			writer.write('{');
			writeGeoJSONSpecifics(writer);
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
		out.writeInt(mVisibility?1:0);
		out.writeInt(mOpen?1:0);
		out.writeString(mStyle);
		//TODO: mExtendedData
		out.writeParcelable(mBB, flags);
	}
	
	/*
	public static final Parcelable.Creator<KmlFeature> CREATOR = new Parcelable.Creator<KmlFeature>() {
		@Override public KmlFeature createFromParcel(Parcel source) {
			return new KmlFeature(source);
		}
		@Override public KmlFeature[] newArray(int size) {
			return new KmlFeature[size];
		}
	};
	*/
	
	public KmlFeature(Parcel in){
		mObjectType = in.readInt();
		mId = in.readString();
		mName = in.readString();
		mDescription = in.readString();
		mVisibility = (in.readInt()==1);
		mOpen = (in.readInt()==1);
		mStyle = in.readString();
		//TODO: mExtendedData
		mBB = in.readParcelable(BoundingBoxE6.class.getClassLoader());
	}

}

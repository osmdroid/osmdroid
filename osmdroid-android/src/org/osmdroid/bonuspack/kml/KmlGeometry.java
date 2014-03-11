package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML Geometry. This is an abstract class. 
 * @author M.Kergall
 *
 */
public abstract class KmlGeometry implements Cloneable, Parcelable {
	/** possible KML Geometry type */
	public final static int UNKNOWN=0, POINT=1, LINE_STRING=2, POLYGON=3;
	
	/** KML Geometry type */
	public int mType;
	/** id attribute, if any. Null if none. */
	public String mId;
	/** coordinates of the geometry. If Point, one and only one entry. */
	public ArrayList<GeoPoint> mCoordinates;
	
	public KmlGeometry(){
		mType = UNKNOWN;
	}
	
	public boolean isA(int geomType){
		return (mType == geomType);
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

	/**
	 * Write a list of coordinates in GeoJSON format. 
	 * @param writer
	 * @param coordinates
	 * @return false if error
	 */
	public static boolean writeGeoJSONCoordinates(Writer writer, ArrayList<GeoPoint> coordinates){
		try {
			Iterator<GeoPoint> it = coordinates.iterator();
			while(it.hasNext()) {
				GeoPoint coord = it.next();
				writer.write("["+coord.getLongitude()+","+coord.getLatitude()/*+","+coord.getAltitude()*/+"]");
					//don't add altitude, as OpenLayers doesn't supports it... (vertigo?)
				if (it.hasNext())
					writer.write(',');
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static ArrayList<GeoPoint> cloneArrayOfGeoPoint(ArrayList<GeoPoint> coords){
		ArrayList<GeoPoint> result = new ArrayList<GeoPoint>(coords.size());
		for (GeoPoint p:coords)
			result.add((GeoPoint)p.clone());
		return result;
	}
	
	// abstract methods
	public abstract void saveAsKML(Writer writer);
	public abstract boolean writeAsGeoJSON(Writer writer);
	
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
		out.writeInt(mType);
		out.writeString(mId);
		out.writeList(mCoordinates);
	}
	
	public KmlGeometry(Parcel in){
		mType = in.readInt();
		mId = in.readString();
		mCoordinates = in.readArrayList(GeoPoint.class.getClassLoader());
	}
}

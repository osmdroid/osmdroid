package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

public class KmlLineString extends KmlGeometry {
	
	public KmlLineString(){
		mType = LINE_STRING;
	}
	
	@Override public void saveAsKML(Writer writer){
		try {
			writer.write("<LineString>\n");
			writeKMLCoordinates(writer, mCoordinates);
			writer.write("</LineString>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override public boolean writeAsGeoJSON(Writer writer){
		try {
			writer.write("\"geometry\": {\n");
			writer.write("\"type\": \"LineString\",\n");
			writer.write("\"coordinates\":\n");
			writer.write("[");
			KmlGeometry.writeGeoJSONCoordinates(writer, mCoordinates);
			writer.write("]");
			writer.write("\n},\n");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//Cloneable implementation ------------------------------------
	
	@Override public KmlLineString clone(){
		KmlLineString kmlLineString = (KmlLineString)super.clone();
		return kmlLineString;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
	}
	
	public static final Parcelable.Creator<KmlLineString> CREATOR = new Parcelable.Creator<KmlLineString>() {
		@Override public KmlLineString createFromParcel(Parcel source) {
			return new KmlLineString(source);
		}
		@Override public KmlLineString[] newArray(int size) {
			return new KmlLineString[size];
		}
	};
	
	public KmlLineString(Parcel in){
		super(in);
	}
}

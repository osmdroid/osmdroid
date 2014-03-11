package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

public class KmlPoint extends KmlGeometry implements Parcelable, Cloneable {

	public KmlPoint(){
		super();
		mType = POINT;
	}
	
	public KmlPoint(GeoPoint position){
		this();
		mCoordinates = new ArrayList<GeoPoint>(1);
		mCoordinates.add(position);
	}
	
	public void setPosition(GeoPoint position){
		mCoordinates.set(0, position);
	}
	
	public GeoPoint getPosition(){
		return mCoordinates.get(0);
	}
	
	@Override public void saveAsKML(Writer writer){
		try {
			writer.write("<Point>\n");
			writeKMLCoordinates(writer, mCoordinates);
			writer.write("</Point>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override public boolean writeAsGeoJSON(Writer writer){
		try {
			writer.write("\"geometry\": {\n");
			writer.write("\"type\": \"Point\",\n");
			writer.write("\"coordinates\":\n");
			KmlGeometry.writeGeoJSONCoordinates(writer, mCoordinates);
			writer.write("\n},\n");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//Cloneable implementation ------------------------------------
	
	@Override public KmlPoint clone(){
		KmlPoint kmlPoint = (KmlPoint)super.clone();
		return kmlPoint;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
	}
	
	public static final Parcelable.Creator<KmlPoint> CREATOR = new Parcelable.Creator<KmlPoint>() {
		@Override public KmlPoint createFromParcel(Parcel source) {
			return new KmlPoint(source);
		}
		@Override public KmlPoint[] newArray(int size) {
			return new KmlPoint[size];
		}
	};
	
	public KmlPoint(Parcel in){
		super(in);
	}
}

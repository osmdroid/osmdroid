package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
	
	/** GeoJSON constructor */
	public KmlPoint(JSONObject json){
		this();
		mCoordinates = new ArrayList<GeoPoint>(1);
		JSONArray coordinates = json.optJSONArray("coordinates");
		if (coordinates != null){
			mCoordinates.add(KmlGeometry.parseGeoJSONPosition(coordinates));
		}
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
	
	@Override public JSONObject asGeoJSON(){
		try {
			JSONObject json = new JSONObject();
			json.put("type", "Point");
			json.put("coordinates", KmlGeometry.geoJSONPosition(mCoordinates.get(0)));
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
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

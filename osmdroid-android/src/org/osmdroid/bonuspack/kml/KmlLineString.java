package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;

public class KmlLineString extends KmlGeometry {
	
	public KmlLineString(){
		mType = LINE_STRING;
	}
	
	public KmlLineString(JSONObject json){
		this();
		JSONArray coordinates = json.optJSONArray("coordinates");
		mCoordinates = KmlGeometry.parseGeoJSONPositions(coordinates);
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
	
	@Override public JSONObject asGeoJSON(){
		try {
			JSONObject json = new JSONObject();
			json.put("type", "LineString");
			json.put("coordinates", KmlGeometry.geoJSONCoordinates(mCoordinates));
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
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

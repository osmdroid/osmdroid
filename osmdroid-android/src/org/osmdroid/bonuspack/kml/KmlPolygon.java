package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

public class KmlPolygon extends KmlGeometry {
	
	/** Polygon holes (can be null) */
	public ArrayList<ArrayList<GeoPoint>> mHoles;

	public KmlPolygon(){
		mType = POLYGON;
	}
	
	@Override public void saveAsKML(Writer writer){
		try {
			writer.write("<Polygon>\n");
			writer.write("<outerBoundaryIs>\n<LinearRing>\n");
			writeKMLCoordinates(writer, mCoordinates);
			writer.write("</LinearRing>\n</outerBoundaryIs>\n");
			if (mHoles != null){
				for (ArrayList<GeoPoint> hole:mHoles){
					writer.write("<innerBoundaryIs>\n<LinearRing>\n");
					writeKMLCoordinates(writer, hole);
					writer.write("</LinearRing>\n</innerBoundaryIs>\n");
				}
			}
			writer.write("</Polygon>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override public boolean writeAsGeoJSON(Writer writer){
		try {
			writer.write("\"geometry\": {\n");
			writer.write("\"type\": \"Polygon\",\n");
			writer.write("\"coordinates\":\n");
			writer.write("[[");
			KmlGeometry.writeGeoJSONCoordinates(writer, mCoordinates);
			writer.write("]]");
			//TODO: write polygon holes if any
			writer.write("\n},\n");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//Cloneable implementation ------------------------------------

	@Override public KmlPolygon clone(){
		KmlPolygon kmlPolygon = (KmlPolygon)super.clone();
		if (mHoles != null){
			kmlPolygon.mHoles = new ArrayList<ArrayList<GeoPoint>>(mHoles.size());
			for (ArrayList<GeoPoint> hole:mHoles){
				kmlPolygon.mHoles.add(cloneArrayOfGeoPoint(hole));
			}
		}
		return kmlPolygon;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		if (mHoles != null){
			out.writeInt(mHoles.size());
			for (ArrayList<GeoPoint> l:mHoles)
				out.writeList(l);
		} else 
			out.writeInt(0);
	}
	
	public static final Parcelable.Creator<KmlPolygon> CREATOR = new Parcelable.Creator<KmlPolygon>() {
		@Override public KmlPolygon createFromParcel(Parcel source) {
			return new KmlPolygon(source);
		}
		@Override public KmlPolygon[] newArray(int size) {
			return new KmlPolygon[size];
		}
	};
	
	public KmlPolygon(Parcel in){
		super(in);
		int holes = in.readInt();
		if (holes != 0){
			mHoles = new ArrayList<ArrayList<GeoPoint>>(holes);
			for (int i=0; i<holes; i++){
				ArrayList<GeoPoint> l = in.readArrayList(GeoPoint.class.getClassLoader());
				mHoles.add(l);
			}
		}
	}
	
}

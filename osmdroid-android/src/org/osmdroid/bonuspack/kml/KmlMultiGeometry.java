package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.kml.KmlFeature.Styler;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML MultiGeometry and/or GeoJSON GeometryCollection. 
 * It can also parse GeoJSON MultiPoint. 
 * @author M.Kergall
 */
public class KmlMultiGeometry extends KmlGeometry implements Cloneable, Parcelable {

	/** list of KmlGeometry items. Can be empty if none, but is not null */
	public ArrayList<KmlGeometry> mItems;
	
	public KmlMultiGeometry(){
		super();
		mItems = new ArrayList<KmlGeometry>();
	}

	/** GeoJSON constructor */
	public KmlMultiGeometry(JSONObject json){
		this();
		String type = json.optString("type");
		if ("GeometryCollection".equals(type)){
			JSONArray geometries = json.optJSONArray("geometries");
	        if (geometries != null) {
	            for (int i=0; i<geometries.length(); i++) {
	                JSONObject geometrieJSON = geometries.optJSONObject(i);
	                if (geometrieJSON != null) {
	                    mItems.add(KmlGeometry.parseGeoJSON(geometrieJSON));
	                }
	            }
	        }
		} else if ("MultiPoint".equals(type)){
			JSONArray coordinates = json.optJSONArray("coordinates");
			ArrayList<GeoPoint> positions = parseGeoJSONPositions(coordinates);
			for (GeoPoint p:positions){
				KmlPoint kmlPoint = new KmlPoint(p);
				mItems.add(kmlPoint);
			}
		}
	}
	
	public void addItem(KmlGeometry item){
		mItems.add(item);
	}
	
	/** Build a FolderOverlay containing all overlays from this MultiGeometry items */
	@Override public Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlPlacemark kmlPlacemark, 
			KmlDocument kmlDocument){
		Context context = map.getContext();
		FolderOverlay folderOverlay = new FolderOverlay(context);
		for (KmlGeometry k:mItems){
			Overlay overlay = k.buildOverlay(map, defaultStyle, styler, kmlPlacemark, kmlDocument);
			folderOverlay.add(overlay);
		}
		return folderOverlay;
	}
	
	@Override public void saveAsKML(Writer writer) {
		try {
			writer.write("<MultiGeometry>\n");
			for (KmlGeometry item:mItems)
				item.saveAsKML(writer);
			writer.write("</MultiGeometry>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override public JSONObject asGeoJSON() {
		try {
			JSONObject json = new JSONObject();
			json.put("type", "GeometryCollection");
			JSONArray geometries = new JSONArray();
			for (KmlGeometry item:mItems)
				geometries.put(item.asGeoJSON());
			json.put("geometries", geometries);
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override public BoundingBoxE6 getBoundingBox(){
		BoundingBoxE6 finalBB = null;
		for (KmlGeometry item:mItems){
			BoundingBoxE6 itemBB = item.getBoundingBox();
			if (itemBB != null){
				if (finalBB == null){
					finalBB = BonusPackHelper.cloneBoundingBoxE6(itemBB);
				} else {
					finalBB = BonusPackHelper.concatBoundingBoxE6(itemBB, finalBB);
				}
			}
		}
		return finalBB;
	}
	
	//Cloneable implementation ------------------------------------
	
	@Override public KmlMultiGeometry clone(){
		KmlMultiGeometry kmlMultiGeometry = (KmlMultiGeometry)super.clone();
		kmlMultiGeometry.mItems = new ArrayList<KmlGeometry>(mItems.size());
		for (KmlGeometry item:mItems)
			kmlMultiGeometry.mItems.add(item.clone());
		return kmlMultiGeometry;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeList(mItems);
	}
	
	public static final Parcelable.Creator<KmlMultiGeometry> CREATOR = new Parcelable.Creator<KmlMultiGeometry>() {
		@Override public KmlMultiGeometry createFromParcel(Parcel source) {
			return new KmlMultiGeometry(source);
		}
		@Override public KmlMultiGeometry[] newArray(int size) {
			return new KmlMultiGeometry[size];
		}
	};
	
	public KmlMultiGeometry(Parcel in){
		super(in);
		mItems = in.readArrayList(KmlGeometry.class.getClassLoader());
	}
}

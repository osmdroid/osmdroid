package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import org.osmdroid.bonuspack.kml.KmlFeature.Styler;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
	public KmlMultiGeometry(JsonObject json){
		this();
		String type = json.get("type").getAsString();
		if ("GeometryCollection".equals(type)){
			JsonArray geometries = json.get("geometries").getAsJsonArray();
	        if (geometries != null) {
	            for (JsonElement geometrieJSON:geometries) {
	            	mItems.add(parseGeoJSON(geometrieJSON.getAsJsonObject()));
	            }
	        }
		} else if ("MultiPoint".equals(type)){
			JsonArray coordinates = json.get("coordinates").getAsJsonArray();
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

	@Override public JsonObject asGeoJSON() {
		JsonObject json = new JsonObject();
		json.addProperty("type", "GeometryCollection");
		JsonArray geometries = new JsonArray();
		for (KmlGeometry item:mItems)
			geometries.add(item.asGeoJSON());
		json.add("geometries", geometries);
		return json;
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
	
	public static final Creator<KmlMultiGeometry> CREATOR = new Creator<KmlMultiGeometry>() {
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

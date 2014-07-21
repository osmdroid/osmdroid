package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.osmdroid.bonuspack.clustering.MarkerClusterer;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML Folder
 * @author M.Kergall
 */
public class KmlFolder extends KmlFeature implements Cloneable, Parcelable {

	/** List of KML Features it contains */
	public ArrayList<KmlFeature> mItems;

	public KmlFolder(){
		super();
		mItems = new ArrayList<KmlFeature>();
	}
	
	public KmlFolder(FolderOverlay overlay, KmlDocument kmlDoc){
		this();
		addOverlays(overlay.getItems(), kmlDoc);
		mName = overlay.getName();
		mDescription = overlay.getDescription();
		mVisibility = overlay.isEnabled();
	}
	
	public KmlFolder(MarkerClusterer overlay, KmlDocument kmlDoc){
		this();
		addOverlays(overlay.getItems(), kmlDoc);
		mName = overlay.getName();
		mDescription = overlay.getDescription();
		mVisibility = overlay.isEnabled();
	}
	
	/** GeoJSON constructor */
	public KmlFolder(JsonObject json){
		this();
		if (json.has("features")){
			JsonArray features = json.get("features").getAsJsonArray();
			for (JsonElement jsonFeature:features) {
		    	KmlFeature feature = KmlFeature.parseGeoJSON(jsonFeature.getAsJsonObject());
		        add(feature);
		    }
		}
	}
	
	@Override public BoundingBoxE6 getBoundingBox(){
		BoundingBoxE6 BB = null;
		for (KmlFeature item:mItems) {
			BoundingBoxE6 itemBB = item.getBoundingBox();
			if (itemBB != null){
				if (BB == null){
					BB = BonusPackHelper.cloneBoundingBoxE6(itemBB);
				} else {
					BB = BonusPackHelper.concatBoundingBoxE6(itemBB, BB);
				}
			}
		}
		return BB;
	}
	
	/** 
	 * Converts the overlay to a KmlFeature and add it inside this. 
	 * Conversion from Overlay subclasses to KML Features is as follow: <br>
	 *   FolderOverlay, MarkerClusterer => Folder<br>
	 *   Marker => Point<br>
	 *   Polygon => Polygon<br>
	 *   Polyline => LineString<br>
	 *   GroundOverlay => GroundOverlay<br>
	 *   Else, add nothing. 
	 * @param overlay to convert and add
	 * @param kmlDoc for style handling. 
	 * @return true if OK, false if the overlay has not been added. 
	 */
	public boolean addOverlay(Overlay overlay, KmlDocument kmlDoc){
		if (overlay == null)
			return false;
		KmlFeature kmlItem;
		if (overlay instanceof GroundOverlay){
			kmlItem = new KmlGroundOverlay((GroundOverlay)overlay);
		} else if (overlay instanceof FolderOverlay){
			kmlItem = new KmlFolder((FolderOverlay)overlay, kmlDoc);
		} else if (overlay instanceof MarkerClusterer){
			kmlItem = new KmlFolder((MarkerClusterer)overlay, kmlDoc);
		} else if (overlay instanceof Marker){
			Marker marker = (Marker)overlay;
			kmlItem = new KmlPlacemark(marker);
		} else if (overlay instanceof Polygon){
			Polygon polygon = (Polygon)overlay;
			kmlItem = new KmlPlacemark(polygon, kmlDoc);
		} else if (overlay instanceof Polyline){
			Polyline polyline = (Polyline)overlay;
			kmlItem = new KmlPlacemark(polyline, kmlDoc);
		} else {
			return false;
		}
		mItems.add(kmlItem);
		return true;
	}
	
	/** 
	 * Adds all overlays inside this, converting them in KmlFeatures. 
	 * @param overlays list of overlays to add
	 * @param kmlDoc
	 */
	public void addOverlays(List<? extends Overlay> overlays, KmlDocument kmlDoc){
		if (overlays != null){
			for (Overlay item:overlays){
				addOverlay(item, kmlDoc);
			}
		}
	}
	
	/** Add an item in the KML Folder, at the end. */
	public void add(KmlFeature item){
		mItems.add(item);
	}
	
	/** 
	 * remove the item at itemPosition. No check for bad usage (itemPosition out of rank)
	 * @param itemPosition position of the item, starting from 0. 
	 * @return item removed
	 */
	public KmlFeature removeItem(int itemPosition){
		KmlFeature removed = mItems.remove(itemPosition);
		return removed;
	}
	
	/**
	 * Build a FolderOverlay, containing (recursively) overlays from all items of this Folder. 
	 * @param map
	 * @param defaultStyle to apply when an item has no Style defined. 
	 * @param styler to apply
	 * @param kmlDocument for Styles
	 * @return the FolderOverlay built
	 */
	@Override public Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlDocument kmlDocument){
		Context context = map.getContext();
		FolderOverlay folderOverlay = new FolderOverlay(context);
		for (KmlFeature k:mItems){
			Overlay overlay = k.buildOverlay(map, defaultStyle, styler, kmlDocument);
			folderOverlay.add(overlay);
		}
		if (styler == null)
			folderOverlay.setEnabled(mVisibility);
		else 
			styler.onFeature(folderOverlay, this);
		return folderOverlay;
	}
	
	@Override public void writeKMLSpecifics(Writer writer){
		try {
			if (!mOpen)
				writer.write("<open>0</open>\n");
			for (KmlFeature item:mItems){
				item.writeAsKML(writer, false, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JsonObject geoJSONNamedCRS(String crsName){
		JsonObject crs = new JsonObject();
		crs.addProperty("type", "name");
		JsonObject properties = new JsonObject();
		properties.addProperty("name", crsName);
		crs.add("properties", properties);
		return crs;
	}
	
	/**
	 * Set isRoot to true if this is the root of the final GeoJSON structure. 
	 * Set isRoot to false if there is an enclosing FeatureCollection. 
	 * As GeoJSON doesn't support nested FeatureCollection, sub-items will be inserted directly in the result. 
	 * This is flattening the resulting GeoJSON hierarchy. 
	 * @return this as a GeoJSON FeatureCollection object. 
	 */
	@Override public JsonObject asGeoJSON(boolean isRoot){
		JsonObject json = new JsonObject();
		if (isRoot){
			json.add("crs", geoJSONNamedCRS("urn:ogc:def:crs:OGC:1.3:CRS84"));
		}
		JsonArray features = new JsonArray();
		for (KmlFeature item:mItems){
			JsonObject subJson = item.asGeoJSON(false);
			if (item instanceof KmlFolder){
				//Flatten the item contents:
				JsonArray subFeatures = subJson.getAsJsonArray("features");
				if (subFeatures != null){
					for (int i=0; i<subFeatures.size(); i++){
						JsonElement j = subFeatures.get(i);
						features.add(j);
					}
				}
			} else if (subJson != null) {
				features.add(subJson);
			}
		}
		json.add("features", features);
		json.addProperty("type", "FeatureCollection");
		return json;
	}
	
	//Cloneable implementation ------------------------------------

	public KmlFolder clone(){
		KmlFolder kmlFolder = (KmlFolder)super.clone();
		if (mItems != null){
			kmlFolder.mItems = new ArrayList<KmlFeature>(mItems.size());
			for (KmlFeature item:mItems)
				kmlFolder.mItems.add(item.clone());
		}
		return kmlFolder;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeList(mItems);
	}
	
	public static final Parcelable.Creator<KmlFolder> CREATOR = new Parcelable.Creator<KmlFolder>() {
		@Override public KmlFolder createFromParcel(Parcel source) {
			return new KmlFolder(source);
		}
		@Override public KmlFolder[] newArray(int size) {
			return new KmlFolder[size];
		}
	};
	
	public KmlFolder(Parcel in){
		super(in);
		mItems = in.readArrayList(KmlFeature.class.getClassLoader());
	}
}

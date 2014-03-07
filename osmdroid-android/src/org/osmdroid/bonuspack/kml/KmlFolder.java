package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.bonuspack.clustering.MarkerClusterer;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
		mObjectType = FOLDER;
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
		mVisibility = overlay.isEnabled();
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
		if (overlay.getClass() == GroundOverlay.class){
			kmlItem = new KmlGroundOverlay((GroundOverlay)overlay);
		} else if (overlay.getClass() == FolderOverlay.class){
			kmlItem = new KmlFolder((FolderOverlay)overlay, kmlDoc);
		} else if (overlay.getClass() == MarkerClusterer.class){
			kmlItem = new KmlFolder((MarkerClusterer)overlay, kmlDoc);
		} else if (overlay.getClass() == Marker.class){
			Marker marker = (Marker)overlay;
			kmlItem = new KmlPlacemark(marker);
		} else if (overlay.getClass() == Polygon.class){
			Polygon polygon = (Polygon)overlay;
			kmlItem = new KmlPlacemark(polygon, kmlDoc);
		} else if (overlay.getClass() == Polyline.class){
			Polyline polyline = (Polyline)overlay;
			kmlItem = new KmlPlacemark(polyline, kmlDoc);
		} else {
			return false;
		}
		mItems.add(kmlItem);
		updateBoundingBoxWith(kmlItem.mBB);
		return true;
	}
	
	/** 
	 * Adds all overlays inside this, converting them in KmlFeatures. 
	 * @param list of overlays to add
	 * @param kmlDoc
	 */
	public void addOverlays(List<? extends Overlay> overlays, KmlDocument kmlDoc){
		if (overlays != null){
			for (Overlay item:overlays){
				addOverlay(item, kmlDoc);
			}
		}
	}
	
	/** Add an item in the KML Folder. */
	public void add(KmlFeature item){
		mItems.add(item);
		updateBoundingBoxWith(item.mBB);
	}
	
	/**
	 * Build a FolderOverlay, containing (recursively) overlays from all items of this Folder. 
	 * @param map
	 * @param defaultIcon to build Markers from Points with no icon
	 * @param kmlDocument for Styles
	 * @param supportVisibility
	 * @return the FolderOverlay built
	 */
	@Override public FolderOverlay buildOverlay(MapView map, Drawable defaultIcon, KmlDocument kmlDocument, boolean supportVisibility){
		Context context = map.getContext();
		FolderOverlay folderOverlay = new FolderOverlay(context);
		for (KmlFeature k:mItems){
			Overlay overlay = k.buildOverlay(map, defaultIcon, kmlDocument, supportVisibility);
			folderOverlay.add(overlay);
		}
		if (!mVisibility)
			folderOverlay.setEnabled(false);
		return folderOverlay;
	}
	
	/** 
	 * remove the item at itemPosition. No check for bad usage (itemPosition out of rank)
	 * @param itemPosition position of the item, starting from 0. 
	 * @return item removed
	 */
	public KmlFeature removeItem(int itemPosition){
		KmlFeature removed = mItems.remove(itemPosition);
		//refresh bounding box from scratch:
		mBB = null;
		for (KmlFeature item:mItems) {
			updateBoundingBoxWith(item.mBB);
		}
		return removed;
	}
	
	public void saveKMLSpecifics(Writer writer){
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
	
	public boolean writeGeoJSONSpecifics(Writer writer){
		try {
			writer.write("\"type\": \"FeatureCollection\",\n");
			writer.write("\"features\": [\n");
			Iterator<KmlFeature> it = mItems.iterator();
			while(it.hasNext()) {
				KmlFeature item = it.next();
				if (!item.writeAsGeoJSON(writer, false))
					return false;
				if (it.hasNext())
					writer.write(',');
			}
			writer.write("],\n");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

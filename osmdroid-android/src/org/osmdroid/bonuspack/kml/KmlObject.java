package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Java representation of KML Feature or Geometry objects. 
 * Currently supports: Folder, Document, Point, LineString, Polygon. 
 * 
 * @author M.Kergall
 */
public class KmlObject implements Parcelable {
	/** possible KML object types. 
	 * Document is handled as a Folder. 
	 * Placemarks are the same object than their Geometry (a Placemark with a Polygon will just be a POLYGON). 
	 * NO_SHAPE is reserved for issues/errors. */
	public static final int NO_SHAPE=0, POINT=1, LINE_STRING=2, POLYGON=3, FOLDER=4;
	
	/** KML object type */
	public int mObjectType = NO_SHAPE;
	/** object id attribute, if any. Null if none. */
	public String mId;
	/** name tag */
	public String mName;
	/** description tag */
	public String mDescription;
	/** if this is a Folder or Document, list of KmlObject features it contains */
	public ArrayList<KmlObject> mItems;
	/** visibility tag */
	public boolean mVisibility=true;
	/** open tag */
	public boolean mOpen=true;
	/** coordinates of the geometry. If Point, just one entry. */
	public ArrayList<GeoPoint> mCoordinates;
	/** styleUrl, without the # */
	public String mStyle;
	/** bounding box */
	public BoundingBoxE6 mBB;
	
	public KmlObject(){
	}

	public void createFromOverlayItem(ExtendedOverlayItem marker){
		mObjectType = POINT;
		mName = marker.getTitle();
		mDescription = marker.getDescription();
		mCoordinates = new ArrayList<GeoPoint>(1);
		GeoPoint p = marker.getPoint();
		mCoordinates.add(p);
		mBB = new BoundingBoxE6(p.getLatitudeE6(), p.getLongitudeE6(), p.getLatitudeE6(), p.getLongitudeE6());
	}

	public void createFromPolygon(Polygon polygon){
		mObjectType = POLYGON;
		mName = polygon.getTitle();
		mDescription = polygon.getSnippet();
		mCoordinates = (ArrayList)polygon.getPoints();
		mBB = BoundingBoxE6.fromGeoPoints(mCoordinates);
	}

	public void createFromPathOverlay(PathOverlay path){
		mObjectType = LINE_STRING;
		mName = "LineString - "+path.getNumberOfPoints()+" points";
		mCoordinates = new ArrayList<GeoPoint>(path.getNumberOfPoints());
		//TODO: BIG ISSUE => no way to access PathOverlay points!...
		mBB = BoundingBoxE6.fromGeoPoints(mCoordinates);
	}
	
	public void createAsFolder(){
		mObjectType = FOLDER;
	}
	
	public void addOverlay(Overlay overlay){
		if (overlay == null)
			return;
		if (mItems == null)
			mItems = new ArrayList<KmlObject>();
		KmlObject kmlItem = new KmlObject();
		kmlItem.createFromOverlay(overlay);
		if (kmlItem.mObjectType != NO_SHAPE){
			mItems.add(kmlItem);
			updateBoundingBoxWith(kmlItem.mBB);
		}
	}
	
	/** 
	 * Set-up the KmlObject from a list of overlays, as a Folder containing each overlay. 
	 * @param overlays to add
	 */
	public void addOverlays(List<Overlay> overlays){
		if (overlays != null){
			for (Overlay item:overlays){
				addOverlay(item);
			}
		}
	}
	
	/** Set-up the KmlObject from the overlay. 
	 * Create a NO_SHAPE if the overlay class is not supported. 
	 * @param overlay
	 */
	public void createFromOverlay(Overlay overlay){
		if (overlay.getClass() == FolderOverlay.class){
			FolderOverlay folderOverlay = (FolderOverlay)overlay;
			createAsFolder();
			addOverlays(folderOverlay.getItems());
			mName = folderOverlay.getName();
			mDescription = folderOverlay.getDescription();
		} else if (overlay.getClass() == ItemizedOverlayWithBubble.class){
			ItemizedOverlayWithBubble<OverlayItem> markers = (ItemizedOverlayWithBubble<OverlayItem>)overlay;
			if (markers.size()==1){ //only 1 item => we can create it as a KML Point:
				ExtendedOverlayItem marker = (ExtendedOverlayItem)markers.getItem(0);
				createFromOverlayItem(marker);
			} else if (markers.size()==0){
				//if empty list, ignore => create a NO_SHAPE. 
				mObjectType = NO_SHAPE;
			} else { //this is a real list => we must create a KML Folder, and put items inside as Points:
				createAsFolder();
				mName = "Points - " + markers.size();
				mItems = new ArrayList<KmlObject>(markers.size());
				for (int j=0; j<markers.size(); j++){
					ExtendedOverlayItem marker = (ExtendedOverlayItem)markers.getItem(j);
					KmlObject kmlItem = new KmlObject();
					kmlItem.createFromOverlayItem(marker);
					mItems.add(kmlItem);
					updateBoundingBoxWith(kmlItem.mBB);
				}
			}
		} else if (overlay.getClass() == Polygon.class){
			Polygon polygon = (Polygon)overlay;
			createFromPolygon(polygon);
		} else if (overlay.getClass() == PathOverlay.class){
			PathOverlay path = (PathOverlay)overlay;
			createFromPathOverlay(path);
		} else { //unsupported overlay - create an empty folder. 
			mObjectType = NO_SHAPE;
			mName = "Unknown object - " + overlay.getClass().getName();
		}
	}
	
	/**
	 * Increase the object bounding box to include an other bounding box. 
	 * Typically needed when adding an item in the object. 
	 * @param itemBB the bounding box to "add". 
	 */
	public void updateBoundingBoxWith(BoundingBoxE6 itemBB){
		if (itemBB != null){
			if (mBB == null){
				mBB = new BoundingBoxE6(
						itemBB.getLatNorthE6(), 
						itemBB.getLonEastE6(), 
						itemBB.getLatSouthE6(), 
						itemBB.getLonWestE6());
			} else {
				mBB = new BoundingBoxE6(
						Math.max(itemBB.getLatNorthE6(), mBB.getLatNorthE6()), 
						Math.max(itemBB.getLonEastE6(), mBB.getLonEastE6()),
						Math.min(itemBB.getLatSouthE6(), mBB.getLatSouthE6()),
						Math.min(itemBB.getLonWestE6(), mBB.getLonWestE6()));
			}
		}
	}
	
	public void add(KmlObject item){
		if (mItems == null)
			mItems = new ArrayList<KmlObject>();
		mItems.add(item);
		updateBoundingBoxWith(item.mBB);
	}
	
	/**
	 * Build the overlay related to this KML object. 
	 * @param context
	 * @param map
	 * @param marker to use for Points
	 * @param kmlProvider
	 * @return the overlay, depending on the KML object type: 
	 * 		Folder=>FolderOverlay, Point=>ItemizedOverlayWithBubble, Polygon=>Polygon, LineString=>PathOverlay
	 */
	public Overlay buildOverlays(Context context, MapView map, Drawable marker, KmlProvider kmlProvider, 
			boolean supportVisibility){
		switch (mObjectType){
		case FOLDER:{
			FolderOverlay folderOverlay = new FolderOverlay(context);
			if (mItems != null){
				for (KmlObject k:mItems){
					Overlay overlay = k.buildOverlays(context, map, marker, kmlProvider, supportVisibility);
					folderOverlay.add(overlay, null);
				}
			}
			if (supportVisibility && !mVisibility)
				folderOverlay.setEnabled(false);
			return folderOverlay;
		}
		case POINT:{
			ExtendedOverlayItem item = new ExtendedOverlayItem(mName, mDescription, mCoordinates.get(0), context);
			item.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
			item.setMarker(marker);
			ArrayList<ExtendedOverlayItem> kmlPointsItems = new ArrayList<ExtendedOverlayItem>();
			ItemizedOverlayWithBubble<ExtendedOverlayItem> kmlPointsOverlay = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(context, 
					kmlPointsItems, map);
			kmlPointsOverlay.addItem(item);
			if (supportVisibility && !mVisibility)
				kmlPointsOverlay.setEnabled(false);
			return kmlPointsOverlay;
		}
		case LINE_STRING:{
			Paint paint = null;
			Style style = kmlProvider.getStyle(mStyle);
			if (style != null){
				paint = style.getOutlinePaint();
			}
			if (paint == null){ 
				//set default:
				paint = new Paint();
				paint.setColor(0x90101010);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(5);
			}
			PathOverlay lineStringOverlay = new PathOverlay(0, context);
			lineStringOverlay.setPaint(paint);
			for (GeoPoint point:mCoordinates)
				lineStringOverlay.addPoint(point);
			if (supportVisibility && !mVisibility)
				lineStringOverlay.setEnabled(false);
			return lineStringOverlay;
		}
		case POLYGON:{
			Paint outlinePaint = null; 
			int fillColor = 0x20101010; //default
			Style style = kmlProvider.getStyle(mStyle);
			if (style != null){
				outlinePaint = style.getOutlinePaint();
				fillColor = style.fillColorStyle.getFinalColor();
			}
			if (outlinePaint == null){ 
				//set default:
				outlinePaint = new Paint();
				outlinePaint.setColor(0x90101010);
				outlinePaint.setStrokeWidth(5);
			}
			Polygon polygonOverlay = new Polygon(context);
			polygonOverlay.setFillColor(fillColor);
			polygonOverlay.setStrokeColor(outlinePaint.getColor());
			polygonOverlay.setStrokeWidth(outlinePaint.getStrokeWidth());
			polygonOverlay.setPoints(mCoordinates);
			polygonOverlay.setTitle(mName);
			polygonOverlay.setSnippet(mDescription);
			if (!mName.equals("") || !mDescription.equals("")){
				String packageName = context.getPackageName();
				int layoutResId = context.getResources().getIdentifier("layout/bonuspack_bubble", null, packageName);
				polygonOverlay.setInfoWindow(layoutResId, map);
			}
			if (supportVisibility && !mVisibility)
				polygonOverlay.setEnabled(false);
			return polygonOverlay;
		}
		default:
			return null;	
		}
	}
	
	/**
	 * Write the object in KML text format (= save as a KML text file)
	 * @param writer on which the object is written. 
	 * @param isDocument true is this object is the root of the whole hierarchy (the "Document" folder). 
	 * @param styles the styles, that will be written if this is the root. Can be null. 
	 */
	public boolean writeAsKML(Writer writer, boolean isDocument, HashMap<String, Style> styles){
		try {
			String objectType = "";
			String feature = null;
			switch (mObjectType){
			case FOLDER:
				if (isDocument)
					objectType = "Document";
				else 
					objectType = "Folder";
				break;
			case POINT:
				objectType = "Placemark";
				feature = "Point";
				break;
			case LINE_STRING:
				objectType = "Placemark";
				feature = "LineString";
				break;
			case POLYGON:
				objectType = "Placemark";
				feature = "Polygon";
				break;
			default:
				break;
			}
			writer.write("<"+objectType);
			if (mId != null)
				writer.write(" id=\"mId\"");
			writer.write(">\n");
			if (mStyle != null){
				writer.write("<styleUrl>#"+mStyle+"</styleUrl>\n");
			}
			if (mName != null)
				writer.write("<name>"+mName+"</name>\n");
			if (mDescription != null)
				writer.write("<description><![CDATA["+mDescription+"]]></description>\n");
			if (!mVisibility)
				writer.write("<visibility>0</visibility>\n");
			if (mObjectType == FOLDER && !mOpen)
				writer.write("<open>0</open>\n");
			if (feature != null){
				writer.write("<"+feature+">\n");
				if (mObjectType == POLYGON)
					writer.write("<outerBoundaryIs>\n<LinearRing>\n");
				if (mCoordinates != null){
					writer.write("<coordinates>");
					for (GeoPoint coord:mCoordinates){
						writer.write(coord.getLongitude()+","+coord.getLatitude()+","+coord.getAltitude()+" ");
					}
					writer.write("</coordinates>\n");
				}
				if (mObjectType == POLYGON)
					writer.write("</LinearRing>\n</outerBoundaryIs>\n");
				writer.write("</"+feature+">\n");
			}
			if (mItems != null){
				for (KmlObject item:mItems){
					item.writeAsKML(writer, false, null);
				}
			}
			if (isDocument && styles != null){
				for (HashMap.Entry<String, Style> entry : styles.entrySet()) {
					String styleId = entry.getKey();
					Style style = entry.getValue();
					style.writeAsKML(writer, styleId);
				}		
			}
			writer.write("</"+objectType+">\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mObjectType);
		out.writeString(mId);
		out.writeString(mName);
		out.writeString(mDescription);
		out.writeList(mItems);
		out.writeInt(mVisibility?1:0);
		out.writeInt(mOpen?1:0);
		out.writeList(mCoordinates);
		out.writeString(mStyle);
		out.writeParcelable(mBB, flags);
	}
	
	public static final Parcelable.Creator<KmlObject> CREATOR = new Parcelable.Creator<KmlObject>() {
		@Override public KmlObject createFromParcel(Parcel source) {
			return new KmlObject(source);
		}
		@Override public KmlObject[] newArray(int size) {
			return new KmlObject[size];
		}
	};
	
	public KmlObject(Parcel in){
		mObjectType = in.readInt();
		mId = in.readString();
		mName = in.readString();
		mDescription = in.readString();
		mItems = in.readArrayList(KmlObject.class.getClassLoader());
		mVisibility = (in.readInt()==1);
		mOpen = (in.readInt()==1);
		mCoordinates = in.readArrayList(GeoPoint.class.getClassLoader());
		mStyle = in.readString();
		mBB = in.readParcelable(BoundingBoxE6.class.getClassLoader());
	}

}

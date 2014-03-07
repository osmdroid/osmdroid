package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML Placemark. Support the following Geometry: Point, LineString, and Polygon. 
 * @author M.Kergall
 */
public class KmlPlacemark extends KmlFeature implements Cloneable, Parcelable {
	
	/** coordinates of the geometry. If Point, one and only one entry. */
	public ArrayList<GeoPoint> mCoordinates;
	/** Polygon holes (can be null) */
	public ArrayList<ArrayList<GeoPoint>> mHoles;

	/** constructs a Placemark of unknown Geometry */
	public KmlPlacemark(){
		super();
	}
	
	/**
	 * Create the KmlFeature as a KML Point.  
	 * @param position position of the point
	 */
	public KmlPlacemark(GeoPoint position){
		super();
		mObjectType = POINT;
		mCoordinates = new ArrayList<GeoPoint>(1);
		GeoPoint p = position;
		mCoordinates.add(p);
		mBB = new BoundingBoxE6(p.getLatitudeE6(), p.getLongitudeE6(), p.getLatitudeE6(), p.getLongitudeE6());
	}
	
	public KmlPlacemark(Marker marker){
		this(marker.getPosition());
		mName = marker.getTitle();
		mDescription = marker.getSnippet();
		mVisibility = marker.isEnabled();
		//TODO: Style / IconStyle => transparency, hotspot, bearing. 
	}

	public KmlPlacemark(Polygon polygon, KmlDocument kmlDoc){
		mObjectType = POLYGON;
		mName = polygon.getTitle();
		mDescription = polygon.getSnippet();
		mCoordinates = (ArrayList<GeoPoint>)polygon.getPoints();
		mHoles = (ArrayList<ArrayList<GeoPoint>>)polygon.getHoles();
		mBB = BoundingBoxE6.fromGeoPoints(mCoordinates);
		mVisibility = polygon.isEnabled();
		//Style:
		Style style = new Style();
		style.mPolyStyle = new ColorStyle(polygon.getFillColor());
		style.mLineStyle = new LineStyle(polygon.getStrokeColor(), polygon.getStrokeWidth());
		mStyle = kmlDoc.addStyle(style);
	}

	public KmlPlacemark(Polyline polyline, KmlDocument kmlDoc){
		mObjectType = LINE_STRING;
		mName = "LineString - "+polyline.getNumberOfPoints()+" points";
		mCoordinates = (ArrayList<GeoPoint>)polyline.getPoints();
		mBB = BoundingBoxE6.fromGeoPoints(mCoordinates);
		mVisibility = polyline.isEnabled();
		//Style:
		Style style = new Style();
		style.mLineStyle = new LineStyle(polyline.getColor(), polyline.getWidth());
		mStyle = kmlDoc.addStyle(style);
	}
	
	/** default listener for dragging a marker built from a KML Point */
	public class OnKMLMarkerDragListener implements OnMarkerDragListener {
		@Override public void onMarkerDrag(Marker marker) {}
		@Override public void onMarkerDragEnd(Marker marker) {
			KmlFeature feature = (KmlFeature)marker.getRelatedObject();
			if (feature != null && feature.isA(POINT))
				((KmlPlacemark)feature).mCoordinates.set(0, marker.getPosition());
		}
		@Override public void onMarkerDragStart(Marker marker) {}		
	}
	
	/** from a Placemark feature having a Point geometry, build a Marker overlay
	 * @param map
	 * @param defaultIcon default icon to be used if no style or no icon specified. If null, default osmdroid marker icon will be used. 
	 * @param kmlDocument
	 * @param supportVisibility
	 * @return the Marker overlay
	 * @see buildOverlays
	 */
	public Overlay buildMarkerFromPoint(MapView map, Drawable defaultIcon, KmlDocument kmlDocument, 
			boolean supportVisibility){
		Context context = map.getContext();
		Marker marker = new Marker(map);
		marker.setTitle(mName);
		marker.setSnippet(mDescription);
		marker.setPosition(mCoordinates.get(0));
		Style style = kmlDocument.getStyle(mStyle);
		if (style != null && style.mIconStyle != null){
			style.mIconStyle.styleMarker(marker, context);
		} else {
			if (defaultIcon != null)
				marker.setIcon(defaultIcon);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		}
		//keep the link from the marker to the KML feature:
		marker.setRelatedObject(this);
		//allow marker drag, acting on KML Point:
		marker.setDraggable(true);
		marker.setOnMarkerDragListener(new OnKMLMarkerDragListener());
		if (supportVisibility && !mVisibility)
			marker.setEnabled(mVisibility);
		return marker;
	}
	
	public Overlay buildPolylineFromLineString(MapView map, KmlDocument kmlDocument, boolean supportVisibility){
		Context context = map.getContext();
		Polyline lineStringOverlay = new Polyline(context);
		Style style = kmlDocument.getStyle(mStyle);
		if (style != null){
			lineStringOverlay.setPaint(style.getOutlinePaint());
		} else { 
			//set default:
			lineStringOverlay.setColor(0x90101010);
			lineStringOverlay.setWidth(5.0f);
		}
		lineStringOverlay.setPoints(mCoordinates);
		if (supportVisibility && !mVisibility)
			lineStringOverlay.setEnabled(mVisibility);
		return lineStringOverlay;
	}
	
	public Overlay buildPolygonFromPolygon(MapView map, KmlDocument kmlDocument, boolean supportVisibility){
		Context context = map.getContext();
		Polygon polygonOverlay = new Polygon(context);
		Style style = kmlDocument.getStyle(mStyle);
		Paint outlinePaint = null;
		int fillColor = 0x20101010; //default
		if (style != null){
			outlinePaint = style.getOutlinePaint();
			fillColor = style.mPolyStyle.getFinalColor();
		}
		if (outlinePaint == null){ 
			//set default:
			outlinePaint = new Paint();
			outlinePaint.setColor(0x90101010);
			outlinePaint.setStrokeWidth(5);
		}
		polygonOverlay.setFillColor(fillColor);
		polygonOverlay.setStrokeColor(outlinePaint.getColor());
		polygonOverlay.setStrokeWidth(outlinePaint.getStrokeWidth());
		polygonOverlay.setPoints(mCoordinates);
		if (mHoles != null)
			polygonOverlay.setHoles(mHoles);
		polygonOverlay.setTitle(mName);
		polygonOverlay.setSnippet(mDescription);
		if ((mName!=null && !"".equals(mName)) || (mDescription!=null && !"".equals(mDescription))){
			//TODO: cache layoutResId retrieval. 
			String packageName = context.getPackageName();
			int layoutResId = context.getResources().getIdentifier("layout/bonuspack_bubble", null, packageName);
			polygonOverlay.setInfoWindow(layoutResId, map);
		}
		if (supportVisibility && !mVisibility)
			polygonOverlay.setEnabled(mVisibility);
		return polygonOverlay;
	}
	
	@Override public Overlay buildOverlay(MapView map, Drawable defaultIcon, KmlDocument kmlDocument, 
			boolean supportVisibility){
		switch (mObjectType){
			case POINT:
				return buildMarkerFromPoint(map, defaultIcon, kmlDocument, supportVisibility);
			case LINE_STRING:
				return buildPolylineFromLineString(map, kmlDocument, supportVisibility);
			case POLYGON:
				return buildPolygonFromPolygon(map, kmlDocument, supportVisibility);
			default:
				return null;
		}
	}
	
	/**
	 * Write a list of coordinates in KML format. 
	 * @param writer
	 * @param coordinates
	 * @return false if error
	 */
	public boolean writeKMLCoordinates(Writer writer, ArrayList<GeoPoint> coordinates){
		try {
			writer.write("<coordinates>");
			for (GeoPoint coord:coordinates){
				writer.write(coord.toInvertedDoubleString());
				writer.write(' ');
			}
			writer.write("</coordinates>\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/** Warning, indexes must match with KML object types */
	protected static String[] KMLGeometries = {"Unknown", "Point", "LineString", "Polygon"};
	
	public void saveKMLSpecifics(Writer writer){
		try {
			writer.write("<"+KMLGeometries[mObjectType]+">\n");
			if (mObjectType == POLYGON)
				writer.write("<outerBoundaryIs>\n<LinearRing>\n");
			if (mCoordinates != null){
				writeKMLCoordinates(writer, mCoordinates);
			}
			if (mObjectType == POLYGON){
				writer.write("</LinearRing>\n</outerBoundaryIs>\n");
				if (mHoles != null){
					for (ArrayList<GeoPoint> hole:mHoles){
						writer.write("<innerBoundaryIs>\n<LinearRing>\n");
						writeKMLCoordinates(writer, hole);
						writer.write("</LinearRing>\n</innerBoundaryIs>\n");
					}
				}
			}
			writer.write("</"+KMLGeometries[mObjectType]+">\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** mapping with object types values */
	protected static String[] GeoJSONTypes = {"Unknown", "Point", "LineString", "Polygon"};
	
	public boolean writeGeoJSONSpecifics(Writer writer){
		try {
			writer.write("\"type\": \"Feature\",\n");
			writer.write("\"geometry\": {\n");
			writer.write("\"type\": \""+GeoJSONTypes[mObjectType]+"\",\n");
			writer.write("\"coordinates\":\n");
			if (isA(LINE_STRING))
				writer.write("[");
			else if (isA(POLYGON))
				writer.write("[[");
			Iterator<GeoPoint> it = mCoordinates.iterator();
			while(it.hasNext()) {
				GeoPoint coord = it.next();
				writer.write("["+coord.getLongitude()+","+coord.getLatitude()/*+","+coord.getAltitude()*/+"]");
					//don't add altitude, as OpenLayers doesn't supports it... (vertigo?)
				if (it.hasNext())
					writer.write(',');
			}
			if (isA(LINE_STRING))
				writer.write("]");
			else if (isA(POLYGON)){
				writer.write("]]");
				//TODO: write polygon holes if any
			}
			writer.write("\n},\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//Cloneable implementation ------------------------------------

	public KmlPlacemark clone(){
		KmlPlacemark kmlPlacemark = (KmlPlacemark)super.clone();
		if (mCoordinates != null){
			kmlPlacemark.mCoordinates = cloneArrayOfGeoPoint(mCoordinates);
		}
		if (mHoles != null){
			kmlPlacemark.mHoles = new ArrayList<ArrayList<GeoPoint>>(mHoles.size());
			for (ArrayList<GeoPoint> hole:mHoles){
				kmlPlacemark.mHoles.add(cloneArrayOfGeoPoint(hole));
			}
		}
		return kmlPlacemark;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeList(mCoordinates);
		//TODO: mHoles
	}
	
	public static final Parcelable.Creator<KmlPlacemark> CREATOR = new Parcelable.Creator<KmlPlacemark>() {
		@Override public KmlPlacemark createFromParcel(Parcel source) {
			return new KmlPlacemark(source);
		}
		@Override public KmlPlacemark[] newArray(int size) {
			return new KmlPlacemark[size];
		}
	};
	
	public KmlPlacemark(Parcel in){
		super(in);
		mCoordinates = in.readArrayList(GeoPoint.class.getClassLoader());
		//TODO: mHoles
	}
}

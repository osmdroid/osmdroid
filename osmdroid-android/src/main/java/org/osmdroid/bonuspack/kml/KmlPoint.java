package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import org.osmdroid.bonuspack.kml.KmlFeature.Styler;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML and/or GeoJSON Point
 * @author M.Kergall
 */
public class KmlPoint extends KmlGeometry implements Parcelable, Cloneable {

	public KmlPoint(){
		super();
	}
	
	public KmlPoint(GeoPoint position){
		this();
		setPosition(position);
	}
	
	/** GeoJSON constructor */
	public KmlPoint(JsonObject json){
		this();
		JsonElement coordinates = json.get("coordinates");
		if (coordinates != null){
			setPosition(KmlGeometry.parseGeoJSONPosition(coordinates.getAsJsonArray()));
		}
	}
	
	public void setPosition(GeoPoint position){
		if (mCoordinates == null){
			mCoordinates = new ArrayList<GeoPoint>(1);
			mCoordinates.add(position);
		} else
			mCoordinates.set(0, position);
	}
	
	public GeoPoint getPosition(){
		return mCoordinates.get(0);
	}
	
	/** default listener for dragging a Marker built from a KML Point */
	public class OnKMLMarkerDragListener implements OnMarkerDragListener {
		@Override public void onMarkerDrag(Marker marker) {}
		@Override public void onMarkerDragEnd(Marker marker) {
			Object object = marker.getRelatedObject();
			if (object != null && object instanceof KmlPoint){
				KmlPoint point = (KmlPoint)object;
				point.setPosition(marker.getPosition());
			}
		}
		@Override public void onMarkerDragStart(Marker marker) {}		
	}
	
	public void applyDefaultStyling(Marker marker, Style defaultStyle, KmlPlacemark kmlPlacemark,
			KmlDocument kmlDocument, MapView map){
		Context context = map.getContext();
		Style style = kmlDocument.getStyle(kmlPlacemark.mStyle);
		if (style != null && style.mIconStyle != null){
			style.mIconStyle.styleMarker(marker, context);
		} else if (defaultStyle!=null && defaultStyle.mIconStyle!=null){
			defaultStyle.mIconStyle.styleMarker(marker, context);
		}
		//allow marker drag, acting on KML Point:
		marker.setDraggable(true);
		marker.setOnMarkerDragListener(new OnKMLMarkerDragListener());
		marker.setEnabled(kmlPlacemark.mVisibility);
	}
	
	/** Build the corresponding Marker overlay */	
	@Override public Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlPlacemark kmlPlacemark, 
			KmlDocument kmlDocument){
		Marker marker = new Marker(map);
		marker.setTitle(kmlPlacemark.mName);
		marker.setSnippet(kmlPlacemark.mDescription);
		marker.setSubDescription(kmlPlacemark.getExtendedDataAsText());
		marker.setPosition(getPosition());
		//keep the link from the marker to the KML feature:
		marker.setRelatedObject(this);
		if (styler == null){
			applyDefaultStyling(marker, defaultStyle, kmlPlacemark, kmlDocument, map);
		} else
			styler.onPoint(marker, kmlPlacemark, this);
		return marker;
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
	
	@Override public JsonObject asGeoJSON(){
		JsonObject json = new JsonObject();
		json.addProperty("type", "Point");
		json.add("coordinates", KmlGeometry.geoJSONPosition(mCoordinates.get(0)));
		return json;
	}

	@Override public BoundingBoxE6 getBoundingBox(){
		return BoundingBoxE6.fromGeoPoints(mCoordinates);
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
	
	public static final Creator<KmlPoint> CREATOR = new Creator<KmlPoint>() {
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

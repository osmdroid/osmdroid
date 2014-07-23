package org.osmdroid.bonuspack.kml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang3.StringEscapeUtils;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import com.google.gson.JsonObject;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML GroundOverlay. 
 * 
 * mCoordinates contains the LatLonBox as 2 GeoPoints: North-West, and South-East. 
 * 
 * @author M.Kergall
 */
public class KmlGroundOverlay extends KmlFeature implements Cloneable, Parcelable {
	/** Overlay Icon href */
	public String mIconHref;
	/** Overlay Icon bitmap (can be null) */
	public Bitmap mIcon;
	/** Overlay color */
	public int mColor;
	/** GroundOverlay rotation - default = 0 */
	public float mRotation;
	/** NW and SE points - TODO: not the simplest way to handle that... */
	public ArrayList<GeoPoint> mCoordinates;

	public KmlGroundOverlay(){
		super();
		mColor = 0xFF000000;
	}

	/** Constructs the KML feature from a GroundOverlay. */
	public KmlGroundOverlay(GroundOverlay overlay){
		this();
		GeoPoint p = overlay.getPosition();
		GeoPoint pN = p.destinationPoint(overlay.getHeight()/2, 0.0f);
		GeoPoint pS = p.destinationPoint(overlay.getHeight()/2, 180.0f);
		GeoPoint pE = p.destinationPoint(overlay.getWidth()/2, 90.0f);
		GeoPoint pW = p.destinationPoint(overlay.getWidth()/2, -90.0f);
		mCoordinates = new ArrayList<GeoPoint>(2);
		mCoordinates.add(new GeoPoint(pN.getLatitudeE6(), pW.getLongitudeE6())); //NW
		mCoordinates.add(new GeoPoint(pS.getLatitudeE6(), pE.getLongitudeE6())); //SE
		//mIconHref = ???
		mIcon = ((BitmapDrawable)overlay.getImage()).getBitmap();
		mRotation = -overlay.getBearing();
		mColor = 255 - Color.alpha((int)(overlay.getTransparency()*255));
		mVisibility = overlay.isEnabled();
	}
	
	@Override public BoundingBoxE6 getBoundingBox(){
		return BoundingBoxE6.fromGeoPoints(mCoordinates);
	}
	
	/** load the icon from its href. 
	 * @param href either the full url, or a relative path to a local file. 
	 * @param containerFile the KML container file - or null if irrelevant. 
	 * @param kmzContainer current KMZ file (as a ZipFile) - or null if irrelevant. 
	 */
	public void setIcon(String href, File containerFile, ZipFile kmzContainer){
		mIconHref = href;
		if (mIconHref.startsWith("http://") || mIconHref.startsWith("https://")){
			mIcon = BonusPackHelper.loadBitmap(mIconHref);
		} else if (kmzContainer == null) {
			if (containerFile != null){
				String actualFullPath = containerFile.getParent()+'/'+mIconHref;
				mIcon = BitmapFactory.decodeFile(actualFullPath);
			} else
				mIcon = null;
		} else {
			try {
				final ZipEntry fileEntry = kmzContainer.getEntry(href);
				InputStream stream = kmzContainer.getInputStream(fileEntry);
				mIcon = BitmapFactory.decodeStream(stream);
			} catch (Exception e) {
				mIcon = null;
			}
		}
	}
	
	public void setLatLonBox(double north, double south, double east, double west){
		mCoordinates = new ArrayList<GeoPoint>(2);
		mCoordinates.add(new GeoPoint(north, west));
		mCoordinates.add(new GeoPoint(south, east));
	}

	/** @return the corresponding GroundOverlay ready to display on the map */
	@Override public Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlDocument kmlDocument){
		Context context = map.getContext();
		GroundOverlay overlay = new GroundOverlay(context);
		if (mCoordinates.size()==2){
			GeoPoint pNW = mCoordinates.get(0);
			GeoPoint pSE = mCoordinates.get(1);
			overlay.setPosition(GeoPoint.fromCenterBetween(pNW, pSE));
			GeoPoint pNE = new GeoPoint(pNW.getLatitude(), pSE.getLongitude());
			int width = pNE.distanceTo(pNW);
			GeoPoint pSW = new GeoPoint(pSE.getLatitude(), pNW.getLongitude());
			int height = pSW.distanceTo(pNW);
			overlay.setDimensions((float)width, (float)height);
		}
		//TODO: else if size=4, nonrectangular quadrilateral
		
		if (mIcon != null){
			overlay.setImage(new BitmapDrawable(mIcon));
			//TODO: not clearly defined in KML spec, but color is supposed to be blended with the image. 
			float transparency = 1.0f - Color.alpha(mColor)/255.0f; //KML transparency is the transparency part of the "color" element. 
			overlay.setTransparency(transparency);
		} else {
			//when no image available, set it as a rectangle filled with the KML color
			Bitmap bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
			bitmap.eraseColor(mColor);
			overlay.setImage(new BitmapDrawable(bitmap));
		}
		
		overlay.setBearing(-mRotation); //from KML counterclockwise to Google Maps API which is clockwise
		if (styler == null)
			overlay.setEnabled(mVisibility);
		else 
			styler.onFeature(overlay, this);
		return overlay;
	}
	
	/** write elements specific to GroundOverlay in KML format */
	@Override public void writeKMLSpecifics(Writer writer){
		try {
			writer.write("<color>"+ColorStyle.colorAsKMLString(mColor)+"</color>\n");
			writer.write("<Icon><href>"+StringEscapeUtils.escapeXml10(mIconHref)+"</href></Icon>\n");
			writer.write("<LatLonBox>");
			GeoPoint pNW = mCoordinates.get(0);
			GeoPoint pSE = mCoordinates.get(1);
			writer.write("<north>"+pNW.getLatitude()+"</north>");
			writer.write("<south>"+pSE.getLatitude()+"</south>");
			writer.write("<east>"+pSE.getLongitude()+"</east>");
			writer.write("<west>"+pNW.getLongitude()+"</west>");
			writer.write("<rotation>"+mRotation+"</rotation>");
			writer.write("</LatLonBox>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override public JsonObject asGeoJSON(boolean isRoot) {
		//TODO: GroundOverlay is not supported by GeoJSON. Output enclosing polygon with mColor?
		return null;
	}
	
	//Cloneable implementation ------------------------------------

	public KmlGroundOverlay clone(){
		KmlGroundOverlay kmlGroundOverlay = (KmlGroundOverlay)super.clone();
		kmlGroundOverlay.mCoordinates = KmlGeometry.cloneArrayOfGeoPoint(mCoordinates);
		return kmlGroundOverlay;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(mIconHref);
		out.writeParcelable(mIcon, flags);
		out.writeInt(mColor);
		out.writeFloat(mRotation);
		out.writeList(mCoordinates);
	}
	
	public static final Parcelable.Creator<KmlGroundOverlay> CREATOR = new Parcelable.Creator<KmlGroundOverlay>() {
		@Override public KmlGroundOverlay createFromParcel(Parcel source) {
			return new KmlGroundOverlay(source);
		}
		@Override public KmlGroundOverlay[] newArray(int size) {
			return new KmlGroundOverlay[size];
		}
	};
	
	public KmlGroundOverlay(Parcel in){
		super(in);
		mIconHref = in.readString();
		mIcon = in.readParcelable(Bitmap.class.getClassLoader());
		mColor = in.readInt();
		mRotation = in.readFloat();
		mCoordinates = in.readArrayList(GeoPoint.class.getClassLoader());
	}

}

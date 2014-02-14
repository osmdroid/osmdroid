package org.osmdroid.bonuspack.kml;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
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

	public KmlGroundOverlay(){
		mObjectType = GROUND_OVERLAY;
		mColor = 0xFF000000;
	}

	/** load the icon from its href. 
	 * @param href either the full url, or a relative path to a local file. 
	 * @param containerFullPath full path of the container file. 
	 */
	public void setIcon(String href, String containerFullPath){
		mIconHref = href;
		if (mIconHref.startsWith("http://") || mIconHref.startsWith("https://")){
			mIcon = BonusPackHelper.loadBitmap(mIconHref);
		} else {
			File file = new File(containerFullPath);
			String actualFullPath = file.getParent()+'/'+mIconHref;
			mIcon = BitmapFactory.decodeFile(actualFullPath);
		}
	}
	
	public void setLatLonBox(double north, double south, double east, double west){
		mCoordinates = new ArrayList<GeoPoint>(2);
		mCoordinates.add(new GeoPoint(north, west));
		mCoordinates.add(new GeoPoint(south, east));
		mBB = BoundingBoxE6.fromGeoPoints(mCoordinates);
	}

	/** @return the corresponding GroundOverlay ready to display on the map */
	public Overlay buildOverlay(MapView map){
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
		//TODO: 
		//else if size=4, nonrectangular quadrilateral
		//else, error
		
		if (mIcon != null)
			overlay.setImage(new BitmapDrawable(mIcon));
		else {
			/* TODO: currently filling the canvas. 
			ColorDrawable rect = new ColorDrawable(mColor);
			rect.setAlpha(255); //transparency will be applied below. 
			overlay.setImage(rect);
			*/
		}
		
		float transparency = 1.0f - Color.alpha(mColor)/255.0f; //KML transparency is the transparency part of the "color" element. 
		overlay.setTransparency(transparency);
		overlay.setBearing(-mRotation); //from KML counterclockwise to Google Maps API which is clockwise
		overlay.setEnabled(mVisibility);
		return overlay;
	}
	
	/** write elements specific to GroundOverlay in KML format */
	protected void saveKMLGroundOverlaySpecifics(Writer writer){
		try {
			writer.write("<color>"+ColorStyle.colorAsKMLString(mColor)+"</color>\n");
			writer.write("<Icon><href>"+mIconHref+"</href></Icon>\n");
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
	
	//Cloneable implementation ------------------------------------

	public KmlGroundOverlay clone(){
		KmlGroundOverlay kmlGroundOverlay = (KmlGroundOverlay)super.clone();
		//nothing else to clone???
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
	}
}

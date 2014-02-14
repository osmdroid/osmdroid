package org.osmdroid.bonuspack.kml;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.utils.WebImageCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/** Handling of KML IconStyle */
public class IconStyle extends ColorStyle implements Parcelable {
	
	public float mScale;
	public float mHeading;
	public String mHref;
	public Bitmap mIcon;
	public float mHotSpotX, mHotSpotY;
	
	private static WebImageCache mIconCache;
	static {
		//one common memory cache for all icons:
		mIconCache = new WebImageCache(100);
	}

	public IconStyle(){
		super();
		mScale = 1.0f;
		mHeading = 0.0f;
		mHotSpotX = 0.5f;
		mHotSpotY = 1.0f;
	}
	
	/** Load and set the icon bitmap, from a url or from a local file. 
	 * @param href either the full url, or a relative path to a local file. 
	 * @param containerFullPath full path of the container file. 
	 */
	public void setIcon(String href, String containerFullPath){
		mHref = href;
		if (mHref.startsWith("http://")){
			mIcon = mIconCache.get(mHref);
		} else {
			File file = new File(containerFullPath);
			String actualFullPath = file.getParent()+'/'+mHref;
			mIcon = BitmapFactory.decodeFile(actualFullPath);
		}
	}
	
	/** @return the icon, scaled and blended with the icon color, as specified in the IconStyle. 
	 * Assumes the icon is already loaded. */
	public BitmapDrawable getFinalIcon(Context context){
		if (mIcon == null)
			return null;
		int sizeX = Math.round(mIcon.getWidth() * mScale);
		int sizeY = Math.round(mIcon.getHeight() * mScale);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(mIcon, sizeX, sizeY, true);
		BitmapDrawable marker = new BitmapDrawable(context.getResources(), scaledBitmap);
		int color = getFinalColor();
		if (color != 0) //there is a real color to blend with:
			marker.setColorFilter(color, Mode.MULTIPLY);
		return marker;
	}

	/** apply all IconStyle attributes to the Marker */
	public void styleMarker(Marker marker, Context context){
		BitmapDrawable icon = getFinalIcon(context);
		marker.setIcon(icon);
		marker.setAnchor(mHotSpotX, mHotSpotY);
		marker.setRotation(mHeading);
	}
	
	public void writeAsKML(Writer writer){
		try {
			writer.write("<IconStyle>\n");
			super.writeAsKML(writer);
			//write the specifics:
			if (mScale != 1.0f)
				writer.write("<scale>"+mScale+"</scale>\n");
			if (mHeading != 0.0f)
				writer.write("<heading>"+mHeading+"</heading>\n");
			if (mHref != null)
				writer.write("<Icon><href>"+mHref+"</href></Icon>\n");
			writer.write("<hotSpot x=\"" + mHotSpotX + "\" y=\"" + mHotSpotY + "\" xunits=\"fraction\" yunits=\"fraction\"/>\n");
			writer.write("</IconStyle>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeFloat(mScale);
		out.writeFloat(mHeading);
		out.writeString(mHref);
		out.writeParcelable(mIcon, flags);
		out.writeFloat(mHotSpotX);
		out.writeFloat(mHotSpotY);
	}
	
	public static final Parcelable.Creator<IconStyle> CREATOR = new Parcelable.Creator<IconStyle>() {
		@Override public IconStyle createFromParcel(Parcel source) {
			return new IconStyle(source);
		}
		@Override public IconStyle[] newArray(int size) {
			return new IconStyle[size];
		}
	};
	
	public IconStyle(Parcel in){
		super(in);
		mScale = in.readFloat();
		mHeading = in.readFloat();
		mHref = in.readString();
		mIcon = in.readParcelable(Bitmap.class.getClassLoader());
		mHotSpotX = in.readFloat();
		mHotSpotY = in.readFloat();
	}
	
}

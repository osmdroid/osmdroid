package com.osmbonuspackdemo;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.drawable.Drawable;


public class ExtendedOverlayItem extends OverlayItem {

	String mTitle, mDescription; // now, they are modifiable
	String mAddress;
	Drawable mImage; //that will be shown in the pop-up window. 
	
	public ExtendedOverlayItem(String aTitle, String aDescription,
			GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
		mTitle = aTitle;
		mDescription = aDescription;
		mAddress = null;
		mImage = null;
	}

	public void setTitle(String aTitle){
		mTitle = aTitle;
	}
	
	public void setDescription(String aDescription){
		mDescription = aDescription;
	}
	
	public void setAddress(String aAddress){
		mAddress = aAddress;
	}
	
	public void setImage(Drawable anImage){
		mImage = anImage;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getAddress() {
		return mAddress;
	}

	public Drawable getImage() {
		return mImage;
	}
	
}

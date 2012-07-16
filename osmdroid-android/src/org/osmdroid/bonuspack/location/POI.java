package org.osmdroid.bonuspack.location;

import org.osmdroid.util.GeoPoint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Point of Interest. 
 * DRAFT, UNDER DEVELOPMENT. 
 * @see NominatimPOIProvider
 * @see GeoNamesPOIProvider
 * @author M.Kergall
 */
public class POI implements Parcelable {
	public long mId;
	public GeoPoint mLocation;
	public String mCategory;
	public String mType;
	public String mDescription;
	public String mIconPath;
	public Drawable mIcon;
	//for GeoNames Wikipedia:
	public String mUrl;
	
	public POI(){
		//default creator lets fields empty or null. That's fine. 
	}
	
	@Override public int describeContents() {
		return 0;
	}
	
	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeLong(mId);
		out.writeParcelable(mLocation, 0);
		out.writeString(mCategory);
		out.writeString(mType);
		out.writeString(mDescription);
		out.writeString(mIconPath);
		//out.writeParcelable(mIcon, 0); Drawable is not parcelable!!! 
		out.writeString(mUrl);
	}
	
	public static final Parcelable.Creator<POI> CREATOR = new Parcelable.Creator<POI>() {
		@Override public POI createFromParcel(Parcel source) {
			return new POI(source);
		}
		@Override public POI[] newArray(int size) {
			return new POI[size];
		}
	};
	
	private POI(Parcel in){
		mId = in.readLong();
		mLocation = in.readParcelable(GeoPoint.class.getClassLoader());
		mCategory = in.readString();
		mType = in.readString();
		mDescription = in.readString();
		mIconPath = in.readString();
		//mIcon = in.readParcelable(Drawable.class.getClassLoader());
		mUrl = in.readString();
	}
}

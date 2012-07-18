package org.osmdroid.bonuspack.location;

import org.osmdroid.util.GeoPoint;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Point of Interest. Exact content may depend of the POI provider used. 
 * @see NominatimPOIProvider
 * @see GeoNamesPOIProvider
 * @author M.Kergall
 */
public class POI implements Parcelable {
	public long mId;
	/** location of the POI */
	public GeoPoint mLocation;
	public String mCategory;
	/** type or title */
	public String mType;
	/** can be the name, the address */
	public String mDescription;
	/** url of the thumbnail. Can be null if none */
	public String mIconPath;
	/** the thumbnail itself. Null if none */
	public Bitmap mIcon;
	/** url to a more detailed information page about this POI */
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
		out.writeParcelable(mIcon, 0);
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
		mIcon = in.readParcelable(Bitmap.class.getClassLoader());
		mUrl = in.readString();
	}
}

package org.osmdroid.bonuspack.location;

import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.GeoPoint;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Point of Interest. Exact content may depend of the POI provider used. 
 * @see NominatimPOIProvider
 * @see GeoNamesPOIProvider
 * @author M.Kergall
 */
public class POI implements Parcelable {
	/** Nominatim: OSM ID. GeoNames: 0 */
	public long mId;
	/** location of the POI */
	public GeoPoint mLocation;
	/** Nominatim "class", GeoNames "feature" */
	public String mCategory;
	/** type or title */
	public String mType;
	/** can be the name, the address, a short description */
	public String mDescription;
	/** url of the thumbnail. Null if none */
	public String mThumbnailPath;
	/** the thumbnail itself. Null if none */
	public Bitmap mThumbnail;
	/** url to a more detailed information page about this POI. Null if none */
	public String mUrl;
	
	public POI(){
		//default creator lets fields empty or null. That's fine. 
	}
	
	public Bitmap getThumbnail(){
		if (mThumbnail == null && mThumbnailPath != null){
			Log.d(BonusPackHelper.LOG_TAG, "POI:load thumbnail:"+mThumbnailPath);
			mThumbnail = BonusPackHelper.loadBitmap(mThumbnailPath);
			if (mThumbnail == null)
				//this path doesn't work, kill it:
				mThumbnailPath = null;
		}
		return mThumbnail;
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
		out.writeString(mThumbnailPath);
		out.writeParcelable(mThumbnail, 0);
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
		mThumbnailPath = in.readString();
		mThumbnail = in.readParcelable(Bitmap.class.getClassLoader());
		mUrl = in.readString();
	}
}

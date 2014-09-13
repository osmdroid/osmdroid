package org.osmdroid.bonuspack.location;

import org.osmdroid.bonuspack.utils.WebImageCache;
import org.osmdroid.util.GeoPoint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;

/**
 * Point of Interest. Exact content may depend of the POI provider used. 
 * @see NominatimPOIProvider
 * @see GeoNamesPOIProvider
 * @author M.Kergall
 */
public class POI implements Parcelable {
	/** IDs of POI services */
	public static int POI_SERVICE_NOMINATIM = 100;
	public static int POI_SERVICE_GEONAMES_WIKIPEDIA = 200;
	public static int POI_SERVICE_FLICKR = 300;
	public static int POI_SERVICE_PICASA = 400;
	public static int POI_SERVICE_OVERPASS_API = 500;
	
	private static WebImageCache mThumbnailCache;
	static {
		//one common cache for all POI thumbnails:
		mThumbnailCache = new WebImageCache(300);
	}
	
	/** Identifies the service provider of this POI. */
	public int mServiceId;
	/** Nominatim: OSM ID. GeoNames: 0 */
	public long mId;
	/** location of the POI */
	public GeoPoint mLocation;
	/** Nominatim "class", GeoNames "feature" */
	public String mCategory;
	/** type or title */
	public String mType;
	/** can be the name, the address, or a "reasonably" short description (displayable in a bubble)*/
	public String mDescription;
	/** url of the thumbnail. Null if none */
	public String mThumbnailPath;
	/** the thumbnail image itself. Null if none */
	public Bitmap mThumbnail;
	/** url to a more detailed information page about this POI. Null if none */
	public String mUrl;
	/** popularity of this POI, from 1 (lowest) to 100 (highest). 0 if not defined. */
	public int mRank;
	/** number of attempts to load the thumbnail that have failed */
	protected int mThumbnailLoadingFailures;
	
	/** constructor of an empty POI. Only specify which service is creating it. */
	public POI(int serviceId){
		mServiceId = serviceId;
		//lets all other fields empty or null: that's exactly the default values we want. 
	}
	
	protected static int MAX_LOADING_ATTEMPTS = 2;
	/** 
	 * @return the POI thumbnail as a Bitmap, if any. 
	 * If not done yet, it will load the POI thumbnail from its url (in mThumbnailPath field). 
	 */
	public Bitmap getThumbnail(){
		if (mThumbnail == null && mThumbnailPath != null){
			/*
			Log.d(BonusPackHelper.LOG_TAG, "POI:load thumbnail:"+mThumbnailPath);
			mThumbnail = BonusPackHelper.loadBitmap(mThumbnailPath);
			*/
			//now we use WebImageCache to share thumbnail loading done at various places:
			mThumbnail = mThumbnailCache.get(mThumbnailPath);
			if (mThumbnail == null){
				mThumbnailLoadingFailures++;
				if (mThumbnailLoadingFailures >= MAX_LOADING_ATTEMPTS){
					//this path really doesn't work, "kill" it for next calls:
					mThumbnailPath = null;
				}
			}
		}
		return mThumbnail;
	}
	
	/**
	 * Fetch the thumbnail from its url on a thread. 
	 * Using AsyncTask: must be invoked from the UI thread. 
	 * @param imageView to update once the thumbnail is retrieved, or to hide if no thumbnail. 
	 */
	public void fetchThumbnailOnThread(final ImageView imageView){
		if (mThumbnail != null){
			imageView.setImageBitmap(mThumbnail);
			imageView.setVisibility(View.VISIBLE);
		} else if (mThumbnailPath != null){
			imageView.setVisibility(View.GONE);
			/*
			final Handler handler = new Handler() {
				@Override public void handleMessage(Message message) {
					ImageView imageView = (ImageView)message.obj;
					if (mThumbnail != null)
						imageView.setImageBitmap(mThumbnail);
						imageView.setVisibility(View.VISIBLE);
				}
			};
			
			Thread thread = new Thread() {
				@Override public void run() {
					getThumbnail();
					Message message = handler.obtainMessage(1, imageView);
					handler.sendMessage(message);
				}
			};
			thread.start();
			*/
			new ThumbnailTask(imageView).execute(imageView);
		} else {
			//No thumbnail to show:
			imageView.setVisibility(View.GONE);
		}
	}
    
	class ThumbnailTask extends AsyncTask<ImageView, Void, ImageView> {

		public ThumbnailTask(ImageView iv) {
			iv.setTag(mThumbnailPath);
		}

		@Override protected ImageView doInBackground(ImageView... params) {
			getThumbnail();
			return params[0];
		}

		@Override protected void onPostExecute(ImageView iv) {
			if (iv.getTag() != null && mThumbnailPath != null){
				if (mThumbnailPath.equals(iv.getTag().toString())){
					iv.setImageBitmap(mThumbnail);
					iv.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	//--- Parcelable implementation
	
	@Override public int describeContents() {
		return 0;
	}
	
	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mServiceId);
		out.writeLong(mId);
		out.writeParcelable(mLocation, 0);
		out.writeString(mCategory);
		out.writeString(mType);
		out.writeString(mDescription);
		out.writeString(mThumbnailPath);
		out.writeParcelable(mThumbnail, 0);
		out.writeString(mUrl);
		out.writeInt(mRank);
		out.writeInt(mThumbnailLoadingFailures);
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
		mServiceId = in.readInt();
		mId = in.readLong();
		mLocation = in.readParcelable(GeoPoint.class.getClassLoader());
		mCategory = in.readString();
		mType = in.readString();
		mDescription = in.readString();
		mThumbnailPath = in.readString();
		mThumbnail = in.readParcelable(Bitmap.class.getClassLoader());
		mUrl = in.readString();
		mRank = in.readInt();
		mThumbnailLoadingFailures = in.readInt();
	}
}

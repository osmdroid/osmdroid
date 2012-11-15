package org.osmdroid.bonuspack.utils;

import java.util.HashMap;
import java.util.Set;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Simple memory cache for handling images loaded from the web. 
 * The url is the key. 
 * TODO: really handle maxItems (with a strategy to remove entries)
 * @author M.Kergall
 */
public class WebImageCache {
	final HashMap<String, Bitmap> mCacheMap;
	int mMaxItems;
	
	public WebImageCache(int maxItems) {
		mMaxItems = maxItems;
		mCacheMap = new HashMap<String, Bitmap>(maxItems);
	}

	/** synchronization restricted to cache handling */
	private synchronized void updateCache(String url, Bitmap image){
		if (mCacheMap.size() == mMaxItems){
			//max already reached => one item to remove. 
			Set<String> keys = mCacheMap.keySet();
			Object[] sKeys = keys.toArray();
			//TODO: handling maxItems, remove older? less used?
			//for now, remove random one:
			int index = (int)(Math.random() * sKeys.length);
			String removedUrl = (String)sKeys[index];
			Log.d(BonusPackHelper.LOG_TAG, "WebImageCache:remove :"+removedUrl);
			mCacheMap.remove(removedUrl);
		}
		mCacheMap.put(url, image);
	}
	
	/**
	 * get the image, either from the cache, or from the web if not in the cache. 
	 * Can be called by multiple threads. 
	 * If 2 threads ask for the same url simultaneously, we can get the image twice in the cache. 
	 * @param url of the image
	 * @return the image, or null if any failure. 
	 */
	public Bitmap get(String url){
		Bitmap image = mCacheMap.get(url);
		if (image == null){
			Log.d(BonusPackHelper.LOG_TAG, "WebImageCache:load :"+url);
			image = BonusPackHelper.loadBitmap(url);
			if (image != null){
				updateCache(url, image);
			}
		}
		return image;
	}

	/* one entry in the cache, with info needed for removal strategy
	class WebImageEntry {
		Bitmap mImage;
		Calendar mTimeStamp;
		int mAccessCount;
		//other ?...
	}
	*/

}

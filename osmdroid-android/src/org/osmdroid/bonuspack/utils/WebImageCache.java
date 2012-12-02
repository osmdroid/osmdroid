package org.osmdroid.bonuspack.utils;

import java.util.LinkedHashMap;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Simple memory cache for handling images loaded from the web. 
 * The url is the key. 
 * @author M.Kergall
 */
public class WebImageCache {
	LinkedHashMap<String, Bitmap> mCacheMap;
	int mCapacity;
	
	public WebImageCache(int maxItems) {
		mCapacity = maxItems;
		mCacheMap = new LinkedHashMap<String, Bitmap>(maxItems+1, 1.1f, true){
			private static final long serialVersionUID = -4831331496601290979L;
			protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
				return size() > mCapacity;
			}			
		};
	}

	private void putInCache(String url, Bitmap image){
		synchronized(mCacheMap){
			mCacheMap.put(url, image);
		}
		//Log.d(BonusPackHelper.LOG_TAG, "WebImageCache:updateCache size="+mCacheMap.size());
	}
	
	/**
	 * get the image, either from the cache, or from the web if not in the cache. 
	 * Can be called by multiple threads. 
	 * If 2 threads ask for the same url simultaneously, this could put the image twice in the cache.
	 *  => TODO, have a "queue" of current requests. 
	 * @param url of the image
	 * @return the image, or null if any failure. 
	 */
	public Bitmap get(String url){
		Bitmap image;
		synchronized(mCacheMap) {
			image = mCacheMap.get(url);
		}
		if (image == null){
			Log.d(BonusPackHelper.LOG_TAG, "WebImageCache:load :"+url);
			image = BonusPackHelper.loadBitmap(url);
			if (image != null){
				putInCache(url, image);
			}
		}
		return image;
	}

}

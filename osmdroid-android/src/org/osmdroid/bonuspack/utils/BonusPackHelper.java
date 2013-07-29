package org.osmdroid.bonuspack.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.apache.http.NameValuePair;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

/** Useful functions and common constants. 
 * @author M.Kergall
 */
public class BonusPackHelper {

	/** Log tag. */
	public static final String LOG_TAG = "BONUSPACK";
	
	/**	User agent sent to services by default */
	public static final String DEFAULT_USER_AGENT = "OsmBonusPack/1";
	
	/** @return true if the device is the emulator, false if actual device. 
	 */
	public static boolean isEmulator(){
		//return Build.MANUFACTURER.equals("unknown");
		return ("google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT));
	}
	
	/** 
	 * @return the whole content of the http request, as a string
	 */
	private static String readStream(HttpConnection connection){
		String result = connection.getContentAsString();
		return result;
	}

	/** sends an http request, and returns the whole content result in a String. 
	 * @param url
	 * @return the whole content, or null if any issue. 
	 */
	public static String requestStringFromUrl(String url) {
		HttpConnection connection = new HttpConnection();
		connection.doGet(url);
		String result = readStream(connection);
		connection.close();
		return result;
	}

	/** requestStringFromPost: do a post request to a url with name-value pairs, 
	 * and returns the whole content result in a String. 
	 * @param url
	 * @param nameValuePairs
	 * @return the content, or null if any issue. 
	 */
	public static String requestStringFromPost(String url, List<NameValuePair> nameValuePairs) {
		HttpConnection connection = new HttpConnection();
		connection.doPost(url, nameValuePairs);
		String result = readStream(connection);
		connection.close();
		return result;
	}

	/**
	 * Loads a bitmap from a url. 
	 * @param url
	 * @return the bitmap, or null if any issue. 
	 */
	public static Bitmap loadBitmap(String url) {
		Bitmap bitmap = null;
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			if (is == null)
				return null;
			bitmap = BitmapFactory.decodeStream(new FlushedInputStream(is));
			//Alternative providing better handling on loading errors?
			/*
			Drawable d = Drawable.createFromStream(new FlushedInputStream(is), null);
			if (is != null)
				is.close();
			if (d != null)
				bitmap = ((BitmapDrawable)d).getBitmap();
			*/
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	/**
	 * Workaround on Android issue
	 * see http://stackoverflow.com/questions/4601352/createfromstream-in-android-returning-null-for-certain-url
	 */
	static class FlushedInputStream extends FilterInputStream {
	    public FlushedInputStream(InputStream inputStream) {
	    	super(inputStream);
	    }

	    @Override public long skip(long n) throws IOException {
	        long totalBytesSkipped = 0L;
	        while (totalBytesSkipped < n) {
	            long bytesSkipped = in.skip(n - totalBytesSkipped);
	            if (bytesSkipped == 0L) {
	                  int byteValue = read();
	                  if (byteValue < 0) {
	                      break;  // we reached EOF
	                  } else {
	                      bytesSkipped = 1; // we read one byte
	                  }
	           }
	           totalBytesSkipped += bytesSkipped;
	        }
	        return totalBytesSkipped;
	    }
	}
}

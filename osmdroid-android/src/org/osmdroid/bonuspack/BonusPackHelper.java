package org.osmdroid.bonuspack;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

	//Static data. Apparently, we can have static data in an Android app... 
	public static final String LOG_TAG = "BONUSPACK"; /** Log tag. */

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
			bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

}

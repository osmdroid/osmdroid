package org.andnav.osm.tileprovider.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;

/**
 * Utility class for implementing Cloudmade authorization.
 * See http://developers.cloudmade.com/projects/show/auth
 */
public class CloudmadeUtil {

	private static final Logger logger = LoggerFactory.getLogger(CloudmadeUtil.class);
	
	/** the meta data key in the manifest */
	public static final String CLOUDMADE_KEY = "CLOUDMADE_KEY";

	/**
	 * Get the key from the manifest.
	 * @return the key, or null if not found
	 */
	public static String getCloudmadeKey(final Context aContext) {

		final ApplicationInfo info = aContext.getApplicationInfo();

		if (info.metaData != null) {
			final String key = info.metaData.getString(CLOUDMADE_KEY);
			if (key != null && key.trim().length() > 0) {
				logger.info("Cloudmade key from info: " + key);
				return key;
			}
		}

		final PackageManager pm = aContext.getPackageManager();
		try {
			final ApplicationInfo info2 = pm.getApplicationInfo(info.packageName, PackageManager.GET_META_DATA);
			if (info2.metaData != null) {
				final String key = info2.metaData.getString(CLOUDMADE_KEY);
				if (key != null && key.trim().length() > 0) {
					logger.info("Cloudmade key from package manager: " + key);
					return key;
				}
			}
		} catch (final NameNotFoundException e) {}
		
		logger.info("Cloudmade key not found in manifest");
		return null;
	}

	/**
	 * Get the token from the Cloudmade server.
	 * @param aKey the cloudmade key
	 * @return the token returned from the server, or null if not found
	 */
	// XXX perhaps we should throw these errors rather than returning null
	// XXX perhaps we should get userid differently
	public static String getCloudmadeToken(final String aKey) {
		final String url = "http://auth.cloudmade.com/token/" + aKey + "?userid=" + Settings.Secure.ANDROID_ID;
		final HttpClient httpClient = new DefaultHttpClient();  
		final HttpPost httpPost = new HttpPost(url);  
	    try {  
	        final HttpResponse response = httpClient.execute(httpPost);  
	        logger.info("Response from Cloudmade auth: " + response.getStatusLine());
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	        	final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        	final String line = br.readLine();
		        logger.info("First line from Cloudmade auth: " + line);
	        	return line.trim();
	        }
	    } catch (final ClientProtocolException e) {  
	        logger.error("Error authorizing with Cloudmade", e);
	    } catch (final IOException e) {  
	        logger.error("Error authorizing with Cloudmade", e);
	    }  		
	    return null;
	};
}

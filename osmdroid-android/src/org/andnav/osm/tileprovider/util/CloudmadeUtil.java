package org.andnav.osm.tileprovider.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
public class CloudmadeUtil implements OpenStreetMapTileProviderConstants {

	private static final Logger logger = LoggerFactory.getLogger(CloudmadeUtil.class);

	/** the meta data key in the manifest */
	public static final String CLOUDMADE_KEY = "CLOUDMADE_KEY";

	private static String mAndroidId = Settings.Secure.ANDROID_ID; // will get real id later

	/**
	 * Get the key from the manifest.
	 * @return the key, or null if not found
	 */
	public static String getCloudmadeKey(final Context aContext) {

		mAndroidId = Settings.Secure.getString(aContext.getContentResolver(), Settings.Secure.ANDROID_ID);

		final PackageManager pm = aContext.getPackageManager();
		try {
			final ApplicationInfo info = pm.getApplicationInfo(aContext.getPackageName(), PackageManager.GET_META_DATA);
			if (info.metaData != null) {
				final String key = info.metaData.getString(CLOUDMADE_KEY);
				if (key != null && key.trim().length() > 0) {
					if (DEBUGMODE)
						logger.debug("Cloudmade key: " + key);
					return key.trim();
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
	 * @throws CloudmadeException
	 */
	public static String getCloudmadeToken(final String aKey) throws CloudmadeException {
		final String url = "http://auth.cloudmade.com/token/" + aKey + "?userid=" + mAndroidId;
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpPost httpPost = new HttpPost(url);
		try {
			final HttpResponse response = httpClient.execute(httpPost);
			if (DEBUGMODE)
				logger.debug("Response from Cloudmade auth: " + response.getStatusLine());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), StreamUtils.IO_BUFFER_SIZE);
				final String line = br.readLine();
				if (DEBUGMODE)
					logger.debug("First line from Cloudmade auth: " + line);
				final String token = line.trim();
				if (token.length() == 0) {
					throw new CloudmadeException("No authorization token received from Cloudmade");
				}
				return token;
			}
		} catch (final IOException e) {
			throw new CloudmadeException("No authorization token received from Cloudmade", e);
		}
		throw new CloudmadeException("No authorization token received from Cloudmade");
	};
}

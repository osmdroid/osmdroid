package org.osmdroid.tileprovider.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.provider.Settings;

/**
 * Utility class for implementing Cloudmade authorization. See
 * http://developers.cloudmade.com/projects/show/auth
 *
 * The CloudMade token is persisted because it doesn't change:
 * http://support.cloudmade.com/answers/api-keys-and-authentication
 * "you will always get the same token for the unique user id"
 *
 */
public class CloudmadeUtil implements OpenStreetMapTileProviderConstants {

	private static final Logger logger = LoggerFactory.getLogger(CloudmadeUtil.class);

	/** the meta data key in the manifest */
	private static final String CLOUDMADE_KEY = "CLOUDMADE_KEY";

	/** the key for the id preference */
	private static final String CLOUDMADE_ID = "CLOUDMADE_ID";

	/** the key for the token preference */
	private static final String CLOUDMADE_TOKEN = "CLOUDMADE_TOKEN";

	private static String mAndroidId = Settings.Secure.ANDROID_ID; // will get real id later

	/** the key retrieved from the manifest */
	private static String mKey = "";

	/** the token */
	private static String mToken = "";

	private static Editor mPreferenceEditor;

	/**
	 * Retrieve the key from the manifest and store it for later use.
	 */
	public static void retrieveCloudmadeKey(final Context pContext) {

		mAndroidId = Settings.Secure.getString(pContext.getContentResolver(), Settings.Secure.ANDROID_ID);

		// get the key from the manifest
		final PackageManager pm = pContext.getPackageManager();
		try {
			final ApplicationInfo info = pm.getApplicationInfo(pContext.getPackageName(),
					PackageManager.GET_META_DATA);
			if (info.metaData == null) {
				logger.info("Cloudmade key not found in manifest");
			} else {
				final String key = info.metaData.getString(CLOUDMADE_KEY);
				if (key != null) {
					if (DEBUGMODE) {
						logger.debug("Cloudmade key: " + key);
					}
					mKey = key.trim();
				}
			}
		} catch (final NameNotFoundException e) {
			logger.info("Cloudmade key not found in manifest", e);
		}

		// if the id hasn't changed then set the token to the previous token
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(pContext);
		mPreferenceEditor = pref.edit();
		final String id = pref.getString(CLOUDMADE_ID, "");
		if (id.equals(mAndroidId)) {
			mToken = pref.getString(CLOUDMADE_TOKEN, "");
			// if we've got a token we don't need the editor any more
			if (mToken.length() > 0) {
				mPreferenceEditor = null;
			}
		} else {
			mPreferenceEditor.putString(CLOUDMADE_ID, mAndroidId);
			mPreferenceEditor.commit();
		}

	}

	/**
	 * Get the key that was previously retrieved from the manifest.
	 *
	 * @return the key, or empty string if not found
	 */
	public static String getCloudmadeKey() {
		return mKey;
	}

	/**
	 * Get the token from the Cloudmade server.
	 *
	 * @return the token returned from the server, or null if not found
	 */
	public static String getCloudmadeToken() {

		if (mToken.length() == 0) {
			synchronized (mToken) {
				// check again because it may have been set while we were blocking
				if (mToken.length() == 0) {
					final String url = "http://auth.cloudmade.com/token/" + mKey + "?userid=" + mAndroidId;
					final HttpClient httpClient = new DefaultHttpClient();
					final HttpPost httpPost = new HttpPost(url);
					try {
						final HttpResponse response = httpClient.execute(httpPost);
						if (DEBUGMODE) {
							logger.debug("Response from Cloudmade auth: " + response.getStatusLine());
						}
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							final BufferedReader br =
								new BufferedReader(
										new InputStreamReader(response.getEntity().getContent()),
										StreamUtils.IO_BUFFER_SIZE);
							final String line = br.readLine();
							if (DEBUGMODE) {
								logger.debug("First line from Cloudmade auth: " + line);
							}
							mToken = line.trim();
							if (mToken.length() > 0) {
								mPreferenceEditor.putString(CLOUDMADE_TOKEN, mToken);
								mPreferenceEditor.commit();
								// we don't need the editor any more
								mPreferenceEditor = null;
							} else {
								logger.error("No authorization token received from Cloudmade");
							}
						}
					} catch (final IOException e) {
						logger.error("No authorization token received from Cloudmade: " + e);
					}
				}
			}
		}

		return mToken;
	};
}

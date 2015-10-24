package org.osmdroid.tileprovider.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

/**
 * Utility class for implementing Cloudmade authorization. See
 * http://developers.cloudmade.com/projects/show/auth
 *
 * The CloudMade token is persisted because it doesn't change:
 * http://support.cloudmade.com/answers/api-keys-and-authentication
 * "you will always get the same token for the unique user id"
 *
 */
public class CloudmadeUtil  {

     public static boolean DEBUGMODE=false;
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
	public static void retrieveCloudmadeKey(final Context aContext) {

		mAndroidId = Settings.Secure.getString(aContext.getContentResolver(), Settings.Secure.ANDROID_ID);

		// get the key from the manifest
		mKey = ManifestUtil.retrieveKey(aContext, CLOUDMADE_KEY);

		// if the id hasn't changed then set the token to the previous token
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(aContext);
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
	 * Get the key that was previously retrieved from the manifest.
	 *
	 * @return the key, or empty string if not found
	 */
	public static void setCloudmadeKey(String key) {
		mKey=key;
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

					HttpURLConnection urlConnection=null;

					try {
						final URL urlToRequest = new URL(url);
						urlConnection = (HttpURLConnection) urlToRequest.openConnection();
						urlConnection.setDoOutput(true);
						urlConnection.setRequestMethod("POST");
						urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						urlConnection.setRequestProperty(OpenStreetMapTileProviderConstants.USER_AGENT, OpenStreetMapTileProviderConstants.getUserAgentValue());
						urlConnection.connect();
						if (DEBUGMODE) {
							Log.d(IMapView.LOGTAG,"Response from Cloudmade auth: " + urlConnection.getResponseMessage());
						}
						if (urlConnection.getResponseCode() == 200) {
							final BufferedReader br =
								new BufferedReader(
										new InputStreamReader(urlConnection.getInputStream()),
										StreamUtils.IO_BUFFER_SIZE);
							final String line = br.readLine();
							if (DEBUGMODE) {
								Log.d(IMapView.LOGTAG,"First line from Cloudmade auth: " + line);
							}
							mToken = line.trim();
							if (mToken.length() > 0) {
								mPreferenceEditor.putString(CLOUDMADE_TOKEN, mToken);
								mPreferenceEditor.commit();
								// we don't need the editor any more
								mPreferenceEditor = null;
							} else {
								Log.e(IMapView.LOGTAG,"No authorization token received from Cloudmade");
							}
						}
					} catch (final IOException e) {
						Log.e(IMapView.LOGTAG,"No authorization token received from Cloudmade: " + e);
					} finally {
						if (urlConnection!=null)
							try {
								urlConnection.disconnect();
							}
							catch (Exception ex){}
					}
				}
			}
		}

		return mToken;
	}
}

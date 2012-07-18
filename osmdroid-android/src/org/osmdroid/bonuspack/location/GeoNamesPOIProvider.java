package org.osmdroid.bonuspack.location;

import java.util.ArrayList;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.GeoPoint;
import android.util.Log;

/**
 * POI Provider using GeoNames services. 
 * Currently, "find Nearby Wikipedia" service. 
 * @see http://www.geonames.org
 * @author M.Kergall
 *
 */
public class GeoNamesPOIProvider {

	protected String mUserName;
	
	/**
	 * @param account the registered "username" to give to GeoNames service. 
	 * @see http://www.geonames.org/login
	 */
	public GeoNamesPOIProvider(String account){
		mUserName = account;
	}
	
	private String getUrlCloseTo(GeoPoint p, int maxResults, double maxDistance){
		StringBuffer url = new StringBuffer("http://api.geonames.org/findNearbyWikipediaJSON?");
		url.append("lat="+p.getLatitudeE6()*1E-6);
		url.append("&lng="+p.getLongitudeE6()*1E-6);
		url.append("&maxRows="+maxResults);
		url.append("&radius="+maxDistance); //km
		url.append("&lang="+Locale.getDefault().getLanguage());
		url.append("&username="+mUserName);
		return url.toString();
	}
	
	/**
	 * @param fullUrl
	 * @return the list of POI
	 */
	public ArrayList<POI> getThem(String fullUrl){
		Log.d(BonusPackHelper.LOG_TAG, "GeoNamesPOIProvider:get:"+fullUrl);
		String jString = BonusPackHelper.requestStringFromUrl(fullUrl);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "GeoNamesPOIProvider: request failed.");
			return null;
		}
		try {
			JSONObject jRoot = new JSONObject(jString);
			JSONArray jPlaceIds = jRoot.getJSONArray("geonames");
			int n = jPlaceIds.length();
			ArrayList<POI> pois = new ArrayList<POI>(n);
			for (int i=0; i<n; i++){
				JSONObject jPlace = jPlaceIds.getJSONObject(i);
				POI poi = new POI();
				poi.mLocation = new GeoPoint(jPlace.getDouble("lat"), 
						jPlace.getDouble("lng"));
				poi.mCategory = jPlace.optString("feature");
				poi.mType = jPlace.getString("title");
				poi.mDescription = jPlace.optString("summary");
				poi.mIconPath = jPlace.optString("thumbnailImg", null);
				if (poi.mIconPath != null){
					poi.mIcon = BonusPackHelper.loadBitmap(poi.mIconPath);
				}
				poi.mUrl = jPlace.optString("wikipediaUrl", null);
				if (poi.mUrl != null)
					poi.mUrl = "http://" + poi.mUrl;
				//other attributes: distance, rank?
				pois.add(poi);
			}
			return pois;
		}catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param position
	 * @param maxResults
	 * @param maxDistance in km. 20 km max for the free service. 
	 * @return list of POI, Wikipedia entries close to the position. Null if technical issue. 
	 */
	public ArrayList<POI> getPOICloseTo(GeoPoint position, 
			int maxResults, double maxDistance){
		String url = getUrlCloseTo(position, maxResults, maxDistance);
		return getThem(url);
	}
}

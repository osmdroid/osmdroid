package org.osmdroid.bonuspack.location;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class GeoNamesPOIProvider {

	protected String mUserName;
	
	public GeoNamesPOIProvider(String userName){
		mUserName = userName;
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
	
	protected ArrayList<POI> getThem(Context ctx, String url){
		Log.d(BonusPackHelper.LOG_TAG, "NominatimPOIProvider:get:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "NominatimPOIProvider: request failed.");
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
				poi.mIconPath = jPlace.optString("thumbnailImg");
				if (!(poi.mIconPath.equals(""))){
			    	Bitmap imgBitmap = BonusPackHelper.loadBitmap(poi.mIconPath);
		    		if (imgBitmap != null){
		    			Drawable img = new BitmapDrawable(ctx.getResources(), imgBitmap);
		    			poi.mIcon = img;
		    		}
				}
				poi.mUrl = jPlace.optString("wikipediaUrl");
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
	 * @param maxDistance in km
	 * @return list of POI, Wikipedia entries close to the position. Null if technical issue. 
	 */
	public ArrayList<POI> getPOICloseTo(Context ctx, GeoPoint position, 
			int maxResults, double maxDistance){
		String url = getUrlCloseTo(position, maxResults, maxDistance);
		return getThem(ctx, url);
	}
}

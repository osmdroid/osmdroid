package org.osmdroid.bonuspack.location;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * POI Provider using Nominatim service. <br>
 * See https://wiki.openstreetmap.org/wiki/Nominatim<br>
 * and http://open.mapquestapi.com/nominatim/<br>
 * 
 * @author M.Kergall
 */
public class NominatimPOIProvider {
/*
	As the doc lacks a lot of features, source code may help:
 	https://trac.openstreetmap.org/browser/applications/utils/nominatim/website/search.php

		 * featuretype= to select on feature type (country, city, state, settlement)<br>
		 * format=jsonv2 to get a place_rank<br>
		 * offset= to offset the result ?... <br>
		 * polygon=1 to get the border of the poi as a polygon<br>
		 * nearlat & nearlon = ???<br>
		 * routewidth/69 and routewidth/30 ???<br>
*/	
	public static final String MAPQUEST_POI_SERVICE = "http://open.mapquestapi.com/nominatim/v1/";
	public static final String NOMINATIM_POI_SERVICE = "http://nominatim.openstreetmap.org/";
	protected String mService;
	
	public NominatimPOIProvider(){
		mService = NOMINATIM_POI_SERVICE;
	}
	
	public void setService(String serviceUrl){
		mService = serviceUrl;
	}
	
	private StringBuffer getCommonUrl(String type, int maxResults){
		StringBuffer urlString = new StringBuffer(mService);
		urlString.append("search?");
		urlString.append("format=json");
		urlString.append("&q=["+type+"]");
		urlString.append("&limit="+maxResults);
		urlString.append("&bounded=1");
		return urlString;
	}
	
	private String getUrlInside(BoundingBoxE6 bb, String type, int maxResults){
		StringBuffer urlString = getCommonUrl(type, maxResults);
		urlString.append("&viewbox="+bb.getLonWestE6()*1E-6+","
				+bb.getLatNorthE6()*1E-6+","
				+bb.getLonEastE6()*1E-6+","
				+bb.getLatSouthE6()*1E-6);
		return urlString.toString();
	}
	
	private String getUrlCloseTo(GeoPoint p, String type, 
			int maxResults, double maxDistance){
		int maxD = (int)(maxDistance*1E6);
		BoundingBoxE6 bb = new BoundingBoxE6(p.getLatitudeE6()+maxD, 
				p.getLongitudeE6()+maxD,
				p.getLatitudeE6()-maxD,
				p.getLongitudeE6()-maxD);
		return getUrlInside(bb, type, maxResults);
	}
	
	/**
	 * @param url full URL request
	 * @return the list of POI, of null if technical issue. 
	 */
	public ArrayList<POI> getThem(String url){
		Log.d(BonusPackHelper.LOG_TAG, "NominatimPOIProvider:get:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "NominatimPOIProvider: request failed.");
			return null;
		}
		try {
			JSONArray jPlaceIds = new JSONArray(jString);
			int n = jPlaceIds.length();
			ArrayList<POI> pois = new ArrayList<POI>(n);
			Bitmap thumbnail = null;
			for (int i=0; i<n; i++){
				JSONObject jPlace = jPlaceIds.getJSONObject(i);
				POI poi = new POI();
				poi.mId = jPlace.optLong("osm_id");
				poi.mLocation = new GeoPoint(jPlace.getDouble("lat"), 
						jPlace.getDouble("lon"));
				poi.mCategory = jPlace.optString("class");
				poi.mType = jPlace.getString("type");
				poi.mDescription = jPlace.optString("display_name");
				poi.mThumbnailPath = jPlace.optString("icon", null);
	    		if (i==0 && poi.mThumbnailPath != null) {
	    			//first POI, and we have a thumbnail: load it
	    			thumbnail = BonusPackHelper.loadBitmap(poi.mThumbnailPath);
				}
	    		poi.mThumbnail = thumbnail;
				pois.add(poi);
			}
			return pois;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

 	/**
	 * @param position
	 * @param type an OpenStreetMap feature. 
	 * See http://wiki.openstreetmap.org/wiki/Map_Features 
		 or http://code.google.com/p/osmbonuspack/source/browse/trunk/OSMBonusPackDemo/res/values/poi_tags.xml
	 * @param maxResults the maximum number of POI returned. 
	 * Note that in any case, Nominatim will have an absolute maximum of 100. 
	 * @param maxDistance to the position, in degrees. 
	 * Note that it is used to build a bounding box around the position, not a circle. 
	 * @return the list of POI, null if technical issue. 
	 */
	public ArrayList<POI> getPOICloseTo(GeoPoint position, String type, 
			int maxResults, double maxDistance){
		String url = getUrlCloseTo(position, type, maxResults, maxDistance);
		return getThem(url);
	}
	
	/**
	 * @param boundingBox
	 * @param type OpenStreetMap feature
	 * @param maxResults
	 * @return list of POIs, null if technical issue. 
	 */
	public ArrayList<POI> getPOIInside(BoundingBoxE6 boundingBox, String type, int maxResults){
		String url = getUrlInside(boundingBox, type, maxResults);
		return getThem(url);
	}
	
	/**
	 * @param path
	 * Warning: a long path may cause a failure due to the url to be too long. 
	 * Using a simplified route may help (see Road.getRouteLow()). 
	 * @param type OpenStreetMap feature
	 * @param maxResults
	 * @param maxWidth to the path. Certainly not in degrees. Probably in km. 
	 * @return list of POIs, null if technical issue. 
	 */
	public ArrayList<POI> getPOIAlong(ArrayList<GeoPoint> path, String type, 
		int maxResults, double maxWidth){
		StringBuffer urlString = getCommonUrl(type, maxResults);
		urlString.append("&routewidth="+maxWidth);
		urlString.append("&route=");
		boolean isFirst = true;
		for (GeoPoint p:path){
			if (isFirst)
				isFirst = false;
			else 
				urlString.append(",");
			String lat = Double.toString(p.getLatitudeE6()*1E-6);
			lat = lat.substring(0, Math.min(lat.length(), 7));
			String lon = Double.toString(p.getLongitudeE6()*1E-6);
			lon = lon.substring(0, Math.min(lon.length(), 7));
			urlString.append(lat+","+lon);
				//limit the size of url as much as possible, as post method is not supported. 
		}
		return getThem(urlString.toString());
	}
}

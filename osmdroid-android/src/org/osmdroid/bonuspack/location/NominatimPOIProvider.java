package org.osmdroid.bonuspack.location;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * POI Provider using Nominatim service. 
 * https://wiki.openstreetmap.org/wiki/Nominatim
 * http://open.mapquestapi.com/nominatim/
 * 
 * As the doc lacks a lot of features, source code may help:
 * https://trac.openstreetmap.org/browser/applications/utils/nominatim/website/search.php
 * 
 * featuretype= to select on feature type (country, city, state, settlement)
 * jsonv2 to get a place_rank
 * offset to offset... 
 * polygon=1 to get the border as a polygon
 * viewboxlbrt???
 * nearlat & nearlon???
 * routewidth/69 and routewidth/30 ???
 * 
 * note that there is an absolute limit = 100
 * 
 * @author M.Kergall
 */
public class NominatimPOIProvider {
	
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
	
	private ArrayList<POI> getThem(Context ctx, String url){
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
			Drawable img = null;
			for (int i=0; i<n; i++){
				JSONObject jPlace = jPlaceIds.getJSONObject(i);
				POI poi = new POI();
				poi.mId = jPlace.optLong("osm_id");
				poi.mLocation = new GeoPoint(jPlace.getDouble("lat"), 
						jPlace.getDouble("lon"));
				poi.mCategory = jPlace.optString("class");
				poi.mType = jPlace.getString("type");
				poi.mDescription = jPlace.optString("display_name");
				poi.mIconPath = jPlace.optString("icon");
	    		if (i==0 && !(poi.mIconPath.equals(""))) { 
	    			//first POI, and we have an icon: load it
			    	Bitmap imgBitmap = BonusPackHelper.loadBitmap(poi.mIconPath);
		    		if (imgBitmap != null){
		    			img = new BitmapDrawable(ctx.getResources(), imgBitmap);
		    		}
				}
	    		poi.mIcon = img;
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
	 * @param type of poi
	 * @param maxResults
	 * @param maxDistance to the position, in degrees. It will be used to build a bounding box around position, not a circle. 
	 * @return list of POI, null if technical issue. 
	 */
	public ArrayList<POI> getPOICloseTo(Context ctx, GeoPoint position, String type, 
			int maxResults, double maxDistance){
		String url = getUrlCloseTo(position, type, maxResults, maxDistance);
		return getThem(ctx, url);
	}
	
	/**
	 * @param bb bounding box
	 * @param type Nominatim tag
	 * @param maxResults
	 * @return list of POIs, null if technical issue. 
	 */
	public ArrayList<POI> getPOIInside(Context ctx, BoundingBoxE6 boundingBox, String type, int maxResults){
		String url = getUrlInside(boundingBox, type, maxResults);
		return getThem(ctx, url);
	}
	
	/**
	 * @param path
	 * @param type Nominatim tag
	 * @param maxResults
	 * @param maxWidth to the path. 
	 * 	In what??? Certainly not in degrees. Probably in km. 
	 * @return list of POIs, null if technical issue. 
	 * TODO: try in POST, as route could make the URL GET too long. 
	 */
	public ArrayList<POI> getPOIAlong(Context ctx, ArrayList<GeoPoint> path, String type, 
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
			urlString.append(""+p.getLatitudeE6()*1E-6+","+p.getLongitudeE6()*1E-6);
		}
		return getThem(ctx, urlString.toString());
	}
}

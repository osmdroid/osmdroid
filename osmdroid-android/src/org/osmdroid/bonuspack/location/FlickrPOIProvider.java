package org.osmdroid.bonuspack.location;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.util.Log;

/**
 * POI Provider using Flickr service to get geolocalized photos. 
 * -- UNDER CONSTRUCTION --
 * @see http://www.flickr.com/services/api/flickr.photos.search.html
 * @author M.Kergall
 */
public class FlickrPOIProvider {

	//http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=5caeee81cda4eac3a3c4b7f81a490ee6&bbox=-1%2C47%2C2%2C49&has_geo=&format=rest&api_sig=2aaf090eef56c2ea6c349efef72cd642
	
	private String getUrlInside(BoundingBoxE6 boundingBox, int maxResults){
		StringBuffer url = new StringBuffer("http://api.flickr.com/services/rest/?method=flickr.photos.search");
		url.append("&api_key="+"5caeee81cda4eac3a3c4b7f81a490ee6");
		url.append("&bbox="+boundingBox.getLonWestE6()*1E-6);
		url.append(","+boundingBox.getLatSouthE6()*1E-6);
		url.append(","+boundingBox.getLonEastE6()*1E-6);
		url.append(","+boundingBox.getLatNorthE6()*1E-6);
		url.append("&has_geo");
		url.append("&format=json&nojsoncallback=1");
		url.append("&per_page="+maxResults);
		return url.toString();
	}
	
	public POI getPhoto(String photoId){
		//http://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=71801dd7da4aac971d4a7756efeb6aae&photo_id=7930486880&format=rest
		String url = "http://api.flickr.com/services/rest/?method=flickr.photos.getInfo"
			+ "&api_key=" + "71801dd7da4aac971d4a7756efeb6aae"
			+ "&photo_id=" + photoId
			+ "&format=json&nojsoncallback=1";
		Log.d(BonusPackHelper.LOG_TAG, "getPhoto:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "FlickrPOIProvider: request failed.");
			return null;
		}
		try {
			POI poi = new POI();
			JSONObject jRoot = new JSONObject(jString);
			JSONObject jPhoto = jRoot.getJSONObject("photo");
			JSONObject jLocation = jPhoto.getJSONObject("location");
			poi.mLocation = new GeoPoint(
					jLocation.getDouble("latitude"), 
					jLocation.getDouble("longitude"));
			JSONObject jTitle = jPhoto.getJSONObject("title");
			poi.mType = jTitle.getString("_content");
			JSONObject jDescription = jPhoto.getJSONObject("description");
			poi.mDescription = jDescription.getString("_content");
			String farm = jPhoto.getString("farm");
			String server = jPhoto.getString("server");
			String secret = jPhoto.getString("secret");
			poi.mThumbnailPath = "http://farm"+farm+".staticflickr.com/"+server+"/"+photoId+"_"+secret+"_s.jpg";
			//TODO: poi.mUrl = "http://www.flickr.com/photos/"+userId+"/"+photoId;
			return poi;
		}catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param fullUrl
	 * @return the list of POI
	 */
	public ArrayList<POI> getThem(String fullUrl){
		Log.d(BonusPackHelper.LOG_TAG, "FlickrPOIProvider:get:"+fullUrl);
		String jString = BonusPackHelper.requestStringFromUrl(fullUrl);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "FlickrPOIProvider: request failed.");
			return null;
		}
		try {
			JSONObject jRoot = new JSONObject(jString);
			JSONObject jPhotos = jRoot.getJSONObject("photos");
			JSONArray jPhotoArray = jPhotos.getJSONArray("photo");
			int n = jPhotoArray.length();
			ArrayList<POI> pois = new ArrayList<POI>(n);
			for (int i=0; i<n; i++){
				JSONObject jPhoto = jPhotoArray.getJSONObject(i);
				String photoId = jPhoto.getString("id");
				POI poi = getPhoto(photoId);
				if (poi != null)
					pois.add(poi);
			}
			Log.d(BonusPackHelper.LOG_TAG, "done");
			return pois;
		}catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param boundingBox
	 * @param maxResults
	 * @return list of POI, Wikipedia entries inside the bounding box. Null if technical issue. 
	 */
	public ArrayList<POI> getPOIInside(BoundingBoxE6 boundingBox, int maxResults){
		String url = getUrlInside(boundingBox, maxResults);
		return getThem(url);
	}
	
}

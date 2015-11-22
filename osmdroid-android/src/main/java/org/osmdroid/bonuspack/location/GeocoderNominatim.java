package org.osmdroid.bonuspack.location;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Implements an equivalent to Android Geocoder class, based on OpenStreetMap data and Nominatim API. <br>
 * @see <a href="http://wiki.openstreetmap.org/wiki/Nominatim">Nominatim Reference</a>
 * @see <a href="http://open.mapquestapi.com/nominatim/">Nominatim at MapQuest Open</a>
 *
 * Important: to use the public Nominatim service, you will have to define a user agent,
 * and adhere to the <a href="http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy">Nominatim usage policy</a>.
 *
 * @author M.Kergall
 */
public class GeocoderNominatim {
	public static final String NOMINATIM_SERVICE_URL = "http://nominatim.openstreetmap.org/";
	public static final String MAPQUEST_SERVICE_URL = "http://open.mapquestapi.com/nominatim/v1/";
	
	protected Locale mLocale;
	protected String mServiceUrl;
	protected String mUserAgent;
	protected boolean mPolygon;
	
	public GeocoderNominatim(Context context, Locale locale, String userAgent){
		mLocale = locale;
		setOptions(false);
		setService(NOMINATIM_SERVICE_URL); //default service
		mUserAgent = userAgent;
	}
	
	public GeocoderNominatim(Context context, String userAgent){
		this(context, Locale.getDefault(), userAgent);
	}

	static public boolean isPresent(){
		return true;
	}
	
	/**
	 * Specify the url of the Nominatim service provider to use. 
	 * Can be one of the predefined (NOMINATIM_SERVICE_URL or MAPQUEST_SERVICE_URL), 
	 * or another one, your local instance of Nominatim for instance. 
	 */
	public void setService(String serviceUrl){
		mServiceUrl = serviceUrl;
	}
	
	/**
	 * @param polygon true to get the polygon enclosing the location. 
	 */
	public void setOptions(boolean polygon){
		mPolygon = polygon;
	}
	
	/** 
	 * Build an Android Address object from the Nominatim address in JSON format. 
	 * Current implementation is mainly targeting french addresses,
	 * and will be quite basic on other countries.
	 * @return Android Address, or null if input is not valid.
	 */
	protected Address buildAndroidAddress(JsonObject jResult) throws JsonSyntaxException{
		Address gAddress = new Address(mLocale);
		if (!jResult.has("lat") || !jResult.has("lon") || !jResult.has("address"))
			return null;

		gAddress.setLatitude(jResult.get("lat").getAsDouble());
		gAddress.setLongitude(jResult.get("lon").getAsDouble());
		JsonObject jAddress = jResult.get("address").getAsJsonObject();

		int addressIndex = 0;
		if (jAddress.has("road")){
			gAddress.setAddressLine(addressIndex++, jAddress.get("road").getAsString());
			gAddress.setThoroughfare(jAddress.get("road").getAsString());
		}
		if (jAddress.has("suburb")){
			//gAddress.setAddressLine(addressIndex++, jAddress.getString("suburb"));
				//not kept => often introduce "noise" in the address.
			gAddress.setSubLocality(jAddress.get("suburb").getAsString());
		}
		if (jAddress.has("postcode")){
			gAddress.setAddressLine(addressIndex++, jAddress.get("postcode").getAsString());
			gAddress.setPostalCode(jAddress.get("postcode").getAsString());
		}
		
		if (jAddress.has("city")){
			gAddress.setAddressLine(addressIndex++, jAddress.get("city").getAsString());
			gAddress.setLocality(jAddress.get("city").getAsString());
		} else if (jAddress.has("town")){
			gAddress.setAddressLine(addressIndex++, jAddress.get("town").getAsString());
			gAddress.setLocality(jAddress.get("town").getAsString());
		} else if (jAddress.has("village")){
			gAddress.setAddressLine(addressIndex++, jAddress.get("village").getAsString());
			gAddress.setLocality(jAddress.get("village").getAsString());
		}
		
		if (jAddress.has("county")){ //France: departement
			gAddress.setSubAdminArea(jAddress.get("county").getAsString());
		}
		if (jAddress.has("state")){ //France: region
			gAddress.setAdminArea(jAddress.get("state").getAsString());
		}
		if (jAddress.has("country")){
			gAddress.setAddressLine(addressIndex++, jAddress.get("country").getAsString());
			gAddress.setCountryName(jAddress.get("country").getAsString());
		}
		if (jAddress.has("country_code"))
			gAddress.setCountryCode(jAddress.get("country_code").getAsString());
		
		/* Other possible OSM tags in Nominatim results not handled yet: 
		 * subway, golf_course, bus_stop, parking,...
		 * house, house_number, building
		 * city_district (13e Arrondissement)
		 * road => or highway, ...
		 * sub-city (like suburb) => locality, isolated_dwelling, hamlet ...
		 * state_district
		*/
		
		//Add non-standard (but very useful) information in Extras bundle:
		Bundle extras = new Bundle();
		if (jResult.has("polygonpoints")){
			JsonArray jPolygonPoints = jResult.get("polygonpoints").getAsJsonArray();
			ArrayList<GeoPoint> polygonPoints = new ArrayList<GeoPoint>(jPolygonPoints.size());
			for (int i=0; i<jPolygonPoints.size(); i++){
				JsonArray jCoords = jPolygonPoints.get(i).getAsJsonArray();
				double lon = jCoords.get(0).getAsDouble();
				double lat = jCoords.get(1).getAsDouble();
				GeoPoint p = new GeoPoint(lat, lon);
				polygonPoints.add(p);
			}
			extras.putParcelableArrayList("polygonpoints", polygonPoints);
		}
		if (jResult.has("boundingbox")){
			JsonArray jBoundingBox = jResult.get("boundingbox").getAsJsonArray();
			BoundingBoxE6 bb = new BoundingBoxE6(
					jBoundingBox.get(1).getAsDouble(), jBoundingBox.get(2).getAsDouble(), 
					jBoundingBox.get(0).getAsDouble(), jBoundingBox.get(3).getAsDouble());
			extras.putParcelable("boundingbox", bb);
		}
		if (jResult.has("osm_id")){
			long osm_id = jResult.get("osm_id").getAsLong();
			extras.putLong("osm_id", osm_id);
		}
		if (jResult.has("osm_type")){
			String osm_type = jResult.get("osm_type").getAsString();
			extras.putString("osm_type", osm_type);
		}
		if (jResult.has("display_name")){
			String display_name = jResult.get("display_name").getAsString();
			extras.putString("display_name", display_name);
		}
		gAddress.setExtras(extras);
		
		return gAddress;
	}
	
	/**
	 * Equivalent to Geocoder::getFromLocation(double latitude, double longitude, int maxResults). 
	 */
	public List<Address> getFromLocation(double latitude, double longitude, int maxResults) 
	throws IOException {
		String url = mServiceUrl
			+ "reverse?"
			+ "format=json"
			+ "&accept-language=" + mLocale.getLanguage()
			//+ "&addressdetails=1"
			+ "&lat=" + latitude 
			+ "&lon=" + longitude;
		Log.d(BonusPackHelper.LOG_TAG, "GeocoderNominatim::getFromLocation:"+url);
		String result = BonusPackHelper.requestStringFromUrl(url, mUserAgent);
		if (result == null)
			throw new IOException();
		try {
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(result);
			JsonObject jResult = json.getAsJsonObject();
			Address gAddress = buildAndroidAddress(jResult);
			List<Address> list = new ArrayList<Address>(1);
			if (gAddress != null)
				list.add(gAddress);
			return list;
		} catch (JsonSyntaxException e) {
			throw new IOException();
		}
	}

	/**
	 * Equivalent to Geocoder::getFromLocation(String locationName, int maxResults, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude)
	 * but adding bounded parameter. 
	 * @param bounded true = return only results which are inside the view box; false = view box is used as a preferred area to find search results. 
	 */
	public List<Address> getFromLocationName(String locationName, int maxResults, 
			double lowerLeftLatitude, double lowerLeftLongitude, 
			double upperRightLatitude, double upperRightLongitude,
			boolean bounded)
	throws IOException {
		String url = mServiceUrl
				+ "search?"
				+ "format=json"
				+ "&accept-language=" + mLocale.getLanguage()
				+ "&addressdetails=1"
				+ "&limit=" + maxResults
				+ "&q=" + URLEncoder.encode(locationName);
		if (lowerLeftLatitude != 0.0 && upperRightLatitude != 0.0){
			//viewbox = left, top, right, bottom:
			url += "&viewbox=" + lowerLeftLongitude
				+ "," + upperRightLatitude
				+ "," + upperRightLongitude
				+ "," + lowerLeftLatitude
				+ "&bounded="+(bounded ? 1 : 0);
		}
		if (mPolygon){
			//get polygon outlines for items found:
			url += "&polygon=1";
			//TODO: polygon param is obsolete. Should be replaced by polygon_geojson. 
			//Upgrade is on hold, waiting for MapQuest service to become compatible. 
		}
		Log.d(BonusPackHelper.LOG_TAG, "GeocoderNominatim::getFromLocationName:"+url);
		String result = BonusPackHelper.requestStringFromUrl(url, mUserAgent);
		//Log.d(BonusPackHelper.LOG_TAG, result);
		if (result == null)
			throw new IOException();
		try {
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(result);
			JsonArray jResults = json.getAsJsonArray();
			List<Address> list = new ArrayList<Address>(jResults.size());
			for (int i=0; i<jResults.size(); i++){
				JsonObject jResult = jResults.get(i).getAsJsonObject();
				Address gAddress = buildAndroidAddress(jResult);
				if (gAddress != null)
					list.add(gAddress);
			}
			//Log.d(BonusPackHelper.LOG_TAG, "done");
			return list;
		} catch (JsonSyntaxException e) {
			throw new IOException();
		}
	}
	
	/**
	 * Equivalent to Geocoder::getFromLocation(String locationName, int maxResults, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude)
	 * @see #getFromLocationName(String locationName, int maxResults) about extra data added in Address results. 
	 */
	public List<Address> getFromLocationName(String locationName, int maxResults, 
			double lowerLeftLatitude, double lowerLeftLongitude, 
			double upperRightLatitude, double upperRightLongitude)
	throws IOException {
		return getFromLocationName(locationName, maxResults, 
				lowerLeftLatitude, lowerLeftLongitude, 
				upperRightLatitude, upperRightLongitude, true);
	}

	/**
	 * Equivalent to Geocoder::getFromLocation(String locationName, int maxResults). <br>
	 * 
	 * Some useful information, returned by Nominatim, that doesn't fit naturally within Android Address, are added in the bundle Address.getExtras():<br>
	 * "boundingbox": the enclosing bounding box, as a BoundingBoxE6<br>
	 * "osm_id": the OSM id, as a long<br>
	 * "osm_type": one of the 3 OSM types, as a string (node, way, or relation). <br>
	 * "display_name": the address, as a single String<br>
	 * "polygonpoints": the enclosing polygon of the location (depending on setOptions usage), as an ArrayList of GeoPoint<br>
	 */
	public List<Address> getFromLocationName(String locationName, int maxResults)
	throws IOException {
		return getFromLocationName(locationName, maxResults, 0.0, 0.0, 0.0, 0.0, false);
	}
	
}

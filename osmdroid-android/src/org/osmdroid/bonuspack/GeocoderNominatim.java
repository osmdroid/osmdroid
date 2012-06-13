package org.osmdroid.bonuspack;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.location.Address;
import android.util.Log;

/**
 * Implements an equivalent to Android Geocoder class, based on OpenStreetMap data and Nominatim API. <br>
 * See http://wiki.openstreetmap.org/wiki/Nominatim 
 * or http://open.mapquestapi.com/nominatim/
 * @author M.Kergall
 */
public class GeocoderNominatim {
	public static final String NOMINATIM_SERVICE_URL = "http://nominatim.openstreetmap.org/";
	public static final String MAPQUEST_SERVICE_URL = "http://open.mapquestapi.com/nominatim/v1/";
	
	protected Locale mLocale;
	protected String mServiceUrl;
	
	protected void init(Context context, Locale locale){
		mLocale = locale;
		setService(MAPQUEST_SERVICE_URL); //default service
	}
	
	public GeocoderNominatim(Context context, Locale locale){
		init(context, locale);
	}
	
	public GeocoderNominatim(Context context){
		init(context, Locale.getDefault());
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
	 * Build an Android Address object from the Nominatim address in JSON format. 
	 * Current implementation is mainly targeting french addresses,
	 * and will be quite basic on other countries. 
	 */
	protected Address buildAndroidAddress(JSONObject jResult) throws JSONException{
		Address gAddress = new Address(mLocale);
		gAddress.setLatitude(jResult.getDouble("lat"));
		gAddress.setLongitude(jResult.getDouble("lon"));

		JSONObject jAddress = jResult.getJSONObject("address");

		int addressIndex = 0;
		if (jAddress.has("road")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("road"));
			gAddress.setThoroughfare(jAddress.getString("road"));
		}
		if (jAddress.has("suburb")){
			//gAddress.setAddressLine(addressIndex++, jAddress.getString("suburb"));
				//not kept => often introduce "noise" in the address.
			gAddress.setSubLocality(jAddress.getString("suburb"));
		}
		if (jAddress.has("postcode")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("postcode"));
			gAddress.setPostalCode(jAddress.getString("postcode"));
		}
		
		if (jAddress.has("city")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("city"));
			gAddress.setLocality(jAddress.getString("city"));
		} else if (jAddress.has("town")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("town"));
			gAddress.setLocality(jAddress.getString("town"));
		} else if (jAddress.has("village")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("village"));
			gAddress.setLocality(jAddress.getString("village"));
		}
		
		if (jAddress.has("county")){ //France: departement
			gAddress.setSubAdminArea(jAddress.getString("county"));
		}
		if (jAddress.has("state")){ //France: region
			gAddress.setAdminArea(jAddress.getString("state"));
		}
		if (jAddress.has("country")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("country"));
			gAddress.setCountryName(jAddress.getString("country"));
		}
		if (jAddress.has("country_code"))
			gAddress.setCountryCode(jAddress.getString("country_code"));
		
		/* Other possible OSM tags in Nominatim results not handled yet: 
		 * subway, golf_course, bus_stop, parking,...
		 * house, house_number, building
		 * city_district (13e Arrondissement)
		 * road => or highway, ...
		 * sub-city (like suburb) => locality, isolated_dwelling, hamlet ...
		 * state_district
		*/
		
		return gAddress;
	}
	
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
		String result = BonusPackHelper.requestStringFromUrl(url);
		//Log.d("NOMINATIM", result);
		if (result == null)
			throw new IOException();
		try {
			JSONObject jResult = new JSONObject(result);
			Address gAddress = buildAndroidAddress(jResult);
			List<Address> list = new ArrayList<Address>();
			list.add(gAddress);
			return list;
		} catch (JSONException e) {
			throw new IOException();
		}
	}
	
	public List<Address> getFromLocationName(String locationName, int maxResults, 
			double lowerLeftLatitude, double lowerLeftLongitude, 
			double upperRightLatitude, double upperRightLongitude)
	throws IOException {
		String url = mServiceUrl
			+ "search?"
			+ "format=json"
			+ "&accept-language=" + mLocale.getLanguage()
			+ "&addressdetails=1"
			+ "&limit=" + maxResults
			+ "&q=" + URLEncoder.encode(locationName);
		if (lowerLeftLatitude != 0.0 && lowerLeftLongitude != 0.0){
			//viewbox = left, top, right, bottom:
			url += "&viewbox=" + lowerLeftLongitude
				+ "," + upperRightLatitude
				+ "," + upperRightLongitude
				+ "," + lowerLeftLatitude
				+ "&bounded=1";
		}
		Log.d(BonusPackHelper.LOG_TAG, "GeocoderNominatim::getFromLocationName:"+url);
		String result = BonusPackHelper.requestStringFromUrl(url);
		//Log.d(BonusPackHelper.LOG_TAG, result);
		if (result == null)
			throw new IOException();
		try {
			JSONArray jResults = new JSONArray(result);
			List<Address> list = new ArrayList<Address>();
			for (int i=0; i<jResults.length(); i++){
				JSONObject jResult = jResults.getJSONObject(i);
				Address gAddress = buildAndroidAddress(jResult);
				list.add(gAddress);
			}
			return list;
		} catch (JSONException e) {
			throw new IOException();
		}
	}

	public List<Address> getFromLocationName(String locationName, int maxResults)
	throws IOException {
		return getFromLocationName(locationName, maxResults, 0.0, 0.0, 0.0, 0.0);
	}
	
}

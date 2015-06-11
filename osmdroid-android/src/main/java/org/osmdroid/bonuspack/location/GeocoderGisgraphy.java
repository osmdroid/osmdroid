package org.osmdroid.bonuspack.location;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import android.content.Context;
import android.location.Address;
import android.util.Log;

/**
 * Experimental. Implements an equivalent to Android Geocoder class, based on OpenStreetMap data and Gisgraphy service. <br>
 * 
 * Usage sample: http://services.gisgraphy.com/geocoding/geocode?address=nantes&country=fr&format=json
 * 
 * First feedback: 
 * 
 * pros: better tolerance than Nominatim to changes in wording (e.g. "street" instead of "avenue", "road", etc)
 * 
 * cons: 
 * - country parameter is currently mandatory => this is a major drawback. 
 * - lot of street names, hamlets, etc - that are displayed on standard OSM map - are not recognized at all, 
 * where Nominatim perfectly geocodes them. 
 * - there is no "maxresults/limit" parameter. 10 results are usually returned, a lot of them completely irrelevant. 
 * 
 * @see <a href="http://www.gisgraphy.com/documentation/user-guide.htm#geocodingservice">Gisgraphy API</a>
 * @author M.Kergall
 *
 */
public class GeocoderGisgraphy {

	public static final String GISGRAPHY_SERVICE_URL = "http://services.gisgraphy.com/";

	protected Locale mLocale;
	protected String mServiceUrl;

	public GeocoderGisgraphy(Context context, Locale locale){
		mLocale = locale;
		setService(GISGRAPHY_SERVICE_URL); //default service
	}
	
	public GeocoderGisgraphy(Context context){
		this(context, Locale.getDefault());
	}

	static public boolean isPresent(){
		return true;
	}
	
	/**
	 * Specify the url of the service provider to use. 
	 */
	public void setService(String serviceUrl){
		mServiceUrl = serviceUrl;
	}
	
	/** 
	 * Build an Android Address object from the Gisgraphy address in JSON format. 
	 */
	protected Address buildAndroidAddress(JSONObject jResult) throws JSONException{
		Address gAddress = new Address(mLocale);
		gAddress.setLatitude(jResult.getDouble("lat"));
		gAddress.setLongitude(jResult.getDouble("lng"));

		int addressIndex = 0;
		if (jResult.has("streetName")){
			gAddress.setAddressLine(addressIndex++, jResult.getString("streetName"));
			gAddress.setThoroughfare(jResult.getString("streetName"));
		}
		/*
		if (jResult.has("suburb")){
			//gAddress.setAddressLine(addressIndex++, jResult.getString("suburb"));
				//not kept => often introduce "noise" in the address.
			gAddress.setSubLocality(jResult.getString("suburb"));
		}
		*/
		if (jResult.has("zipCode")){
			gAddress.setAddressLine(addressIndex++, jResult.getString("zipCode"));
			gAddress.setPostalCode(jResult.getString("zipCode"));
		}
		
		if (jResult.has("city")){
			gAddress.setAddressLine(addressIndex++, jResult.getString("city"));
			gAddress.setLocality(jResult.getString("city"));
		}
		
		if (jResult.has("state")){ //France: region
			gAddress.setAdminArea(jResult.getString("state"));
		}
		if (jResult.has("country")){
			gAddress.setAddressLine(addressIndex++, jResult.getString("country"));
			gAddress.setCountryName(jResult.getString("country"));
		}
		if (jResult.has("countrycode"))
			gAddress.setCountryCode(jResult.getString("countrycode"));
		
		return gAddress;
	}

	/**
	 * Equivalent to Geocoder::getFromLocation(String locationName, int maxResults). 
	 */
	public List<Address> getFromLocationName(String locationName, int maxResults)
	throws IOException {
		String url = mServiceUrl
			+ "geocoding/geocode?"
			+ "format=json"
			+ "&country=" + mLocale.getLanguage()
			+ "&address=" + URLEncoder.encode(locationName);
		Log.d(BonusPackHelper.LOG_TAG, "GeocoderGisgraphy::getFromLocationName:"+url);
		String result = BonusPackHelper.requestStringFromUrl(url);
		//Log.d(BonusPackHelper.LOG_TAG, result);
		if (result == null)
			throw new IOException();
		try {
			JSONObject jsonResult = new JSONObject(result);
			JSONArray jResults = jsonResult.getJSONArray("result");
			List<Address> list = new ArrayList<Address>(jResults.length());
			int n = Math.min(maxResults, jResults.length());
			for (int i=0; i<n; i++){
				JSONObject jResult = jResults.getJSONObject(i);
				Address gAddress = buildAndroidAddress(jResult);
				list.add(gAddress);
			}
			return list;
		} catch (JSONException e) {
			throw new IOException();
		}
	}
	
}

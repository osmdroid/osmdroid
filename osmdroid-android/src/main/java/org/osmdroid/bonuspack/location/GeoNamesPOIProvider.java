package org.osmdroid.bonuspack.location;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * POI Provider using GeoNames services. 
 * Currently, "find Nearby Wikipedia" and "Wikipedia Articles in Bounding Box" services. 
 * @see <a href="http://www.geonames.org">GeoNames API</a>
 * @author M.Kergall
 */
public class GeoNamesPOIProvider {

	protected String mUserName;
	
	/**
	 * @param account the registered "username" to give to GeoNames service. 
	 * @see <a href="http://www.geonames.org/login">GeoNames Account</a>
	 */
	public GeoNamesPOIProvider(String account){
		mUserName = account;
	}
	
	private String getUrlCloseTo(GeoPoint p, int maxResults, double maxDistance){
		StringBuffer url = new StringBuffer("http://api.geonames.org/findNearbyWikipediaJSON?");
		url.append("lat="+p.getLatitude());
		url.append("&lng="+p.getLongitude());
		url.append("&maxRows="+maxResults);
		url.append("&radius="+maxDistance); //km
		url.append("&lang="+Locale.getDefault().getLanguage());
		url.append("&username="+mUserName);
		return url.toString();
	}
	
	private String getUrlInside(BoundingBoxE6 boundingBox, int maxResults){
		StringBuffer url = new StringBuffer("http://api.geonames.org/wikipediaBoundingBoxJSON?");
		url.append("south="+boundingBox.getLatSouthE6()*1E-6);
		url.append("&north="+boundingBox.getLatNorthE6()*1E-6);
		url.append("&east="+boundingBox.getLonEastE6()*1E-6);
		url.append("&west="+boundingBox.getLonWestE6()*1E-6);
		url.append("&maxRows="+maxResults);
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
				POI poi = new POI(POI.POI_SERVICE_GEONAMES_WIKIPEDIA);
				poi.mLocation = new GeoPoint(jPlace.getDouble("lat"), 
						jPlace.getDouble("lng"));
				poi.mCategory = jPlace.optString("feature");
				poi.mType = jPlace.getString("title");
				poi.mDescription = jPlace.optString("summary");
				poi.mThumbnailPath = jPlace.optString("thumbnailImg", null);
				/* This makes loading too long. 
				 * Thumbnail loading will be done only when needed, with POI.getThumbnail()
				if (poi.mThumbnailPath != null){
					poi.mThumbnail = BonusPackHelper.loadBitmap(poi.mThumbnailPath);
				}
				*/
				poi.mUrl = jPlace.optString("wikipediaUrl", null);
				if (poi.mUrl != null)
					poi.mUrl = "http://" + poi.mUrl;
				poi.mRank = jPlace.optInt("rank", 0);
				//other attributes: distance?
				pois.add(poi);
			}
			Log.d(BonusPackHelper.LOG_TAG, "done");
			return pois;
		}catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	//XML parsing seems 2 times slower than JSON parsing
	public ArrayList<POI> getThemXML(String fullUrl){
		Log.d(BonusPackHelper.LOG_TAG, "GeoNamesPOIProvider:get:"+fullUrl);
		HttpConnection connection = new HttpConnection();
		connection.doGet(fullUrl);
		InputStream stream = connection.getStream();
		if (stream == null){
			return null;
		}
		GeoNamesXMLHandler handler = new GeoNamesXMLHandler();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connection.close();
		Log.d(BonusPackHelper.LOG_TAG, "done");
		return handler.mPOIs;
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

class GeoNamesXMLHandler extends DefaultHandler {
	
	private String mString;
	double mLat, mLng;
	POI mPOI;
	ArrayList<POI> mPOIs;
	
	public GeoNamesXMLHandler() {
		mPOIs = new ArrayList<POI>();
	}
	
	@Override public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if (localName.equals("entry")){
			mPOI = new POI(POI.POI_SERVICE_GEONAMES_WIKIPEDIA);
		}
		mString = new String();
	}
	
	@Override public void characters(char[] ch, int start, int length)
	throws SAXException {
		String chars = new String(ch, start, length);
		mString = mString.concat(chars);
	}

	@Override public void endElement(String uri, String localName, String name)
	throws SAXException {
		if (localName.equals("lat")) {
			mLat = Double.parseDouble(mString);
		} else if (localName.equals("lng")) {
			mLng = Double.parseDouble(mString);
		} else if (localName.equals("feature")){
			mPOI.mCategory = mString;
		} else if (localName.equals("title")){
			mPOI.mType = mString;
		} else if (localName.equals("summary")){
			mPOI.mDescription = mString;
		} else if (localName.equals("thumbnailImg")){
			if (mString != null && !mString.equals(""))
				mPOI.mThumbnailPath = mString;
		} else if (localName.equals("wikipediaUrl")){
			if (mString != null && !mString.equals(""))
				mPOI.mUrl = "http://" + mString;
		} else if (localName.equals("rank")){
			mPOI.mRank = Integer.parseInt(mString);
		} else if (localName.equals("entry")) {
			mPOI.mLocation = new GeoPoint(mLat, mLng);
			mPOIs.add(mPOI);
		};
	}

}

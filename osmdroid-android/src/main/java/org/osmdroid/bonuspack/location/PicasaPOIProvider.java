package org.osmdroid.bonuspack.location;

import android.util.Log;

import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * POI Provider using Picasa service. 
 * @see <a href="https://developers.google.com/picasa-web/docs/2.0/reference">Picasa API</a>
 * @author M.Kergall
 */
public class PicasaPOIProvider {
	
	String mAccessToken;
	
	/**
	 * @param accessToken the account to give to the service. Null for public access. 
	 * @see <a href="https://developers.google.com/picasa-web/docs/2.0/developers_guide_protocol#CreatingAccount">Picasa Accounts</a>
	 */
	public PicasaPOIProvider(String accessToken){
		mAccessToken = accessToken;
	}

	private String getUrlInside(BoundingBoxE6 boundingBox, int maxResults, String query){
		StringBuffer url = new StringBuffer("http://picasaweb.google.com/data/feed/api/all?");
		url.append("bbox="+boundingBox.getLonWestE6()*1E-6);
		url.append(","+boundingBox.getLatSouthE6()*1E-6);
		url.append(","+boundingBox.getLonEastE6()*1E-6);
		url.append(","+boundingBox.getLatNorthE6()*1E-6);
		url.append("&max-results="+maxResults);
		url.append("&thumbsize=64c"); //thumbnail size: 64, cropped. 
		url.append("&fields=openSearch:totalResults,entry(summary,media:group/media:thumbnail,media:group/media:title,gphoto:*,georss:where,link)");
		if (query != null)
			url.append("&q="+URLEncoder.encode(query));
		if (mAccessToken != null){
			//TODO: warning: not tested... 
			url.append("&access_token="+mAccessToken);
		}
		return url.toString();
	}
	
	public ArrayList<POI> getThem(String fullUrl){
		Log.d(BonusPackHelper.LOG_TAG, "PicasaPOIProvider:get:"+fullUrl);
		HttpConnection connection = new HttpConnection();
		connection.doGet(fullUrl);
		InputStream stream = connection.getStream();
		if (stream == null){
			return null;
		}
		PicasaXMLHandler handler = new PicasaXMLHandler();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.getXMLReader().setFeature("http://xml.org/sax/features/namespaces", false);
			parser.getXMLReader().setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			parser.parse(stream, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connection.close();
		if (handler.mPOIs != null)
			Log.d(BonusPackHelper.LOG_TAG, "done:"+handler.mPOIs.size()+" got, on a total of:"+handler.mTotalResults);
		return handler.mPOIs;
	}
	
	/**
	 * @param boundingBox
	 * @param maxResults
	 * @param query - optional - full-text query string. Searches the title, caption and tags for the specified string value.
	 * @return list of POI, Picasa photos inside the bounding box. Null if technical issue. 
	 */
	public ArrayList<POI> getPOIInside(BoundingBoxE6 boundingBox, int maxResults, String query){
		String url = getUrlInside(boundingBox, maxResults, query);
		return getThem(url);
	}
}

class PicasaXMLHandler extends DefaultHandler {
	
	private String mString;
	double mLat, mLng;
	POI mPOI;
	ArrayList<POI> mPOIs;
	int mTotalResults;
	
	public PicasaXMLHandler() {
		mPOIs = new ArrayList<POI>();
	}
	
	@Override public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("entry")){
			mPOI = new POI(POI.POI_SERVICE_PICASA);
		} else if(qName.equals("media:thumbnail")){
			mPOI.mThumbnailPath = attributes.getValue("url");
		} else if (qName.equals("link")){
			String rel = attributes.getValue("rel");
			if ("http://schemas.google.com/photos/2007#canonical".equals(rel)){
				mPOI.mUrl = attributes.getValue("href");
				mPOI.mUrl = mPOI.mUrl.replaceFirst("https://", "http://");
			}
		}
		mString = new String();
	}
	
	@Override public void characters(char[] ch, int start, int length)
	throws SAXException {
		String chars = new String(ch, start, length);
		mString = mString.concat(chars);
	}

	final static int MAX_DESC_SIZE = 250;
	@Override public void endElement(String uri, String localName, String qName)
	throws SAXException {
		if (qName.equals("gml:pos")) {
			String[] coords = mString.split(" ");
			mLat = Double.parseDouble(coords[0]);
			mLng = Double.parseDouble(coords[1]);
		} else if (qName.equals("gphoto:id")){
			mPOI.mId = Long.parseLong(mString);
		} else if (qName.equals("media:title")){
			mPOI.mType = mString;
		} else if (qName.equals("summary")){
			mPOI.mDescription = mString;
			if (mPOI.mDescription.length()>MAX_DESC_SIZE)
				mPOI.mDescription = mPOI.mDescription.substring(0, MAX_DESC_SIZE)+" (...)";
		} else if (qName.equals("gphoto:albumtitle")){
			mPOI.mCategory = mString;
		} else if (qName.equals("entry")) {
			mPOI.mLocation = new GeoPoint(mLat, mLng);
			mPOIs.add(mPOI);
			mPOI = null;
		} else if (qName.equals("openSearch:totalResults")) {
			mTotalResults = Integer.parseInt(mString);
		}
	}

}

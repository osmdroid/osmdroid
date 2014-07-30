package org.osmdroid.bonuspack.routing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/** class to get a route between a start and a destination point, going through a list of waypoints. <br>
 * Note that displaying a route provided by Google on a non-Google map (like OSM) is not allowed by Google T&C. 
 * @see <a href="https://developers.google.com/maps/documentation/directions">Google Maps Directions API</a>
 * @author M.Kergall
 */
public class GoogleRoadManager extends RoadManager {
	
	static final String GOOGLE_DIRECTIONS_SERVICE = "http://maps.googleapis.com/maps/api/directions/xml?";
	
	/**
	 * Build the URL to Google Directions service returning a route in XML format
	 */
	protected String getUrl(ArrayList<GeoPoint> waypoints) {
		StringBuffer urlString = new StringBuffer(GOOGLE_DIRECTIONS_SERVICE);
		urlString.append("origin=");
		GeoPoint p = waypoints.get(0);
		urlString.append(geoPointAsString(p));
		urlString.append("&destination=");
		int destinationIndex = waypoints.size()-1;
		p = waypoints.get(destinationIndex);
		urlString.append(geoPointAsString(p));
		
		for (int i=1; i<destinationIndex; i++){
			if (i == 1)
				urlString.append("&waypoints=");
			else
				urlString.append("%7C"); // the pipe (|), url-encoded
			p = waypoints.get(i);
			urlString.append(geoPointAsString(p));
		}
		urlString.append("&units=metric&sensor=false");
		Locale locale = Locale.getDefault();
		urlString.append("&language="+locale.getLanguage());
		urlString.append(mOptions);
		return urlString.toString();
	}
	
	/** 
	 * @param waypoints list of GeoPoints. Must have at least 2 entries, start and end points. 
	 * @return the road
	 */
	@Override public Road getRoad(ArrayList<GeoPoint> waypoints) {
		String url = getUrl(waypoints);
		Log.d(BonusPackHelper.LOG_TAG, "GoogleRoadManager.getRoad:"+url);
		Road road = null;
		HttpConnection connection = new HttpConnection();
		connection.doGet(url);
		InputStream stream = connection.getStream();
		if (stream != null)
				road = getRoadXML(stream);
		connection.close();
		if (road == null || road.mRouteHigh.size()==0){
			//Create default road:
			road = new Road(waypoints);
		} else {
			//finalize road data update:
			for (RoadLeg leg : road.mLegs){
				road.mDuration += leg.mDuration;
				road.mLength += leg.mLength;
			}
			road.mStatus = Road.STATUS_OK;
		}
		Log.d(BonusPackHelper.LOG_TAG, "GoogleRoadManager.getRoad - finished");
		return road;
	}

	protected Road getRoadXML(InputStream is) {
		GoogleDirectionsHandler handler = new GoogleDirectionsHandler();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return handler.mRoad;
	}

}

class GoogleDirectionsHandler extends DefaultHandler {
	Road mRoad;
	RoadLeg mLeg;
	RoadNode mNode;
	boolean isPolyline, isOverviewPolyline, isLeg, isStep, isDuration, isDistance, isBB;
	int mValue;
	double mLat, mLng;
	double mNorth, mWest, mSouth, mEast;
	private StringBuilder mStringBuilder = new StringBuilder(1024);
	
	public GoogleDirectionsHandler() {
		isOverviewPolyline = isBB = isPolyline = isLeg = isStep = isDuration = isDistance = false;
		mRoad = new Road();
	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if (localName.equals("polyline")) {
			isPolyline = true;
		} else if (localName.equals("overview_polyline")) {
			isOverviewPolyline = true;
		} else if (localName.equals("leg")) {
			mLeg = new RoadLeg();
			isLeg = true;
		} else if (localName.equals("step")) {
			mNode = new RoadNode();
			isStep = true;
		} else if (localName.equals("duration")) {
			isDuration = true;
		} else if (localName.equals("distance")) {
			isDistance = true;
		} else if (localName.equals("bounds")) {
			isBB = true;
		}
		mStringBuilder.setLength(0);
	}

	/**
	 * Overrides org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public @Override void characters(char[] ch, int start, int length)
			throws SAXException {
		mStringBuilder.append(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (localName.equals("points")) {
			if (isPolyline) {
				//detailed piece of road for the step, to add:
				ArrayList<GeoPoint> polyLine = PolylineEncoder.decode(mStringBuilder.toString(), 10, false);
				mRoad.mRouteHigh.addAll(polyLine);
			} else if (isOverviewPolyline){
				//low-def polyline for the whole road:
				mRoad.setRouteLow(PolylineEncoder.decode(mStringBuilder.toString(), 10, false));
			}
		} else if (localName.equals("polyline")) {
			isPolyline = false;
		} else if (localName.equals("overview_polyline")) {
			isOverviewPolyline = false;
		} else if (localName.equals("value")) {
			mValue = Integer.parseInt(mStringBuilder.toString());
		} else if (localName.equals("duration")) {
			if (isStep)
				mNode.mDuration = mValue;
			else
				mLeg.mDuration = mValue;
			isDuration = false;
		} else if (localName.equals("distance")) {
			if (isStep)
				mNode.mLength = mValue/1000.0;
			else
				mLeg.mLength = mValue/1000.0;
			isDistance = false;
		} else if (localName.equals("html_instructions")) {
			if (isStep){
				String value = mStringBuilder.toString();
				//value = value.replaceAll("<[^>]*>", " "); //remove everything in <...>
				//value = value.replaceAll("&nbsp;", " ");
				mNode.mInstructions = value;
				//Log.d(BonusPackHelper.LOG_TAG, mString);
			}
		} else if (localName.equals("start_location")) {
			if (isStep)
				mNode.mLocation = new GeoPoint(mLat, mLng);
		} else if (localName.equals("step")) {
			mRoad.mNodes.add(mNode);
			isStep = false;
		} else if (localName.equals("leg")) {
			mRoad.mLegs.add(mLeg);
			isLeg = false;
		} else if (localName.equals("lat")) {
				mLat = Double.parseDouble(mStringBuilder.toString());
		} else if (localName.equals("lng")) {
				mLng = Double.parseDouble(mStringBuilder.toString());
		} else if (localName.equals("northeast")){
			if (isBB){
				mNorth = mLat;
				mEast = mLng;
			}
		} else if (localName.equals("southwest")){
			if (isBB){
				mSouth = mLat;
				mWest = mLng;
			}
		} else if (localName.equals("bounds")){
			mRoad.mBoundingBox = new BoundingBoxE6(mNorth, mEast, mSouth, mWest);
			isBB = false;
		}
	}
	
}

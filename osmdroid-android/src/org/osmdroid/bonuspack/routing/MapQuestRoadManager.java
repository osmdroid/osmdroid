package org.osmdroid.bonuspack.routing;

import java.io.InputStream;
import java.util.ArrayList;
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

/** class to get a route between a start and a destination point, going through a list of waypoints. 
 * 
 * It uses MapQuest open, public and free API, based on OpenStreetMap data. <br>
 * Return a "Road" object. 
 * @see Road
 * @see <a href="http://open.mapquestapi.com/guidance">MapQuest Guidance API</a>
 * @author M.Kergall
 */
public class MapQuestRoadManager extends RoadManager {
	
	static final String MAPQUEST_GUIDANCE_SERVICE = "http://open.mapquestapi.com/guidance/v1/route?";
	protected String mApiKey;
	
	/**
	 * @param apiKey MapQuest API key, mandatory to use the MapQuest Open service. 
	 * @see <a href="http://developer.mapquest.com">MapQuest API</a> for registration. 
	 */
	public MapQuestRoadManager(String apiKey){
		super();
		mApiKey = apiKey;
	}
	
	/**
	 * Build the URL to MapQuest service returning a route in XML format
	 * @param waypoints: array of waypoints, as [lat, lng], from start point to end point. 
	 */
	protected String getUrl(ArrayList<GeoPoint> waypoints) {
		StringBuffer urlString = new StringBuffer(MAPQUEST_GUIDANCE_SERVICE);
		urlString.append("key="+mApiKey);
		urlString.append("&from=");
		GeoPoint p = waypoints.get(0);
		urlString.append(geoPointAsString(p));
		
		for (int i=1; i<waypoints.size(); i++){
			p = waypoints.get(i);
			urlString.append("&to="+geoPointAsString(p));
		}
		
		urlString.append("&outFormat=xml");
		urlString.append("&shapeFormat=cmp"); //encoded polyline, much faster
		
		urlString.append("&narrativeType=text"); //or "none"
		//Locale locale = Locale.getDefault();
		//urlString.append("&locale="+locale.getLanguage()+"_"+locale.getCountry());
		
		urlString.append("&unit=k&fishbone=false");
		
		//urlString.append("&generalizeAfter=500" /*+&generalize=2"*/); 
			//500 points max, 2 meters tolerance
		
		//Warning: MapQuest Open API doc is sometimes WRONG:
		//- use unit, not units
		//- use fishbone, not enableFishbone
		//- locale (fr_FR, en_US) is supported but not documented. 
		//- generalize and generalizeAfter are not properly implemented
		urlString.append(mOptions);
		return urlString.toString();
	}
	
	/**
	 * @param waypoints list of GeoPoints. Must have at least 2 entries, start and end points. 
	 * @return the road
	 */
	@Override public Road getRoad(ArrayList<GeoPoint> waypoints) {
		String url = getUrl(waypoints);
		Log.d(BonusPackHelper.LOG_TAG, "MapQuestRoadManager.getRoute:"+url);
		Road road = null;
		HttpConnection connection = new HttpConnection();
		connection.doGet(url);
		InputStream stream = connection.getStream();
		if (stream != null)
			road = getRoadXML(stream, waypoints);
		connection.close();
		Log.d(BonusPackHelper.LOG_TAG, "MapQuestRoadManager.getRoute - finished");
		return road;
	}

	/** 
	 * XML implementation
	 * @param is: input stream to parse
	 * @return the road
	 */
	protected Road getRoadXML(InputStream is, ArrayList<GeoPoint> waypoints) {
		MapQuestGuidanceHandler handler = new MapQuestGuidanceHandler();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Road road = handler.mRoad;
		if (road != null && road.mStatus == Road.STATUS_OK){
			road.mNodes = finalizeNodes(road.mNodes, handler.mLinks, road.mRouteHigh);
			road.mRouteHigh = finalizeRoadShape(road, handler.mLinks);
			road.buildLegs(waypoints);
			road.mStatus = Road.STATUS_OK;
		} else {
			int status = (road != null ? road.mStatus : Road.STATUS_TECHNICAL_ISSUE);
			//Create default road:
			road = new Road(waypoints);
			road.mStatus = status;
		}
		return road;
	}
	
	protected ArrayList<RoadNode> finalizeNodes(ArrayList<RoadNode> mNodes, 
			ArrayList<RoadLink> mLinks, ArrayList<GeoPoint> polyline){
		int n = mNodes.size();
		if (n == 0)
			return mNodes;
		ArrayList<RoadNode> newNodes = new ArrayList<RoadNode>(n);
		RoadNode lastNode = null;
		for (int i=1; i<n-1; i++){ //1, n-1 => first and last MapQuest nodes are irrelevant.
			RoadNode node = mNodes.get(i);
			RoadLink link = mLinks.get(node.mNextRoadLink);
			if (lastNode!=null && (node.mInstructions == null || node.mManeuverType == 0)){
				//this node is irrelevant, don't keep it, 
				//but update values of last node:
				lastNode.mLength += link.mLength;
				lastNode.mDuration += (node.mDuration + link.mDuration);
			} else {
				//TODO: check not the end node (n-2)
				node.mLength = link.mLength;
				node.mDuration += link.mDuration;
				int locationIndex = link.mShapeIndex;
				node.mLocation = polyline.get(locationIndex);
				newNodes.add(node);
				lastNode = node;
			}
		}
		//switch to the new array of nodes:
		return newNodes;
	}
	
	/**
	 * Clean-up 2 useless portions of MapQuest road shape: before start node, and after end node. 
	 * @return new road shape
	 */
	public ArrayList<GeoPoint> finalizeRoadShape(Road road, ArrayList<RoadLink> links){
		ArrayList<GeoPoint> newShape = new ArrayList<GeoPoint>(road.mRouteHigh.size());
		RoadNode nodeStart = road.mNodes.get(0);
		RoadNode nodeEnd = road.mNodes.get(road.mNodes.size()-1);
		int shapeIndexStart = links.get(nodeStart.mNextRoadLink).mShapeIndex;
		int shapeIndexEnd = links.get(nodeEnd.mNextRoadLink).mShapeIndex;
		for (int i=shapeIndexStart; i<=shapeIndexEnd; i++){
		    newShape.add(road.mRouteHigh.get(i));
		}
		return newShape;
	}
}

/** Road Link is a portion of road between 2 "nodes" or intersections */
class RoadLink {
	/** in km/h */
	public double mSpeed; 
	/** in km */
	public double mLength; 
	/** in sec */
	public double mDuration;
	/** starting point of the link, as index in initial polyline */
	public int mShapeIndex; 
}

/** class to handle XML generated by MapQuest "guidance" open API. */
class MapQuestGuidanceHandler extends DefaultHandler {
	public Road mRoad;
	public ArrayList<RoadLink> mLinks;
	
	boolean isBB;
	boolean isGuidanceNodeCollection;
	private StringBuilder mStringBuilder = new StringBuilder(1024);
	double mLat, mLng;
	double mNorth, mWest, mSouth, mEast;
	RoadLink mLink;
	RoadNode mNode;
	
	public MapQuestGuidanceHandler() {
		isBB = isGuidanceNodeCollection = false;
		mRoad = new Road();
		mLinks = new ArrayList<RoadLink>();
	}

	@Override public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {		
		if (localName.equals("boundingBox"))
			isBB = true;
		else if (localName.equals("link"))
			mLink = new RoadLink();
		else if (localName.equals("node"))
			mNode = new RoadNode();
		else if (localName.equals("GuidanceNodeCollection"))
			isGuidanceNodeCollection = true;
		mStringBuilder.setLength(0);
	}

	/**
	 * Overrides org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override public void characters(char[] ch, int start, int length)
			throws SAXException {
			mStringBuilder.append(ch, start, length);
	}
	
	@Override public void endElement(String uri, String localName, String name)
	throws SAXException {
		if (localName.equals("lat")) {
			mLat = Double.parseDouble(mStringBuilder.toString());
		} else if (localName.equals("lng")) {
			mLng = Double.parseDouble(mStringBuilder.toString());
		} else if (localName.equals("shapePoints")) {
			mRoad.mRouteHigh = PolylineEncoder.decode(mStringBuilder.toString(), 10, false);
			//Log.d("DD", "High="+mRoad.mRouteHigh.size());
		} else if (localName.equals("generalizedShape")) {
			mRoad.setRouteLow(PolylineEncoder.decode(mStringBuilder.toString(), 10, false));
			//Log.d("DD", "Low="+mRoad.mRouteLow.size());
	    } else if (localName.equals("length")) {
			mLink.mLength = Double.parseDouble(mStringBuilder.toString());
	    } else if (localName.equals("speed")) {
			mLink.mSpeed = Double.parseDouble(mStringBuilder.toString());
	    } else if (localName.equals("shapeIndex")){
			mLink.mShapeIndex = Integer.parseInt(mStringBuilder.toString());
	    } else if (localName.equals("link")) {
            //End of a link: update road attributes:
            //GuidanceLinkCollection could in theory contain additional unused links, 
            //but normally not with fishbone set to false. 
            mLink.mDuration = mLink.mLength / mLink.mSpeed * 3600.0;
            mLinks.add(mLink);
            mRoad.mLength += mLink.mLength;
            mRoad.mDuration += mLink.mDuration;
            mLink = null;
		} else if (localName.equals("turnCost")){
			int turnCost = Integer.parseInt(mStringBuilder.toString());
			mNode.mDuration += turnCost;
			mRoad.mDuration += turnCost;
		} else if (localName.equals("maneuverType")){
			mNode.mManeuverType = Integer.parseInt(mStringBuilder.toString());
		} else if (localName.equals("info")){
			if (isGuidanceNodeCollection){
				if (mNode.mInstructions == null) 
					//this is first "info" value for this node, keep it:
					mNode.mInstructions = mStringBuilder.toString();
			}
		} else if (localName.equals("linkId")){
			if (isGuidanceNodeCollection)
				mNode.mNextRoadLink = Integer.parseInt(mStringBuilder.toString());
		} else if (localName.equals("node")){
			mRoad.mNodes.add(mNode);
			mNode = null;
		} else if (localName.equals("GuidanceNodeCollection")){
			isGuidanceNodeCollection = false;
		} else if (localName.equals("ul")){
			if (isBB){
				mNorth = mLat;
				mWest = mLng;
			}
		} else if (localName.equals("lr")){
			if (isBB){
				mSouth = mLat;
				mEast = mLng;
			}
		} else if (localName.equals("boundingBox")){
			mRoad.mBoundingBox = new BoundingBoxE6(mNorth, mEast, mSouth, mWest);
			isBB = false;
		} else if (localName.equals("statusCode")){
			mRoad.mStatus = Integer.parseInt(mStringBuilder.toString());
		}
	}
}

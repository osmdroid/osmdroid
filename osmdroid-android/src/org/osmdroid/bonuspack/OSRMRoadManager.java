package org.osmdroid.bonuspack;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import android.util.Log;

/** get a route between a start and a destination point.
 * It uses OSRM open source API, based on OpenSteetMap data. 
 * @see https://github.com/DennisOSRM/Project-OSRM/wiki/Server-api
 * 
 * It requests by default the OSRM demo site. 
 * Look at setService to request an other (for instance your own) OSRM service. 
 * 
 * Note that the result of OSRM is quite close with Cloudmade NavEngine format:
 * http://developers.cloudmade.com/wiki/navengine/JSON_format
 * 
 * @author M.Kergall
 */
public class OSRMRoadManager extends RoadManager {

	static final String OSRM_SERVICE = "http://router.project-osrm.org/viaroute?";
	
	//From: Project-OSRM-Web / WebContent / localization / OSRM.Locale.en.js
	// driving directions
	// %s: road name
	// %d: direction => removed
	// <*>: will only be printed when there actually is a road name
	static final HashMap<String, String> DIRECTIONS;
	static {
		DIRECTIONS = new HashMap<String, String>();
		DIRECTIONS.put("0", "Unknown instruction< on %s>");
		DIRECTIONS.put("1","Continue< on %s>");
		DIRECTIONS.put("2","Turn slight right< on %s>");
		DIRECTIONS.put("3","Turn right< on %s>");
		DIRECTIONS.put("4","Turn sharp right< on %s>");
		DIRECTIONS.put("5","U-Turn< on %s>");
		DIRECTIONS.put("6","Turn slight left< on %s>");
		DIRECTIONS.put("7","Turn left< on %s>");
		DIRECTIONS.put("8","Turn sharp left< on %s>");
		DIRECTIONS.put("10","Head< on %s>");
		DIRECTIONS.put("11-1","Enter roundabout and leave at first exit< on %s>");
		DIRECTIONS.put("11-2","Enter roundabout and leave at second exit< on %s>");
		DIRECTIONS.put("11-3","Enter roundabout and leave at third exit< on %s>");
		DIRECTIONS.put("11-4","Enter roundabout and leave at fourth exit< on %s>");
		DIRECTIONS.put("11-5","Enter roundabout and leave at fifth exit< on %s>");
		DIRECTIONS.put("11-6","Enter roundabout and leave at sixth exit< on %s>");
		DIRECTIONS.put("11-7","Enter roundabout and leave at seventh exit< on %s>");
		DIRECTIONS.put("11-8","Enter roundabout and leave at eighth exit< on %s>");
		DIRECTIONS.put("11-9","Enter roundabout and leave at nineth exit< on %s>");
		DIRECTIONS.put("11-x","Enter roundabout<and leave on %s>");
		DIRECTIONS.put("15","You have reached your destination");
	}
	
	protected String mServiceUrl;
	
	public OSRMRoadManager(){
		super();
		mServiceUrl = OSRM_SERVICE;
	}
	
	/** allows to request on an other site than OSRM demo site */
	public void setService(String serviceUrl){
		mServiceUrl = serviceUrl;
	}
	
	protected String getUrl(ArrayList<GeoPoint> waypoints){
		StringBuffer urlString = new StringBuffer(mServiceUrl);
		for (int i=0; i<waypoints.size(); i++){
			GeoPoint p = waypoints.get(i);
			urlString.append("&loc="+geoPointAsString(p));
		}
		urlString.append(mOptions);
		return urlString.toString();
	}

	public Road getRoad(ArrayList<GeoPoint> waypoints) {
		String url = getUrl(waypoints);
		Log.d(BonusPackHelper.LOG_TAG, "OSRMRoadManager.getRoad:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "OSRMRoadManager::getRoad: request failed.");
			return new Road(waypoints);
		}
		Road road = new Road();
		try {
			JSONObject jObject = new JSONObject(jString);
			String route_geometry = jObject.getString("route_geometry");
			road.mRouteHigh = PolylineEncoder.decode(route_geometry, 10);
			JSONArray jInstructions = jObject.getJSONArray("route_instructions");
			int n = jInstructions.length();
			RoadNode lastNode = null;
			for (int i=0; i<n; i++){
				JSONArray jInstruction = jInstructions.getJSONArray(i);
				RoadNode node = new RoadNode();
				int positionIndex = jInstruction.getInt(3);
				node.mLocation = road.mRouteHigh.get(positionIndex);
				node.mLength = jInstruction.getInt(2)/1000.0;
				node.mDuration = 1.0 * jInstruction.getInt(4)/10.0; //10th of seconds?
				String direction = jInstruction.getString(0);
				String roadName = jInstruction.getString(1);
				if (lastNode!=null && "1".equals(direction) && "".equals(roadName)){
					//node is useless, don't add it
					lastNode.mLength += node.mLength;
					lastNode.mDuration += node.mDuration;
				} else {
					node.mInstructions = buildInstructions(direction, roadName);
					road.mNodes.add(node);
					lastNode = node;
				}
			}
			JSONObject jSummary = jObject.getJSONObject("route_summary");
			road.mLength = jSummary.getInt("total_distance")/1000.0;
			road.mDuration = jSummary.getInt("total_time");
		} catch (JSONException e) {
			e.printStackTrace();
			return new Road(waypoints);
		}
		road.mBoundingBox = BoundingBoxE6.fromGeoPoints(road.mRouteHigh);
		//TODO: Road Legs to build...
		return road;
	}
	
	protected String buildInstructions(String direction, String roadName){
		direction = DIRECTIONS.get(direction);
		String instructions = null;
		if (direction != null){
			if (roadName.equals(""))
				//remove "<*>"
				instructions = direction.replaceFirst("<[^>]*>", "");
			else {
				direction = direction.replace('<', ' ');
				direction = direction.replace('>', ' ');
				instructions = String.format(direction, roadName);
			}
		}
		return instructions;
	}
}

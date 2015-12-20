package org.osmdroid.bonuspack.routing;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.R;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;

/** get a route between a start and a destination point, going through a list of waypoints.
 * It uses OSRM, a free open source routing service based on OpenSteetMap data. <br>
 *
 * It requests by default the OSRM demo site.
 * Use setService() to request an other (for instance your own) OSRM service. <br>
 *
 * TODO: improve internationalization of instructions
 *
 * @see <a href="https://github.com/DennisOSRM/Project-OSRM/wiki/Server-api">OSRM</a>
 *
 * @author M.Kergall
 */
public class OSRMRoadManager extends RoadManager {

	static final String SERVICE = "http://router.project-osrm.org/viaroute?";
	private final Context mContext;
	protected String mServiceUrl;
	protected String mUserAgent;

	/** mapping from OSRM directions to MapQuest maneuver IDs: */
	static final HashMap<String, Integer> MANEUVERS;
	static {
		MANEUVERS = new HashMap<String, Integer>();
		MANEUVERS.put("0", 0); //No instruction
		MANEUVERS.put("1", 1); //Continue
		MANEUVERS.put("2", 6); //Slight right
		MANEUVERS.put("3", 7); //Right
		MANEUVERS.put("4", 8); //Sharp right
		MANEUVERS.put("5", 12); //U-turn
		MANEUVERS.put("6", 5); //Sharp left
		MANEUVERS.put("7", 4); //Left
		MANEUVERS.put("8", 3); //Slight left
		MANEUVERS.put("9", 24); //Arrived (at waypoint)
		MANEUVERS.put("10", 24); //"Head" => used by OSRM as the start node. Considered here as a "waypoint". 
		MANEUVERS.put("11-1", 27); //Round-about, 1st exit
		MANEUVERS.put("11-2", 28); //2nd exit, etc ...
		MANEUVERS.put("11-3", 29);
		MANEUVERS.put("11-4", 30);
		MANEUVERS.put("11-5", 31);
		MANEUVERS.put("11-6", 32);
		MANEUVERS.put("11-7", 33);
		MANEUVERS.put("11-8", 34); //Round-about, 8th exit
		MANEUVERS.put("15", 24); //Arrived
	}
	
	//From: Project-OSRM-Web / WebContent / localization / OSRM.Locale.en.js
	// driving directions
	// %s: road name
	// %d: direction => removed
	// <*>: will only be printed when there actually is a road name
	static final HashMap<String, Object> DIRECTIONS;
	static {
		DIRECTIONS = new HashMap<>();
    DIRECTIONS.put("0", R.string.osmbonuspack_directions_0);
    DIRECTIONS.put("1", R.string.osmbonuspack_directions_1);
    DIRECTIONS.put("2", R.string.osmbonuspack_directions_2);
    DIRECTIONS.put("3", R.string.osmbonuspack_directions_3);
    DIRECTIONS.put("4", R.string.osmbonuspack_directions_4);
    DIRECTIONS.put("5", R.string.osmbonuspack_directions_5);
    DIRECTIONS.put("6", R.string.osmbonuspack_directions_6);
    DIRECTIONS.put("7", R.string.osmbonuspack_directions_7);
    DIRECTIONS.put("8", R.string.osmbonuspack_directions_8);
    DIRECTIONS.put("9", R.string.osmbonuspack_directions_9);
    DIRECTIONS.put("10", R.string.osmbonuspack_directions_10);
    DIRECTIONS.put("11-1", R.string.osmbonuspack_directions_11_1);
    DIRECTIONS.put("11-2", R.string.osmbonuspack_directions_11_2);
    DIRECTIONS.put("11-3", R.string.osmbonuspack_directions_11_3);
    DIRECTIONS.put("11-4", R.string.osmbonuspack_directions_11_4);
    DIRECTIONS.put("11-5", R.string.osmbonuspack_directions_11_5);
    DIRECTIONS.put("11-6", R.string.osmbonuspack_directions_11_6);
    DIRECTIONS.put("11-7", R.string.osmbonuspack_directions_11_7);
    DIRECTIONS.put("11-8", R.string.osmbonuspack_directions_11_8);
    DIRECTIONS.put("11-9", R.string.osmbonuspack_directions_11_9);
    DIRECTIONS.put("15", R.string.osmbonuspack_directions_15);
	}

	public OSRMRoadManager(Context context){
		super();
		mContext = context;
		mServiceUrl = SERVICE;
		mUserAgent = BonusPackHelper.DEFAULT_USER_AGENT; //set user agent to the default one. 
	}
	
	/** allows to request on an other site than OSRM demo site */
	public void setService(String serviceUrl){
		mServiceUrl = serviceUrl;
	}

	/** allows to send to OSRM service a user agent specific to the app, 
	 * instead of the default user agent of OSMBonusPack lib. 
	 */
	public void setUserAgent(String userAgent){
		mUserAgent = userAgent;
	}
	
	protected String getUrl(ArrayList<GeoPoint> waypoints, boolean getAlternate){
		StringBuffer urlString = new StringBuffer(mServiceUrl);
		for (int i=0; i<waypoints.size(); i++){
			GeoPoint p = waypoints.get(i);
			urlString.append("&loc="+geoPointAsString(p));
		}
		urlString.append("&instructions=true&alt="+(getAlternate?"true":"false"));
		urlString.append(mOptions);
		return urlString.toString();
	}

	protected void getInstructions(Road road, JSONArray jInstructions){
		try {
			int n = jInstructions.length();
			RoadNode lastNode = null;
			for (int i=0; i<n; i++){
				JSONArray jInstruction = jInstructions.getJSONArray(i);
				RoadNode node = new RoadNode();
				int positionIndex = jInstruction.getInt(3);
				node.mLocation = road.mRouteHigh.get(positionIndex);
				node.mLength = jInstruction.getInt(2)/1000.0;
				node.mDuration = jInstruction.getInt(4); //Segment duration in seconds.
				String direction = jInstruction.getString(0);
				String roadName = jInstruction.getString(1);
				if (lastNode!=null && "1".equals(direction) && "".equals(roadName)){
					//node "Continue" with no road name is useless, don't add it
					lastNode.mLength += node.mLength;
					lastNode.mDuration += node.mDuration;
				} else {
					node.mManeuverType = getManeuverCode(direction);
					node.mInstructions = buildInstructions(direction, roadName);
					//Log.d(BonusPackHelper.LOG_TAG, direction+"=>"+node.mManeuverType+"; "+node.mInstructions);
					road.mNodes.add(node);
					lastNode = node;
				}
			}
		} catch (JSONException e) {
			road.mStatus = Road.STATUS_TECHNICAL_ISSUE;
			e.printStackTrace();
		}
	}

	protected void getAlternateRoad(Road road, int altRoadIndex, JSONObject jObject){
		try {
			JSONArray alternative_geometries = jObject.getJSONArray("alternative_geometries");
			String route_geometry = alternative_geometries.getString(altRoadIndex);
			road.mRouteHigh = PolylineEncoder.decode(route_geometry, 1, false);
			JSONArray jInstructions = jObject.getJSONArray("alternative_instructions");
			getInstructions(road, jInstructions.getJSONArray(altRoadIndex));
			JSONArray jSummaries = jObject.getJSONArray("alternative_summaries");
			JSONObject jSummary = jSummaries.getJSONObject(altRoadIndex);
			road.mLength = jSummary.getInt("total_distance")/1000.0;
			road.mDuration = jSummary.getInt("total_time");
			road.mStatus = Road.STATUS_OK;
		} catch (JSONException e) {
			road.mStatus = Road.STATUS_TECHNICAL_ISSUE;
			e.printStackTrace();
		}
	}

	protected final static int OSRM_STATUS_OK = 200;

	protected Road[] getRoads(ArrayList<GeoPoint> waypoints, boolean getAlternate) {
		String url = getUrl(waypoints, getAlternate);
		Log.d(BonusPackHelper.LOG_TAG, "OSRMRoadManager.getRoads:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url, mUserAgent);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "OSRMRoadManager::getRoad: request failed.");
			Road[] roads = new Road[1];
			roads[0] = new Road(waypoints);
			return roads;
		}
		Road roads[] = new Road[0];
		Road road = new Road();
		try {
			JSONObject jObject = new JSONObject(jString);
			int jStatus = jObject.getInt("status");
			if (jStatus != OSRM_STATUS_OK)
				road.mStatus = Road.STATUS_TECHNICAL_ISSUE;
			else {
				road.mStatus = Road.STATUS_OK;
				String route_geometry = jObject.getString("route_geometry");
				road.mRouteHigh = PolylineEncoder.decode(route_geometry, 1, false);
				JSONArray jInstructions = jObject.getJSONArray("route_instructions");
				getInstructions(road, jInstructions);
				JSONObject jSummary = jObject.getJSONObject("route_summary");
				road.mLength = jSummary.getInt("total_distance") / 1000.0;
				road.mDuration = jSummary.getInt("total_time");
				String found_alternative = jObject.getString("found_alternative");
				if ("true".equals(found_alternative)) {
					JSONArray alternative_geometries = jObject.getJSONArray("alternative_geometries");
					int nbAltRoads = alternative_geometries.length();
					roads = new Road[nbAltRoads + 1];
					roads[0] = road;
					for (int i = 0; i < nbAltRoads; i++) {
						roads[i + 1] = new Road(waypoints);
						getAlternateRoad(roads[i + 1], i, jObject);
					}
				} else {
					roads = new Road[1];
					roads[0] = road;
				}
			} //if status OK
		} catch (JSONException e) {
			road.mStatus = Road.STATUS_TECHNICAL_ISSUE;
			e.printStackTrace();
		}
		if (road.mStatus != Road.STATUS_OK){
			//Create default road:
			int status = road.mStatus;
			road = new Road(waypoints);
			road.mStatus = status;
			roads = new Road[1];
			roads[0] = road;
		} else {
			for (int i = 0; i < roads.length; i++){
				roads[i].buildLegs(waypoints);
				roads[i].mBoundingBox = BoundingBoxE6.fromGeoPoints(road.mRouteHigh);
				roads[i].mStatus = Road.STATUS_OK;
			}
		}
		Log.d(BonusPackHelper.LOG_TAG, "OSRMRoadManager.getRoads - finished");
		return roads;
	}

	@Override public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
		return getRoads(waypoints, true);
	}

	@Override public Road getRoad(ArrayList<GeoPoint> waypoints) {
		Road[] roads = getRoads(waypoints, false);
		return roads[0];
	}

	protected int getManeuverCode(String direction){
		Integer code = MANEUVERS.get(direction);
		if (code != null)
			return code;
		else 
			return 0;
	}

	protected String buildInstructions(String direction, String roadName){
		Integer resDirection = (Integer) DIRECTIONS.get(direction);
		if (resDirection == null) return null;
		direction = mContext.getString(resDirection);
		String instructions;
		if (roadName.equals(""))
			//remove "<*>"
			instructions = direction.replaceFirst("\\[[^\\]]*\\]", "");
		else {
			direction = direction.replace('[', ' ');
			direction = direction.replace(']', ' ');
			instructions = String.format(direction, roadName);
		}
		return instructions;
	}
}

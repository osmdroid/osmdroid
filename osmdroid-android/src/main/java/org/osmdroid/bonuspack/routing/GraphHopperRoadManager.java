package org.osmdroid.bonuspack.routing;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;

/** get a route between a start and a destination point, going through a list of waypoints.
 * It uses GraphHopper, an open source routing service based on OpenSteetMap data. <br>
 * 
 * It requests by default the GraphHopper demo site. 
 * Use setService() to request another (for instance your own) GraphHopper-compliant service. <br> 
 * 
 * @see <a href="https://github.com/graphhopper/web-api/blob/master/docs-routing.md">GraphHopper</a>
 * @author M.Kergall
 */
public class GraphHopperRoadManager extends RoadManager {

	protected static final String SERVICE = "https://graphhopper.com/api/1/route?";
	public static final int STATUS_NO_ROUTE = Road.STATUS_TECHNICAL_ISSUE+1;
	
	protected String mServiceUrl;
	protected String mKey;
	protected boolean mWithElevation;
	
	/** mapping from GraphHopper directions to MapQuest maneuver IDs: */
	static final HashMap<Integer, Integer> MANEUVERS;
	static {
		MANEUVERS = new HashMap<Integer, Integer>();
		MANEUVERS.put(0, 1); //Continue
		MANEUVERS.put(1, 6); //Slight right
		MANEUVERS.put(2, 7); //Right
		MANEUVERS.put(3, 8); //Sharp right
		MANEUVERS.put(-3, 5); //Sharp left
		MANEUVERS.put(-2, 4); //Left
		MANEUVERS.put(-1, 3); //Slight left
		MANEUVERS.put(4, 24); //Arrived
		MANEUVERS.put(5, 24); //Arrived at waypoint
	}
	
	/**
	 * @param apiKey GraphHopper API key, mandatory to use the public GraphHopper service.
	 * @see <a href="http://graphhopper.com/#enterprise">GraphHopper</a> to obtain an API key.
	 */
	public GraphHopperRoadManager(String apiKey) {
		super();
		mServiceUrl = SERVICE;
		mKey = apiKey;
		mWithElevation = false;
	}
	
	/** allows to request on an other site than GraphHopper demo site */
	public void setService(String serviceUrl){
		mServiceUrl = serviceUrl;
	}
	
	/** set if altitude of every route point should be requested or not. Default is false. */
	public void setElevation(boolean withElevation){
		mWithElevation = withElevation;
	}
	
	protected String getUrl(ArrayList<GeoPoint> waypoints){
		StringBuffer urlString = new StringBuffer(mServiceUrl);
		urlString.append("key="+mKey);
		for (int i=0; i<waypoints.size(); i++){
			GeoPoint p = waypoints.get(i);
			urlString.append("&point="+geoPointAsString(p));
		}
		//urlString.append("&instructions=true"); already set by default
		urlString.append("&elevation="+(mWithElevation?"true":"false"));
		urlString.append(mOptions);
		return urlString.toString();
	}

	@Override public Road getRoad(ArrayList<GeoPoint> waypoints) {
		String url = getUrl(waypoints);
		Log.d(BonusPackHelper.LOG_TAG, "GraphHopper.getRoad:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			return new Road(waypoints);
		}
		Road road = new Road();
		try {
			JSONObject jRoot = new JSONObject(jString);
			JSONArray jPaths = jRoot.optJSONArray("paths");
			if (jPaths == null || jPaths.length() == 0){
				road = new Road(waypoints);
				road.mStatus = STATUS_NO_ROUTE;
				return road;
			}
			JSONObject jFirstPath = jPaths.getJSONObject(0);
			String route_geometry = jFirstPath.getString("points");
			road.mRouteHigh = PolylineEncoder.decode(route_geometry, 10, mWithElevation);
			JSONArray jInstructions = jFirstPath.getJSONArray("instructions");
			int n = jInstructions.length();
			for (int i=0; i<n; i++){
				JSONObject jInstruction = jInstructions.getJSONObject(i);
				RoadNode node = new RoadNode();
				JSONArray jInterval = jInstruction.getJSONArray("interval");
				int positionIndex = jInterval.getInt(0);
				node.mLocation = road.mRouteHigh.get(positionIndex);
				node.mLength = jInstruction.getDouble("distance")/1000.0;
				node.mDuration = jInstruction.getInt("time")/1000.0; //Segment duration in seconds.
				int direction = jInstruction.getInt("sign");
				node.mManeuverType = getManeuverCode(direction);
				node.mInstructions = jInstruction.getString("text");
				road.mNodes.add(node);
			}
			road.mLength = jFirstPath.getDouble("distance")/1000.0;
			road.mDuration = jFirstPath.getInt("time")/1000.0;
			JSONArray jBBox = jFirstPath.getJSONArray("bbox");
			road.mBoundingBox = new BoundingBoxE6(jBBox.getDouble(3), jBBox.getDouble(2), 
					jBBox.getDouble(1), jBBox.getDouble(0));
			road.mStatus = Road.STATUS_OK;
		} catch (JSONException e) {
			road.mStatus = Road.STATUS_TECHNICAL_ISSUE;
			e.printStackTrace();
		}
		if (road.mStatus != Road.STATUS_OK){
			//Create default road:
			int status = road.mStatus;
			road = new Road(waypoints);
			road.mStatus = status;
		} else {
			road.buildLegs(waypoints);
		}
		Log.d(BonusPackHelper.LOG_TAG, "GraphHopper.getRoad - finished");
		return road;
	}

	@Override public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
		Road road = getRoad(waypoints);
		Road[] roads = new Road[1];
		roads[0] = road;
		return roads;
	}

	protected int getManeuverCode(int direction){
		Integer code = MANEUVERS.get(direction);
		if (code != null)
			return code;
		else 
			return 0;
	}

}

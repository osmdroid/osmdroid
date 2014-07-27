package org.osmdroid.bonuspack.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import android.util.Log;

/** get a route between a start and a destination point.
 * It uses GraphHopper, an open source routing service based on OpenSteetMap data. <br>
 * 
 * It requests by default the GraphHopper demo site. 
 * Use setService() to request an other (for instance your own) GraphHopper-compliant service. <br> 
 * 
 * @see <a href="https://github.com/graphhopper/web-api/blob/master/docs-routing.md">GraphHopper</a>
 * @author M.Kergall
 */
public class GraphHopperRoadManager extends RoadManager {

	static final String SERVICE = "http://graphhopper.com/api/1/route?";

	protected String mServiceUrl;
	protected String mUserAgent;
	protected String mKey;
	
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
		MANEUVERS.put(5, 24); //Arrived (at waypoint)
	}
	
	public GraphHopperRoadManager(String key){
		super();
		mServiceUrl = SERVICE;
		mKey = key;
		mUserAgent = BonusPackHelper.DEFAULT_USER_AGENT; //set user agent to the default one. 
	}
	
	/** allows to request on an other site than GraphHopper demo site */
	public void setService(String serviceUrl){
		mServiceUrl = serviceUrl;
	}

	/** allows to send to GraphHopper service a user agent specific to the app, 
	 * instead of the default user agent of OSMBonusPack lib. 
	 */
	public void setUserAgent(String userAgent){
		mUserAgent = userAgent;
	}
	
	protected String getUrl(ArrayList<GeoPoint> waypoints){
		StringBuffer urlString = new StringBuffer(mServiceUrl);
		for (int i=0; i<waypoints.size(); i++){
			GeoPoint p = waypoints.get(i);
			urlString.append("&point="+geoPointAsString(p));
		}
		//urlString.append("&instructions=true"); already by default
		urlString.append("&key="+mKey);

		//Locale locale = Locale.getDefault();
		//urlString.append("&locale="+locale.getLanguage());
		urlString.append(mOptions);
		return urlString.toString();
	}

	@Override public Road getRoad(ArrayList<GeoPoint> waypoints) {
		String url = getUrl(waypoints);
		Log.d(BonusPackHelper.LOG_TAG, "GraphHopper.getRoad:"+url);

		//String jString = BonusPackHelper.requestStringFromUrl(url);
		HttpConnection connection = new HttpConnection();
		connection.setUserAgent(mUserAgent);
		connection.doGet(url);
		String jString = connection.getContentAsString();
		connection.close();

		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "GraphHopper::getRoad: request failed.");
			return new Road(waypoints);
		}
		Road road = new Road();
		try {
			JSONObject jRoot = new JSONObject(jString);
			JSONArray jPaths = jRoot.getJSONArray("paths");
			if (jPaths.length() == 0){
				road.mStatus = Road.STATUS_TECHNICAL_ISSUE+1; //TODO - document
				return new Road(waypoints);
			}
			JSONObject jFirstPath = jPaths.getJSONObject(0);
			road.mStatus = Road.STATUS_OK; //TODO => info.errors
			String route_geometry = jFirstPath.getString("points");
			road.mRouteHigh = PolylineEncoder.decode(route_geometry, 1);
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
				node.mInstructions = jInstruction.getString("text");
				node.mManeuverType = getManeuverCode(direction);
				road.mNodes.add(node);
			}
			road.mLength = jFirstPath.getDouble("distance")/1000.0;
			road.mDuration = jFirstPath.getInt("time")/1000.0;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (road.mStatus != Road.STATUS_OK){
			//Create default road:
			int status = road.mStatus;
			road = new Road(waypoints);
			road.mStatus = status;
		} else {
			road.buildLegs(waypoints);
			road.mBoundingBox = BoundingBoxE6.fromGeoPoints(road.mRouteHigh); //TODO: use jFirstPath.get("bbox");
			road.mStatus = Road.STATUS_OK;
		}
		Log.d(BonusPackHelper.LOG_TAG, "GraphHopper.getRoad - finished");
		return road;
	}
	
	protected int getManeuverCode(int direction){
		Integer code = MANEUVERS.get(direction);
		if (code != null)
			return code;
		else 
			return 0;
	}

}

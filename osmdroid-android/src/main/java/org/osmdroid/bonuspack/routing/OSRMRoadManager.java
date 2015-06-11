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
		DIRECTIONS = new HashMap<String, Object>();
		HashMap<String, String> directions;
		
		directions = new HashMap<String, String>();
		DIRECTIONS.put("en", directions);
		directions.put("0", "Unknown instruction< on %s>");
		directions.put("1","Continue< on %s>");
		directions.put("2","Turn slight right< on %s>");
		directions.put("3","Turn right< on %s>");
		directions.put("4","Turn sharp right< on %s>");
		directions.put("5","U-Turn< on %s>");
		directions.put("6","Turn sharp left< on %s>");
		directions.put("7","Turn left< on %s>");
		directions.put("8","Turn slight left< on %s>");
		directions.put("9","You have reached a waypoint of your trip");
		directions.put("10","<Go on %s>");
		directions.put("11-1","Enter roundabout and leave at first exit< on %s>");
		directions.put("11-2","Enter roundabout and leave at second exit< on %s>");
		directions.put("11-3","Enter roundabout and leave at third exit< on %s>");
		directions.put("11-4","Enter roundabout and leave at fourth exit< on %s>");
		directions.put("11-5","Enter roundabout and leave at fifth exit< on %s>");
		directions.put("11-6","Enter roundabout and leave at sixth exit< on %s>");
		directions.put("11-7","Enter roundabout and leave at seventh exit< on %s>");
		directions.put("11-8","Enter roundabout and leave at eighth exit< on %s>");
		directions.put("11-9","Enter roundabout and leave at nineth exit< on %s>");
		directions.put("15","You have reached your destination");
		
		directions = new HashMap<String, String>();
		DIRECTIONS.put("fr", directions);
		directions.put("0", "Instruction inconnue< sur %s>");
		directions.put("1","Continuez< sur %s>");
		directions.put("2","Tournez légèrement à droite< sur %s>");
		directions.put("3","Tournez à droite< sur %s>");
		directions.put("4","Tournez fortement à droite< sur %s>");
		directions.put("5","Faites demi-tour< sur %s>");
		directions.put("6","Tournez fortement à gauche< sur %s>");
		directions.put("7","Tournez à gauche< sur %s>");
		directions.put("8","Tournez légèrement à gauche< sur %s>");
		directions.put("9","Vous êtes arrivé à une étape de votre voyage");
		directions.put("10","<Prenez %s>");
		directions.put("11-1","Au rond-point, prenez la première sortie< sur %s>");
		directions.put("11-2","Au rond-point, prenez la deuxième sortie< sur %s>");
		directions.put("11-3","Au rond-point, prenez la troisième sortie< sur %s>");
		directions.put("11-4","Au rond-point, prenez la quatrième sortie< sur %s>");
		directions.put("11-5","Au rond-point, prenez la cinquième sortie< sur %s>");
		directions.put("11-6","Au rond-point, prenez la sixième sortie< sur %s>");
		directions.put("11-7","Au rond-point, prenez la septième sortie< sur %s>");
		directions.put("11-8","Au rond-point, prenez la huitième sortie< sur %s>");
		directions.put("11-9","Au rond-point, prenez la neuvième sortie< sur %s>");
		directions.put("15","Vous êtes arrivé");
		
		directions = new HashMap<String, String>();
		DIRECTIONS.put("pl", directions);
		directions.put("0", "Nieznana instrukcja<w %s>");
		directions.put("1","Kontynuuj jazdę<na %s>");
		directions.put("2","Skręć lekko w prawo<w %s>");
		directions.put("3","Skręć w prawo<w %s>");
		directions.put("4","Skręć ostro w prawo<w %s>");
		directions.put("5","Zawróć<na %s>");
		directions.put("6","Skręć ostro w lewo<w %s>");
		directions.put("7","Skręć w lewo<w %s>");
		directions.put("8","Skręć lekko w lewo<w %s>");
		directions.put("9","Dotarłeś do punktu pośredniego");
		directions.put("10","<Jedź %s>");
		directions.put("11-1","Wjedź na rondo i opuść je pierwszym zjazdem<w %s>");
		directions.put("11-2","Wjedź na rondo i opuść je drugim zjazdem<w %s>");
		directions.put("11-3","Wjedź na rondo i opuść je trzecim zjazdem<w %s>");
		directions.put("11-4","Wjedź na rondo i opuść je czwartym zjazdem<w %s>");
		directions.put("11-5","Wjedź na rondo i opuść je piątym zjazdem<w %s>");
		directions.put("11-6","Wjedź na rondo i opuść je szóstym zjazdem<w %s>");
		directions.put("11-7","Wjedź na rondo i opuść je siódmym zjazdem<w %s>");
		directions.put("11-8","Wjedź na rondo i opuść je ósmym zjazdem<w %s>");
		directions.put("11-9","Wjedź na rondo i opuść je dziewiątym zjazdem<w %s>");
		directions.put("15","Dotarłeś do celu podróży");

		directions = new HashMap<String, String>();
        DIRECTIONS.put("de", directions);
        directions.put("0", "Unbekannte Instruktion< auf %s>");
        directions.put("1","Bleiben Sie< auf %s>");
        directions.put("2","Biegen Sie leicht rechts ab< auf %s>");
        directions.put("3","Biegen Sie rechts ab< auf %s>");
        directions.put("4","Biegen Sie scharf rechts ab< auf %s>");
        directions.put("5","Bitte wenden< auf %s>");
        directions.put("6","Biegen Sie scharf links ab< auf %s>");
        directions.put("7","Biegen Sie links ab< auf %s>");
        directions.put("8","Biegen Sie leicht links ab< auf %s>");
        directions.put("9","Sie haben einen Wegpunkt ihrer Reise erreicht"); 
        directions.put("10","<Begeben Sie sich auf %s>");  
        directions.put("11-1","Begeben Sie sich in den Kreisverkehr und nehmen die erste Ausfahrt< auf %s>");
        directions.put("11-2","Begeben Sie sich in den Kreisverkehr und nehmen die zweite Ausfahrt< auf %s>");
        directions.put("11-3","Begeben Sie sich in den Kreisverkehr und nehmen die dritte Ausfahrt< auf %s>");
        directions.put("11-4","Begeben Sie sich in den Kreisverkehr und nehmen die vierte Ausfahrt< auf %s>");
        directions.put("11-5","Begeben Sie sich in den Kreisverkehr und nehmen die fünfte Ausfahrt< auf %s>");
        directions.put("11-6","Begeben Sie sich in den Kreisverkehr und nehmen die sechste Ausfahrt< auf %s>");
        directions.put("11-7","Begeben Sie sich in den Kreisverkehr und nehmen die siebente Ausfahrt< auf %s>");
        directions.put("11-8","Begeben Sie sich in den Kreisverkehr und nehmen die achte Ausfahrt< auf %s>");
        directions.put("11-9","Begeben Sie sich in den Kreisverkehr und nehmen die neunte Ausfahrt< auf %s>");
        directions.put("15","Sie haben ihr Ziel erreicht");
	}
	
	public OSRMRoadManager(){
		super();
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
	
	protected String getUrl(ArrayList<GeoPoint> waypoints){
		StringBuffer urlString = new StringBuffer(mServiceUrl);
		for (int i=0; i<waypoints.size(); i++){
			GeoPoint p = waypoints.get(i);
			urlString.append("&loc="+geoPointAsString(p));
		}
		urlString.append("&instructions=true&alt=false");
		urlString.append(mOptions);
		return urlString.toString();
	}

	@Override public Road getRoad(ArrayList<GeoPoint> waypoints) {
		String url = getUrl(waypoints);
		Log.d(BonusPackHelper.LOG_TAG, "OSRMRoadManager.getRoad:"+url);

		//String jString = BonusPackHelper.requestStringFromUrl(url);
		HttpConnection connection = new HttpConnection();
		connection.setUserAgent(mUserAgent);
		connection.doGet(url);
		String jString = connection.getContentAsString();
		connection.close();

		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "OSRMRoadManager::getRoad: request failed.");
			return new Road(waypoints);
		}
		Locale l = Locale.getDefault();
		HashMap<String, String> directions = (HashMap<String, String>)DIRECTIONS.get(l.getLanguage());
		if (directions == null)
			directions = (HashMap<String, String>)DIRECTIONS.get("en");
		Road road = new Road();
		try {
			JSONObject jObject = new JSONObject(jString);
			road.mStatus = jObject.getInt("status");
			String route_geometry = jObject.getString("route_geometry");
			road.mRouteHigh = PolylineEncoder.decode(route_geometry, 1, false);
			JSONArray jInstructions = jObject.getJSONArray("route_instructions");
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
					node.mInstructions = buildInstructions(direction, roadName, directions);
					//Log.d(BonusPackHelper.LOG_TAG, direction+"=>"+node.mManeuverType+"; "+node.mInstructions);
					road.mNodes.add(node);
					lastNode = node;
				}
			}
			JSONObject jSummary = jObject.getJSONObject("route_summary");
			road.mLength = jSummary.getInt("total_distance")/1000.0;
			road.mDuration = jSummary.getInt("total_time");
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
			road.mBoundingBox = BoundingBoxE6.fromGeoPoints(road.mRouteHigh);
			road.mStatus = Road.STATUS_OK;
		}
		Log.d(BonusPackHelper.LOG_TAG, "OSRMRoadManager.getRoad - finished");
		return road;
	}
	
	protected int getManeuverCode(String direction){
		Integer code = MANEUVERS.get(direction);
		if (code != null)
			return code;
		else 
			return 0;
	}
	
	protected String buildInstructions(String direction, String roadName,
			HashMap<String, String> directions){
		if (directions == null)
			return null;
		direction = directions.get(direction);
		if (direction == null)
			return null;
		String instructions = null;
		if (roadName.equals(""))
			//remove "<*>"
			instructions = direction.replaceFirst("<[^>]*>", "");
		else {
			direction = direction.replace('<', ' ');
			direction = direction.replace('>', ' ');
			instructions = String.format(direction, roadName);
		}
		return instructions;
	}
}

package org.osmdroid.bonuspack.location;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlGeometry;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlMultiGeometry;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import android.util.Log;

/**
 * Access to Overpass API, a super-powerful search API on OpenStreetMap data. <br>
 * 
 * Two strategies are implemented: <br>
 * - Get result as POIs, as simplified content and geometry (one point), using {@link #getPOIsFromUrl(String)}<br>
 * - Get results with full content and geometry as KML, using {@link #addInKmlFolder(KmlFolder, String)}<br>
 * 
 * Helper methods are provided to build URLs for usual search requests. <br>
 * 
 * TODO Improve/revise the API => add an API targeting boundaries? <br>
 * 
 * @see <a href="http://wiki.openstreetmap.org/wiki/Overpass_API">Overpass API Reference</a>
 * @author M.Kergall
 */
public class OverpassAPIProvider {

	public static final String OVERPASS_API_DE_SERVICE = "http://overpass-api.de/api/interpreter";
	public static final String OVERPASS_API_SERVICE = "http://api.openstreetmap.fr/oapi/interpreter";
	protected String mService;
	
	public OverpassAPIProvider(){
		setService(OVERPASS_API_DE_SERVICE); //good default, as it seems fast and reliable. 
	}
	
	/**
	 * Allows to change the OverPass API service
	 * @param serviceUrl
	 */
	public void setService(String serviceUrl){
		mService = serviceUrl;
	}
	
	/**
	 * Build the URL to search for elements having a specific OSM Tag (key=value), within a bounding box. 
	 * Elements will be OSM nodes, ways and relations. Ways and relations will have no geometry, only their center. <br>
	 * Usage: urlForPOISearch("amenity=cinema", map.getBoundingBox(), 200, 30);<br>
	 * @param tag OpenStreetMap tag to search. Can be either "key=value", or "key". 
	 * @param bb bounding box
	 * @param limit max number of results. 
	 * @param timeout in seconds
	 * @return the url for this request. 
	 * @see <a href="http://wiki.openstreetmap.org/wiki/Tags">OSM Tags</a>
	 */
	public String urlForPOISearch(String tag, BoundingBoxE6 bb, int limit, int timeout){
		StringBuffer s = new StringBuffer();
		s.append(mService+"?data=");
		String sBB = "("+bb.getLatSouthE6()*1E-6+","+bb.getLonWestE6()*1E-6+","+bb.getLatNorthE6()*1E-6+","+bb.getLonEastE6()*1E-6+")";
		String data = 
			"[out:json][timeout:"+timeout+"];("
			+ "node["+tag+"]"+sBB+";"
			+ "way["+tag+"]"+sBB+";"
			+ "relation["+tag+"]"+sBB+";"
			+ ");out qt center "+ limit + " tags;";
		Log.d(BonusPackHelper.LOG_TAG, "data="+data);
		s.append(URLEncoder.encode(data));
		return s.toString();
	}
	
	protected GeoPoint geoPointFromJson(JsonObject jLatLon){
		double lat = jLatLon.get("lat").getAsDouble();
		double lon = jLatLon.get("lon").getAsDouble();
		GeoPoint p = new GeoPoint(lat, lon);
		return p;
	}
	
	protected String tagValueFromJson(String key, JsonObject jTags){
		JsonElement jTag = jTags.get(key);
		if (jTag == null)
			return null;
		else
			return jTag.getAsString();
	}

	protected String tagValueFromJsonNotNull(String key, JsonObject jTags){
		String v = tagValueFromJson(key, jTags);
		return (v != null ? ","+v : "");
	}
	
	/** 
	 * Search for POI. 
	 * @param url full URL request, built with #urlForPOISearch or equivalent. 
	 * Main requirements: <br>
	 * - Content must be in JSON format<br>
	 * - ways and relations must contain the "center" element. <br>
	 * @return elements as a list of POI
	 */
	public ArrayList<POI> getPOIsFromUrl(String url){
		Log.d(BonusPackHelper.LOG_TAG, "OverpassAPIProvider:getPOIsFromUrl:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: request failed.");
			return null;
		}
		try {
			//parse JSON and build POIs
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(jString);
			JsonObject jResult = json.getAsJsonObject();
			JsonArray jElements = jResult.get("elements").getAsJsonArray();
			ArrayList<POI> pois = new ArrayList<POI>(jElements.size());
			for (JsonElement j:jElements){
				JsonObject jo = j.getAsJsonObject();
				POI poi = new POI(POI.POI_SERVICE_OVERPASS_API);
				poi.mId = jo.get("id").getAsLong();
				poi.mCategory = jo.get("type").getAsString();
				if (jo.has("tags")){
					JsonObject jTags = jo.get("tags").getAsJsonObject();
					poi.mType = tagValueFromJson("name", jTags);
					//Try to set a relevant POI type by searching for an OSM commonly used tag key, and getting its value:
					poi.mDescription = tagValueFromJsonNotNull("amenity", jTags)
							+ tagValueFromJsonNotNull("boundary", jTags) 
							+ tagValueFromJsonNotNull("building", jTags) 
							+ tagValueFromJsonNotNull("craft", jTags) 
							+ tagValueFromJsonNotNull("emergency", jTags) 
							+ tagValueFromJsonNotNull("highway", jTags) 
							+ tagValueFromJsonNotNull("historic", jTags) 
							+ tagValueFromJsonNotNull("landuse", jTags) 
							+ tagValueFromJsonNotNull("leisure", jTags) 
							+ tagValueFromJsonNotNull("natural", jTags) 
							+ tagValueFromJsonNotNull("shop", jTags) 
							+ tagValueFromJsonNotNull("sport", jTags) 
							+ tagValueFromJsonNotNull("tourism", jTags);
					//remove first "," (quite ugly, I know)
					if (poi.mDescription.length()>0)
						poi.mDescription = poi.mDescription.substring(1);
					//TODO: try to set a relevant thumbnail image, according to key/value tags. 
					//We could try to replicate Nominatim/lib/lib.php/getClassTypes(), but it sounds crazy for the added value. 
					poi.mUrl = tagValueFromJson("website", jTags);
					if (poi.mUrl != null){
						//normalize the url (often needed):
						if (!poi.mUrl.startsWith("http://") && !poi.mUrl.startsWith("https://"))
							poi.mUrl = "http://" + poi.mUrl;
					}
				}
				if ("node".equals(poi.mCategory)){
					poi.mLocation = geoPointFromJson(jo);
				} else {
					if (jo.has("center")){
						JsonObject jCenter = jo.get("center").getAsJsonObject();
						poi.mLocation = geoPointFromJson(jCenter);
					}
				}
				if (poi.mLocation != null)
					pois.add(poi);
			}
			return pois;
		} catch (JsonSyntaxException e) {
			Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: parsing error.");
			return null;
		}
	}
	
	/**
	 * Build the URL to search for elements having a specific OSM Tag (key=value), within a bounding box. 
	 * Similar to {@link #urlForPOISearch}, but here the request is built to retrieve the full geometry. 
	 * @param tag
	 * @param bb bounding box
	 * @param limit max number of results. 
	 * @param timeout in seconds
	 * @return the url for this request. 
	 */
	public String urlForTagSearchKml(String tag, BoundingBoxE6 bb, int limit, int timeout){
		StringBuffer s = new StringBuffer();
		s.append(mService+"?data=");
		String sBB = "("+bb.getLatSouthE6()*1E-6+","+bb.getLonWestE6()*1E-6+","+bb.getLatNorthE6()*1E-6+","+bb.getLonEastE6()*1E-6+")";
		String data = 
			"[out:json][timeout:"+timeout+"];"
			+ "(node["+tag+"]"+sBB+";"
			+ "way["+tag+"]"+sBB+";);"
			+ "out qt geom tags "+ limit + ";"
			+ "relation["+tag+"]"+sBB+";out qt geom body "+ limit+";"; //relation isolated to get geometry with body option
		//TODO: see issue https://github.com/drolbr/Overpass-API/issues/134#issuecomment-58847362
		//When solved, simplify. 
		Log.d(BonusPackHelper.LOG_TAG, "data="+data);
		s.append(URLEncoder.encode(data));
		return s.toString();
	}
	
	/**
	 * Attempt to detect if a way is an area. 
	 * Assume that a closed way is an area, without handling very specific OSM exceptions. 
	 */
	protected boolean isAnArea(ArrayList<GeoPoint> coords){
		return (coords!=null) && (coords.size()>=3) && (coords.get(0).equals(coords.get(coords.size()-1)));
	}
	
	protected ArrayList<GeoPoint> parseGeometry(JsonObject jo){
		JsonArray jGeometry = jo.get("geometry").getAsJsonArray();
		ArrayList<GeoPoint> coords = new ArrayList<GeoPoint>(jGeometry.size());
		for (JsonElement j:jGeometry){
			JsonObject jLatLon = j.getAsJsonObject();
			GeoPoint p = geoPointFromJson(jLatLon);
			coords.add(p);
		}
		return coords;
	}
	
	protected KmlMultiGeometry buildMultiGeometry(JsonArray jMembers){
		KmlMultiGeometry geometry = new KmlMultiGeometry();
		for (JsonElement j:jMembers){
			JsonObject jMember = j.getAsJsonObject();
			KmlGeometry item = buildGeometry(jMember);
			geometry.addItem(item);
		}
		return geometry;
	}
	
	protected KmlGeometry buildGeometry(JsonObject jo){
		KmlGeometry geometry = null;
		String type = jo.get("type").getAsString();
		if ("node".equals(type)){
			geometry = new KmlPoint(geoPointFromJson(jo));
		} else if ("way".equals(type)){
			ArrayList<GeoPoint> coords = parseGeometry(jo);
			if (isAnArea(coords)){
				geometry = new KmlPolygon();
				geometry.mCoordinates = coords;
			} else {
				geometry = new KmlLineString();
				geometry.mCoordinates = coords;
			}
		} else { //relation:
			JsonArray jMembers = jo.get("members").getAsJsonArray();
			geometry = buildMultiGeometry(jMembers);
		}
		return geometry;
	}
	
	/**
	 * Retrieve elements from url, and add them in a KML Folder, as KML Placemarks: Point, LineString, Polygon, or MultiGeometry. 
	 * @param kmlFolder KML folder in which elements will be added
	 * @param url OverPass API url to retrieve elements. 
	 * Main requirements:<br>
	 * - Content must be in JSON format<br>
	 * - ways and relations must have the "geometry" element<br>
	 * @return true if ok, false if technical error. 
	 */
	public boolean addInKmlFolder(KmlFolder kmlFolder, String url){
		Log.d(BonusPackHelper.LOG_TAG, "OverpassAPIProvider:addInKmlFolder:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: request failed.");
			return false;
		}
		try {
			//parse JSON and build KML
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(jString);
			JsonObject jResult = json.getAsJsonObject();
			JsonArray jElements = jResult.get("elements").getAsJsonArray();
			for (JsonElement j:jElements){
				JsonObject jo = j.getAsJsonObject();
				KmlPlacemark placemark = new KmlPlacemark();
				placemark.mGeometry = buildGeometry(jo);
				placemark.mId = jo.get("id").getAsString();
				//Tags:
				if (jo.has("tags")){
					JsonObject jTags = jo.get("tags").getAsJsonObject();
					if (jTags.has("name"))
						placemark.mName = jTags.get("name").getAsString();
					//copy all tags as KML Extended Data:
					Set<Map.Entry<String,JsonElement>> entrySet = jTags.entrySet();
					for (Map.Entry<String,JsonElement> entry:entrySet){
						String key = entry.getKey();
						String value = entry.getValue().getAsString();
						placemark.setExtendedData(key, value);
					}
				}
				kmlFolder.add(placemark);
			}
			return true;
		} catch (JsonSyntaxException e) {
			Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: parsing error.");
			return false;
		}
	}

}

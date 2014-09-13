package org.osmdroid.bonuspack.location;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
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
 * Access to Overpass API, a powerful search API on OpenStreetMap data. 
 * @see <a href="http://wiki.openstreetmap.org/wiki/Overpass_API">Overpass API Reference</a>
 * 
 * @author M.Kergall
 */
public class OverpassAPIProvider {

	public static final String OVERPASS_API_DE_SERVICE = "http://overpass-api.de/api/interpreter";
	public static final String OVERPASS_API_SERVICE = "http://api.openstreetmap.fr/oapi/interpreter";
	protected String mService;
	
	public OverpassAPIProvider(){
		setService(OVERPASS_API_DE_SERVICE); //seems fast and reliable. 
	}
	
	/**
	 * Allows to change the OverPass API service
	 * @param serviceUrl
	 */
	public void setService(String serviceUrl){
		mService = serviceUrl;
	}
	
	/**
	 * Build the url to search for an amenity within a bounding box. 
	 * @param amenity OpenStreetMap amenity value
	 * @param bb bounding box
	 * @param limit max number of results - warning, this is not reliable. 
	 * @param timeout in seconds
	 * @return the url
	 */
	public String urlForAmenitySearch(String amenity, BoundingBoxE6 bb, int limit, int timeout){
		StringBuffer s = new StringBuffer();
		s.append(mService+"?data=");
		String sBB = "("+bb.getLatSouthE6()*1E-6+","+bb.getLonWestE6()*1E-6+","+bb.getLatNorthE6()*1E-6+","+bb.getLonEastE6()*1E-6+")";
		String data = 
			"[out:json][timeout:"+timeout+"];("
			+ "node[\"amenity\"=\""+amenity+"\"]"+sBB+";"
			+ "way[\"amenity\"=\""+amenity+"\"]"+sBB+";"
			+ "relation[\"amenity\"=\""+amenity+"\"]"+sBB+";"
			+ ");out "+ limit + " body;>;out qt;";
		s.append(URLEncoder.encode(data));
		return s.toString();
	}
	
	public class Element {
		public static final int TYPE_NODE=0, TYPE_WAY=1, TYPE_RELATION=2;
		public int mType;
		public long mId;
		public GeoPoint mPosition; //if node
		protected long[] mNodes; //if way: related nodes
		public ArrayList<GeoPoint> mCoords; //if way: polyline coordinates
		public HashMap<String, String> mTags;
		protected boolean mToRemove;
		
		protected Element(JsonObject jo){
			mId = jo.get("id").getAsLong();
			String type = jo.get("type").getAsString();
			if ("node".equals(type)){
				mType = TYPE_NODE;
				double lat = jo.get("lat").getAsDouble();
				double lon = jo.get("lon").getAsDouble();
				mPosition = new GeoPoint(lat, lon);
			} else if ("way".equals(type) ){
				mType = TYPE_WAY;
				if (jo.has("nodes")){
					JsonArray jNodes = jo.get("nodes").getAsJsonArray();
					int n = jNodes.size();
					mNodes = new long[n];
					for (int i=0; i<n; i++){
						mNodes[i] = jNodes.get(i).getAsLong();
					}
				}
			} else if ("relation".equals(type) ){
				mType = TYPE_RELATION;
			}
			if (jo.has("tags")){
				//Parse JSON tags to build element tags:
				JsonObject jTags = jo.get("tags").getAsJsonObject();
				Set<Map.Entry<String,JsonElement>> entrySet = jTags.entrySet();
				mTags = new HashMap<String, String>(entrySet.size());
				for (Map.Entry<String,JsonElement> entry:entrySet){
					String key = entry.getKey();
					String value = entry.getValue().getAsString();
					if (key!=null && value!=null)
						mTags.put(key, value);
				}
			}
			//mToRemove = false; - done by default. 
		}
	}
	
	protected void removeRemovableElements(HashMap<Long, Element> elements){
		Iterator<Entry<Long, Element>> it = elements.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, Element> pair = (Map.Entry<Long, Element>)it.next();
			Element e = pair.getValue();
			if (e.mToRemove)
				it.remove();
		}		
	}
	
	protected void fulfillWaysWithCoords(HashMap<Long, Element> elements){
		Iterator<Entry<Long, Element>> it = elements.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, Element> pair = (Map.Entry<Long, Element>)it.next();
			Element e = pair.getValue();
			if (e.mType == Element.TYPE_WAY){
				e.mCoords = new ArrayList<GeoPoint>(e.mNodes.length);
				for (int i=0; i<e.mNodes.length; i++){
					long relatedNodeId = e.mNodes[i];
					Element relatedNode = elements.get(relatedNodeId);
					if (relatedNode != null)
						e.mCoords.add(relatedNode.mPosition);
					//mark for removal the related node, as it should be present only for the geometry of its way:
					relatedNode.mToRemove = true;
				}
			}
		}
	}
	
	/**
	 * @param url full URL request
	 * @return the Elements, of null if technical issue. 
	 */
	public HashMap<Long, Element> getThemAsJson(String url){
		Log.d(BonusPackHelper.LOG_TAG, "OverpassAPIProvider:get:"+url);
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: request failed.");
			return null;
		}
		try {
			//parse and build elements
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(jString);
			JsonObject jResult = json.getAsJsonObject();
			JsonArray jElements = jResult.get("elements").getAsJsonArray();
			HashMap<Long, Element> elements = new HashMap<Long, Element>(jElements.size());
			for (JsonElement j:jElements){
				JsonObject jo = j.getAsJsonObject();
				Element e = new Element(jo);
				elements.put(e.mId, e);
			}
			
			//fulfill Elements ways with their nodes coords:
			fulfillWaysWithCoords(elements);
			
			//remove elements marked for removal:
			removeRemovableElements(elements);
			
			return elements;
		} catch (JsonSyntaxException e) {
			Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: parsing error.");
			return null;
		}
	}
	
	/**
	 * Add the elements in kmlFolder, as KML Placemarks (Point or LineString)
	 * @param kmlFolder
	 * @param elements
	 */
	public void addInKmlFolder(KmlFolder kmlFolder, HashMap<Long, Element> elements){
		Iterator<Entry<Long, Element>> it = elements.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, Element> pair = (Map.Entry<Long, Element>)it.next();
			Element e = pair.getValue();
			KmlPlacemark placemark;
			if (e.mType == Element.TYPE_NODE){
				placemark = new KmlPlacemark(e.mPosition);
			} else /*if (e.mType == Element.TYPE_WAY) */{
				placemark = new KmlPlacemark();
				placemark.mGeometry = new KmlLineString();
				placemark.mGeometry.mCoordinates = e.mCoords;
			}
			placemark.mName = e.mTags.get("name");
			//TODO: copy tags to KML properties
			kmlFolder.add(placemark);
		}
	}

	/**
	 * @param elements
	 * @return elements as an ArrayList of POI
	 */
	public ArrayList<POI> asPOIs(HashMap<Long, Element> elements){
		Iterator<Entry<Long, Element>> it = elements.entrySet().iterator();
		ArrayList<POI> pois = new ArrayList<POI>(elements.entrySet().size());
		while (it.hasNext()) {
			Map.Entry<Long, Element> pair = (Map.Entry<Long, Element>)it.next();
			Element e = pair.getValue();
			POI poi = new POI(POI.POI_SERVICE_OVERPASS_API);
			poi.mId = e.mId;
			poi.mCategory = (e.mType==Element.TYPE_NODE?"node":"way");
			poi.mType = e.mTags.get("amenity");
			poi.mDescription = e.mTags.get("name");
			if (e.mType == Element.TYPE_NODE){
				poi.mLocation = e.mPosition;
			} else /*if (e.mType == Element.TYPE_WAY) */{
				BoundingBoxE6 bb = BoundingBoxE6.fromGeoPoints(e.mCoords);
				poi.mLocation = bb.getCenter();
			}
			pois.add(poi);
		}
		return pois;
	}
}

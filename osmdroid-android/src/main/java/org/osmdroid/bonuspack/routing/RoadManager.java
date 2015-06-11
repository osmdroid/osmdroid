package org.osmdroid.bonuspack.routing;

import java.util.ArrayList;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import android.content.Context;

/**
 * Generic class to get a route between a start and a destination point, 
 * going through a list of waypoints. 
 * @see MapQuestRoadManager
 * @see GoogleRoadManager
 * @see OSRMRoadManager
 * 
 * @author M.Kergall
 */
public abstract class RoadManager {
	
	protected String mOptions;
	
	public abstract Road getRoad(ArrayList<GeoPoint> waypoints);
	
	public RoadManager(){
		mOptions = "";
	}
	
	/**
	 * Add an option that will be used in the route request. 
	 * Note that some options are set in the request in all cases. 
	 * @param requestOption see provider documentation. 
	 * Just one example: "routeType=bicycle" for MapQuest; "mode=bicycling" for Google. 
	 */
	public void addRequestOption(String requestOption){
		mOptions += "&" + requestOption;
	}
	
	/**
	 * @return the GeoPoint as a string, properly formatted: lat,lon
	 */
	protected String geoPointAsString(GeoPoint p){
		StringBuffer result = new StringBuffer();
		double d = p.getLatitude();
		result.append(Double.toString(d));
		d = p.getLongitude();
		result.append("," + Double.toString(d));
		return result.toString();
	}
	
	/**
	 * Using the road high definition shape, builds and returns a Polyline. 
	 * @param road
	 * @param color
	 * @param width
	 * @param context
	 */
	public static Polyline buildRoadOverlay(Road road, int color, float width, Context context){
		Polyline roadOverlay = new Polyline(context);
		roadOverlay.setColor(color);
		roadOverlay.setWidth(width);
		if (road != null) {
			ArrayList<GeoPoint> polyline = road.mRouteHigh;
			roadOverlay.setPoints(polyline);
		}
		return roadOverlay;
	}
	
	/**
	 * Builds an overlay for the road shape with a default (and nice!) style. 
	 * @return route shape overlay
	 */
	public static Polyline buildRoadOverlay(Road road, Context context){
		return buildRoadOverlay(road, 0x800000FF, 5.0f, context);
	}

}

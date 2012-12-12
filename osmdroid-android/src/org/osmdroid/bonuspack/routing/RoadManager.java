package org.osmdroid.bonuspack.routing;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;
import android.graphics.Paint;

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
		double d = p.getLatitudeE6()*1E-6;
		result.append(Double.toString(d));
		d = p.getLongitudeE6()*1E-6;
		result.append("," + Double.toString(d));
		return result.toString();
	}
	
	/**
	 * Using the road high definition shape, builds and returns a PathOverlay using the Paint. 
	 * @param road
	 * @param paint
	 * @param context
	 */
	public static PathOverlay buildRoadOverlay(Road road, Paint paint, Context context){
		PathOverlay roadOverlay = new PathOverlay(0, context);
		roadOverlay.setPaint(paint);
		if (road != null) {
			ArrayList<GeoPoint> polyline = road.mRouteHigh;
			for (GeoPoint p:polyline){
				roadOverlay.addPoint(p);
			}
		}
		return roadOverlay;
	}
	
	/**
	 * Builds an overlay for the road shape with a default (and nice!) color. 
	 * @return route shape overlay
	 */
	public static PathOverlay buildRoadOverlay(Road road, Context context){
		Paint paint = new Paint();
		paint.setColor(0x800000FF);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		return buildRoadOverlay(road, paint, context);
	}

}

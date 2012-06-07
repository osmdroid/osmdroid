package org.osmdroid.bonuspack;

import java.util.ArrayList;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.util.Log;

/** Road Link is a portion of road between 2 "nodes" or intersections */
class RoadLink {
	public double mSpeed; /** in km/h */
	public double mLength; /** in km */
	public double mDuration; /** in sec */
	public int mShapeIndex; /** starting point of the link, as index in initial polyline */
}


/** describes the way to go from a position to an other. 
 * Normally returned by a call to a Directions API (from MapQuest, GoogleMaps or other)
 * @see MapQuestRoadManager, GoogleRoadManager, OSRMRoadManager
 * 
 * @author M.Kergall
 */
public class Road {
	public double mLength; /** length of the whole route in km. */
	public double mDuration; /** duration of the whole trip in sec. */
	
	public ArrayList<RoadLink> mLinks; /** */
	public ArrayList<RoadNode> mNodes; /** */
	public ArrayList<RoadLeg> mLegs; /** there is one leg between each waypoint */
	
	public ArrayList<GeoPoint> mRouteHigh; /** full shape: polyline, as an array of GeoPoints */
	private ArrayList<GeoPoint> mRouteLow; /** the same, in low resolution (less points) */
	public BoundingBoxE6 mBoundingBox; /** road bounding box */
	
	private void init(){
		mLength = 0.0;
		mDuration = 0.0;
		mLinks = new ArrayList<RoadLink>();
		mNodes = new ArrayList<RoadNode>();
		mRouteHigh = new ArrayList<GeoPoint>();
		mRouteLow = null;
		mLegs = new ArrayList<RoadLeg>();
		mBoundingBox = null;
	}
	
	public Road(){
		init();
	}
	
	/** default constructor when normal loading failed */
	public Road(ArrayList<GeoPoint> waypoints){
		init();
		int n = waypoints.size();
		for (int i=0; i<n; i++){
			GeoPoint p = waypoints.get(i);
			mRouteHigh.add(p);
			RoadLink link = new RoadLink();
			link.mShapeIndex = i;
			mLinks.add(link);
		}
		for (int i=0; i<n-1; i++){
			RoadLeg leg = new RoadLeg(i, i+1, mLinks);
			mLegs.add(leg);
		}
		mBoundingBox = BoundingBoxE6.fromGeoPoints(mRouteHigh);
	}
	
	protected double distanceLLSquared(GeoPoint p1, GeoPoint p2){
		double deltaLat = p2.getLatitudeE6()-p1.getLatitudeE6();
		double deltaLon = p2.getLongitudeE6()-p1.getLongitudeE6();
		return (deltaLat*deltaLat + deltaLon*deltaLon);
	}
	
	/**
	 * When the service provider (like MapQuest) do not provide legs information, 
	 * we have to rebuild it, using the waypoints and the road links. 
	 * @param waypoints
	 */
	public void buildLegs(ArrayList<GeoPoint> waypoints){
		int firstLinkIndex = 0;
		//For all intermediate waypoints, search the link node closest to the waypoint
		int n = waypoints.size();
		for (int i=1; i<n-1; i++){
			GeoPoint waypoint = waypoints.get(i);
			double distanceMin = -1.0;
			int linkIndexMin = -1;
			for (int l=firstLinkIndex; l<mLinks.size(); l++){
				int shapeIndex = mLinks.get(l).mShapeIndex;
				GeoPoint roadPoint = mRouteHigh.get(shapeIndex);
				double dSquared = distanceLLSquared(roadPoint, waypoint);
				if (linkIndexMin == -1 || dSquared < distanceMin){
					distanceMin = dSquared;
					linkIndexMin = l;
				}
			}
			//Build the leg as ending with this closest link:
			RoadLeg s = new RoadLeg(firstLinkIndex, linkIndexMin, mLinks);
			mLegs.add(s);
			firstLinkIndex = linkIndexMin+1; //restart next leg from end
		}
		//Build last leg ending with last link:
		RoadLeg lastLeg = new RoadLeg(firstLinkIndex, mLinks.size()-1, mLinks);
		mLegs.add(lastLeg);
	}
	
	public ArrayList<GeoPoint> getRouteLow(){
		if (mRouteLow == null){
			//Simplify the route (divide number of points by around 10):
			Log.d(BonusPackHelper.LOG_TAG, "initial road size:"+mRouteHigh.size());
			mRouteLow = DouglasPeuckerReducer.reduceWithTolerance(mRouteHigh, 600.0);
			Log.d(BonusPackHelper.LOG_TAG, "road size after reduction:"+mRouteLow.size());
		}
		return mRouteLow;
	}
	
	public void setRouteLow(ArrayList<GeoPoint> route){
		mRouteLow = route;
	}
	
	/**
	 * TODO: For MapQuest only - should be moved in MapQuestRoadManager
	 */
	public void finalizeNodes(){
		int n = mNodes.size();
		if (n == 0)
			return;
		ArrayList<RoadNode> newNodes = new ArrayList<RoadNode>(n);
		RoadNode lastNode = null;
		for (int i=0; i<n-1; i++){
			RoadNode node = mNodes.get(i);
			RoadLink link = mLinks.get(node.mNextRoadLink);
			if (lastNode!=null && (node.mInstructions == null || node.mManeuverType == 0)){
				//this node is irrelevant, don't keep it, 
				//but update values of last node:
				lastNode.mLength += link.mLength;
				lastNode.mDuration += (node.mDuration + link.mDuration);
			} else {
				node.mLength = link.mLength;
				node.mDuration += link.mDuration;
				int locationIndex = link.mShapeIndex;
				node.mLocation = mRouteHigh.get(locationIndex);
				newNodes.add(node);
				lastNode = node;
			}
		}
		//switch to the new array of nodes:
		mNodes = newNodes;
	}
	
	public String getLengthDurationText(double length, double duration){
		String result;
		if (length >= 100.0){
			result = (int)(length) + " km, ";
		} else if (length >= 1.0){
			result = Math.round(length*10)/10.0 + " km, ";
		} else {
			result = (int)(length*1000) + " m, ";
		}
		int totalSeconds = (int)duration;
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds / 60) - (hours*60);
		int seconds = (totalSeconds % 60);
		if (hours != 0){
			result += hours + " h ";
		}
		if (minutes != 0){
			result += minutes + " min";
		}
		if (hours == 0 && minutes == 0){
			result += seconds + " s";
		}
		return result;
	}
	
	/**
	 * @return length and duration of the whole road, or of a leg of the road,
	 * as a String, in a readable format. 
	 * @param leg leg index, starting from 0. -1 for the whole road
	 */
	public String getLengthDurationText(int leg){
		double length = (leg == -1 ? mLength : mLegs.get(leg).mLength);
		double duration = (leg == -1 ? mDuration : mLegs.get(leg).mDuration);
		return getLengthDurationText(length, duration);
	}
	
}

package org.osmdroid.bonuspack.routing;

import java.util.ArrayList;

import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.DouglasPeuckerReducer;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


/** describes the way to go from a position to an other. 
 * Normally returned by a call to a Directions API (from MapQuest, GoogleMaps, OSRM or other)
 * @see MapQuestRoadManager
 * @see GoogleRoadManager
 * @see OSRMRoadManager
 * 
 * @author M.Kergall
 */
public class Road  implements Parcelable {
	/** 
	 * STATUS_OK = road properly retrieved and built. 
	 * STATUS_INVALID = road has not been built yet. 
	 * STATUS_TECHNICAL_ISSUE = technical issue, no answer from the service provider. 
	 * All other values: functional errors/issues, depending on the service provider. 
	 * */
	public int mStatus;

	/** length of the whole route in km. */
	public double mLength; 
	/** duration of the whole trip in sec. */
	public double mDuration;
	/** list of intersections or "nodes" */
	public ArrayList<RoadNode> mNodes;
	/** there is one leg between each waypoint */
	public ArrayList<RoadLeg> mLegs; 
	/** full shape: polyline, as an array of GeoPoints */
	public ArrayList<GeoPoint> mRouteHigh; 
	/** the same, in low resolution (less points) */
	private ArrayList<GeoPoint> mRouteLow; 
	/** road bounding box */
	public BoundingBoxE6 mBoundingBox; 
	
	public static final int STATUS_INVALID=-1;
	public static final int STATUS_OK=0;
	public static final int STATUS_TECHNICAL_ISSUE=2;
	
	private void init(){
		mStatus = STATUS_INVALID;
		mLength = 0.0;
		mDuration = 0.0;
		mNodes = new ArrayList<RoadNode>();
		mRouteHigh = new ArrayList<GeoPoint>();
		mRouteLow = null;
		mLegs = new ArrayList<RoadLeg>();
		mBoundingBox = null;
	}
	
	public Road(){
		init();
	}
	
	/** default constructor when normal loading failed: 
	 * the road shape only contains the waypoints; All distances and times are at 0;
	 * there is no node; mStatus set to TECHNICAL_ISSUE. 
	 */
	public Road(ArrayList<GeoPoint> waypoints){
		init();
		int n = waypoints.size();
		for (int i=0; i<n; i++){
			GeoPoint p = waypoints.get(i);
			mRouteHigh.add(p);
		}
		for (int i=0; i<n-1; i++){
			RoadLeg leg = new RoadLeg(/*i, i+1, mLinks*/);
			mLegs.add(leg);
		}
		mBoundingBox = BoundingBoxE6.fromGeoPoints(mRouteHigh);
		mStatus = STATUS_TECHNICAL_ISSUE;
	}
	
	/**
	 * @return the road shape in "low resolution" = simplified by around 10 factor. 
	 */
	public ArrayList<GeoPoint> getRouteLow(){
		if (mRouteLow == null){
			//Simplify the route (divide number of points by around 10):
			int n = mRouteHigh.size();
			mRouteLow = DouglasPeuckerReducer.reduceWithTolerance(mRouteHigh, 1500.0);
			Log.d(BonusPackHelper.LOG_TAG, "Road reduced from "+n+" to "+mRouteLow.size()+ " points");
		}
		return mRouteLow;
	}
	
	public void setRouteLow(ArrayList<GeoPoint> route){
		mRouteLow = route;
	}
	
	/**
	 * @param length in km
	 * @param duration in sec
	 * @return a human-readable length&duration text. 
	 */
	public static String getLengthDurationText(double length, double duration){
		String result;
		if (length >= 100.0){
			result = (int)(length) + "km, ";
		} else if (length >= 1.0){
			result = Math.round(length*10)/10.0 + "km, ";
		} else {
			result = (int)(length*1000) + "m, ";
		}
		int totalSeconds = (int)duration;
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds / 60) - (hours*60);
		int seconds = (totalSeconds % 60);
		if (hours != 0){
			result += hours + "h ";
		}
		if (minutes != 0){
			result += minutes + "min ";
		}
		if (hours == 0 && minutes == 0){
			result += seconds + "sec";
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
	
	protected double distanceLLSquared(GeoPoint p1, GeoPoint p2){
		double deltaLat = p2.getLatitudeE6()-p1.getLatitudeE6();
		double deltaLon = p2.getLongitudeE6()-p1.getLongitudeE6();
		return (deltaLat*deltaLat + deltaLon*deltaLon);
	}
	
	/**
	 * As MapQuest and OSRM doesn't provide legs information, 
	 * we have to rebuild it, using the waypoints and the road nodes. <br>
	 * Note that MapQuest legs fit well with waypoints, as there is a "dedicated" node for each waypoint. 
	 * But OSRM legs are not precise, as there is no node "dedicated" to waypoints. 
	 */
	public void buildLegs(ArrayList<GeoPoint> waypoints){
		mLegs = new ArrayList<RoadLeg>();
		int firstNodeIndex = 0;
		//For all intermediate waypoints, search the node closest to the waypoint
		int w = waypoints.size();
		int n = mNodes.size();
		for (int i=1; i<w-1; i++){
			GeoPoint waypoint = waypoints.get(i);
			double distanceMin = -1.0;
			int nodeIndexMin = -1;
			for (int j=firstNodeIndex; j<n; j++){
				GeoPoint roadPoint = mNodes.get(j).mLocation;
				double dSquared = distanceLLSquared(roadPoint, waypoint);
				if (nodeIndexMin == -1 || dSquared < distanceMin){
					distanceMin = dSquared;
					nodeIndexMin = j;
				}
			}
			//Build the leg as ending with this closest node:
			RoadLeg leg = new RoadLeg(firstNodeIndex, nodeIndexMin, mNodes);
			mLegs.add(leg);
			firstNodeIndex = nodeIndexMin+1; //restart next leg from end
		}
		//Build last leg ending with last node:
		RoadLeg lastLeg = new RoadLeg(firstNodeIndex, n-1, mNodes);
		mLegs.add(lastLeg);
	}

	//--- Parcelable implementation
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mStatus);
		out.writeDouble(mLength);
		out.writeDouble(mDuration);
		out.writeList(mNodes);
		out.writeList(mLegs);
		out.writeList(mRouteHigh);
		out.writeParcelable(mBoundingBox, 0);
	}
	
	public static final Parcelable.Creator<Road> CREATOR = new Parcelable.Creator<Road>() {
		@Override public Road createFromParcel(Parcel source) {
			return new Road(source);
		}
		@Override public Road[] newArray(int size) {
			return new Road[size];
		}
	};
	
	private Road(Parcel in){
		mStatus = in.readInt();
		mLength = in.readDouble();
		mDuration = in.readDouble();
		mNodes = in.readArrayList(RoadNode.class.getClassLoader());
		mLegs = in.readArrayList(RoadLeg.class.getClassLoader());
		mRouteHigh = in.readArrayList(GeoPoint.class.getClassLoader());
		mBoundingBox = in.readParcelable(BoundingBoxE6.class.getClassLoader());
	}
}

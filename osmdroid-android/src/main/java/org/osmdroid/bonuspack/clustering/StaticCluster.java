package org.osmdroid.bonuspack.clustering;

import java.util.ArrayList;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;

/** 
 * Cluster of Markers. 
 * @author M.Kergall
 */
public class StaticCluster {
	protected final ArrayList<Marker> mItems = new ArrayList<Marker>();
	protected GeoPoint mCenter;
	protected Marker mMarker;
	
	public StaticCluster(GeoPoint center) {
	    mCenter = center;
	}
	
	public void setPosition(GeoPoint center){
		mCenter = center;
	}
	
	public GeoPoint getPosition() {
	    return mCenter;
	}
	
	public int getSize() {
	    return mItems.size();
	}
	
	public Marker getItem(int index) {
	    return mItems.get(index);
	}
	
	public boolean add(Marker t) {
	    return mItems.add(t);
	}
	
	/** set the Marker to be displayed for this cluster */
	public void setMarker(Marker marker){
		mMarker = marker;
	}
	
	/** @return the Marker to be displayed for this cluster */
	public Marker getMarker(){
		return mMarker;
	}
}

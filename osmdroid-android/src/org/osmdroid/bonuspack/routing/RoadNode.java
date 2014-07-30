package org.osmdroid.bonuspack.routing;

import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

/** Road intersection, with instructions about what to do at this intersection. 
 * 
 * @author M.Kergall
 */
public class RoadNode implements Parcelable {
	/** A common reference has been chosen for maneuver types. 
	 * The MapQuest Open Maneuver Types list has been selected, as it was the most precise and seems stable. 
	 * All road managers convert the service-specific values to this common reference. 
	 * @see <a href="http://open.mapquestapi.com/guidance/#maneuvertypes">MapQuest Maneuver Types</a> 
	 * */
	public int mManeuverType; 
	/** textual information on what to do at this intersection */
	public String mInstructions;
	/** index in road links array - internal use only, for MapQuest directions */
	public int mNextRoadLink;
	/** in km to the next node */
	public double mLength; 
	/** in seconds to the next node */
	public double mDuration; 
	/** position of the node */
	public GeoPoint mLocation;
	
	public RoadNode(){
		mManeuverType = 0;
		mNextRoadLink = -1;
		mLength = mDuration = 0.0;
	}
	
	//--- Parcelable implementation
	
	@Override public int describeContents() {
		return 0;
	}
	
	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mManeuverType);
		out.writeString(mInstructions);
		out.writeDouble(mLength);
		out.writeDouble(mDuration);
		out.writeParcelable(mLocation, 0);
	}
	
	public static final Parcelable.Creator<RoadNode> CREATOR = new Parcelable.Creator<RoadNode>() {
		@Override public RoadNode createFromParcel(Parcel source) {
			return new RoadNode(source);
		}
		@Override public RoadNode[] newArray(int size) {
			return new RoadNode[size];
		}
	};
	
	private RoadNode(Parcel in){
		mManeuverType = in.readInt();
		mInstructions = in.readString();
		mLength = in.readDouble();
		mDuration = in.readDouble();
		mLocation = in.readParcelable(GeoPoint.class.getClassLoader());
	}
}

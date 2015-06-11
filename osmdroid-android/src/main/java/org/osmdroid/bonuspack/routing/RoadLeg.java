package org.osmdroid.bonuspack.routing;

import java.util.ArrayList;

import org.osmdroid.bonuspack.utils.BonusPackHelper;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/** Road Leg is the portion of the road between 2 waypoints (intermediate points requested) 
 * 
 * @author M.Kergall
 * 
 */
public class RoadLeg implements Parcelable {
	/** in km */
	public double mLength; 
	/** in sec */
	public double mDuration; 
	/** starting node of the leg, as index in nodes array */
	public int mStartNodeIndex;
	/** and ending node */
	public int mEndNodeIndex; 
	
	public RoadLeg(){
		mLength = mDuration = 0.0;
		mStartNodeIndex = mEndNodeIndex = 0;
	}
	
	public RoadLeg(int startNodeIndex, int endNodeIndex, 
			ArrayList<RoadNode> nodes){
		mStartNodeIndex = startNodeIndex;
		mEndNodeIndex = endNodeIndex;
		mLength = mDuration = 0.0;
		for (int i=startNodeIndex; i<=endNodeIndex; i++){ //TODO: <= or < ??? To check. 
			RoadNode node = nodes.get(i);
			mLength += node.mLength;
			mDuration += node.mDuration;
		}
		Log.d(BonusPackHelper.LOG_TAG, "Leg: " + startNodeIndex + "-" + endNodeIndex
				+ ", length=" + mLength + "km, duration="+mDuration+"s");
	}

	//--- Parcelable implementation
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeDouble(mLength);
		out.writeDouble(mDuration);
		out.writeInt(mStartNodeIndex);
		out.writeInt(mEndNodeIndex);
	}
	
	public static final Creator<RoadLeg> CREATOR = new Creator<RoadLeg>() {
		@Override public RoadLeg createFromParcel(Parcel source) {
			return new RoadLeg(source);
		}
		@Override public RoadLeg[] newArray(int size) {
			return new RoadLeg[size];
		}
	};
	
	private RoadLeg(Parcel in){
		mLength = in.readDouble();
		mDuration = in.readDouble();
		mStartNodeIndex = in.readInt();
		mEndNodeIndex = in.readInt();
	}
}

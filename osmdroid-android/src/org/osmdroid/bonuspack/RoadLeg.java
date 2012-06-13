package org.osmdroid.bonuspack;

import java.util.ArrayList;
import android.util.Log;

/** Road Leg is the portion of the road between 2 waypoints (intermediate points requested) 
 * 
 * @author M.Kergall
 * 
 */
public class RoadLeg {
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
		for (int i=startNodeIndex; i<=endNodeIndex; i++){
			RoadNode node = nodes.get(i);
			mLength += node.mLength;
			mDuration += node.mDuration;
		}
		Log.d(BonusPackHelper.LOG_TAG, "Leg: " + startNodeIndex + "-" + endNodeIndex
				+ ", length=" + mLength + "km, duration="+mDuration+"s");
	}
}

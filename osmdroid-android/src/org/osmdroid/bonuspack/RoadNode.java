package org.osmdroid.bonuspack;

import org.osmdroid.util.GeoPoint;

/** Road intersection, with instructions to continue. 
 * 
 * @author M.Kergall
 */
public class RoadNode {
	/** @see http://open.mapquestapi.com/guidance/#maneuvertypes */
	public int mManeuverType = 0; 
	/** textual information on what to do at this intersection */
	public String mInstructions = null;
	/** index in road links array */
	public int mNextRoadLink = -1;
	/** in km to the next node */
	public double mLength = 0.0; 
	/** in seconds to the next node */
	public double mDuration = 0.0; 
	/** position of the node */
	public GeoPoint mLocation = null; 
}

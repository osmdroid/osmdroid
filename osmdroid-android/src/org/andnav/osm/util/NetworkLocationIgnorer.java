package org.andnav.osm.util;

import android.location.LocationManager;

/**
 * 
 * A class to check whether we want to use a location.
 * If there are multiple location providers, i.e. network and GPS,
 * then you want to ignore network locations shortly after a GPS location
 * because you will get another GPS location soon.
 * 
 * @author Neil Boyd
 *
 */
public class NetworkLocationIgnorer {

	/**
	 * The time we wait after the last gps location before displaying
	 *  a non-gps location.
	 */
	private static final long GPS_WAIT_TIME = 20000; // 20 seconds
	
	/** last time we got a location from the gps provider */
	private long mLastGps = 0; 

	/**
	 * Whether we should ignore this location.
	 * @param pProvider the provider that provided the location
	 * @param pTime the time of the location
	 * @return true if we should ignore this location, false if not
	 */
	public boolean shouldIgnore(final String pProvider, final long pTime) {
		
		if (LocationManager.GPS_PROVIDER.equals(pProvider)) {
			mLastGps = pTime;
		} else {
			if (pTime < mLastGps + GPS_WAIT_TIME) {
				return true;
			}
		}
		
		return false;
	}
}

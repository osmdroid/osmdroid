package org.osmdroid.util.constants;

import org.osmdroid.config.Configuration;

@Deprecated
public interface UtilConstants {

	/**
	 * The time we wait after the last gps location before using a non-gps location.
	 * @deprecated
	 * @see org.osmdroid.config.Configuration
	 */
	@Deprecated
	public static long GPS_WAIT_TIME = Configuration.getInstance().getGpsWaitTime(); // 20 seconds

}

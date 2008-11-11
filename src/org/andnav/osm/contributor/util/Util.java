// Created by plusminus on 13:24:05 - 21.09.2008
package org.andnav.osm.contributor.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class Util {
	// ===========================================================
	// Constants
	// ===========================================================
	
	public static final SimpleDateFormat UTCSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	{
		UTCSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	
	public static final String convertTimestampToUTCString(final long aTimestamp){
		return UTCSimpleDateFormat.format(new Date(aTimestamp));
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

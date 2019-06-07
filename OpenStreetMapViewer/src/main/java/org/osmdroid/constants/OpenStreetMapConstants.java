// Created by plusminus on 23:11:31 - 22.09.2008
package org.osmdroid.constants;

/**
 *
 * This class contains constants used by the sample applications.
 *
 * @author Nicolas Gramlich
 *
 */
public interface OpenStreetMapConstants {
	// ===========================================================
	// Final Fields
	// ===========================================================

	String PREFS_NAME = "org.andnav.osm.prefs";
	String PREFS_TILE_SOURCE = "tilesource";
	@Deprecated
	String PREFS_SCROLL_X = "scrollX";
	@Deprecated
	String PREFS_SCROLL_Y = "scrollY";
	/**
	 * as String because we cannot use double in Preferences, only float
	 * and float is not accurate enough
	 */
	String PREFS_LATITUDE_STRING = "latitudeString";
	String PREFS_LONGITUDE_STRING = "longitudeString";
	String PREFS_ORIENTATION = "orientation";
	@Deprecated
	String PREFS_ZOOM_LEVEL = "zoomLevel";
	String PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble";
	String PREFS_SHOW_LOCATION = "showLocation";
	String PREFS_SHOW_COMPASS = "showCompass";

	// ===========================================================
	// Methods
	// ===========================================================
}

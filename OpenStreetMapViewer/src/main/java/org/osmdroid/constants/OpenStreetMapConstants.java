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

	public static final String PREFS_NAME = "org.andnav.osm.prefs";
	public static final String PREFS_TILE_SOURCE = "tilesource";
	@Deprecated
	public static final String PREFS_SCROLL_X = "scrollX";
	@Deprecated
	public static final String PREFS_SCROLL_Y = "scrollY";
	/**
	 * as String because we cannot use double in Preferences, only float
	 * and float is not accurate enough
	 */
	public static final String PREFS_LATITUDE_STRING = "latitudeString";
	public static final String PREFS_LONGITUDE_STRING = "longitudeString";
	public static final String PREFS_ORIENTATION = "orientation";
	@Deprecated
	public static final String PREFS_ZOOM_LEVEL = "zoomLevel";
	public static final String PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble";
	public static final String PREFS_SHOW_LOCATION = "showLocation";
	public static final String PREFS_SHOW_COMPASS = "showCompass";

	// ===========================================================
	// Methods
	// ===========================================================
}

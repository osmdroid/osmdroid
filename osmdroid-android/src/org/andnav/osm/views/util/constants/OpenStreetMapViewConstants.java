// Created by plusminus on 18:00:24 - 25.09.2008
package org.andnav.osm.views.util.constants;

/**
 *
 * This class contains constants used by the map view.
 *
 * @author Nicolas Gramlich
 *
 */
public interface OpenStreetMapViewConstants {
	// ===========================================================
	// Final Fields
	// ===========================================================

	public static final boolean DEBUGMODE = false;

	public static final int NOT_SET = Integer.MIN_VALUE;

	/**
	 * Initial tile cache size.
	 * The size will be increased as required by calling
	 * {@link LRUMapTileCache.ensureCapacity(int)}
	 * The tile cache will always be at least 3x3.
	 */
	public static final int CACHE_MAPTILECOUNT_DEFAULT = 9;

	public static final int MAPTILE_LATITUDE_INDEX = 0;
	public static final int MAPTILE_LONGITUDE_INDEX = 1;

	public static final int ANIMATION_SMOOTHNESS_LOW = 4;
	public static final int ANIMATION_SMOOTHNESS_DEFAULT = 10;
	public static final int ANIMATION_SMOOTHNESS_HIGH = 20;

	public static final int ANIMATION_DURATION_SHORT = 500;
	public static final int ANIMATION_DURATION_DEFAULT = 1000;
	public static final int ANIMATION_DURATION_LONG = 2000;

	// ===========================================================
	// Methods
	// ===========================================================
}

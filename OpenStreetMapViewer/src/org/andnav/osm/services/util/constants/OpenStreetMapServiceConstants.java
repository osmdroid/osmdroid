package org.andnav.osm.services.util.constants;

import android.os.Environment;

/**
 * 
 * @author Neil Boyd
 *
 */
public interface OpenStreetMapServiceConstants {
	// ===========================================================
	// Final Fields
	// ===========================================================
	
	// FIXME set DEBUGMODE to false
	public static final boolean DEBUGMODE = true;
	
	public static final String TILE_PATH_BASE = Environment.getExternalStorageDirectory() + "/andnav2/tiles/";
	public static final String TILE_PATH_EXTENSION = ".andnav";

	// ===========================================================
	// Methods
	// ===========================================================
}

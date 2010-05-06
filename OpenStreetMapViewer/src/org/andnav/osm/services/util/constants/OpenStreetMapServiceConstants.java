package org.andnav.osm.services.util.constants;

import android.os.Environment;

/**
 * 
 * This class contains constants used by the service.
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

	public static final int FS_CACHE_SIZE = 64 * 1024 * 1024; // 64 MB
	
	public static final int NUMBER_OF_TILE_DOWNLOAD_THREADS = 8;
	public static final int NUMBER_OF_TILE_FILESYSTEM_THREADS = 8;

	public static final int TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE = 40;
	public static final int TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE = 40;
	
	// ===========================================================
	// Methods
	// ===========================================================
}

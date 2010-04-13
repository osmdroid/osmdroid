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

	public static final int FS_CACHE_SIZE = 16 * 1024 * 1024; // 16 MB
	
	public static final int NUMBER_OF_TILE_DOWNLOAD_THREADS = 4;
	public static final int NUMBER_OF_TILE_FILESYSTEM_THREADS = 2;

	public static final int TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE = 50;
	public static final int TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE = 50;

	// ===========================================================
	// Methods
	// ===========================================================
}

package org.andnav.osm.tileprovider.constants;

import android.os.Environment;

/**
 *
 * This class contains constants used by the service.
 *
 * @author Neil Boyd
 *
 */
public interface OpenStreetMapTileProviderConstants {
	// ===========================================================
	// Final Fields
	// ===========================================================

	public static final boolean DEBUGMODE = false;

	public static final String TILE_PATH_BASE = Environment.getExternalStorageDirectory() + "/osmdroid/tiles/";
	public static final String TILE_PATH_EXTENSION = ""; // TODO could just remove this

	public static final int NUMBER_OF_TILE_DOWNLOAD_THREADS = 8;
	public static final int NUMBER_OF_TILE_FILESYSTEM_THREADS = 8;

	public static final int TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE = 40;
	public static final int TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE = 40;

	/** 30 days */
	public static final long TILE_EXPIRY_TIME_MILLISECONDS = 1000l * 60 * 60 * 24 * 30;

	// ===========================================================
	// Methods
	// ===========================================================
}

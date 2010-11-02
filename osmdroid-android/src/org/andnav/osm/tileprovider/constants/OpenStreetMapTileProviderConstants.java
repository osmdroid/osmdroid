package org.andnav.osm.tileprovider.constants;

import java.io.File;

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

	/** Base path for osmdroid files. Zip files are in this folder. */
	public static final File OSMDROID_PATH = new File(Environment.getExternalStorageDirectory(), "osmdroid");

	/** Base path for tiles. */
	public static final File TILE_PATH_BASE = new File(OSMDROID_PATH, "tiles");

	/** add an extension to files on sdcard so that gallery doesn't index them */
	public static final String TILE_PATH_EXTENSION = ".tile";

	/**
	 * number of tile download threads, conforming to OSM policy:
	 * http://wiki.openstreetmap.org/wiki/Tile_usage_policy
	 */
	public static final int NUMBER_OF_TILE_DOWNLOAD_THREADS = 2;

	public static final int NUMBER_OF_TILE_FILESYSTEM_THREADS = 8;

	public static final int TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE = 40;
	public static final int TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE = 40;

	/** 30 days */
	public static final long TILE_EXPIRY_TIME_MILLISECONDS = 1000L * 60 * 60 * 24 * 30;

	// ===========================================================
	// Methods
	// ===========================================================
}

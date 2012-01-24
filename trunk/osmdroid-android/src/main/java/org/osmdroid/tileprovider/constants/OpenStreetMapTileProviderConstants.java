package org.osmdroid.tileprovider.constants;

import java.io.File;

import android.os.Environment;

/**
 *
 * This class contains constants used by the tile provider.
 *
 * @author Neil Boyd
 *
 */
public interface OpenStreetMapTileProviderConstants {

	public static final boolean DEBUGMODE = false;

	/** Minimum Zoom Level */
	public static final int MINIMUM_ZOOMLEVEL = 0;

	/**
	 * Maximum Zoom Level - we use Integers to store zoom levels so overflow happens at 2^32 - 1,
	 * but we also have a tile size that is typically 2^8, so (32-1)-8-1 = 22
	 */
	public static final int MAXIMUM_ZOOMLEVEL = 22;

	/** Base path for osmdroid files. Zip files are in this folder. */
	public static final File OSMDROID_PATH = new File(Environment.getExternalStorageDirectory(),
			"osmdroid");

	/** Base path for tiles. */
	public static final File TILE_PATH_BASE = new File(OSMDROID_PATH, "tiles");

	/** add an extension to files on sdcard so that gallery doesn't index them */
	public static final String TILE_PATH_EXTENSION = ".tile";

	/**
	 * Initial tile cache size. The size will be increased as required by calling {@link
	 * LRUMapTileCache.ensureCapacity(int)} The tile cache will always be at least 3x3.
	 */
	public static final int CACHE_MAPTILECOUNT_DEFAULT = 9;

	/**
	 * number of tile download threads, conforming to OSM policy:
	 * http://wiki.openstreetmap.org/wiki/Tile_usage_policy
	 */
	public static final int NUMBER_OF_TILE_DOWNLOAD_THREADS = 2;

	public static final int NUMBER_OF_TILE_FILESYSTEM_THREADS = 8;

	public static final long ONE_SECOND = 1000;
	public static final long ONE_MINUTE = ONE_SECOND * 60;
	public static final long ONE_HOUR = ONE_MINUTE * 60;
	public static final long ONE_DAY = ONE_HOUR * 24;
	public static final long ONE_WEEK = ONE_DAY * 7;
	public static final long ONE_YEAR = ONE_DAY * 365;
	public static final long DEFAULT_MAXIMUM_CACHED_FILE_AGE = ONE_WEEK;

	public static final int TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE = 40;
	public static final int TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE = 40;

	/** 30 days */
	public static final long TILE_EXPIRY_TIME_MILLISECONDS = 1000L * 60 * 60 * 24 * 30;

	/** 600 Mb */
	public static final long TILE_MAX_CACHE_SIZE_BYTES = 600L * 1024 * 1024;

	/** 500 Mb */
	public static final long TILE_TRIM_CACHE_SIZE_BYTES = 500L * 1024 * 1024;

}

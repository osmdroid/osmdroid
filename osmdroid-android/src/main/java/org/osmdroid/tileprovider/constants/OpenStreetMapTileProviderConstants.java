package org.osmdroid.tileprovider.constants;

import java.io.File;

import org.osmdroid.tileprovider.LRUMapTileCache;

import android.os.Environment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class contains constants used by the tile provider.
 *
 * @author Neil Boyd
 *
 */
public class OpenStreetMapTileProviderConstants {

	public static boolean DEBUGMODE = false;
	public static final boolean DEBUG_TILE_PROVIDERS = false;
	public static String USER_AGENT="User-Agent";
	private static String USER_AGENT_VALUE="osmdroid";

	/**
	 * Enables you to get the value for HTTP user agents. Used when downloading tiles
	 * @since 5.0
	 * @return
	 */
	public static String getUserAgentValue(){
		return USER_AGENT_VALUE;
	}

	/**
	 * Enables you to override the default "osmdroid" value for HTTP user agents. Used when downloading tiles
	 * @since 5.0
	 * @param val
	 */
	public static void setUserAgentValue(String val){
		USER_AGENT_VALUE = val;
	}

	/** Minimum Zoom Level */
	public static final int MINIMUM_ZOOMLEVEL = 0;

	/** Base path for osmdroid files. Zip/sqlite/mbtiles/etc files are in this folder. 
          Note: also used for offline tile sources*/
	private static File OSMDROID_PATH = new File(Environment.getExternalStorageDirectory(),
			"osmdroid");
     
	/** Base path for tiles. 
      /sdcard/osmdroid
      */
	public static File TILE_PATH_BASE = new File(OSMDROID_PATH, "tiles");

	/** add an extension to files on sdcard so that gallery doesn't index them */
	public static final String TILE_PATH_EXTENSION = ".tile";

	/**
	 * Initial tile cache size. The size will be increased as required by calling
	 * {@link LRUMapTileCache#ensureCapacity(int)} The tile cache will always be at least 3x3.
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

	/** default is 600 Mb */
	public static long TILE_MAX_CACHE_SIZE_BYTES = 600L * 1024 * 1024;

	/** default is 500 Mb */
	public static long TILE_TRIM_CACHE_SIZE_BYTES = 500L * 1024 * 1024;

     /** Change the root path of the osmdroid cache. 
     * By default, it is defined in SD card, osmdroid directory. 
     * @param newFullPath
     */
     public static void setCachePath(String newFullPath){
         File f=new File(newFullPath);
         if (f.exists()){
               TILE_PATH_BASE = f.getAbsoluteFile();
         }
     }
     
     /** Change the osmdroid tiles cache sizes. (note this represents size of the cache on disk, not in memory)
      * @param maxCacheSize in Mb. Default is 600 Mb. 
      * @param trimCacheSize When the cache size exceeds maxCacheSize, tiles will be automatically removed to reach this target. In Mb. Default is 500 Mb. 
      * @since 4.4
      * @author MKer
      */
     public static void setCacheSizes(long maxCacheSize, long trimCacheSize){
         TILE_MAX_CACHE_SIZE_BYTES = maxCacheSize * 1024 * 1024;
         TILE_TRIM_CACHE_SIZE_BYTES = trimCacheSize * 1024 * 1024;
     }  
     
     /**
      * allows for altering the osmdroid_path variable, which controls the location
      * of where to search for offline tile sources
      * @param path 
      */
     public static void setOfflineMapsPath(String path){
          OSMDROID_PATH = new File(path);
     }
}

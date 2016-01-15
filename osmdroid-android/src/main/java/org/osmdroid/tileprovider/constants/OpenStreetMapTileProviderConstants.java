package org.osmdroid.tileprovider.constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.osmdroid.tileprovider.LRUMapTileCache;

import android.os.Environment;
import android.util.Log;
import org.osmdroid.api.IMapView;

/**
 *
 * This class contains constants used by the tile provider.
 *
 * @author Neil Boyd
 *
 */
public class OpenStreetMapTileProviderConstants {

     /** Base path for osmdroid files. Zip/sqlite/mbtiles/etc files are in this folder. 
          Note: also used for offline tile sources*/
	private static File OSMDROID_PATH = new File(Environment.getExternalStorageDirectory(),
			"osmdroid");
     
     public static File getBasePath(){
          return OSMDROID_PATH;
     }
     
	/** Base path for tiles. 
      /sdcard/osmdroid/tiles
      */
	public static File TILE_PATH_BASE = new File(OSMDROID_PATH, "tiles");
     
     static{
          try {
                   TILE_PATH_BASE.mkdirs();
                   new File(TILE_PATH_BASE + "/.nomedia").createNewFile();
              } catch (Exception ex) {
                   Log.e(IMapView.LOGTAG, "unable to create a nomedia file. downloaded tiles may be visible to the gallery. " + ex.getMessage());
              }
     }
	public static boolean DEBUGMODE = false;
	public static boolean DEBUG_TILE_PROVIDERS = false;
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
	private static int NUMBER_OF_TILE_DOWNLOAD_THREADS = 2;
     public static int getNumberOfTileDownloadThreads(){
          return NUMBER_OF_TILE_DOWNLOAD_THREADS;
     }
     /**
      * Overrides the number of tile download threads. The default value is '2' conforming to OSM policy:
	 * http://wiki.openstreetmap.org/wiki/Tile_usage_policy
      * 
      * Only use the value of '2' when connecting to OSM tile sources. 
      * 
      * @param threads
      * @since 5.1
	 */
     public void setNumberOfTileDownloadThreads(int threads){
          if (threads > 12)
               NUMBER_OF_TILE_DOWNLOAD_THREADS=12;
          else if (threads < 1)
               NUMBER_OF_TILE_DOWNLOAD_THREADS = 1;
          else
               NUMBER_OF_TILE_DOWNLOAD_THREADS = threads;
     }
     
     

	public static final short NUMBER_OF_TILE_FILESYSTEM_THREADS = 8;

	public static final long ONE_SECOND = 1000;
	public static final long ONE_MINUTE = ONE_SECOND * 60;
	public static final long ONE_HOUR = ONE_MINUTE * 60;
	public static final long ONE_DAY = ONE_HOUR * 24;
	public static final long ONE_WEEK = ONE_DAY * 7;
	public static final long ONE_YEAR = ONE_DAY * 365;
	public static final long DEFAULT_MAXIMUM_CACHED_FILE_AGE = ONE_WEEK;

	public static final short TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE = 40;
	public static final short TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE = 40;

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
              try {
                   new File(TILE_PATH_BASE + "/.nomedia").createNewFile();
              } catch (Exception ex) {
                   Log.e(IMapView.LOGTAG, "unable to create a nomedia file. downloaded tiles may be visible to the gallery.",ex);
              }
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

	/**
	 * @since 5.1
	 */
	public static final String HTTP_EXPIRES_HEADER = "Expires";
	/**
	 * @since 5.1
	 */
	public static final String HTTP_EXPIRES_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
	/**
	 * used for HTTP expires headers
	 * @since 5.1
	 */
	public static final SimpleDateFormat HTTP_HEADER_SDF = new SimpleDateFormat(HTTP_EXPIRES_HEADER_FORMAT, Locale.US);
}

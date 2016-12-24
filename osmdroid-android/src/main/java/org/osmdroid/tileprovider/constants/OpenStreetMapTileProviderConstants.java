package org.osmdroid.tileprovider.constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.LRUMapTileCache;

import android.util.Log;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.util.StorageUtils;

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
	private static File OSMDROID_PATH = new File(StorageUtils.getStorage().getAbsolutePath(),
			"osmdroid");
     @Deprecated
     public static File getBasePath(){
          return Configuration.getInstance().getOsmdroidBasePath();
     }
     
	/** Base path for tiles. 
      /sdcard/osmdroid/tiles

	//public static File TILE_PATH_BASE = new File(OSMDROID_PATH, "tiles");
     
     static{
          try {
                   TILE_PATH_BASE.mkdirs();
                   new File(TILE_PATH_BASE + "/.nomedia").createNewFile();
              } catch (Exception ex) {
                   Log.e(IMapView.LOGTAG, "unable to create a nomedia file. downloaded tiles may be visible to the gallery. " + ex.getMessage());
              }
	 }
	 */
	/**
	 * Enables you to get the value for HTTP user agents. Used when downloading tiles
	 * @since 5.0
	 * @return
	 * @deprecated
	 * @see Configuration.getInstance().getUserAgentValue()
	 */
	@Deprecated
	public static String getUserAgentValue(){
		return Configuration.getInstance().getUserAgentValue();
	}

	/**
	 * Enables you to override the default "osmdroid" value for HTTP user agents. Used when downloading tiles
	 * @since 5.0
	 * @param val
	 * @deprecated
	 * @see Configuration.getInstance().getUserAgentValue()
	 */
	@Deprecated
	public static void setUserAgentValue(String val){
		Configuration.getInstance().setUserAgentValue(val);
	}

	/** Minimum Zoom Level */
	public static final int MINIMUM_ZOOMLEVEL = 0;

	

	/** add an extension to files on sdcard so that gallery doesn't index them */
	public static final String TILE_PATH_EXTENSION = ".tile";

	public static final long ONE_SECOND = 1000;
	public static final long ONE_MINUTE = ONE_SECOND * 60;
	public static final long ONE_HOUR = ONE_MINUTE * 60;
	public static final long ONE_DAY = ONE_HOUR * 24;
	public static final long ONE_WEEK = ONE_DAY * 7;
	public static final long ONE_YEAR = ONE_DAY * 365;
	public static final long DEFAULT_MAXIMUM_CACHED_FILE_AGE = ONE_WEEK;

	/** default tile expiration time, only used if the server doesn't specify
	 * 30 days */
	public static final long TILE_EXPIRY_TIME_MILLISECONDS = 1000L * 60 * 60 * 24 * 30;

     /** Change the root path of the osmdroid cache. 
     * By default, it is defined in SD card, osmdroid directory. 
     * @param newFullPath

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
	  */


	/**
	 * this is the expected http header to expect from a tile server
	 * @since 5.1
	 */
	public static final String HTTP_EXPIRES_HEADER = "Expires";
	/**
	 * this is the default and expected http header for Expires, date time format that is used
	 * for more http servers. Can be overridden via Configuration
	 * @since 5.1
	 */
	public static final String HTTP_EXPIRES_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

}

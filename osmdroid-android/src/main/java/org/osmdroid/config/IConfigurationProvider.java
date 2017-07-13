package org.osmdroid.config;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.osmdroid.tileprovider.LRUMapTileCache;
import org.osmdroid.tileprovider.MapTileCache;
import org.osmdroid.tileprovider.MapTileProviderBase;

import java.io.File;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Singleton class to get/set a configuration provider for osmdroid
 * <a href="https://github.com/osmdroid/osmdroid/issues/481">Issue 481</a>
 * Created on 11/29/2016.
 * @author Alex O'Ree
 * @see org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants
 * @since 5.6
 */

public interface IConfigurationProvider {
    /**
     * The time we wait after the last gps location before using a non-gps location.
     * was previously at org.osmdroid.util.constants.UtilConstants
     * @return time in ms
     */
    long getGpsWaitTime();

    /**
     * The time we wait after the last gps location before using a non-gps location.
     * @param gpsWaitTime
     */
    void setGpsWaitTime(long gpsWaitTime);

    /**
     * Typically used to enable additional debugging
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     * @return
     */
    boolean isDebugMode();

    void setDebugMode(boolean debugMode);

    /**
     * Typically used to enable additional debugging
     * from {@link org.osmdroid.views.util.constants.MapViewConstants}
     * @return
     */
    boolean isDebugMapView();

    void setDebugMapView(boolean debugMapView);

    /**
     * Typically used to enable additional debugging
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     * @return
     */
    boolean isDebugTileProviders();

    void setDebugTileProviders(boolean debugTileProviders);

    boolean isDebugMapTileDownloader();

    void setDebugMapTileDownloader(boolean debugMapTileDownloader);

    /**
     * default is false
     * @return
     */
    boolean isMapViewHardwareAccelerated();

    /**
     * must be set before the mapview is created or inflated from a layout.
     * If you're only using single point icons, then youc an probably get away with setting this to true
     * otherwise (using polylines, paths, polygons) set it to false.
     *
     * default is false
     * @param mapViewHardwareAccelerated
     * @see org.osmdroid.views.overlay.Polygon
     * @see org.osmdroid.views.overlay.Polyline
     * @see org.osmdroid.views.drawing.OsmPath
     */
    void setMapViewHardwareAccelerated(boolean mapViewHardwareAccelerated);

    String getUserAgentValue();

    /**
     * Enables you to override the default "osmdroid" value for HTTP user agents. Used when downloading tiles
     *
     *
     * You MUST use this to set the user agent to some value specific to your application.
     * Typical usage: Context.getApplicationContext().getPackageName();
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     * @since 5.0
     * @param userAgentValue
     */
    void setUserAgentValue(String userAgentValue);

    /**
     * Enables you to set and get additional HTTP request properties. Used when downloading tiles.
     * Mustn't be null, but will be empty in most cases.
     *
     * A simple use case would be:
     * Configuration.getInstance().getAdditionalHttpRequestProperties().put("Origin", "http://www.example-social-network.com");
     *
     * See https://github.com/osmdroid/osmdroid/issues/570
     * @since 5.6.5
     */
    Map<String, String> getAdditionalHttpRequestProperties();

    /**
     * Initial tile cache size (in memory). The size will be increased as required by calling
     * {@link LRUMapTileCache#ensureCapacity(int)} The tile cache will always be at least 3x3.
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     * used by MapTileCache
     * @see MapTileCache
     * @return
     */
    short getCacheMapTileCount();

    void setCacheMapTileCount(short cacheMapTileCount);

    /**
     * number of tile download threads, conforming to OSM policy:
     * http://wiki.openstreetmap.org/wiki/Tile_usage_policy
     * default is 2
     */
    short getTileDownloadThreads();

    void setTileDownloadThreads(short tileDownloadThreads);

    /**
     * used for both file system cache and the sqlite cache
     * @return
     */
    short getTileFileSystemThreads();

    /**
     * used for both file system cache and the sqlite cache
     * @param tileFileSystemThreads
     */
    void setTileFileSystemThreads(short tileFileSystemThreads);

    short getTileDownloadMaxQueueSize();

    void setTileDownloadMaxQueueSize(short tileDownloadMaxQueueSize);

    short getTileFileSystemMaxQueueSize();

    void setTileFileSystemMaxQueueSize(short tileFileSystemMaxQueueSize);

    /** default is 600 Mb */
    long getTileFileSystemCacheMaxBytes();

    void setTileFileSystemCacheMaxBytes(long tileFileSystemCacheMaxBytes);

    /**
     * When the cache size exceeds maxCacheSize, tiles will be automatically removed to reach this target. In bytes. Default is 500 Mb.
     * @return
     */
    long getTileFileSystemCacheTrimBytes();

    void setTileFileSystemCacheTrimBytes(long tileFileSystemCacheTrimBytes);

    SimpleDateFormat getHttpHeaderDateTimeFormat();

    void setHttpHeaderDateTimeFormat(SimpleDateFormat httpHeaderDateTimeFormat);

    Proxy getHttpProxy();

    void setHttpProxy(Proxy httpProxy);

    /**
     * Base path for osmdroid files. Zip/sqlite/mbtiles/etc files are in this folder.
     Note: also used for offline tile sources
     * @return
     */
    File getOsmdroidBasePath();

    /**
     * Base path for osmdroid files. Zip/sqlite/mbtiles/etc files are in this folder.
     Note: also used for offline tile sources

     Default is
     StorageUtils.getStorage().getAbsolutePath(),"osmdroid", which usually maps to /sdcard/osmdroid
     * @param osmdroidBasePath
     */
    void setOsmdroidBasePath(File osmdroidBasePath);

    /**
     * by default, maps to getOsmdroidBasePath() + "/tiles"
     * By default, it is defined in SD card, osmdroid directory.
     * Sets the location where the tile cache is stored. Changes are only in effect when the @{link {@link org.osmdroid.views.MapView}}
     * is created. Changes made after it's creation (either pogrammatic or via layout inflator) have
     * no effect until the map is restarted or the {@link org.osmdroid.views.MapView#setTileProvider(MapTileProviderBase)}
     * is changed or recreated.
     *
     * Note: basePath and tileCache directories can be changed independently
     * This has no effect on offline archives and can be changed independently
     * @return
     */
    File getOsmdroidTileCache();

    /**
     * by default, maps to getOsmdroidBasePath() + "/tiles"
     * Sets the location where the tile cache is stored. Changes are only in effect when the @{link {@link org.osmdroid.views.MapView}}
     * is created. Changes made after it's creation (either pogrammatic or via layout inflator) have
     * no effect until the map is restarted or the {@link org.osmdroid.views.MapView#setTileProvider(MapTileProviderBase)}
     * is changed or recreated.
     *
     * This has no effect on offline archives and can be changed independently
     * @param osmdroidTileCache
     */
    void setOsmdroidTileCache(File osmdroidTileCache);

    /**
     * "User-Agent" is the default value and standard used throughout all http servers, unlikely to change
     * When calling @link {@link #load(Context, SharedPreferences)}, it is set to
     * {@link Context#getPackageName()} which is defined your manifest file
     *
     * made adjustable just in case
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     * @return
     */
    String getUserAgentHttpHeader();

    /**
     * "User-Agent" is the default value and standard used throughout all http servers, unlikely to change
     * When calling @link {@link #load(Context, SharedPreferences)}, it is set to
     * {@link Context#getPackageName()} which is defined your manifest file
     *
     * made adjustable just in case
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
          */
    void setUserAgentHttpHeader(String userAgentHttpHeader);

    /**
     * loads the configuration from shared preferences, if the preferences defined in this file are not already
     * set, them they will be populated with defaults. This also initializes the tile storage cache to
     * the largested writable storage partition available.
     * @param ctx
     * @param preferences
     */
    void load(Context ctx, SharedPreferences preferences);

    /**
     * saves the current configuration to the shared preference location
     * @param ctx
     * @param preferences
     */
    void save(Context ctx, SharedPreferences preferences);

    /**
     * Returns the amount of time in ms added to server specified tile expiration time
     * Added as part of issue https://github.com/osmdroid/osmdroid/issues/490
     * @since 5.6.1
     * @return time in ms
     */
    long getExpirationExtendedDuration();

    /**
     * Optionally extends the amount of time that downloaded tiles remain in the cache beyond either the
     * server specified expiration time stamp or the default expiration time {{@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants#DEFAULT_MAXIMUM_CACHED_FILE_AGE}}
     *
     * Note: this setting only controls tiles as they are downloaded. tiles already in the cache are
     * not effected by this setting
     * Added as part of issue https://github.com/osmdroid/osmdroid/issues/490
     * @since 5.6.1
     * @param period time in ms, if 0, no additional time to the 'server provided expiration' or the
     *               'default expiration time' is added. If the value is less than 0, 0 will be used
     */
    void setExpirationExtendedDuration(long period);

    /**
     * Optional period of time in ms that will override any downloaded tile's expiration timestamp
     * @since 5.6.1
     * @param period if null, this setting is unset, server value + getExpirationExtendedDuration apply
     *               if not null, this this value is used
     */
    void setExpirationOverrideDuration(Long period);
    /**
     * Optional period of time in ms that will override any downloaded tile's expiration timestamp
     * @since 5.6.1
     * @return period if null, this setting is unset, server value + getExpirationExtendedDuration apply
     *               if not null, this this value is used
     */
    Long getExpirationOverrideDuration();
}

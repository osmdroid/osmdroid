package org.osmdroid.config;


import android.content.Context;
import android.content.SharedPreferences;

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
 *
 * @author Alex O'Ree
 * @see org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants
 * @since 5.6
 */

public interface IConfigurationProvider {
    /**
     * The time we wait after the last gps location before using a non-gps location.
     * was previously at org.osmdroid.util.constants.UtilConstants
     *
     * @return time in ms
     */
    long getGpsWaitTime();

    /**
     * The time we wait after the last gps location before using a non-gps location.
     *
     * @param gpsWaitTime
     */
    void setGpsWaitTime(long gpsWaitTime);

    /**
     * Typically used to enable additional debugging
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     *
     * @return
     */
    boolean isDebugMode();

    void setDebugMode(boolean debugMode);

    /**
     * Typically used to enable additional debugging
     *
     * @return
     */
    boolean isDebugMapView();

    void setDebugMapView(boolean debugMapView);

    /**
     * Typically used to enable additional debugging
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     *
     * @return
     */
    boolean isDebugTileProviders();

    void setDebugTileProviders(boolean debugTileProviders);

    boolean isDebugMapTileDownloader();

    void setDebugMapTileDownloader(boolean debugMapTileDownloader);

    /**
     * default is false
     *
     * @return
     */
    boolean isMapViewHardwareAccelerated();

    /**
     * must be set before the mapview is created or inflated from a layout.
     * If you're only using single point icons, then you can probably get away with setting this to true
     * otherwise (using polylines, paths, polygons) set it to false.
     * <p>
     * default is false
     *
     * @param mapViewHardwareAccelerated
     * @see org.osmdroid.views.overlay.Polygon
     * @see org.osmdroid.views.overlay.Polyline
     */
    void setMapViewHardwareAccelerated(boolean mapViewHardwareAccelerated);

    String getUserAgentValue();

    /**
     * Enables you to override the default "osmdroid" value for HTTP user agents. Used when downloading tiles
     * <p>
     * <p>
     * You MUST use this to set the user agent to some value specific to your application.
     * Typical usage: Context.getApplicationContext().getPackageName();
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     *
     * @param userAgentValue
     * @since 5.0
     */
    void setUserAgentValue(String userAgentValue);

    /**
     * Enables you to set and get additional HTTP request properties. Used when downloading tiles.
     * Mustn't be null, but will be empty in most cases.
     * <p>
     * A simple use case would be:
     * Configuration.getInstance().getAdditionalHttpRequestProperties().put("Origin", "http://www.example-social-network.com");
     * <p>
     * See https://github.com/osmdroid/osmdroid/issues/570
     *
     * @since 5.6.5
     */
    Map<String, String> getAdditionalHttpRequestProperties();

    /**
     * Initial tile cache size (in memory). The size will be increased as required by calling
     * {@link MapTileCache#ensureCapacity(int)} The tile cache will always be at least 3x3.
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     * used by MapTileCache
     *
     * @return
     * @see MapTileCache
     */
    short getCacheMapTileCount();

    /**
     * Initial tile cache size (in memory). The size will be increased as required by calling
     * {@link MapTileCache#ensureCapacity(int)} The tile cache will always be at least 3x3.
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     * used by MapTileCache
     *
     * @param cacheMapTileCount
     * @see MapTileCache
     */
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
     *
     * @return
     */
    short getTileFileSystemThreads();

    /**
     * used for both file system cache and the sqlite cache
     *
     * @param tileFileSystemThreads
     */
    void setTileFileSystemThreads(short tileFileSystemThreads);

    short getTileDownloadMaxQueueSize();

    void setTileDownloadMaxQueueSize(short tileDownloadMaxQueueSize);

    short getTileFileSystemMaxQueueSize();

    void setTileFileSystemMaxQueueSize(short tileFileSystemMaxQueueSize);

    /**
     * default is 600 Mb
     */
    long getTileFileSystemCacheMaxBytes();

    void setTileFileSystemCacheMaxBytes(long tileFileSystemCacheMaxBytes);

    /**
     * When the cache size exceeds maxCacheSize, tiles will be automatically removed to reach this target. In bytes. Default is 500 Mb.
     *
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
     * Note: also used for offline tile sources
     * <p>
     * If no directory has been set before with {@link #setOsmdroidBasePath(File)} it tries
     * to automatically detect one. On API>29 and for better results use
     * {@link #getOsmdroidBasePath(Context)}
     *
     * @return
     */
    File getOsmdroidBasePath();

    /**
     * Base path for osmdroid files. Zip/sqlite/mbtiles/etc files are in this folder.
     * Note: also used for offline tile sources
     * <p>
     * If no directory has been set before with {@link #setOsmdroidBasePath(File)} it tries
     * to automatically detect one. Passing a context gives better results than
     * {@link #getOsmdroidBasePath()} and is required to find any location on API29.
     *
     * @return
     */
    File getOsmdroidBasePath(Context context);

    /**
     * Base path for osmdroid files. Zip/sqlite/mbtiles/etc files are in this folder.
     * Note: also used for offline tile sources
     * <p>
     * Default is
     * StorageUtils.getStorage().getAbsolutePath(),"osmdroid", which usually maps to /sdcard/osmdroid
     *
     * @param osmdroidBasePath
     */
    void setOsmdroidBasePath(File osmdroidBasePath);

    /**
     * by default, maps to getOsmdroidBasePath() + "/tiles"
     * By default, it is defined in SD card, osmdroid directory.
     * Sets the location where the tile cache is stored. Changes are only in effect when the {@link {@link org.osmdroid.views.MapView}}
     * is created. Changes made after it's creation (either pogrammatic or via layout inflator) have
     * no effect until the map is restarted or the {@link org.osmdroid.views.MapView#setTileProvider(MapTileProviderBase)}
     * is changed or recreated.
     * <p>
     * Note: basePath and tileCache directories can be changed independently
     * This has no effect on offline archives and can be changed independently
     * <p>
     * If no directory has been set before with {@link #setOsmdroidTileCache(File)} it tries
     * to automatically detect one. On API>29 and for better results use
     * {@link #getOsmdroidTileCache(Context)}
     *
     * @return
     */
    File getOsmdroidTileCache();

    /**
     * by default, maps to getOsmdroidBasePath() + "/tiles"
     * By default, it is defined in SD card, osmdroid directory.
     * Sets the location where the tile cache is stored. Changes are only in effect when the {@link org.osmdroid.views.MapView}}
     * is created. Changes made after it's creation (either pogrammatic or via layout inflator) have
     * no effect until the map is restarted or the {@link org.osmdroid.views.MapView#setTileProvider(MapTileProviderBase)}
     * is changed or recreated.
     * <p>
     * Note: basePath and tileCache directories can be changed independently
     * This has no effect on offline archives and can be changed independently
     * <p>
     * If no directory has been set before with {@link #setOsmdroidTileCache(File)} it tries
     * to automatically detect one. Passing a context gives better results than
     * {@link #getOsmdroidTileCache()} and is required to find any location on API29.
     *
     * @return
     */
    File getOsmdroidTileCache(Context context);

    /**
     * by default, maps to getOsmdroidBasePath() + "/tiles"
     * Sets the location where the tile cache is stored. Changes are only in effect when the @{link {@link org.osmdroid.views.MapView}}
     * is created. Changes made after it's creation (either pogrammatic or via layout inflator) have
     * no effect until the map is restarted or the {@link org.osmdroid.views.MapView#setTileProvider(MapTileProviderBase)}
     * is changed or recreated.
     * <p>
     * This has no effect on offline archives and can be changed independently
     *
     * @param osmdroidTileCache
     */
    void setOsmdroidTileCache(File osmdroidTileCache);

    /**
     * "User-Agent" is the default value and standard used throughout all http servers, unlikely to change
     * When calling @link {@link #load(Context, SharedPreferences)}, it is set to
     * {@link Context#getPackageName()} which is defined your manifest file
     * <p>
     * made adjustable just in case
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     *
     * @return
     */
    String getUserAgentHttpHeader();

    /**
     * "User-Agent" is the default value and standard used throughout all http servers, unlikely to change
     * When calling @link {@link #load(Context, SharedPreferences)}, it is set to
     * {@link Context#getPackageName()} which is defined your manifest file
     * <p>
     * made adjustable just in case
     * from {@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants}
     */
    void setUserAgentHttpHeader(String userAgentHttpHeader);

    /**
     * loads the configuration from shared preferences, if the preferences defined in this file are not already
     * set, them they will be populated with defaults. This also initializes the tile storage cache to
     * the largested writable storage partition available.
     *
     * @param ctx
     * @param preferences
     */
    void load(Context ctx, SharedPreferences preferences);

    /**
     * saves the current configuration to the shared preference location
     *
     * @param ctx
     * @param preferences
     */
    void save(Context ctx, SharedPreferences preferences);

    /**
     * Returns the amount of time in ms added to server specified tile expiration time
     * Added as part of issue https://github.com/osmdroid/osmdroid/issues/490
     *
     * @return time in ms
     * @since 5.6.1
     */
    long getExpirationExtendedDuration();

    /**
     * Optionally extends the amount of time that downloaded tiles remain in the cache beyond either the
     * server specified expiration time stamp or the default expiration time {{@link org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants#DEFAULT_MAXIMUM_CACHED_FILE_AGE}}
     * <p>
     * Note: this setting only controls tiles as they are downloaded. tiles already in the cache are
     * not effected by this setting
     * Added as part of issue https://github.com/osmdroid/osmdroid/issues/490
     *
     * @param period time in ms, if 0, no additional time to the 'server provided expiration' or the
     *               'default expiration time' is added. If the value is less than 0, 0 will be used
     * @since 5.6.1
     */
    void setExpirationExtendedDuration(long period);

    /**
     * Optional period of time in ms that will override any downloaded tile's expiration timestamp
     *
     * @param period if null, this setting is unset, server value + getExpirationExtendedDuration apply
     *               if not null, this this value is used
     * @since 5.6.1
     */
    void setExpirationOverrideDuration(Long period);

    /**
     * Optional period of time in ms that will override any downloaded tile's expiration timestamp
     *
     * @return period if null, this setting is unset, server value + getExpirationExtendedDuration apply
     * if not null, this this value is used
     * @since 5.6.1
     */
    Long getExpirationOverrideDuration();


    /**
     * Used during zoom animations
     * https://github.com/osmdroid/osmdroid/issues/650
     *
     * @param durationsMilliseconds
     * @since 6.0.0
     */
    void setAnimationSpeedDefault(int durationsMilliseconds);

    /**
     * Used during zoom animations
     * https://github.com/osmdroid/osmdroid/issues/650
     *
     * @return
     * @since 6.0.0
     */
    int getAnimationSpeedDefault();

    /**
     * Used during zoom animations
     * https://github.com/osmdroid/osmdroid/issues/650
     *
     * @param durationsMilliseconds
     * @since 6.0.0
     */
    void setAnimationSpeedShort(int durationsMilliseconds);

    /**
     * Used during zoom animations
     * https://github.com/osmdroid/osmdroid/issues/650
     *
     * @return
     * @since 6.0.0
     */
    int getAnimationSpeedShort();

    /**
     * If true, the map view will set .setHasTransientState(true) for API 16+ devices.
     * This is now the default setting. Set to false if this is causing you issues
     *
     * @return
     * @since 6.0.0
     */
    boolean isMapViewRecyclerFriendly();

    /**
     * If true, the map view will set .setHasTransientState(true) for API 16+ devices.
     * This is now the default setting. Set to false if this is causing you issues
     *
     * @return
     * @since 6.0.0
     */
    void setMapViewRecyclerFriendly(boolean enabled);

    /**
     * In memory tile count, used by the tiles overlay
     *
     * @param value
     * @see org.osmdroid.views.overlay.TilesOverlay
     * @since 6.0.0
     */
    void setCacheMapTileOvershoot(short value);

    /**
     * In memory tile count, used by the tiles overlay
     *
     * @return
     * @since 6.0.0
     */
    short getCacheMapTileOvershoot();

    /**
     * Delay between tile garbage collection calls
     *
     * @since 6.0.2
     */
    long getTileGCFrequencyInMillis();

    /**
     * @since 6.0.2
     */
    void setTileGCFrequencyInMillis(final long pMillis);

    /**
     * Tile garbage collection bulk size
     *
     * @since 6.0.2
     */
    int getTileGCBulkSize();

    /**
     * @since 6.0.2
     */
    void setTileGCBulkSize(final int pSize);

    /**
     * Pause during tile garbage collection bulk deletions
     *
     * @since 6.0.2
     */
    long getTileGCBulkPauseInMillis();

    /**
     * @since 6.0.2
     */
    void setTileGCBulkPauseInMillis(final long pMillis);

    /**
     * enables/disables tile downloading following redirects. default is true
     *
     * @param value
     * @since 6.0.2
     */
    void setMapTileDownloaderFollowRedirects(boolean value);

    boolean isMapTileDownloaderFollowRedirects();

    /**
     * @since 6.1.0
     */
    String getNormalizedUserAgent();

    /**
     * Default is false for the DefaultConfigurationProvider<br><br>
     * If true and a bounding box is beyond that of the {@link org.osmdroid.util.TileSystem},
     * then an exception is thrown by checks in {@link org.osmdroid.util.BoundingBox}
     * <br><br>
     * If false, then no exception is thrown.<br><br>
     * Historical note. Prior to late Feb 2018, which could have been around v6.0.2,
     * the behavior was to NOT throw an exception, Starting with 6.0.2, it starting throwing.
     * This caused a number of issues when importing content from other sources.<br>
     * July 2022, this method was added to help reduce the pain associated with this with
     * the default set to false (do not throw).<br><br>
     *
     * Keep in mind, that coordinates beyond that of the tile system may render inaccurately or
     * have strange behavior.
     *
     * @since 6.1.14
     * @return true = throw an exception when the bounding box is beyond the tile system, false = do not throw
     */
    boolean isEnforceTileSystemBounds();

    /**
     * See {@link #isEnforceTileSystemBounds()}.
     * @since 6.1.14
     * @param mValue
     */
    void setEnforceTileSystemBounds(boolean mValue);
}

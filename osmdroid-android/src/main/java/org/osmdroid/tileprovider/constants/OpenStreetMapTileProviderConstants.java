package org.osmdroid.tileprovider.constants;

/**
 * This class contains constants used by the tile provider.
 *
 * @author Neil Boyd
 */
public class OpenStreetMapTileProviderConstants {

    /**
     * Minimum Zoom Level
     */
    public static final int MINIMUM_ZOOMLEVEL = 0;

    /**
     * add an extension to files on sdcard so that gallery doesn't index them
     */
    public static final String TILE_PATH_EXTENSION = ".tile";

    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = ONE_SECOND * 60;
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;
    public static final long ONE_WEEK = ONE_DAY * 7;
    public static final long ONE_YEAR = ONE_DAY * 365;
    public static final long DEFAULT_MAXIMUM_CACHED_FILE_AGE = ONE_WEEK;

    /**
     * default tile expiration time, only used if the server doesn't specify
     * 30 days
     */
    public static final long TILE_EXPIRY_TIME_MILLISECONDS = 1000L * 60 * 60 * 24 * 30;

    /**
     * this is the expected http header to expect from a tile server
     *
     * @since 5.1
     */
    public static final String HTTP_EXPIRES_HEADER = "Expires";

    /**
     * @since 6.0.3
     */
    public static final String HTTP_CACHECONTROL_HEADER = "Cache-Control";

    /**
     * this is the default and expected http header for Expires, date time format that is used
     * for more http servers. Can be overridden via Configuration
     *
     * @since 5.1
     */
    public static final String HTTP_EXPIRES_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
}

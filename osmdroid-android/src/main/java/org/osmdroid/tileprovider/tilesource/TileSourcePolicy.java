package org.osmdroid.tileprovider.tilesource;

import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.DefaultConfigurationProvider;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.TileDownloader;

import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Online Tile Source Usage Policy, including<ul>
 * <li>the max number of concurrent downloads</li>
 * <li>if it accepts a meaningless user agent</li>
 * <li>if it accepts bulk downloads</li>
 * <li>if the user agent must be normalized</li>
 * </ul>
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class TileSourcePolicy {

    /**
     * No bulk downloads allowed
     */
    public static final int FLAG_NO_BULK = 1;

    /**
     * Don't try to preventively download tiles that aren't currently displayed
     */
    public static final int FLAG_NO_PREVENTIVE = 2;

    /**
     * Demands a user agent different from the default value
     */
    public static final int FLAG_USER_AGENT_MEANINGFUL = 4;

    /**
     * Uses the "normalized" user agent (package name + version)
     */
    public static final int FLAG_USER_AGENT_NORMALIZED = 8;

    /**
     * maximum number of concurrent downloads
     */
    private final int mMaxConcurrent;

    private final int mFlags;

    public TileSourcePolicy() {
        this(0, 0);
    }

    public TileSourcePolicy(final int pMaxConcurrent,
                            final int pFlags) {
        mMaxConcurrent = pMaxConcurrent;
        mFlags = pFlags;
    }

    public int getMaxConcurrent() {
        return mMaxConcurrent;
    }

    public boolean acceptsBulkDownload() {
        return (mFlags & FLAG_NO_BULK) == 0;
    }

    private boolean acceptsMeaninglessUserAgent() {
        return (mFlags & FLAG_USER_AGENT_MEANINGFUL) == 0;
    }

    public boolean normalizesUserAgent() {
        return (mFlags & FLAG_USER_AGENT_NORMALIZED) != 0;
    }

    public boolean acceptsPreventive() {
        return (mFlags & FLAG_NO_PREVENTIVE) == 0;
    }

    public boolean acceptsUserAgent(final String pUserAgent) {
        if (acceptsMeaninglessUserAgent()) {
            return true;
        }
        return pUserAgent != null
                && pUserAgent.trim().length() > 0
                && (!pUserAgent.equals(DefaultConfigurationProvider.DEFAULT_USER_AGENT));
    }

    /**
     * @return the Epoch timestamp corresponding to the http header (in milliseconds), or null
     * @since 6.1.7
     * Used to be in {@link TileDownloader}
     */
    public Long getHttpExpiresTime(final String pHttpExpiresHeader) {
        if (pHttpExpiresHeader != null && pHttpExpiresHeader.length() > 0) {
            try {
                final Date dateExpires = Configuration.getInstance().getHttpHeaderDateTimeFormat().parse(pHttpExpiresHeader);
                return dateExpires.getTime();
            } catch (final Exception ex) {
                if (Configuration.getInstance().isDebugMapTileDownloader())
                    Log.d(IMapView.LOGTAG, "Unable to parse expiration tag for tile, server returned " + pHttpExpiresHeader, ex);
            }
        }
        return null;
    }

    /**
     * @return the max-age corresponding to the http header (in seconds), or null
     * @since 6.1.7
     * Used to be in {@link TileDownloader}
     */
    public Long getHttpCacheControlDuration(final String pHttpCacheControlHeader) {
        if (pHttpCacheControlHeader != null && pHttpCacheControlHeader.length() > 0) {
            try {
                final String[] parts = pHttpCacheControlHeader.split(", ");
                final String maxAge = "max-age=";
                for (final String part : parts) {
                    final int pos = part.indexOf(maxAge);
                    if (pos == 0) {
                        final String durationString = part.substring(maxAge.length());
                        return Long.valueOf(durationString);
                    }
                }
            } catch (final Exception ex) {
                if (Configuration.getInstance().isDebugMapTileDownloader())
                    Log.d(IMapView.LOGTAG,
                            "Unable to parse cache control tag for tile, server returned " + pHttpCacheControlHeader, ex);
            }
        }
        return null;
    }

    /**
     * @return the expiration time (as Epoch timestamp in milliseconds)
     * @since 6.1.7
     * Used to be in {@link TileDownloader}
     */
    public long computeExpirationTime(final String pHttpExpiresHeader, final String pHttpCacheControlHeader, final long pNow) {
        final Long override = Configuration.getInstance().getExpirationOverrideDuration();
        if (override != null) {
            return pNow + override;
        }

        final long extension = Configuration.getInstance().getExpirationExtendedDuration();
        final Long cacheControlDuration = getHttpCacheControlDuration(pHttpCacheControlHeader);
        if (cacheControlDuration != null) {
            return pNow + cacheControlDuration * 1000 + extension;
        }

        final Long httpExpiresTime = getHttpExpiresTime(pHttpExpiresHeader);
        if (httpExpiresTime != null) {
            return httpExpiresTime + extension;
        }

        return pNow + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE + extension;
    }

    /**
     * @return the expiration time (as Epoch timestamp in milliseconds)
     * @since 6.1.7
     */
    public long computeExpirationTime(final HttpURLConnection pHttpURLConnection, final long pNow) {
        final String expires = pHttpURLConnection.getHeaderField(OpenStreetMapTileProviderConstants.HTTP_EXPIRES_HEADER);
        final String cacheControl = pHttpURLConnection.getHeaderField(OpenStreetMapTileProviderConstants.HTTP_CACHECONTROL_HEADER);
        final long result = computeExpirationTime(expires, cacheControl, pNow);
        if (Configuration.getInstance().isDebugMapTileDownloader()) {
            Log.d(IMapView.LOGTAG, "computeExpirationTime('" + expires + "','" + cacheControl + "'," + pNow + "=" + result);
        }
        return result;
    }
}

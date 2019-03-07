package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.config.DefaultConfigurationProvider;

/**
 * Online Tile Source Usage Policy, including<ul>
 * <li>the max number of concurrent downloads</li>
 * <li>if it accepts a meaningless user agent</li>
 * <li>if it accepts bulk downloads</li>
 * </ul>
 * @since 6.1.0
 * @author Fabrice Fontaine
 */
public class TileSourcePolicy {

    /**
     * maximum number of concurrent downloads
     */
    private final int mMaxConcurrent;

    /**
     * accepts bulk download
     */
    private final boolean mAcceptsBulkDownload;

    /**
     * accepts meaningless default user agent
     */
    private final boolean mAcceptsMeaninglessUserAgent;

    public TileSourcePolicy() {
        this(0, true, true);
    }

    public TileSourcePolicy(final int pMaxConcurrent,
                            final boolean pAcceptsBulkDownload,
                            final boolean pAcceptsMeaninglessUserAgent) {
        mMaxConcurrent = pMaxConcurrent;
        mAcceptsBulkDownload = pAcceptsBulkDownload;
        mAcceptsMeaninglessUserAgent = pAcceptsMeaninglessUserAgent;
    }

    public int getMaxConcurrent() {
        return mMaxConcurrent;
    }

    public boolean acceptsBulkDownload() {
        return mAcceptsBulkDownload;
    }

    public boolean acceptsUserAgent(final String pUserAgent) {
        if (mAcceptsMeaninglessUserAgent) {
            return true;
        }
        return pUserAgent != null
                && pUserAgent.trim().length() > 0
                && (!pUserAgent.equals(DefaultConfigurationProvider.DEFAULT_USER_AGENT));
    }
}

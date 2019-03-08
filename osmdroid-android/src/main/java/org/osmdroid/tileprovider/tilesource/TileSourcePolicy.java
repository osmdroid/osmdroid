package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.config.DefaultConfigurationProvider;

/**
 * Online Tile Source Usage Policy, including<ul>
 * <li>the max number of concurrent downloads</li>
 * <li>if it accepts a meaningless user agent</li>
 * <li>if it accepts bulk downloads</li>
 * <li>if the user agent must be normalized</li>
 * </ul>
 * @since 6.1.0
 * @author Fabrice Fontaine
 */
public class TileSourcePolicy {

    /**
     * No bulk downloads allowed
     */
    public static final int FLAG_NO_BULK                = 1;

    /**
     * Don't try to preventively download tiles that aren't currently displayed
     */
    public static final int FLAG_NO_PREVENTIVE          = 2;

    /**
     * Demands a user agent different from the default value
     */
    public static final int FLAG_USER_AGENT_MEANINGFUL  = 4;

    /**
     * Uses the "normalized" user agent (package name + version)
     */
    public static final int FLAG_USER_AGENT_NORMALIZED  = 8;

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
}

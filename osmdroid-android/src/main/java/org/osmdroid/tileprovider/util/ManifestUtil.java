package org.osmdroid.tileprovider.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Utility class for reading the manifest
 */
public class ManifestUtil {

    private static final Logger logger = LoggerFactory.getLogger(ManifestUtil.class);

    /**
     * Retrieve a key from the manifest meta data, or empty string if not found.
     */
    public static String retrieveKey(final Context aContext, final String aKey) {

        // get the key from the manifest
        final PackageManager pm = aContext.getPackageManager();
        try {
            final ApplicationInfo info = pm.getApplicationInfo(aContext.getPackageName(),
					PackageManager.GET_META_DATA);
            if (info.metaData == null) {
                logger.info("Key %s not found in manifest", aKey);
            } else {
                final String value = info.metaData.getString(aKey);
                if (value == null) {
                    logger.info("Key %s not found in manifest", aKey);
                } else {
                    return value.trim();
                }
            }
        } catch (final PackageManager.NameNotFoundException e) {
            logger.info("Key %s not found in manifest", aKey);
        }
        return "";
    }


}

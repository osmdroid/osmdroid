package org.osmdroid.tileprovider.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * The counters class is a simple container for tracking various internal statistics for osmdroid,
 * useful for troubleshooting osmdroid, finding memory leaks and more
 * Created by alex on 6/16/16.
 */
public class Counters {
    static final String TAG = "osmCounters";
    /**
     * out of memory errors
     */
    public static int countOOM = 0;

    public static int tileDownloadErrors = 0;

    public static int fileCacheSaveErrors = 0;

    public static int fileCacheMiss = 0;

    public static int fileCacheOOM = 0;
    public static int fileCacheHit = 0;

    /**
     * @since 6.2.0
     */
    private static final Map<String, Integer> sMap = new HashMap<>();

    public static void printToLogcat() {
        Log.d(TAG, "countOOM " + countOOM);
        Log.d(TAG, "tileDownloadErrors " + tileDownloadErrors);
        Log.d(TAG, "fileCacheSaveErrors " + fileCacheSaveErrors);
        Log.d(TAG, "fileCacheMiss " + fileCacheMiss);
        Log.d(TAG, "fileCacheOOM " + fileCacheOOM);
        Log.d(TAG, "fileCacheHit " + fileCacheHit);
    }

    public static void reset() {
        countOOM = 0;
        tileDownloadErrors = 0;
        fileCacheSaveErrors = 0;
        fileCacheMiss = 0;
        fileCacheOOM = 0;
        fileCacheHit = 0;
    }

    /**
     * @since 6.2.0
     */
    public static void reset(final String pTag) {
        sMap.remove(pTag);
    }

    /**
     * @since 6.2.0
     */
    public static void increment(final String pTag) {
        final Integer value = sMap.get(pTag);
        if (value == null) {
            sMap.put(pTag, 1);
        } else {
            sMap.put(pTag, value + 1);
        }
    }

    /**
     * @since 6.2.0
     */
    public static int get(final String pTag) {
        final Integer value = sMap.get(pTag);
        if (value == null) {
            return 0;
        } else {
            return value;
        }
    }
}

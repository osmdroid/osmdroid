package org.osmdroid.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabrice Fontaine
 * @since 6.0.2
 */
public class UrlBackoff {

    private static final long[] mExponentialBackoffDurationInMillisDefault = new long[]{
            5000, 15000, 60000, 120000, 300000
    };

    private long[] mExponentialBackoffDurationInMillis = mExponentialBackoffDurationInMillisDefault;
    private final Map<String, Delay> mDelays = new HashMap<>();

    public void next(final String pUrl) {
        Delay delay;
        synchronized (mDelays) {
            delay = mDelays.get(pUrl);
        }
        if (delay == null) {
            delay = new Delay(mExponentialBackoffDurationInMillis);
            synchronized (mDelays) {
                mDelays.put(pUrl, delay);
            }
        } else {
            delay.next();
        }
    }

    public Delay remove(final String pUrl) {
        synchronized (mDelays) {
            return mDelays.remove(pUrl);
        }
    }

    public boolean shouldWait(final String pUrl) {
        final Delay delay;
        synchronized (mDelays) {
            delay = mDelays.get(pUrl);
        }
        return delay != null && delay.shouldWait();
    }

    public void clear() {
        synchronized (mDelays) {
            mDelays.clear();
        }
    }

    public void setExponentialBackoffDurationInMillis(final long[] pExponentialBackoffDurationInMillis) {
        mExponentialBackoffDurationInMillis = pExponentialBackoffDurationInMillis;
    }
}

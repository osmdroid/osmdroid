package org.osmdroid.util;

/**
 * @author Fabrice Fontaine
 * @since 6.0.2
 */
public class Delay {

    private final long[] mDurations;
    private long mDuration;
    private long mNextTime;
    private int mIndex;

    public Delay(final long pDuration) {
        mDurations = null;
        mDuration = pDuration;
        next();
    }

    public Delay(final long[] pDurations) {
        if (pDurations == null || pDurations.length == 0) {
            throw new IllegalArgumentException();
        }
        mDurations = pDurations;
        next();
    }

    public long next() {
        final long duration;
        if (mDurations == null) {
            duration = mDuration;
        } else {
            duration = mDurations[mIndex];
            if (mIndex < mDurations.length - 1) {
                mIndex++;
            }
        }
        mNextTime = now() + duration;
        return duration;
    }

    public boolean shouldWait() {
        return now() < mNextTime;
    }

    private long now() {
        return System.nanoTime() / 1000000L;
    }
}

package org.osmdroid.tileprovider;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.LruCache;

import androidx.annotation.NonNull;

import java.util.HashMap;

/**
 * A {@link BitmapDrawable} for a {@link org.osmdroid.util.MapTileIndex} that has a state to indicate its relevancy:
 * up-to-date (not expired yet), expired, scaled (computed during zoom) and not found (default grey tile)
 */
public class ExpirableBitmapDrawable extends BitmapDrawable {

    public static final int UP_TO_DATE = -1; // should not be set manually, just leave an empty int[] state
    public static final int EXPIRED = -2;
    public static final int SCALED = -3;
    public static final int NOT_FOUND = -4;
    private static final int defaultStatus = UP_TO_DATE;

    private static final int[] settableStatuses = new int[]{ EXPIRED, SCALED, NOT_FOUND };
    private static final HashMap<Integer,int[]> mStatusCache = new HashMap<>();

    private int[] mState;

    public ExpirableBitmapDrawable(final Bitmap pBitmap) {
        super(pBitmap);
        mState = new int[0];
    }

    @NonNull
    @Override
    public int[] getState() {
        return mState;
    }

    @Override
    public boolean isStateful() {
        return mState.length > 0;
    }

    @Override
    public boolean setState(@NonNull final int[] pStateSet) {
        mState = pStateSet;
        return true;
    }

    @Deprecated
    public static boolean isDrawableExpired(final Drawable pTile) {
        return getState(pTile) == EXPIRED;
    }

    public static int getState(final Drawable pTile) {
        for (final int statusItem : pTile.getState()) {
            for (final int statusReference : settableStatuses) {
                if (statusItem == statusReference) {
                    return statusItem;
                }
            }
        }
        return defaultStatus;
    }

    /**
     * @deprecated use {@link #setState(Drawable, int)} instead
     */
    @Deprecated
    public static void setDrawableExpired(final Drawable pTile) {
        setState(pTile, EXPIRED);
    }

    public static void setState(final Drawable pTile, final int status) {
        int[] cFound;
        synchronized (mStatusCache) {
            cFound = mStatusCache.get(status);
            if (cFound == null) {
                cFound = new int[]{ status };
                mStatusCache.put(status, cFound);
            }
        }
        pTile.setState(cFound);
    }
}

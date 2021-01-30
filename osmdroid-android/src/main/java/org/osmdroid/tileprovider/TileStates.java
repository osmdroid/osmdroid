package org.osmdroid.tileprovider;

import android.graphics.drawable.Drawable;

import org.osmdroid.views.overlay.TilesOverlay;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * To be used by some kind of {@link TilesOverlay}, in order to get a count of the tiles, by state
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class TileStates {

    private Collection<Runnable> mRunAfters = new LinkedHashSet<>();
    private boolean mDone;
    private int mTotal;
    private int mUpToDate;
    private int mExpired;
    private int mScaled;
    private int mNotFound;

    public Collection<Runnable> getRunAfters() {
        return mRunAfters;
    }

    public void initialiseLoop() {
        mDone = false;
        mTotal = 0;
        mUpToDate = 0;
        mExpired = 0;
        mScaled = 0;
        mNotFound = 0;
    }

    public void finaliseLoop() {
        mDone = true;
        for (final Runnable runnable : mRunAfters) {
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public void handleTile(final Drawable pDrawable) {
        mTotal++;
        if (pDrawable == null) {
            mNotFound++;
        } else {
            final int state = ExpirableBitmapDrawable.getState(pDrawable);
            switch (state) {
                case ExpirableBitmapDrawable.UP_TO_DATE:
                    mUpToDate++;
                    break;
                case ExpirableBitmapDrawable.EXPIRED:
                    mExpired++;
                    break;
                case ExpirableBitmapDrawable.SCALED:
                    mScaled++;
                    break;
                case ExpirableBitmapDrawable.NOT_FOUND:
                    mNotFound++;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown state: " + state);
            }
        }
    }

    public boolean isDone() {
        return mDone;
    }

    public int getTotal() {
        return mTotal;
    }

    public int getUpToDate() {
        return mUpToDate;
    }

    public int getExpired() {
        return mExpired;
    }

    public int getScaled() {
        return mScaled;
    }

    public int getNotFound() {
        return mNotFound;
    }

    @Override
    public String toString() {
        if (mDone) {
            return "TileStates: " + mTotal
                    + " = " + mUpToDate + "(U)"
                    + " + " + mExpired + "(E)"
                    + " + " + mScaled + "(S)"
                    + " + " + mNotFound + "(N)";
        }
        return "TileStates";
    }
}

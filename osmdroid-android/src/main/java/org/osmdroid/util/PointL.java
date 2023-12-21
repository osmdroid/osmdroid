package org.osmdroid.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A {@link android.graphics.Point} with coordinates in long type instead of int
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class PointL {

    public long x;
    public long y;

    public PointL() {
    }

    public PointL(final long pX, final long pY) {
        x = pX;
        y = pY;
    }

    public PointL(@NonNull final PointL pOther) {
        set(pOther);
    }

    /**
     * @since 6.0.0
     */
    public void set(@NonNull final PointL pOther) {
        x = pOther.x;
        y = pOther.y;
    }

    /**
     * @since 6.0.0
     */
    public void set(final long pX, final long pY) {
        x = pX;
        y = pY;
    }

    /**
     * @since 6.0.0
     */
    public final void offset(long dx, long dy) {
        x += dx;
        y += dy;
    }

    /**
     * @since 6.0.0
     */
    @Override
    public String toString() {
        return "PointL(" + x + ", " + y + ")";
    }

    /**
     * @since 6.0.0
     */
    @Override
    public boolean equals(@Nullable final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PointL)) {
            return false;
        }
        final PointL other = (PointL) object;
        return x == other.x && y == other.y;
    }
}

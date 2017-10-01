package org.osmdroid.util;

/**
 * A {@link android.graphics.Point} with coordinates in long type instead of int
 * @since 5.6.6
 * @author Fabrice Fontaine
 */

public class PointL {

    public long x;
    public long y;

    public PointL() {}

    public PointL(final long pX, final long pY) {
        x = pX;
        y = pY;
    }
}

package org.osmdroid.util;

/**
 * A {@link android.graphics.Rect} with corners in long type instead of int
 * @since 5.6.6
 * @author Fabrice Fontaine
 */

public class RectL {

    public long left;
    public long top;
    public long right;
    public long bottom;

    public RectL() {}

    public RectL(final long pLeft, final long pTop, final long pRight, final long pBottom) {
        set(pLeft, pTop, pRight, pBottom);
    }

    public RectL(final RectL pOther) {
        set(pOther);
    }

    public void set(final long pLeft, final long pTop, final long pRight, final long pBottom) {
        left = pLeft;
        top = pTop;
        right = pRight;
        bottom = pBottom;
    }

    public void set(final RectL pOther) {
        left = pOther.left;
        top = pOther.top;
        right = pOther.right;
        bottom = pOther.bottom;
    }

    public void union(long x, long y) {
        if (x < left) {
            left = x;
        } else if (x > right) {
            right = x;
        }
        if (y < top) {
            top = y;
        } else if (y > bottom) {
            bottom = y;
        }
    }

    public static boolean intersects(RectL a, RectL b) {
        return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
    }

    public final long width() {
        return right - left;
    }

    public final long height() {
        return bottom - top;
    }

    @Override
    public String toString() {
        return "RectL(" +left+", "+top+" - "+right+", "+bottom+")";
    }
}

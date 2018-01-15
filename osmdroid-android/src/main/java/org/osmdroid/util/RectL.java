package org.osmdroid.util;

/**
 * A {@link android.graphics.Rect} with corners in long type instead of int
 * @since 6.0.0
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

    /**
     * Returns true if (x,y) is inside the rectangle. Left and top coordinates are considered
     * inside the bounds, while right and bottom are not.
     *
     * @since 6.0.0
     */
    public boolean contains(long x, long y) {
        return left < right && top < bottom && x >= left && x < right && y >= top && y < bottom;
    }

    /**
     * @since 6.0.0
     */
    public void inset(long dx, long dy) {
        left += dx;
        top += dy;
        right -= dx;
        bottom -= dy;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RectL r = (RectL) o;
        return left == r.left && top == r.top && right == r.right && bottom == r.bottom;
    }

    @Override
    public int hashCode() {
        long result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return (int) (result % Integer.MAX_VALUE);
    }
}

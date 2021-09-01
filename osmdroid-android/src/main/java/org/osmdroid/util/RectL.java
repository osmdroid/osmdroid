package org.osmdroid.util;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * A {@link android.graphics.Rect} with corners in long type instead of int
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class RectL {

    public long left;
    public long top;
    public long right;
    public long bottom;

    public RectL() {
    }

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
        return "RectL(" + left + ", " + top + " - " + right + ", " + bottom + ")";
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

    /**
     * Rough computation of the smaller {@link RectL} that contains a rotated {@link RectL}
     * Emulating {@link Canvas#getClipBounds(Rect)} after a canvas rotation
     * The code is supposed to be exactly the same as the Rect version, except for int/long
     *
     * @since 6.0.2
     */
    public static RectL getBounds(final RectL pIn,
                                  final long pCenterX, final long pCenterY, final double pDegrees,
                                  final RectL pReuse) {
        final RectL out = pReuse != null ? pReuse : new RectL();
        if (pDegrees == 0) { // optimization
            out.top = pIn.top;
            out.left = pIn.left;
            out.bottom = pIn.bottom;
            out.right = pIn.right;
            return out;
        }
        final double radians = pDegrees * Math.PI / 180.;
        final double cos = Math.cos(radians);
        final double sin = Math.sin(radians);
        long inputX;
        long inputY;
        long outputX;
        long outputY;
        inputX = pIn.left; // corner 1
        inputY = pIn.top;
        outputX = getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        out.top = out.bottom = outputY;
        out.left = out.right = outputX;
        inputX = pIn.right; // corner 2
        inputY = pIn.top;
        outputX = getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        if (out.top > outputY) {
            out.top = outputY;
        }
        if (out.bottom < outputY) {
            out.bottom = outputY;
        }
        if (out.left > outputX) {
            out.left = outputX;
        }
        if (out.right < outputX) {
            out.right = outputX;
        }
        inputX = pIn.right; // corner 3
        inputY = pIn.bottom;
        outputX = getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        if (out.top > outputY) {
            out.top = outputY;
        }
        if (out.bottom < outputY) {
            out.bottom = outputY;
        }
        if (out.left > outputX) {
            out.left = outputX;
        }
        if (out.right < outputX) {
            out.right = outputX;
        }
        inputX = pIn.left; // corner 4
        inputY = pIn.bottom;
        outputX = getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        if (out.top > outputY) {
            out.top = outputY;
        }
        if (out.bottom < outputY) {
            out.bottom = outputY;
        }
        if (out.left > outputX) {
            out.left = outputX;
        }
        if (out.right < outputX) {
            out.right = outputX;
        }
        return out;
    }

    /**
     * Rough computation of the smaller {@link Rect} that contains a rotated {@link Rect}
     * Emulating {@link Canvas#getClipBounds(Rect)} after a canvas rotation
     * The code is supposed to be exactly the same as the RectL version, except for int/long
     * The code is written to run as fast as possible because it's constantly used when drawing markers
     *
     * @since 6.0.2
     */
    public static Rect getBounds(final Rect pIn,
                                 final int pCenterX, final int pCenterY, final double pDegrees,
                                 final Rect pReuse) {
        final Rect out = pReuse != null ? pReuse : new Rect();
        if (pDegrees == 0) { // optimization
            out.top = pIn.top;
            out.left = pIn.left;
            out.bottom = pIn.bottom;
            out.right = pIn.right;
            return out;
        }
        final double radians = pDegrees * Math.PI / 180.;
        final double cos = Math.cos(radians);
        final double sin = Math.sin(radians);
        int inputX;
        int inputY;
        int outputX;
        int outputY;
        inputX = pIn.left; // corner 1
        inputY = pIn.top;
        outputX = (int) getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = (int) getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        out.top = out.bottom = outputY;
        out.left = out.right = outputX;
        inputX = pIn.right; // corner 2
        inputY = pIn.top;
        outputX = (int) getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = (int) getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        if (out.top > outputY) {
            out.top = outputY;
        }
        if (out.bottom < outputY) {
            out.bottom = outputY;
        }
        if (out.left > outputX) {
            out.left = outputX;
        }
        if (out.right < outputX) {
            out.right = outputX;
        }
        inputX = pIn.right; // corner 3
        inputY = pIn.bottom;
        outputX = (int) getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = (int) getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        if (out.top > outputY) {
            out.top = outputY;
        }
        if (out.bottom < outputY) {
            out.bottom = outputY;
        }
        if (out.left > outputX) {
            out.left = outputX;
        }
        if (out.right < outputX) {
            out.right = outputX;
        }
        inputX = pIn.left; // corner 4
        inputY = pIn.bottom;
        outputX = (int) getRotatedX(inputX, inputY, pCenterX, pCenterY, cos, sin);
        outputY = (int) getRotatedY(inputX, inputY, pCenterX, pCenterY, cos, sin);
        if (out.top > outputY) {
            out.top = outputY;
        }
        if (out.bottom < outputY) {
            out.bottom = outputY;
        }
        if (out.left > outputX) {
            out.left = outputX;
        }
        if (out.right < outputX) {
            out.right = outputX;
        }
        return out;
    }

    /**
     * Apply a rotation on a point and get the resulting X
     *
     * @since 6.0.2
     */
    public static long getRotatedX(final long pX, final long pY,
                                   final double pDegrees, final long pCenterX, final long pCenterY) {
        if (pDegrees == 0) { // optimization
            return pX;
        }
        final double radians = pDegrees * Math.PI / 180.;
        return getRotatedX(pX, pY, pCenterX, pCenterY, Math.cos(radians), Math.sin(radians));
    }

    /**
     * Apply a rotation on a point and get the resulting Y
     *
     * @since 6.0.2
     */
    public static long getRotatedY(final long pX, final long pY,
                                   final double pDegrees, final long pCenterX, final long pCenterY) {
        if (pDegrees == 0) { // optimization
            return pY;
        }
        final double radians = pDegrees * Math.PI / 180.;
        return getRotatedY(pX, pY, pCenterX, pCenterY, Math.cos(radians), Math.sin(radians));
    }

    /**
     * Apply a rotation on a point and get the resulting X
     *
     * @since 6.0.2
     */
    public static long getRotatedX(final long pX, final long pY,
                                   final long pCenterX, final long pCenterY,
                                   final double pCos, final double pSin) {
        return pCenterX + Math.round((pX - pCenterX) * pCos - (pY - pCenterY) * pSin);
    }

    /**
     * Apply a rotation on a point and get the resulting Y
     *
     * @since 6.0.2
     */
    public static long getRotatedY(final long pX, final long pY,
                                   final long pCenterX, final long pCenterY,
                                   final double pCos, final double pSin) {
        return pCenterY + Math.round((pX - pCenterX) * pSin + (pY - pCenterY) * pCos);
    }

    /**
     * @since 6.0.2
     */
    public void offset(final long pDx, final long pDy) {
        left += pDx;
        top += pDy;
        right += pDx;
        bottom += pDy;
    }

    /**
     * @since 6.0.2
     */
    public void union(final long pLeft, final long pTop, final long pRight, final long pBottom) {
        if ((pLeft < pRight) && (pTop < pBottom)) {
            if ((left < right) && (top < bottom)) {
                if (left > pLeft) left = pLeft;
                if (top > pTop) top = pTop;
                if (right < pRight) right = pRight;
                if (bottom < pBottom) bottom = pBottom;
            } else {
                left = pLeft;
                top = pTop;
                right = pRight;
                bottom = pBottom;
            }
        }
    }

    /**
     * @since 6.0.2
     */
    public void union(final RectL pRect) {
        union(pRect.left, pRect.top, pRect.right, pRect.bottom);
    }

    /**
     * @since 6.1.1
     */
    public long centerX() {
        return (left + right) / 2;
    }

    /**
     * @since 6.1.1
     */
    public long centerY() {
        return (top + bottom) / 2;
    }
}

package org.osmdroid.util;

/**
 * Computes a map tile index as `long` to/from zoom/x/y
 * Algorithm unfortunately different from SqlTileWriter.getIndex for historical reasons.
 * This version is better, because it's easy to get zoom, X and Y back from the index.
 * This version is limited to zooms between 0 and 29, which should be enough.
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class MapTileIndex {

    public static int mMaxZoomLevel = TileSystem.primaryKeyMaxZoomLevel;
    private static int mModulo = 1 << mMaxZoomLevel;

    public static long getTileIndex(final int pZoom, final int pX, final int pY) {
        checkValues(pZoom, pX, pY);
        return (((long) pZoom) << (mMaxZoomLevel * 2))
                + (((long) pX) << mMaxZoomLevel)
                + (long) pY;
    }

    public static int getZoom(final long pTileIndex) {
        return (int) (pTileIndex >> (mMaxZoomLevel * 2));
    }

    public static int getX(final long pTileIndex) {
        return (int) ((pTileIndex >> mMaxZoomLevel) % mModulo);
    }

    public static int getY(final long pTileIndex) {
        return (int) (pTileIndex % mModulo);
    }

    /**
     * @since 6.0.0
     */
    public static String toString(final int pZoom, final int pX, final int pY) {
        return "/" + pZoom + "/" + pX + "/" + pY;
    }

    /**
     * @since 6.0.0
     */
    public static String toString(final long pIndex) {
        return toString(getZoom(pIndex), getX(pIndex), getY(pIndex));
    }

    /**
     * @since 6.0.0
     */
    private static void checkValues(final int pZoom, final int pX, final int pY) {
        if (pZoom < 0 || pZoom > mMaxZoomLevel) {
            throwIllegalValue(pZoom, pZoom, "Zoom");
        }
        final long max = 1 << pZoom;
        if (pX < 0 || pX >= max) {
            throwIllegalValue(pZoom, pX, "X");
        }
        if (pY < 0 || pY >= max) {
            throwIllegalValue(pZoom, pY, "Y");
        }
    }

    /**
     * @since 6.0.0
     */
    private static void throwIllegalValue(final int pZoom, final int pValue, final String pTag) {
        throw new IllegalArgumentException(
                "MapTileIndex: " + pTag + " (" + pValue + ") is too big (zoom=" + pZoom + ")");
    }
}

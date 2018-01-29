package org.osmdroid.util;

import org.osmdroid.tileprovider.MapTile;

import microsoft.mappoint.TileSystem;

/**
 * Computes a map tile index as `long` to/from zoom/x/y
 * Algorithm unfortunately different from SqlTileWriter.getIndex for historical reasons.
 * This version is better, because it's easy to get zoom, X and Y back from the index.
 * This version is limited to zooms between 0 and 29, which should be enough.
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class MapTileIndex {

    private static int mMaxZoomLevel = TileSystem.primaryKeyMaxZoomLevel;
    private static int mModulo = 1 << mMaxZoomLevel;

    public static long getTileIndex(final int pZoom, final int pX, final int pY) {
        return (((long)pZoom) << (mMaxZoomLevel * 2))
                + (((long)pX) << mMaxZoomLevel)
                + (long)pY;
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
}

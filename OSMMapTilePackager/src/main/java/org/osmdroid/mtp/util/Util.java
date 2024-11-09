// Created by plusminus on 10:15:51 PM - Mar 5, 2009
package org.osmdroid.mtp.util;

import org.osmdroid.mtp.adt.OSMTileInfo;

public class Util {


    public static final double MinLatitude = -85.05112877980658;
    public static final double MaxLatitude = 85.05112877980658;
    public static final double MinLongitude = -180;
    public static final double MaxLongitude = 180;
    /**
     * For a description see:
     * see <a href="http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames">http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames</a>
     * For a code-description see:
     * see <a href="http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames#compute_bounding_box_for_tile_number">http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames#compute_bounding_box_for_tile_number</a>
     *
     * @param aLat latitude to get the {@link OSMTileInfo} for.
     * @param aLon longitude to get the {@link OSMTileInfo} for.
     * @return The {@link OSMTileInfo} providing 'x' 'y' and 'z'(oom) for the coordinates passed.
     */
    public static OSMTileInfo getMapTileFromCoordinates(final double aLat, final double aLon, final int zoom) {
        final int y = getTileYFromLatitude(aLat, zoom);
        final int x = getTileXFromLongitude(aLon, zoom);
        return new OSMTileInfo(x, y,zoom);
    }
    /**
     * @since 6.0.3
     */
    public static int getTileXFromLongitude(final double pLongitude, final int pZoom) {
        return clipTile((int) Math.floor(getX01FromLongitude(pLongitude) * (1 << pZoom)), pZoom);
    }

    public static double getX01FromLongitude(final double pLongitude) {
        return (pLongitude - getMinLongitude()) / (getMaxLongitude() - getMinLongitude());
    }


    public static double getY01FromLatitude(final double pLatitude) {
        final double sinus = Math.sin(pLatitude * Math.PI / 180);
        return 0.5 - Math.log((1 + sinus) / (1 - sinus)) / (4 * Math.PI);
    }


    public static double getLongitudeFromX01(final double pX01) {
        return getMinLongitude() + (getMaxLongitude() - getMinLongitude()) * pX01;
    }


    public static double getLatitudeFromY01(final double pY01) {
        return 90 - 360 * Math.atan(Math.exp((pY01 - 0.5) * 2 * Math.PI)) / Math.PI;
    }


    public static double getMinLatitude() {
        return MinLatitude;
    }


    public static double getMaxLatitude() {
        return MaxLatitude;
    }


    public static double getMinLongitude() {
        return MinLongitude;
    }


    public static double getMaxLongitude() {
        return MaxLongitude;
    }
    /**
     * @since 6.0.3
     */
    private static int clipTile(final int pTile, final int pZoom) {
        if (pTile < 0) {
            return 0;
        }
        final int max = 1 << pZoom;
        if (pTile >= max) {
            return max - 1;
        }
        return pTile;
    }
    /**
     * @since 6.0.3
     */
    public static int getTileYFromLatitude(final double pLatitude, final int pZoom) {
        return clipTile((int) Math.floor(getY01FromLatitude(pLatitude) * (1 << pZoom)), pZoom);
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}

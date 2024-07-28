// Created by plusminus on 10:15:51 PM - Mar 5, 2009
package org.osmdroid.mtp.util;

import androidx.annotation.Nullable;

import org.osmdroid.mtp.adt.OSMTileInfo;

public class Util {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

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
        final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1 / Math.cos(aLat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
        final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));

        return new OSMTileInfo(x, y, zoom);
    }

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
    public static OSMTileInfo getMapTileFromCoordinates(final double aLat, final double aLon, final int zoom, @Nullable final OSMTileInfo outAndReusableTileInfo) {
        final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1 / Math.cos(aLat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
        final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));

        if (outAndReusableTileInfo != null) {
            outAndReusableTileInfo.set(x, y, zoom);
            return outAndReusableTileInfo;
        }
        return new OSMTileInfo(x, y, zoom);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}

package org.osmdroid.tileprovider.tilesource;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;

import java.io.InputStream;

public interface ITileSource {

    /**
     * An ordinal identifier for this tile source
     *
     * @return the ordinal value
     */
    @Deprecated
    int ordinal();

    /**
     * A human-friendly name for this tile source
     *
     * @return the tile source name
     */
    String name();


    /**
     * Get a unique file path for the tile. This file path may be used to store the tile on a file
     * system and performance considerations should be taken into consideration. It can include
     * multiple paths. It should not begin with a leading path separator.
     *
     * @param pMapTileIndex the tile
     * @return the unique file path
     */
    String getTileRelativeFilenameString(final long pMapTileIndex);

    /**
     * Get a rendered Drawable from the specified file path.
     *
     * @param aFilePath a file path
     * @return the rendered Drawable
     */
    Drawable getDrawable(String aFilePath) throws LowMemoryException;

    /**
     * Get a rendered Drawable from the specified InputStream.
     *
     * @param aTileInputStream an InputStream
     * @return the rendered Drawable
     */
    Drawable getDrawable(InputStream aTileInputStream) throws LowMemoryException;

    /**
     * Get the minimum zoom level this tile source can provide.
     *
     * @return the minimum zoom level
     */
    public int getMinimumZoomLevel();

    /**
     * Get the maximum zoom level this tile source can provide.
     *
     * @return the maximum zoom level
     */
    public int getMaximumZoomLevel();

    /**
     * Get the tile size in pixels this tile source provides.
     *
     * @return the tile size in pixels
     */
    public int getTileSizePixels();

    /**
     * Returns an I18N sensitive string representing the copy right notice (if any) of the tile source
     *
     * @return a string or null
     * @since 5.6.1
     */
    String getCopyrightNotice();
}

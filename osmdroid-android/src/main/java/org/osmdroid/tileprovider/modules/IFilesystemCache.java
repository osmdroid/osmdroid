package org.osmdroid.tileprovider.modules;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.InputStream;

import androidx.annotation.Nullable;

/**
 * Represents a write-only interface into a file system cache.
 *
 * @author Marc Kurtz
 */
public interface IFilesystemCache {
    /**
     * Save an InputStream as the specified tile in the file system cache for the specified tile
     * source.
     *
     * @param pTileSourceInfo a tile source
     * @param pMapTileIndex   a tile
     * @param pStream         an InputStream
     */
    boolean saveFile(ITileSource pTileSourceInfo, long pMapTileIndex,
                     InputStream pStream, Long pExpirationTime);

    /**
     * return true if the map file for download already exists
     */
    boolean exists(ITileSource pTileSourceInfo, long pMapTileIndex);

    /** Used when the map engine is shutdown, use it to perform any clean up activities and to terminate any background threads */
    void onDetach(@Nullable Context context);

    /**
     * Removes a tile from the cache, see issue
     * <a href="https://github.com/osmdroid/osmdroid/issues/426">...</a>
     *
     * @return true if it was removed, false otherwise
     * @since 5.4.2
     */
    boolean remove(ITileSource tileSource, long pMapTileIndex);

    /**
     * Gets the cache expiration timestamp of a tile
     *
     * @return cache expiration timestamp in time since UTC epoch (in milliseconds),
     * or null if expiration timestamp is not supported or if the tile is not cached
     * @since 5.6.5
     */
    Long getExpirationTimestamp(ITileSource pTileSource, long pMapTileIndex);

    /**
     * Gets the tile drawable
     *
     * @since 6.0.0
     */
    Drawable loadTile(ITileSource pTileSource, long pMapTileIndex) throws Exception;
}

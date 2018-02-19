package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;

import java.io.InputStream;

import org.osmdroid.tileprovider.tilesource.ITileSource;

/**
 * Represents a write-only interface into a file system cache.
 *
 * @author Marc Kurtz
 *
 */
public interface IFilesystemCache {
	/**
	 * Save an InputStream as the specified tile in the file system cache for the specified tile
	 * source.
	 *
	 * @param pTileSourceInfo
	 *            a tile source
	 * @param pMapTileIndex
	 *            a tile
	 * @param pStream
	 *            an InputStream
	 * @return
	 */
	boolean saveFile(final ITileSource pTileSourceInfo, final long pMapTileIndex,
					 final InputStream pStream, final Long pExpirationTime);

	/**
	 * return true if the map file for download already exists
	 * @return
	 */
	boolean exists(final ITileSource pTileSourceInfo, final long pMapTileIndex);

	/**
	 * Used when the map engine is shutdown, use it to perform any clean up activities and to terminate
	 * any background threads
	 * @since 5.3
	 */
	void onDetach();

	/**
	 * Removes a tile from the cache, see issue
	 * https://github.com/osmdroid/osmdroid/issues/426
	 *
	 * @since 5.4.2
	 * @return true if it was removed, false otherwise
	 */
	boolean remove(ITileSource tileSource, final long pMapTileIndex);

	/**
	 * Gets the cache expiration timestamp of a tile
	 *
	 * @since 5.6.5
	 * @return cache expiration timestamp in time since UTC epoch (in milliseconds),
	 * or null if expiration timestamp is not supported or if the tile is not cached
	 */
	Long getExpirationTimestamp(final ITileSource pTileSource, final long pMapTileIndex);

	/**
	 * Gets the tile drawable
	 *
	 * @since 6.0.0
	 */
	Drawable loadTile(final ITileSource pTileSource, final long pMapTileIndex) throws Exception;
}

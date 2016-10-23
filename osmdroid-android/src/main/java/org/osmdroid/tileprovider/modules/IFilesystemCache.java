package org.osmdroid.tileprovider.modules;

import java.io.InputStream;

import org.osmdroid.tileprovider.MapTile;
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
	 * @param pTile
	 *            a tile
	 * @param pStream
	 *            an InputStream
	 * @return
	 */
	boolean saveFile(final ITileSource pTileSourceInfo, MapTile pTile,
			final InputStream pStream);

	/**
	 * return true if the map file for download already exists
	 * @param pTileSourceInfo
	 * @param pTile
     * @return
     */
	boolean exists(final ITileSource pTileSourceInfo, final MapTile pTile );

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
	 * @param tileSource
	 * @param tile
	 * @return true if it was removed, false otherwise
     */
	boolean remove(ITileSource tileSource, MapTile tile);
}

package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.osmdroid.tileprovider.tilesource.ITileSource;

/**
 * The IArchiveFile is primary used to load tiles from a file archive. Generally, this should only
 * be used for archives that require little to no computation in order to provide a given tile.
 *
 * For cases thereby the tiles are rendered or manipulated (such as from another projection)
 * @see MapTileModuleProviderBase
 * @see ArchiveFileFactory
 */
public interface IArchiveFile {

	/**
	 * initialize the file archive, such as performing initial scans, queries, opening a database, etc
	 * @param pFile
	 * @throws Exception
	 */
	void init(File pFile) throws Exception;

	/**
	 * Get the input stream for the requested tile and tile source.
	 *
	 * Also keep in mind that the tile source has an explicit tile size in pixels, and tile source name.
	 *
	 * @see org.osmdroid.tileprovider.tilesource.TileSourceFactory
	 * @return the input stream, or null if the archive doesn't contain an entry for the requested tile.
	 */
	InputStream getInputStream(final ITileSource tileSource, final long pMapTileIndex);

	/**
	 * Closes the archive file and releases resources.
	 */
	void close();

	/**
	 * returns a list of tile source names that are available in the archive, if supported. If
	 * not supported, return an empty set
	 * @since 5.0
	 * @return
	 */
	public Set<String> getTileSources();


	/**
	 * @since 6.0
	 * If set to true, tiles from this archive will be loaded regardless of their associated tile source name
	 */
	public void setIgnoreTileSource(boolean pIgnoreTileSource);

}

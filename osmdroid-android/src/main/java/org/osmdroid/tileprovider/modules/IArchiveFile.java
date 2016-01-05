package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

public interface IArchiveFile {

	void init(File pFile) throws Exception;

	/**
	 * Get the input stream for the requested tile.
	 * @return the input stream, or null if the archive doesn't contain an entry for the requested tile
	 */
	InputStream getInputStream(ITileSource tileSource, MapTile tile);

	/**
	 * Closes the archive file and releases resources.
	 */
	void close();

	/**
	 * returns a list of tile source names that are available in the archive, if supported
	 * @since 5.0
	 * @return
	 */
	public Set<String> getTileSources();

}

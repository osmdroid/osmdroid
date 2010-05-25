// Created by plusminus on 21:46:41 - 25.09.2008
package org.andnav.osm.tileprovider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.util.Log;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapTileFilesystemProvider extends OpenStreetMapAsyncTileProvider {
	
	// ===========================================================
	// Constants
	// ===========================================================

	final static String DEBUGTAG = "OSM_FS_PROVIDER";

	// ===========================================================
	// Fields
	// ===========================================================

	/** online provider */
	protected OpenStreetMapTileDownloader mTileDownloader;

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * @param ctx
	 * @param aCache to load fs-tiles to.
	 */
	public OpenStreetMapTileFilesystemProvider(final IOpenStreetMapTileProviderCallback pCallback) {
		super(pCallback, NUMBER_OF_TILE_FILESYSTEM_THREADS, TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
		this.mTileDownloader = new OpenStreetMapTileDownloader(pCallback, this);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected String debugtag() {
		return DEBUGTAG;
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	};
	
	
	/**
	 * Stops all workers, the service is shutting down.
	 */
	@Override
	public void stopWorkers()
	{
		super.stopWorkers();
		this.mTileDownloader.stopWorkers();
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	private String buildPath(final OpenStreetMapTile tile) {
		final OpenStreetMapRendererInfo renderer = OpenStreetMapRendererInfo.values()[tile.getRendererId()];
		return TILE_PATH_BASE + renderer.name() + "/" + tile.getZoomLevel() + "/"
					+ tile.getX() + "/" + tile.getY() + renderer.IMAGE_FILENAMEENDING + TILE_PATH_EXTENSION; 
	}
	
	/**
	 * Get the file location for the tile.
	 * @param tile
	 * @return 
	 * @throws CantContinueException if the directory containing the file doesn't exist
	 * and can't be created
	 */
	File getOutputFile(final OpenStreetMapTile tile) throws CantContinueException {
		final File file = new File(buildPath(tile));
		final File parent = file.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new CantContinueException();
		}
		return file;
	}
	
	void saveFile(final OpenStreetMapTile tile, final File outputFile, final byte[] someData) throws IOException{
		final OutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile, false), StreamUtils.IO_BUFFER_SIZE);		
		bos.write(someData);
		bos.flush();
		bos.close();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class TileLoader extends OpenStreetMapAsyncTileProvider.TileLoader {

		@Override
		public void loadTile(final OpenStreetMapTile aTile) throws CantContinueException {
			final File tileFile = getOutputFile(aTile);
			try {
				if (tileFile.exists()) {
					if (DEBUGMODE)
						Log.d(DEBUGTAG, "Loaded tile: " + aTile);
					tileLoaded(aTile, tileFile.getPath(), true);
				} else {
					if (DEBUGMODE)
						Log.d(DEBUGTAG, "Tile not exist, request for download: " + aTile);
					mTileDownloader.loadMapTileAsync(aTile);
					// don't refresh the screen because there's nothing new
					tileLoaded(aTile, null, false);
				}
			} catch (final Throwable e) {
				Log.e(DEBUGTAG, "Error loading tile", e);
				tileLoaded(aTile, null, false);
			}
		}
	}
}

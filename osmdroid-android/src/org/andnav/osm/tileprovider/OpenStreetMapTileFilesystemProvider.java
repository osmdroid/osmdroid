// Created by plusminus on 21:46:41 - 25.09.2008
package org.andnav.osm.tileprovider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapTileFilesystemProvider extends OpenStreetMapAsyncTileProvider {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapTileFilesystemProvider.class);

	// ===========================================================
	// Fields
	// ===========================================================

	/** online provider */
	protected final OpenStreetMapTileDownloader mTileDownloader;

	private final ArrayList<ZipFile> mZipFiles = new ArrayList<ZipFile>();

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
		findZipFiles();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected String threadGroupName() {
		return "filesystem";
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

	private String buildFullPath(final OpenStreetMapTile tile) {
		return TILE_PATH_BASE + buildPath(tile);
	}

	private String buildPath(final OpenStreetMapTile tile) {
		final IOpenStreetMapRendererInfo renderer = tile.getRenderer();
		return renderer.name() + "/" + tile.getZoomLevel() + "/"
					+ tile.getX() + "/" + tile.getY() + renderer.imageFilenameEnding() + TILE_PATH_EXTENSION;
	}

	/**
	 * Get the file location for the tile.
	 * @param tile
	 * @return
	 * @throws CantContinueException if the directory containing the file doesn't exist
	 * and can't be created
	 */
	File getOutputFile(final OpenStreetMapTile tile) throws CantContinueException {
		final File file = new File(buildFullPath(tile));
		final File parent = file.getParentFile();
		// check exists twice because maybe mkdirs returned false because another thread created it
		if (!parent.exists() && !parent.mkdirs() && !parent.exists()) {
			throw new CantContinueException("Tile directory doesn't exist: " + parent);
		}
		return file;
	}

	void saveFile(final OpenStreetMapTile tile, final File outputFile, final byte[] someData) throws IOException{
		final OutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile, false), StreamUtils.IO_BUFFER_SIZE);
		bos.write(someData);
		bos.flush();
		bos.close();
	}

	private void findZipFiles() {
		final File baseFolder = new File(TILE_PATH_BASE);

		final File[] z = baseFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File aFile) {
				return aFile.isFile() && aFile.getName().endsWith(".zip");
			}
		});

		for(final File file : z) {
			try {
				mZipFiles.add(new ZipFile(file));
			} catch(final Throwable e) {
				logger.warn("Error opening zip file: " + file, e);
			}
		}
	}

	private synchronized InputStream fileFromZip(final OpenStreetMapTile aTile) {
		final String path = buildPath(aTile);
		for(final ZipFile zipFile : mZipFiles) {
			try {
				final ZipEntry entry = zipFile.getEntry(path);
				if (entry != null) {
					final InputStream in = zipFile.getInputStream(entry);
					return in;
				}
			} catch(final Throwable e) {
				logger.warn("Error getting zip stream: " + aTile, e);
			}
		}

		return null;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class TileLoader extends OpenStreetMapAsyncTileProvider.TileLoader {

		@Override
		public void loadTile(final OpenStreetMapTile aTile) throws CantContinueException {
			final File tileFile = getOutputFile(aTile);

			// TODO need a policy for deciding which file to use, eg:
			// always prefer local file,
			// always prefer zip,
			// prefer local file, but if old use zip
			// prefer local file, but if old use most recent of local and zip
			// ... etc ...

			try {
				if (tileFile.exists()) {
					if (DEBUGMODE)
						logger.debug("Loaded tile: " + aTile);
					tileLoaded(aTile, tileFile.getPath(), true);

					// check for old tile
					final long now = System.currentTimeMillis();
					final long lastModified = tileFile.lastModified();
					if (now - lastModified > TILE_EXPIRY_TIME_MILLISECONDS) {
						logger.info("Tile has expired, requesting new download: " + aTile);
						mTileDownloader.loadMapTileAsync(aTile);
					}

				} else {

					if (DEBUGMODE)
						logger.debug("Tile doesn't exist: " + aTile);

					final InputStream fileFromZip = fileFromZip(aTile);
					if (fileFromZip == null) {
						if (DEBUGMODE)
							logger.debug("Request for download: " + aTile);
						mTileDownloader.loadMapTileAsync(aTile);
						// don't refresh the screen because there's nothing new
						tileLoaded(aTile, (String)null, false);
					} else {
						if (DEBUGMODE)
							logger.debug("Use tile from zip: " + aTile);
						tileLoaded(aTile, fileFromZip, true);
					}
				}
			} catch (final Throwable e) {
				logger.error("Error loading tile", e);
				tileLoaded(aTile, (String)null, false);
			}
		}
	}
}

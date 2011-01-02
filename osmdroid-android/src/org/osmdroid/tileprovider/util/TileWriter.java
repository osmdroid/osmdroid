package org.osmdroid.tileprovider.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.osmdroid.tileprovider.OpenStreetMapTile;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.views.util.IOpenStreetMapRendererInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link IFilesystemCache}. It writes tiles to the file system cache. If the
 * cache exceeds 600 Mb then it will be trimmed to 500 Mb.
 * 
 * @author Neil Boyd
 * 
 */
public class TileWriter implements IFilesystemCache, OpenStreetMapTileProviderConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(TileWriter.class);

	// ===========================================================
	// Fields
	// ===========================================================

	/** amount of disk space used by tile cache **/
	private static long mUsedCacheSpace;

	// ===========================================================
	// Constructors
	// ===========================================================

	public TileWriter() {
		mUsedCacheSpace = calculateDirectorySize(TILE_PATH_BASE);
		if (mUsedCacheSpace > TILE_MAX_CACHE_SIZE_BYTES) {
			cutCurrentCache();
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * Get the amount of disk space used by the tile cache.
	 * 
	 * @return size in bytes
	 */
	public static long getUsedCacheSpace() {
		return mUsedCacheSpace;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean saveFile(final IOpenStreetMapRendererInfo pTileSource,
			final OpenStreetMapTile pTile, final InputStream pStream) {

		final File file = new File(TILE_PATH_BASE, pTileSource.getTileRelativeFilenameString(pTile));

		final File parent = file.getParentFile();
		if (!parent.exists() && !createFolderAndCheckIfExists(parent)) {
			return false;
		}

		BufferedOutputStream outputStream = null;
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(file.getPath()),
					StreamUtils.IO_BUFFER_SIZE);
			final long length = StreamUtils.copy(pStream, outputStream);

			mUsedCacheSpace += length; // XXX should this be synchronized? or is
										// it a single operation?
			if (mUsedCacheSpace > TILE_MAX_CACHE_SIZE_BYTES) {
				cutCurrentCache();
			}
		} catch (final IOException e) {
			return false;
		} finally {
			if (outputStream != null)
				StreamUtils.closeStream(outputStream);
		}
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private boolean createFolderAndCheckIfExists(final File pFile) {
		if (pFile.mkdirs()) {
			return true;
		}
		if (DEBUGMODE)
			logger.debug("Failed to create " + pFile + " - wait and check again");

		// if create failed, wait a bit in case another thread created it
		try {
			Thread.sleep(500);
		} catch (final InterruptedException ignore) {
		}
		// and then check again
		if (pFile.exists()) {
			if (DEBUGMODE)
				logger.debug("Seems like another thread created " + pFile);
			return true;
		} else {
			if (DEBUGMODE)
				logger.debug("File still doesn't exist: " + pFile);
			return false;
		}
	}

	private long calculateDirectorySize(final File aDirectory) {
		long size = 0;

		final File[] z = aDirectory.listFiles();
		if (z != null) {
			for (final File file : z) {
				if (file.isFile()) {
					size += file.length();
				}
				if (file.isDirectory()) {
					size += calculateDirectorySize(file);
				}
			}
		}

		return size;
	}

	private List<File> getDirectoryFileList(final File aDirectory) {
		final List<File> files = new ArrayList<File>();

		final File[] z = aDirectory.listFiles();
		if (z != null) {
			for (final File file : z) {
				if (file.isFile()) {
					files.add(file);
				}
				if (file.isDirectory()) {
					files.addAll(getDirectoryFileList(file));
				}
			}
		}

		return files;
	}

	/**
	 * If the cache size is greater than the max then trim it down to the trim level. This method is
	 * synchronized so that only one thread can run it at a time.
	 */
	private void cutCurrentCache() {

		synchronized (TILE_PATH_BASE) {

			if (mUsedCacheSpace > TILE_TRIM_CACHE_SIZE_BYTES) {

				logger.info("Trimming tile cache from " + mUsedCacheSpace + " to "
						+ TILE_TRIM_CACHE_SIZE_BYTES);

				final List<File> z = getDirectoryFileList(TILE_PATH_BASE);

				// order list by files day created from old to new
				final File[] files = z.toArray(new File[0]);
				Arrays.sort(files, new Comparator<File>() {
					@Override
					public int compare(final File f1, final File f2) {
						return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
					}
				});

				for (final File file : files) {
					if (mUsedCacheSpace <= TILE_TRIM_CACHE_SIZE_BYTES) {
						break;
					}

					final long length = file.length();
					if (file.delete()) {
						mUsedCacheSpace -= length;
					}
				}
			}
		}
	}

}

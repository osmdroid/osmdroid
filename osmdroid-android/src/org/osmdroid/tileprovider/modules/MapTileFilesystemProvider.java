package org.osmdroid.tileprovider.modules;

import java.io.File;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;

/**
 * Implements a file system cache and provides cached tiles. This functions as a tile provider by
 * serving cached tiles for the supplied tile source.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 *
 */
public class MapTileFilesystemProvider extends MapTileFileStorageProviderBase {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(MapTileFilesystemProvider.class);

	// ===========================================================
	// Fields
	// ===========================================================

	private final long mMaximumCachedFileAge;

	private ITileSource mTileSource;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver) {
		this(pRegisterReceiver, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
			final ITileSource aTileSource) {
		this(pRegisterReceiver, aTileSource, DEFAULT_MAXIMUM_CACHED_FILE_AGE);
	}

	/**
	 * Provides a file system based cache tile provider. Other providers can register and store data
	 * in the cache.
	 *
	 * @param pRegisterReceiver
	 */
	public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource, final long pMaximumCachedFileAge) {
		super(pRegisterReceiver, NUMBER_OF_TILE_FILESYSTEM_THREADS,
				TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
		mTileSource = pTileSource;

		mMaximumCachedFileAge = pMaximumCachedFileAge;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean getUsesDataConnection() {
		return false;
	}

	@Override
	protected String getName() {
		return "File System Cache Provider";
	}

	@Override
	protected String getThreadGroupName() {
		return "filesystem";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	};

	@Override
	public int getMinimumZoomLevel() {
		return mTileSource != null ? mTileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL;
	}

	@Override
	public int getMaximumZoomLevel() {
		return mTileSource != null ? mTileSource.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL;
	}

	@Override
	public void setTileSource(final ITileSource pTileSource) {
		mTileSource = pTileSource;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class TileLoader extends MapTileModuleProviderBase.TileLoader {

		@Override
		public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {

			if (mTileSource == null) {
				return null;
			}

			final MapTile tile = pState.getMapTile();

			// if there's no sdcard then don't do anything
			if (!getSdCardAvailable()) {
				if (DEBUGMODE) {
					logger.debug("No sdcard - do nothing for tile: " + tile);
				}
				return null;
			}

			// Check the tile source to see if its file is available and if so, then render the
			// drawable and return the tile
			final File file = new File(TILE_PATH_BASE,
					mTileSource.getTileRelativeFilenameString(tile) + TILE_PATH_EXTENSION);
			if (file.exists()) {

				// Check to see if file has expired
				final long now = System.currentTimeMillis();
				final long lastModified = file.lastModified();
				final boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

				// TODO use ExpiredBitmapDrawable.EXPIRED_IN_FILESYSTEM here
				// and handle that.
				// That probably means I can get rid of tileCandidateLoaded.

				if (!fileExpired) {
					// If the file has not expired, then render it and return it!
					try {
						final Drawable drawable = mTileSource.getDrawable(file.getPath());
						return drawable;
					} catch (final LowMemoryException e) {
						// low memory so empty the queue
						logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
						throw new CantContinueException(e);
					}
				} else {
					// If the file has expired then we render it, but we return it as a candidate
					// and then fail on the request. This allows the tile to be loaded, but also
					// allows other tile providers to do a better job.
					try {
						final Drawable drawable = mTileSource.getDrawable(file.getPath());
						tileCandidateLoaded(pState, drawable);
						return null;
					} catch (final LowMemoryException e) {
						// low memory so empty the queue
						logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
						throw new CantContinueException(e);
					}
				}
			}

			// If we get here then there is no file in the file cache
			return null;
		}
	}
}

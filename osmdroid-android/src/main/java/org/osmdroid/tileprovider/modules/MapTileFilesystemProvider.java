package org.osmdroid.tileprovider.modules;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import android.graphics.drawable.Drawable;
import android.util.Log;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.util.StreamUtils;

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

	// ===========================================================
	// Fields
	// ===========================================================

	private final long mMaximumCachedFileAge;

	private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver) {
		this(pRegisterReceiver, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
			final ITileSource aTileSource) {
		this(pRegisterReceiver, aTileSource, OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE);
	}

	public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource, final long pMaximumCachedFileAge) {
		this(pRegisterReceiver, pTileSource, pMaximumCachedFileAge,
				OpenStreetMapTileProviderConstants.NUMBER_OF_TILE_FILESYSTEM_THREADS,
				OpenStreetMapTileProviderConstants.TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
	}

	/**
	 * Provides a file system based cache tile provider. Other providers can register and store data
	 * in the cache.
	 *
	 * @param pRegisterReceiver
	 */
	public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource, final long pMaximumCachedFileAge, int pThreadPoolSize,
			int pPendingQueueSize) {
		super(pRegisterReceiver, pThreadPoolSize, pPendingQueueSize);
		setTileSource(pTileSource);

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
	}

	@Override
	public int getMinimumZoomLevel() {
		ITileSource tileSource = mTileSource.get();
		return tileSource != null ? tileSource.getMinimumZoomLevel() : OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL;
	}

	@Override
	public int getMaximumZoomLevel() {
		ITileSource tileSource = mTileSource.get();
		return tileSource != null ? tileSource.getMaximumZoomLevel()
				: microsoft.mappoint.TileSystem.getMaximumZoomLevel();
	}

	@Override
	public void setTileSource(final ITileSource pTileSource) {
		mTileSource.set(pTileSource);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

		@Override
		public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {

			ITileSource tileSource = mTileSource.get();
			if (tileSource == null) {
				return null;
			}

			final MapTile tile = pState.getMapTile();

			// if there's no sdcard then don't do anything
			if (!getSdCardAvailable()) {
				if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                         Log.d(IMapView.LOGTAG,"No sdcard - do nothing for tile: " + tile);
				}
				return null;
			}

			// Check the tile source to see if its file is available and if so, then render the
			// drawable and return the tile
			final File file = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE,
					tileSource.getTileRelativeFilenameString(tile) +
						OpenStreetMapTileProviderConstants.TILE_PATH_EXTENSION);

			if (file.exists()) {
				final String tilePath = file.getPath();
				InputStream inputStream = null;
				InputStream propertiesInputStream = null;
				BufferedOutputStream propertiesOutputStream = null;
				try {

					final File propertiesFile = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE,
						tileSource.getTileRelativeFilenameString(tile) +
							OpenStreetMapTileProviderConstants.TILE_PROPERTIES_EXTENSION);

					boolean updatePropertiesFiles = false;
					if(propertiesFile.exists()) {
						propertiesInputStream = new BufferedInputStream(new FileInputStream(propertiesFile),
							StreamUtils.IO_BUFFER_SIZE);
						tile.readProperties(propertiesInputStream);
					} else {
						updatePropertiesFiles = true;
					}

					inputStream = new BufferedInputStream(new FileInputStream(tilePath),
						StreamUtils.IO_BUFFER_SIZE);

					final Drawable drawable = tileSource.getDrawable(tilePath);

					// Check to see if file has expired
					Date tileExpires = tile.getExpires();
					if (tileExpires == null) {
						tileExpires= new Date(file.lastModified() + mMaximumCachedFileAge);
						tile.setExpires(tileExpires);
						updatePropertiesFiles = true;
					}

					final boolean fileExpired = tileExpires.before(new Date());

					if (fileExpired && drawable != null) {
						if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
							Log.d(IMapView.LOGTAG,"Tile expired: " + tile);
						}
						ExpirableBitmapDrawable.setDrawableExpired(drawable);
					}

					if(updatePropertiesFiles) {
						propertiesOutputStream = new BufferedOutputStream(new FileOutputStream(propertiesFile),
							StreamUtils.IO_BUFFER_SIZE);
						tile.writeProperties(propertiesOutputStream);
					}

					return drawable;
				} catch (final LowMemoryException e) {
					// low memory so empty the queue
					Log.w(IMapView.LOGTAG,"LowMemoryException downloading MapTile: " + tile + " : " + e);
					throw new CantContinueException(e);
				} catch (final FileNotFoundException e) {
					Log.e(IMapView.LOGTAG,"FileNotFoundException loading bitmap: " + tilePath);
				} catch (final IOException e) {
					Log.e(IMapView.LOGTAG,"IOException loading bitmap: " + tilePath);
				} finally {
					StreamUtils.closeStream(inputStream);
					StreamUtils.closeStream(propertiesInputStream);
					StreamUtils.closeStream(propertiesOutputStream);
				}
			}

			// If we get here then there is no file in the file cache
			return null;
		}
	}
}

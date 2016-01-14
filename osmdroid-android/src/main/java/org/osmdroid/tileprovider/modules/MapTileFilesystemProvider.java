package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

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
	private DatabaseFileArchive databaseFileArchive;
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
		File db_file = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getAbsolutePath() + File.separator + "cache.db");
		databaseFileArchive = DatabaseFileArchive.getDatabaseFileArchive(db_file);
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

			byte[] bits =databaseFileArchive.getImage(tileSource,tile);
			if (bits != null) {
				try {
					if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
						Log.d(IMapView.LOGTAG, "Use tile from archive: " + tile);
					}
					BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
					BitmapPool.getInstance().applyReusableOptions(bitmapOptions);
					final Bitmap bitmap = BitmapFactory.decodeByteArray(bits,0,bits.length,bitmapOptions);
					if (bitmap != null) {
						return new ReusableBitmapDrawable(bitmap);
					}
				}
				catch (Throwable t){

				}
			}

			// If we get here then there is no file in the file cache
			return null;
		}
	}
}

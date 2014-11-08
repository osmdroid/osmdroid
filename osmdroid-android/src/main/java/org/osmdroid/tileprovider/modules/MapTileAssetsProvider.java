package org.osmdroid.tileprovider.modules;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implements a file system cache and provides cached tiles from Assets. This
 * functions as a tile provider by serving cached tiles for the supplied tile
 * source.
 *
 * tiles should be put into apk's assets directory just like following example:
 *
 *     assets/Mapnik/11/1316/806.png
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Behrooz Shabani (everplays)
 *
 */
public class MapTileAssetsProvider extends MapTileFileStorageProviderBase {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(MapTileAssetsProvider.class);

	// ===========================================================
	// Fields
	// ===========================================================

	private final long mMaximumCachedFileAge;

	private AssetManager mAssets;

	private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver, final AssetManager pAssets) {
		this(pRegisterReceiver, pAssets, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver,
			final AssetManager pAssets,
			final ITileSource aTileSource) {
		this(pRegisterReceiver, pAssets, aTileSource, DEFAULT_MAXIMUM_CACHED_FILE_AGE);
	}

	public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver,
			final AssetManager pAssets,
			final ITileSource pTileSource, final long pMaximumCachedFileAge) {
		this(pRegisterReceiver, pAssets, pTileSource, pMaximumCachedFileAge,
				NUMBER_OF_TILE_FILESYSTEM_THREADS,
				TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
	}

	/**
	 * Provides a file system based cache tile provider. Other providers can register and store data
	 * in the cache.
	 *
	 * @param pRegisterReceiver
	 */
	public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver,
			final AssetManager pAssets,
			final ITileSource pTileSource, final long pMaximumCachedFileAge, int pThreadPoolSize,
			int pPendingQueueSize) {
		super(pRegisterReceiver, pThreadPoolSize, pPendingQueueSize);
		setTileSource(pTileSource);

		mAssets = pAssets;
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
		return "Assets Cache Provider";
	}

	@Override
	protected String getThreadGroupName() {
		return "assets";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader(mAssets);
	}

	@Override
	public int getMinimumZoomLevel() {
		ITileSource tileSource = mTileSource.get();
		return tileSource != null ? tileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL;
	}

	@Override
	public int getMaximumZoomLevel() {
		ITileSource tileSource = mTileSource.get();
		return tileSource != null ? tileSource.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL;
	}

	@Override
	public void setTileSource(final ITileSource pTileSource) {
		mTileSource.set(pTileSource);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	protected class TileLoader extends MapTileModuleProviderBase.TileLoader {
		private AssetManager mAssets = null;

		public TileLoader(AssetManager pAssets) {
			mAssets = pAssets;
		}

		@Override
		public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {
			ITileSource tileSource = mTileSource.get();
			if (tileSource == null) {
				return null;
			}

			final MapTile tile = pState.getMapTile();

			try {
				InputStream is = mAssets.open(tileSource.getTileRelativeFilenameString(tile));
				return tileSource.getDrawable(is);
			} catch (IOException e) {
			} catch (final LowMemoryException e) {
				throw new CantContinueException(e);
			}

			// If we get here then there is no file in the file cache
			return null;
		}
	}
}

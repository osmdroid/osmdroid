package org.osmdroid.tileprovider.modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

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

	private final AssetManager mAssets;

	private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver, final AssetManager pAssets) {
		this(pRegisterReceiver, pAssets, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver,
								 final AssetManager pAssets,
								 final ITileSource pTileSource) {
		this(pRegisterReceiver, pAssets, pTileSource,
				NUMBER_OF_TILE_FILESYSTEM_THREADS,
				TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
	}

	public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver,
								 final AssetManager pAssets,
								 final ITileSource pTileSource, int pThreadPoolSize,
								 int pPendingQueueSize) {
		super(pRegisterReceiver, pThreadPoolSize, pPendingQueueSize);
		setTileSource(pTileSource);

		mAssets = pAssets;
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
				final Drawable drawable = tileSource.getDrawable(is);
				if (drawable != null) {
					ExpirableBitmapDrawable.setDrawableExpired(drawable);
				}
				return drawable;
			} catch (IOException e) {
			} catch (final LowMemoryException e) {
				throw new CantContinueException(e);
			}

			// If we get here then there is no file in the file cache
			return null;
		}
	}
}

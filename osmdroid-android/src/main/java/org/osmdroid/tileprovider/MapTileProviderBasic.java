package org.osmdroid.tileprovider;

import android.content.Context;
import android.os.Build;

import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.MapTileAreaBorderComputer;
import org.osmdroid.util.MapTileAreaZoomComputer;
import org.osmdroid.util.MapTileIndex;

/**
 * This top-level tile provider implements a basic tile request chain which includes a
 * {@link MapTileFilesystemProvider} (a file-system cache), a {@link MapTileFileArchiveProvider}
 * (archive provider), and a {@link MapTileDownloader} (downloads map tiles via tile source).
 * <p>
 * Behavior change since osmdroid 5.3: If the device is less than API 10, the file system based cache and writer are used
 * otherwise, the sqlite based
 *
 * @author Marc Kurtz
 * @see TileWriter
 * @see SqlTileWriter
 * @see MapTileFilesystemProvider
 * @see MapTileSqlCacheProvider
 */
public class MapTileProviderBasic extends MapTileProviderArray implements IMapTileProviderCallback {

	protected IFilesystemCache tileWriter;
	final private INetworkAvailablityCheck mNetworkAvailabilityCheck;

	/**
	 * @since 6.1.0
	 */
	final private MapTileDownloader mDownloaderProvider;
	final private MapTileApproximater mApproximationProvider;

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final Context pContext) {
		this(pContext, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final Context pContext, final ITileSource pTileSource) {
		this(pContext, pTileSource, null);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final Context pContext, final ITileSource pTileSource, final IFilesystemCache cacheWriter) {
		this(new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
				pTileSource, pContext,cacheWriter);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final IRegisterReceiver pRegisterReceiver,
			final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource,
			final Context pContext, final IFilesystemCache cacheWriter) {
		super(pTileSource, pRegisterReceiver);

		mNetworkAvailabilityCheck = aNetworkAvailablityCheck;

		if (cacheWriter != null) {
			tileWriter = cacheWriter;
		} else {
			if (Build.VERSION.SDK_INT < 10) {
				tileWriter = new TileWriter();
			} else {
				tileWriter = new SqlTileWriter();
			}
		}
		final MapTileFileStorageProviderBase assetsProvider =
				createAssetsProvider(pRegisterReceiver, pTileSource, pContext);
		mTileProviderList.add(assetsProvider);

		final MapTileFileStorageProviderBase cacheProvider =
				getMapTileFileStorageProviderBase(pRegisterReceiver, pTileSource, tileWriter);
		mTileProviderList.add(cacheProvider);

		final MapTileFileStorageProviderBase archiveProvider =
				createArchiveProvider(pRegisterReceiver, pTileSource);
		mTileProviderList.add(archiveProvider);

		mApproximationProvider =
				createApproximater(assetsProvider, cacheProvider, archiveProvider);
		mTileProviderList.add(mApproximationProvider);

		mDownloaderProvider =
				createDownloaderProvider(aNetworkAvailablityCheck, pTileSource);
		mTileProviderList.add(mDownloaderProvider);

		// protected-cache-tile computers
		getTileCache().getProtectedTileComputers().add(new MapTileAreaZoomComputer(-1));
		getTileCache().getProtectedTileComputers().add(new MapTileAreaBorderComputer(1));
		getTileCache().setAutoEnsureCapacity(false);
		getTileCache().setStressedMemory(false);

		// pre-cache providers
		getTileCache().getPreCache().addProvider(assetsProvider);
		getTileCache().getPreCache().addProvider(cacheProvider);
		getTileCache().getPreCache().addProvider(archiveProvider);
		getTileCache().getPreCache().addProvider(mDownloaderProvider);

		// tiles currently being processed
		getTileCache().getProtectedTileContainers().add(this);

		setOfflineFirst(true);
	}

	protected MapTileApproximater createApproximater(MapTileFileStorageProviderBase assetsProvider, MapTileFileStorageProviderBase cacheProvider, MapTileFileStorageProviderBase archiveProvider) {
		MapTileApproximater approximationProvider = new MapTileApproximater();
		approximationProvider.addProvider(assetsProvider);
		approximationProvider.addProvider(cacheProvider);
		approximationProvider.addProvider(archiveProvider);
		return approximationProvider;
	}

	protected MapTileFileStorageProviderBase createArchiveProvider(IRegisterReceiver pRegisterReceiver, ITileSource pTileSource) {
		return new MapTileFileArchiveProvider(
				pRegisterReceiver, pTileSource);
	}

	protected MapTileFileStorageProviderBase createAssetsProvider(IRegisterReceiver pRegisterReceiver, ITileSource pTileSource, Context pContext) {
		return new MapTileAssetsProvider(
				pRegisterReceiver, pContext.getAssets(), pTileSource);
	}

	@Override
	public IFilesystemCache getTileWriter() {
		return tileWriter;
	}

	/**
	 * @since 6.1.7
	 * allow customization of tile downloader per tile source
	 */
	protected MapTileDownloader createDownloaderProvider(INetworkAvailablityCheck aNetworkAvailablityCheck, ITileSource pTileSource) {
		return new MapTileDownloader(pTileSource, this.tileWriter, aNetworkAvailablityCheck);
	}

	@Override
	public void detach(){
		//https://github.com/osmdroid/osmdroid/issues/213
		//close the writer
		if (tileWriter!=null)
			tileWriter.onDetach();
		tileWriter=null;
		super.detach();
	}

	/**
	 * @since 6.0.3
	 */
	@Override
	protected boolean isDowngradedMode(final long pMapTileIndex) {
		if ((mNetworkAvailabilityCheck != null && !mNetworkAvailabilityCheck.getNetworkAvailable())
				|| !useDataConnection()) {
			return true;
		}
		int zoomMin = -1;
		int zoomMax = -1;
		for(final MapTileModuleProviderBase provider : mTileProviderList) {
			if (provider.getUsesDataConnection()) {
				int tmp;
				tmp = provider.getMinimumZoomLevel();
				if (zoomMin == -1 || zoomMin > tmp) {
					zoomMin = tmp;
				}
				tmp = provider.getMaximumZoomLevel();
				if (zoomMax == -1 || zoomMax < tmp) {
					zoomMax = tmp;
				}
			}
		}
		if (zoomMin == -1 || zoomMax == -1) {
			return true;
		}
		final int zoom = MapTileIndex.getZoom(pMapTileIndex);
		return zoom < zoomMin || zoom > zoomMax;
	}

	/**
	 * @since 6.0.3
	 * cf. https://github.com/osmdroid/osmdroid/issues/1172
	 */
	public static MapTileFileStorageProviderBase getMapTileFileStorageProviderBase(
			final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource,
			final IFilesystemCache pTileWriter
	) {
		if (pTileWriter instanceof TileWriter) {
			return new MapTileFilesystemProvider(pRegisterReceiver, pTileSource);
		}
		return new MapTileSqlCacheProvider(pRegisterReceiver, pTileSource);
	}

	/**
	 * @since 6.1.0
	 * @return true if possible and done
	 */
	public boolean setOfflineFirst(final boolean pOfflineFirst) {
		int downloaderIndex = -1;
		int approximationIndex = -1;
		int i = 0;
		for(final MapTileModuleProviderBase provider : mTileProviderList) {
			if (downloaderIndex == -1 && provider == mDownloaderProvider) {
				downloaderIndex = i;
			}
			if (approximationIndex == -1 && provider == mApproximationProvider) {
				approximationIndex = i;
			}
			i++;
		}
		if (downloaderIndex == -1 || approximationIndex == -1) {
			return false;
		}
		if (approximationIndex < downloaderIndex && pOfflineFirst) {
			return true;
		}
		if (approximationIndex > downloaderIndex && !pOfflineFirst) {
			return true;
		}
		mTileProviderList.set(downloaderIndex, mApproximationProvider);
		mTileProviderList.set(approximationIndex, mDownloaderProvider);
		return true;
	}
}

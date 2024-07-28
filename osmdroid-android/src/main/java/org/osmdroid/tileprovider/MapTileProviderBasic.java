package org.osmdroid.tileprovider;

import android.content.Context;

import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileDownloaderProvider;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This top-level tile provider implements a basic tile request chain which includes a
 * {@link MapTileFilesystemProvider} (a file-system cache), a {@link MapTileFileArchiveProvider}
 * (archive provider), and a {@link MapTileDownloaderProvider} (downloads map tiles via tile source).
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
	private final INetworkAvailablityCheck mNetworkAvailabilityCheck;

	/**
	 * @since 6.1.0
	 */
	private final MapTileDownloaderProvider mDownloaderProvider;
	private final MapTileApproximater mApproximationProvider;

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(@NonNull final Context pContext) {
		this(pContext, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(@NonNull final Context pContext, final ITileSource pTileSource) {
		this(pContext, pTileSource, null);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(@NonNull final Context pContext, final ITileSource pTileSource, @Nullable final IFilesystemCache cacheWriter) {
		this(new SimpleRegisterReceiver(), null, pTileSource, pContext, cacheWriter);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final IRegisterReceiver pRegisterReceiver,
								@Nullable final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource,
								@NonNull final Context pContext, @Nullable final IFilesystemCache cacheWriter) {
		super(pContext, pTileSource, pRegisterReceiver);

		mNetworkAvailabilityCheck = ((aNetworkAvailablityCheck != null) ? aNetworkAvailablityCheck : new NetworkAvailabliltyCheck(pContext));

		if (cacheWriter != null) {
			tileWriter = cacheWriter;
		} else {
			tileWriter = new SqlTileWriter();
		}
		final MapTileFileStorageProviderBase assetsProvider = createAssetsProvider(pRegisterReceiver, pTileSource, pContext);
		mTileProviderList.add(assetsProvider);

		final MapTileFileStorageProviderBase cacheProvider = getMapTileFileStorageProviderBase(pContext, pRegisterReceiver, pTileSource, tileWriter);
		mTileProviderList.add(cacheProvider);

		final MapTileFileStorageProviderBase archiveProvider = createArchiveProvider(pRegisterReceiver, pTileSource, pContext);
		mTileProviderList.add(archiveProvider);

		mApproximationProvider = createApproximater(assetsProvider, cacheProvider, archiveProvider);
		mTileProviderList.add(mApproximationProvider);

		mDownloaderProvider = createDownloaderProvider(pContext, aNetworkAvailablityCheck, pTileSource);
		mTileProviderList.add(mDownloaderProvider);

		// protected-cache-tile computers
		final MapTileCache cMapTileCache = getTileCache();
		cMapTileCache.getProtectedTileComputers().add(new MapTileAreaZoomComputer(-1));
		cMapTileCache.getProtectedTileComputers().add(new MapTileAreaBorderComputer(1));
		cMapTileCache.setAutoEnsureCapacity(false);
		cMapTileCache.setStressedMemory(false);

		// pre-cache providers
		final MapTilePreCache cMapTilePreCache = cMapTileCache.getPreCache();
		cMapTilePreCache.addProvider(assetsProvider);
		cMapTilePreCache.addProvider(cacheProvider);
		cMapTilePreCache.addProvider(archiveProvider);
		cMapTilePreCache.addProvider(mDownloaderProvider);

		// tiles currently being processed
		cMapTileCache.getProtectedTileContainers().add(this);

		setOfflineFirst(true);
	}

	protected MapTileApproximater createApproximater(MapTileFileStorageProviderBase assetsProvider, MapTileFileStorageProviderBase cacheProvider, MapTileFileStorageProviderBase archiveProvider) {
		MapTileApproximater approximationProvider = new MapTileApproximater();
		approximationProvider.addProvider(assetsProvider);
		approximationProvider.addProvider(cacheProvider);
		approximationProvider.addProvider(archiveProvider);
		return approximationProvider;
	}

	@Deprecated
	protected MapTileFileStorageProviderBase createArchiveProvider(@NonNull final IRegisterReceiver pRegisterReceiver, @NonNull final ITileSource pTileSource) {
		//noinspection DataFlowIssue
		return this.createArchiveProvider(pRegisterReceiver, pTileSource, null);
	}
	/** @noinspection NullableProblems*/
	protected MapTileFileStorageProviderBase createArchiveProvider(@NonNull final IRegisterReceiver pRegisterReceiver, @NonNull final ITileSource pTileSource, @NonNull final Context pContext) {
		return new MapTileFileArchiveProvider(pContext, pRegisterReceiver, pTileSource);
	}

	protected MapTileFileStorageProviderBase createAssetsProvider(@NonNull final IRegisterReceiver pRegisterReceiver, @NonNull final ITileSource pTileSource, @NonNull final Context pContext) {
		return new MapTileAssetsProvider(pContext, pRegisterReceiver, pContext.getAssets(), pTileSource);
	}

	@Override
	public IFilesystemCache getTileWriter() {
		return tileWriter;
	}

	protected MapTileDownloaderProvider createDownloaderProvider(@NonNull final Context context, INetworkAvailablityCheck aNetworkAvailablityCheck, ITileSource pTileSource) {
		return new MapTileDownloaderProvider(context, pTileSource, this.tileWriter, aNetworkAvailablityCheck);
	}

	@Override
	public void onDetach(@Nullable final Context context) {
		//https://github.com/osmdroid/osmdroid/issues/213
		if (tileWriter != null)
			tileWriter.onDetach(context);
		tileWriter = null;
		super.onDetach(context);
	}

	/**
	 * @since 6.0.3
	 */
	@Override
	protected boolean isDowngradedMode(final long pMapTileIndex) {
		if ((mNetworkAvailabilityCheck != null && !mNetworkAvailabilityCheck.getNetworkAvailable()) || !useDataConnection()) {
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
	 * cf. <a href="https://github.com/osmdroid/osmdroid/issues/1172">...</a>
	 */
	public static MapTileFileStorageProviderBase getMapTileFileStorageProviderBase(
			@NonNull final Context context,
			final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource,
			final IFilesystemCache pTileWriter
	) {
		if (pTileWriter instanceof TileWriter) {
			return new MapTileFilesystemProvider(context, pRegisterReceiver, pTileSource);
		}
		return new MapTileSqlCacheProvider(context, pRegisterReceiver, pTileSource);
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

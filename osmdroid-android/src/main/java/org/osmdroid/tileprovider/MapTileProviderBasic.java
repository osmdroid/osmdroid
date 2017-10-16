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
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

/**
 * This top-level tile provider implements a basic tile request chain which includes a
 * {@link MapTileFilesystemProvider} (a file-system cache), a {@link MapTileFileArchiveProvider}
 * (archive provider), and a {@link MapTileDownloader} (downloads map tiles via tile source).
 *
 * Behavior change since osmdroid 5.3: If the device is less than API 10, the file system based cache and writer are used
 * otherwise, the sqlite based
 *
 * @see TileWriter
 * @see SqlTileWriter
 * @see MapTileFilesystemProvider
 * @see MapTileSqlCacheProvider
 * @author Marc Kurtz
 *
 */
public class MapTileProviderBasic extends MapTileProviderArray implements IMapTileProviderCallback {

	protected IFilesystemCache tileWriter;
	private final INetworkAvailablityCheck mNetworkAvailabilityCheck;

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
		this(new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
				pTileSource, pContext,null);
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
		final MapTileAssetsProvider assetsProvider = new MapTileAssetsProvider(
				pRegisterReceiver, pContext.getAssets(), pTileSource);
		mTileProviderList.add(assetsProvider);

		final MapTileFileStorageProviderBase cacheProvider;
		if (Build.VERSION.SDK_INT < 10) {
			cacheProvider = new MapTileFilesystemProvider(pRegisterReceiver, pTileSource);
		} else {
			cacheProvider = new MapTileSqlCacheProvider(pRegisterReceiver, pTileSource);
		}
		mTileProviderList.add(cacheProvider);

		final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
				pRegisterReceiver, pTileSource);
		mTileProviderList.add(archiveProvider);

		final MapTileDownloader downloaderProvider = new MapTileDownloader(pTileSource, tileWriter,
				aNetworkAvailablityCheck);
		mTileProviderList.add(downloaderProvider);

		final MapTileApproximater approximationProvider = new MapTileApproximater();
		mTileProviderList.add(approximationProvider);
		approximationProvider.addProvider(assetsProvider);
		approximationProvider.addProvider(cacheProvider);
		approximationProvider.addProvider(archiveProvider);
	}

	@Override
	public IFilesystemCache getTileWriter() {
		return tileWriter;
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
	 * @since 6.0
	 */
	@Override
	protected boolean isDowngradedMode() {
		return (mNetworkAvailabilityCheck != null && !mNetworkAvailabilityCheck.getNetworkAvailable())
				|| !useDataConnection();
	}
}

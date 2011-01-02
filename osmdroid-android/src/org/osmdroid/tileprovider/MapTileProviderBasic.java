package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

/**
 * This top-level tile provider implements a default tile request chain which includes a
 * FileSystemProvider (a file-system cache), and a TileDownloaderProvider (downloads map tiles via
 * tile source).
 * 
 * @author Marc Kurtz
 * 
 */
public class MapTileProviderBasic extends MapTileProviderArray implements
		IMapTileProviderCallback {

	private static final Logger logger = LoggerFactory
			.getLogger(MapTileProviderBasic.class);

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public MapTileProviderBasic(final Context aContext) {
		this(aContext, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public MapTileProviderBasic(final Context aContext, final ITileSource aTileSource) {
		this(new SimpleRegisterReceiver(aContext), new NetworkAvailabliltyCheck(aContext),
				aTileSource);
	}

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public MapTileProviderBasic(final IRegisterReceiver aRegisterReceiver,
			final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource aTileSource) {
		super(aRegisterReceiver);

		final TileWriter tileWriter = new TileWriter();

		final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
				aRegisterReceiver);
		mTileProviderList.add(fileSystemProvider);

		final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
				aTileSource, aRegisterReceiver);
		mTileProviderList.add(archiveProvider);

		final MapTileDownloader downloaderProvider = new MapTileDownloader(
				aTileSource, tileWriter, aNetworkAvailablityCheck);
		mTileProviderList.add(downloaderProvider);
	}
}

package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.OpenStreetMapTileDownloader;
import org.osmdroid.tileprovider.modules.OpenStreetMapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.OpenStreetMapTileFilesystemProvider;
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
public class OpenStreetMapTileProviderDirect extends OpenStreetMapTileProviderArray implements
		IOpenStreetMapTileProviderCallback {

	private static final Logger logger = LoggerFactory
			.getLogger(OpenStreetMapTileProviderDirect.class);

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public OpenStreetMapTileProviderDirect(final Context aContext) {
		this(aContext, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public OpenStreetMapTileProviderDirect(final Context aContext, final ITileSource aTileSource) {
		this(new SimpleRegisterReceiver(aContext), new NetworkAvailabliltyCheck(aContext),
				aTileSource);
	}

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public OpenStreetMapTileProviderDirect(final IRegisterReceiver aRegisterReceiver,
			final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource aTileSource) {
		super(aRegisterReceiver);

		final TileWriter tileWriter = new TileWriter();

		final OpenStreetMapTileFilesystemProvider fileSystemProvider = new OpenStreetMapTileFilesystemProvider(
				aRegisterReceiver);
		mTileProviderList.add(fileSystemProvider);

		final OpenStreetMapTileFileArchiveProvider archiveProvider = new OpenStreetMapTileFileArchiveProvider(
				aTileSource, aRegisterReceiver);
		mTileProviderList.add(archiveProvider);

		final OpenStreetMapTileDownloader downloaderProvider = new OpenStreetMapTileDownloader(
				aTileSource, tileWriter, aNetworkAvailablityCheck);
		mTileProviderList.add(downloaderProvider);
	}
}

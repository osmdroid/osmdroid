package org.osmdroid.views.util;

import org.osmdroid.tileprovider.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.IOpenStreetMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.OpenStreetMapTileDownloader;
import org.osmdroid.tileprovider.OpenStreetMapTileFileArchiveProvider;
import org.osmdroid.tileprovider.OpenStreetMapTileFilesystemProvider;
import org.osmdroid.tileprovider.TileWriter;
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
		this(aContext, OpenStreetMapRendererFactory.DEFAULT_TILE_SOURCE);
	}

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public OpenStreetMapTileProviderDirect(final Context aContext,
			final IOpenStreetMapRendererInfo aTileSource) {
		this(new SimpleRegisterReceiver(aContext), new NetworkAvailabliltyCheck(aContext),
				aTileSource);
	}

	/**
	 * Creates an OpenStreetMapTileProviderDirect.
	 */
	public OpenStreetMapTileProviderDirect(final IRegisterReceiver aRegisterReceiver,
			final INetworkAvailablityCheck aNetworkAvailablityCheck,
			final IOpenStreetMapRendererInfo aTileSource) {
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

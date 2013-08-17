package org.osmdroid.tileprovider.modules;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * The {@link MapTileDownloader} loads tiles from an HTTP server. It saves downloaded tiles to an
 * IFilesystemCache if available.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 *
 */
public class MapTileDownloader extends MapTileModuleProviderBase {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(MapTileDownloader.class);

	// ===========================================================
	// Fields
	// ===========================================================

	private final IFilesystemCache mFilesystemCache;

	private final AtomicReference<OnlineTileSourceBase> mTileSource = new AtomicReference<OnlineTileSourceBase>();

	private final INetworkAvailablityCheck mNetworkAvailablityCheck;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileDownloader(final ITileSource pTileSource) {
		this(pTileSource, null, null);
	}

	public MapTileDownloader(final ITileSource pTileSource, final IFilesystemCache pFilesystemCache) {
		this(pTileSource, pFilesystemCache, null);
	}

	public MapTileDownloader(final ITileSource pTileSource,
			final IFilesystemCache pFilesystemCache,
			final INetworkAvailablityCheck pNetworkAvailablityCheck) {
		this(pTileSource, pFilesystemCache, pNetworkAvailablityCheck,
				NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
	}

	public MapTileDownloader(final ITileSource pTileSource,
			final IFilesystemCache pFilesystemCache,
			final INetworkAvailablityCheck pNetworkAvailablityCheck, int pThreadPoolSize,
			int pPendingQueueSize) {
		super(pThreadPoolSize, pPendingQueueSize);

		mFilesystemCache = pFilesystemCache;
		mNetworkAvailablityCheck = pNetworkAvailablityCheck;
		setTileSource(pTileSource);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public ITileSource getTileSource() {
		return mTileSource.get();
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean getUsesDataConnection() {
		return true;
	}

	@Override
	protected String getName() {
		return "Online Tile Download Provider";
	}

	@Override
	protected String getThreadGroupName() {
		return "downloader";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	}

	@Override
	public int getMinimumZoomLevel() {
		OnlineTileSourceBase tileSource = mTileSource.get();
		return (tileSource != null ? tileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
	}

	@Override
	public int getMaximumZoomLevel() {
		OnlineTileSourceBase tileSource = mTileSource.get();
		return (tileSource != null ? tileSource.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
	}

	@Override
	public void setTileSource(final ITileSource tileSource) {
		// We are only interested in OnlineTileSourceBase tile sources
		if (tileSource instanceof OnlineTileSourceBase) {
			mTileSource.set((OnlineTileSourceBase) tileSource);
		} else {
			// Otherwise shut down the tile downloader
			mTileSource.set(null);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class TileLoader extends MapTileModuleProviderBase.TileLoader {

		@Override
		public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {

			OnlineTileSourceBase tileSource = mTileSource.get();
			if (tileSource == null) {
				return null;
			}

			InputStream in = null;
			OutputStream out = null;
			final MapTile tile = aState.getMapTile();

			try {

				if (mNetworkAvailablityCheck != null
						&& !mNetworkAvailablityCheck.getNetworkAvailable()) {
					if (DEBUGMODE) {
						logger.debug("Skipping " + getName() + " due to NetworkAvailabliltyCheck.");
					}
					return null;
				}

				final String tileURLString = tileSource.getTileURLString(tile);

				if (DEBUGMODE) {
					logger.debug("Downloading Maptile from url: " + tileURLString);
				}

				if (TextUtils.isEmpty(tileURLString)) {
					return null;
				}

				final HttpClient client = HttpClientFactory.createHttpClient();
				final HttpUriRequest head = new HttpGet(tileURLString);
				final HttpResponse response = client.execute(head);

				// Check to see if we got success
				final org.apache.http.StatusLine line = response.getStatusLine();
				if (line.getStatusCode() != 200) {
					logger.warn("Problem downloading MapTile: " + tile + " HTTP response: " + line);
					return null;
				}

				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					logger.warn("No content downloading MapTile: " + tile);
					return null;
				}
				in = entity.getContent();

				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
				StreamUtils.copy(in, out);
				out.flush();
				final byte[] data = dataStream.toByteArray();
				final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

				// Save the data to the filesystem cache
				if (mFilesystemCache != null) {
					mFilesystemCache.saveFile(tileSource, tile, byteStream);
					byteStream.reset();
				}
				final Drawable result = tileSource.getDrawable(byteStream);

				return result;
			} catch (final UnknownHostException e) {
				// no network connection so empty the queue
				logger.warn("UnknownHostException downloading MapTile: " + tile + " : " + e);
				throw new CantContinueException(e);
			} catch (final LowMemoryException e) {
				// low memory so empty the queue
				logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
				throw new CantContinueException(e);
			} catch (final FileNotFoundException e) {
				logger.warn("Tile not found: " + tile + " : " + e);
			} catch (final IOException e) {
				logger.warn("IOException downloading MapTile: " + tile + " : " + e);
			} catch (final Throwable e) {
				logger.error("Error downloading MapTile: " + tile, e);
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
			}

			return null;
		}

		@Override
		protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
			removeTileFromQueues(pState.getMapTile());
			// don't return the tile because we'll wait for the fs provider to ask for it
			// this prevent flickering when a load of delayed downloads complete for tiles
			// that we might not even be interested in any more
			pState.getCallback().mapTileRequestCompleted(pState, null);
		}

	}
}

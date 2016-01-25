package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.StreamUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

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

	// ===========================================================
	// Fields
	// ===========================================================

	public static long MAX_CACHED_TILE_AGE=OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE;

	public static boolean DEBUG=false;
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
				OpenStreetMapTileProviderConstants.getNumberOfTileDownloadThreads(), 
                    OpenStreetMapTileProviderConstants.TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
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
		return (tileSource != null ? tileSource.getMinimumZoomLevel() : OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL);
	}

	@Override
	public int getMaximumZoomLevel() {
		OnlineTileSourceBase tileSource = mTileSource.get();
		return (tileSource != null ? tileSource.getMaximumZoomLevel()
				: microsoft.mappoint.TileSystem.getMaximumZoomLevel());
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

	protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

		@Override
		public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {

			OnlineTileSourceBase tileSource = mTileSource.get();
			if (tileSource == null) {
				return null;
			}

			InputStream in = null;
			OutputStream out = null;
			HttpURLConnection c=null;
			final MapTile tile = aState.getMapTile();

			try {

				if (mNetworkAvailablityCheck != null
						&& !mNetworkAvailablityCheck.getNetworkAvailable()) {
					if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
						Log.d(IMapView.LOGTAG,"Skipping " + getName() + " due to NetworkAvailabliltyCheck.");
					}
					return null;
				}

				final String tileURLString = tileSource.getTileURLString(tile);

				if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
					Log.d(IMapView.LOGTAG,"Downloading Maptile from url: " + tileURLString);
				}

				if (TextUtils.isEmpty(tileURLString)) {
					return null;
				}

				c = (HttpURLConnection) new URL(tileURLString).openConnection();
				c.setUseCaches(true);
				c.setRequestProperty(OpenStreetMapTileProviderConstants.USER_AGENT, OpenStreetMapTileProviderConstants.getUserAgentValue());
				c.connect();
                                

				// Check to see if we got success
				
				if (c.getResponseCode() != 200) {
					Log.w(IMapView.LOGTAG, "Problem downloading MapTile: " + tile + " HTTP response: " + c.getResponseMessage());
					return null;
				}

				
				in = c.getInputStream();

				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
				Date dateExpires = new Date(System.currentTimeMillis() + MAX_CACHED_TILE_AGE);
				final String expires = c.getHeaderField(OpenStreetMapTileProviderConstants.HTTP_EXPIRES_HEADER);
				if (expires!=null && expires.length() > 0) {
					try {
						dateExpires = OpenStreetMapTileProviderConstants.HTTP_HEADER_SDF.parse(expires);
					}catch (Exception ex){
						if (DEBUG)
							Log.d(IMapView.LOGTAG, "Unable to parse expiration tag for tile, using default, server returned " + expires,ex);
					}
				}
				tile.setExpires(dateExpires);
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
				Log.w(IMapView.LOGTAG,"UnknownHostException downloading MapTile: " + tile + " : " + e);
				throw new CantContinueException(e);
			} catch (final LowMemoryException e) {
				// low memory so empty the queue
				Log.w(IMapView.LOGTAG,"LowMemoryException downloading MapTile: " + tile + " : " + e);
				throw new CantContinueException(e);
			} catch (final FileNotFoundException e) {
				Log.w(IMapView.LOGTAG,"Tile not found: " + tile + " : " + e);
			} catch (final IOException e) {
				Log.w(IMapView.LOGTAG,"IOException downloading MapTile: " + tile + " : " + e);
			} catch (final Throwable e) {
				Log.e(IMapView.LOGTAG,"Error downloading MapTile: " + tile, e);
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
				try{
					c.disconnect();
				} catch (Exception ex){}
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
			// We want to return the Bitmap to the BitmapPool if applicable
			if (pDrawable instanceof ReusableBitmapDrawable)
				BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) pDrawable);
		}

	}
}

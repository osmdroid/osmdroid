// Created by plusminus on 21:31:36 - 25.09.2008
package org.andnav.osm.tileprovider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;

import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OpenStreetMapTileDownloader loads tiles from a server and passes them to
 * a OpenStreetMapTileFilesystemProvider.
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 *
 */
public class OpenStreetMapTileDownloader extends OpenStreetMapAsyncTileProvider {
	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapTileDownloader.class);
	
	private static final String DEBUGTAG = "OSM_DOWNLOADER";

	// ===========================================================
	// Fields
	// ===========================================================

	private final OpenStreetMapTileFilesystemProvider mMapTileFSProvider;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileDownloader(final IOpenStreetMapTileProviderCallback pCallback, final OpenStreetMapTileFilesystemProvider aMapTileFSProvider){
		super(pCallback, NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
		this.mMapTileFSProvider = aMapTileFSProvider;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected String debugtag() {
		return DEBUGTAG;
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	};
	
	// ===========================================================
	// Methods
	// ===========================================================

	private String buildURL(final OpenStreetMapTile tile) {
		OpenStreetMapRendererInfo renderer = OpenStreetMapRendererInfo.values()[tile.getRendererId()];
		return renderer.getTileURLString(tile);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private class TileLoader extends OpenStreetMapAsyncTileProvider.TileLoader {

		@Override
		public void loadTile(final OpenStreetMapTile aTile) throws CantContinueException {

			InputStream in = null;
			OutputStream out = null;

			final File outputFile = mMapTileFSProvider.getOutputFile(aTile);
			final String tileURLString = buildURL(aTile);
			
			try {
				if(DEBUGMODE)
					logger.debug(DEBUGTAG, "Downloading Maptile from url: " + tileURLString);

				in = new BufferedInputStream(new URL(tileURLString).openStream(), StreamUtils.IO_BUFFER_SIZE);

				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
				StreamUtils.copy(in, out);
				out.flush();

				final byte[] data = dataStream.toByteArray();
				
				// sanity check - don't save an empty file
				if (data.length == 0) {
					logger.info(DEBUGTAG, "Empty maptile not saved: " + aTile);
				} else {
					mMapTileFSProvider.saveFile(aTile, outputFile, data);
					if(DEBUGMODE)
						logger.debug(DEBUGTAG, "Maptile saved " + data.length + " bytes : " + aTile);
				}
			} catch (final UnknownHostException e) {
				// no network connection so empty the queue
				logger.warn(DEBUGTAG, "UnknownHostException downloading MapTile: " + aTile + " : " + e);
				throw new CantContinueException();
			} catch(final FileNotFoundException e){
				logger.warn(DEBUGTAG, "Url not found: " + aTile+ " : " + e);
			} catch (final IOException e) {
				logger.warn(DEBUGTAG, "IOException downloading MapTile: " + aTile + " : " + e);
			} catch(final Throwable e) {
				logger.error(DEBUGTAG, "Error downloading MapTile: " + aTile, e);
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
			}

			/* Don't immediately send the tile back.
			 * If we're moving, and the internet is a bit patchy, then by the time
			 * the download has finished we don't need this tile any more.
			 * If we still do need it then the file system provider will get it 
			 * again next time it's needed.
			 * That should be immediately because the view is redrawn when it
			 * receives this completion event.
			 */
			tileLoaded(aTile, null, true);
		}
	};

}

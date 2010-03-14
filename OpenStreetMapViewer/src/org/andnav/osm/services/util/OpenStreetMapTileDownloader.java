// Created by plusminus on 21:31:36 - 25.09.2008
package org.andnav.osm.services.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Stack;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.os.RemoteException;
import android.util.Log;

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

	private static final String DEBUGTAG = "OSM_DOWNLOADER";

	// ===========================================================
	// Fields
	// ===========================================================

	private final OpenStreetMapTileFilesystemProvider mMapTileFSProvider;
	private final Stack<OpenStreetMapTile> mTileStack = new Stack<OpenStreetMapTile>();

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileDownloader(final OpenStreetMapTileFilesystemProvider aMapTileFSProvider){
		super(4);
		this.mMapTileFSProvider = aMapTileFSProvider;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected Runnable getTileLoader(OpenStreetMapTile aTile, IOpenStreetMapTileProviderCallback aCallback) {
		return new TileLoader(aTile, aCallback);
	};
	
	// ===========================================================
	// Methods
	// ===========================================================

	private String buildURL(final OpenStreetMapTile tile) {
		OpenStreetMapRendererInfo renderer = OpenStreetMapRendererInfo.values()[tile.rendererID];
		return renderer.getTileURLString(tile);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private class TileLoader extends OpenStreetMapAsyncTileProvider.TileLoader {

		public TileLoader(final OpenStreetMapTile aTile, final IOpenStreetMapTileProviderCallback aCallback) {
			super(aTile, aCallback);
			mTileStack.push(aTile);
		}

		@Override
		public void run() {

			// do nothing if the stack is empty
			if (mTileStack.empty()) {
				return;
			}
			
			/**
			 * We will run this thread with the most recently added tile,
			 * which is not necessarily the one we started with.
			 */
			String initialTile = mTile.toString();
			mTile = mTileStack.pop();
			if(DEBUGMODE)
				Log.d(DEBUGTAG, "Started with " + initialTile + " - running with " + mTile);
			
			InputStream in = null;
			OutputStream out = null;

			String tileURLString = buildURL(mTile);
			
			try {
				if(DEBUGMODE)
					Log.d(DEBUGTAG, "Downloading Maptile from url: " + tileURLString);

				in = new BufferedInputStream(new URL(tileURLString).openStream(), StreamUtils.IO_BUFFER_SIZE);

				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
				StreamUtils.copy(in, out);
				out.flush();

				final byte[] data = dataStream.toByteArray();
				
				// sanity check - don't save an empty file
				if (data.length == 0) {
					Log.i(DEBUGTAG, "Empty maptile not saved: " + mTile);
				} else {
					mMapTileFSProvider.saveFile(mTile, data);
					if(DEBUGMODE)
						Log.d(DEBUGTAG, "Maptile saved " + data.length + " bytes : " + mTile);
				}
			} catch (IOException e) {
				if(DEBUGMODE)
					Log.e(DEBUGTAG, "Error downloading MapTile: " + mTile + " : " + e);
			} catch(final Throwable e) {
				Log.e(DEBUGTAG, "Error downloading MapTile: " + mTile, e);
			} finally {
				/* Tell the callback we've finished.
				 * Don't immediately send the tile back.
				 * If we're moving, and the internet is a bit patchy, then by the time
				 * the download has finished we don't need this tile any more.
				 * If we still do need it then the file system provider will get it 
				 * again next time it's needed.
				 * That should be immediately because the view is redrawn when it
				 * receives this completion event.
				 */
				try {
					mCallback.mapTileRequestCompleted(mTile.rendererID, mTile.zoomLevel, mTile.x, mTile.y, null);
				} catch (RemoteException e) {
					Log.e(DEBUGTAG, "Service failed", e);
				}

				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
				finished();
			}
		}
	};

}

// Created by plusminus on 21:31:36 - 25.09.2008
package org.andnav.osm.services.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.Executors;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.content.Context;
import android.graphics.BitmapFactory;
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

	protected final Context mCtx;
	protected final OpenStreetMapTileFilesystemProvider mMapTileFSProvider;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileDownloader(final Context ctx, final OpenStreetMapTileFilesystemProvider aMapTileFSProvider){
		this.mCtx = ctx;
		this.mMapTileFSProvider = aMapTileFSProvider;
		mThreadPool = Executors.newFixedThreadPool(4);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

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
		}

		@Override
		public void run() {
			InputStream in = null;
			OutputStream out = null;

			String tileURLString = buildURL(mTile);
			
			try {
				if(Log.isLoggable(DEBUGTAG, Log.DEBUG))
					Log.d(DEBUGTAG, "Downloading Maptile from url: " + tileURLString);


				in = new BufferedInputStream(new URL(tileURLString).openStream(), StreamUtils.IO_BUFFER_SIZE);

				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
				StreamUtils.copy(in, out);
				out.flush();

				final byte[] data = dataStream.toByteArray();

				OpenStreetMapTileDownloader.this.mMapTileFSProvider.saveFile(mTile, data);
				if(Log.isLoggable(DEBUGTAG, Log.DEBUG))
					Log.d(DEBUGTAG, "Maptile saved to: " + tileURLString);

				mCallback.mapTileLoaded(mTile.rendererID, mTile.zoomLevel, mTile.x, mTile.y, BitmapFactory.decodeByteArray(data, 0, data.length));
			} catch (IOException e) {
				try {
					mCallback.mapTileFailed(mTile.rendererID, mTile.zoomLevel, mTile.x, mTile.y);
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(Log.isLoggable(DEBUGTAG, Log.ERROR))
					Log.e(DEBUGTAG, "Error Downloading MapTile. Exception: " + e.getClass().getSimpleName(), e);
				/* TODO What to do when downloading tile caused an error?
				 * Also remove it from the mPending?
				 * Doing not blocks it for the whole existence of this TileDownloder.
				 * -> we remove it and the application has to re-request it.
				 */
			} catch (RemoteException re) {
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
				finished();
			}
		}
	};

}

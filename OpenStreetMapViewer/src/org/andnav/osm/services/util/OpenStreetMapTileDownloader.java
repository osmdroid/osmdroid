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
import org.andnav.osm.services.util.constants.OpenStreetMapServiceConstants;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.content.Context;
import android.graphics.Bitmap;
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
public class OpenStreetMapTileDownloader extends OpenStreetMapAsyncTileProvider implements OpenStreetMapServiceConstants {
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
		}

		@Override
		public void run() {
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

				OpenStreetMapTileDownloader.this.mMapTileFSProvider.saveFile(mTile, data);
				if(DEBUGMODE)
					Log.d(DEBUGTAG, "Maptile saved: " + mTile);

				Bitmap bitmap = null;
				try {
					bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				}catch(OutOfMemoryError e) {
					// it's downloaded, so success, but we just can't return it immediately
					Log.e(DEBUGTAG, "OutOfMemoryError creating bitmap from downloaded MapTile: " + mTile);
				}
				mCallback.mapTileLoaded(mTile.rendererID, mTile.zoomLevel, mTile.x, mTile.y, bitmap);
			} catch (IOException e) {
				try {
					mCallback.mapTileFailed(mTile.rendererID, mTile.zoomLevel, mTile.x, mTile.y);
				} catch (RemoteException e1) {
					Log.e(DEBUGTAG, "Service failed", e1);
				}
				if(DEBUGMODE)
					Log.e(DEBUGTAG, "Error downloading MapTile: " + mTile + " : " + e);
			} catch (RemoteException re) {
				Log.e(DEBUGTAG, "Service failed downloading MapTile: " + mTile, re);
			} catch(final Throwable t) {
				// don't expect to get this
				Log.e(DEBUGTAG, "Error downloading MapTile: " + mTile, t);
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
				finished();
			}
		}
	};

}

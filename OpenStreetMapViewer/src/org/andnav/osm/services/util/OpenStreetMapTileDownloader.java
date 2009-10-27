// Created by plusminus on 21:31:36 - 25.09.2008
package org.andnav.osm.services.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;

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
public class OpenStreetMapTileDownloader {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final String DEBUGTAG = "OSM_DOWNLOADER";

	// ===========================================================
	// Fields
	// ===========================================================

	protected HashSet<String> mPending = new HashSet<String>();
	protected Context mCtx;
	protected OpenStreetMapTileFilesystemProvider mMapTileFSProvider;
	protected ExecutorService mThreadPool = Executors.newFixedThreadPool(4);

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileDownloader(final Context ctx, final OpenStreetMapTileFilesystemProvider aMapTileFSProvider){
		this.mCtx = ctx;
		this.mMapTileFSProvider = aMapTileFSProvider;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void requestMapTileAsync(final String aTileURLString, final IOpenStreetMapTileProviderCallback callback) {
		if(this.mPending.contains(aTileURLString))
			return;

		this.mPending.add(aTileURLString);

		this.mThreadPool.execute(new Runnable(){
			@Override
			public void run() {
				InputStream in = null;
				OutputStream out = null;

				try {
					if(Log.isLoggable(DEBUGTAG, Log.DEBUG))
						Log.d(DEBUGTAG, "Downloading Maptile from url: " + aTileURLString);


					in = new BufferedInputStream(new URL(aTileURLString).openStream(), StreamUtils.IO_BUFFER_SIZE);

					final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
					out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
					StreamUtils.copy(in, out);
					out.flush();

					final byte[] data = dataStream.toByteArray();

					OpenStreetMapTileDownloader.this.mMapTileFSProvider.saveFile(aTileURLString, data);
					if(Log.isLoggable(DEBUGTAG, Log.DEBUG))
						Log.d(DEBUGTAG, "Maptile saved to: " + aTileURLString);

					callback.mapTileLoaded(aTileURLString, BitmapFactory.decodeByteArray(data, 0, data.length));
				} catch (IOException e) {
					try {
						callback.mapTileFailed(aTileURLString);
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
				}
				OpenStreetMapTileDownloader.this.mPending.remove(aTileURLString);
			}
		});
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

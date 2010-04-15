// Created by plusminus on 21:46:41 - 25.09.2008
package org.andnav.osm.services.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.andnav.osm.exceptions.EmptyCacheException;
import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapTileFilesystemProvider extends OpenStreetMapAsyncTileProvider {
	// ===========================================================
	// Constants
	// ===========================================================

	final static String DEBUGTAG = "OSM_FS_PROVIDER";

	// ===========================================================
	// Fields
	// ===========================================================

	protected final OpenStreetMapTileProviderDataBase mDatabase;
	protected final int mMaxFSCacheByteSize;
	protected int mCurrentFSCacheByteSize;

	/** online provider */
	protected OpenStreetMapTileDownloader mTileDownloader;

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * @param ctx
	 * @param aMaxFSCacheByteSize the size of the cached MapTiles will not exceed this size.
	 * @param aCache to load fs-tiles to.
	 */
	public OpenStreetMapTileFilesystemProvider(final Context ctx, final int aMaxFSCacheByteSize) {
		super(NUMBER_OF_TILE_FILESYSTEM_THREADS, TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
		this.mMaxFSCacheByteSize = aMaxFSCacheByteSize;
		this.mDatabase = new OpenStreetMapTileProviderDataBase(ctx);
		this.mCurrentFSCacheByteSize = this.mDatabase.getCurrentFSCacheByteSize();

		this.mTileDownloader = new OpenStreetMapTileDownloader(this);

		if(DEBUGMODE)
			Log.d(DEBUGTAG, "Currently used cache-size is: " + this.mCurrentFSCacheByteSize + " of " + this.mMaxFSCacheByteSize + " Bytes");
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public int getCurrentFSCacheByteSize() {
		return this.mCurrentFSCacheByteSize;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected String debugtag() {
		return DEBUGTAG;
	}

	@Override
	protected Runnable getTileLoader(final IOpenStreetMapTileProviderCallback aTileProviderCallback) {
		return new TileLoader(aTileProviderCallback);
	};
	
	// ===========================================================
	// Methods
	// ===========================================================

	private String buildPath(final OpenStreetMapTile tile) {
		final OpenStreetMapRendererInfo renderer = OpenStreetMapRendererInfo.values()[tile.rendererID];
		return TILE_PATH_BASE + renderer.name() + "/" + tile.zoomLevel + "/"
					+ tile.x + "/" + tile.y + renderer.IMAGE_FILENAMEENDING + TILE_PATH_EXTENSION; 
	}
	
	/**
	 * Get the file location for the tile.
	 * @param tile
	 * @return 
	 * @throws CantContinueException if the directory containing the file doesn't exist
	 * and can't be created
	 */
	File getOutputFile(final OpenStreetMapTile tile) throws CantContinueException {
		final File file = new File(buildPath(tile));
		final File parent = file.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new CantContinueException();
		}
		return file;
	}
	
	void saveFile(final OpenStreetMapTile tile, final File outputFile, final byte[] someData) throws IOException{
		final OutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile, false), StreamUtils.IO_BUFFER_SIZE);		
		bos.write(someData);
		bos.flush();
		bos.close();

		synchronized (this) {
			final int bytesGrown = this.mDatabase.addTileOrIncrement(tile, someData.length);
			this.mCurrentFSCacheByteSize += bytesGrown;

			if(DEBUGMODE)
				Log.d(DEBUGTAG, "FSCache Size is now: " + this.mCurrentFSCacheByteSize + " Bytes");

			/* If Cache is full... */
			try {

				if(this.mCurrentFSCacheByteSize > this.mMaxFSCacheByteSize){
					if(DEBUGMODE)
						Log.d(DEBUGTAG, "Freeing FS cache...");
					this.mCurrentFSCacheByteSize -= this.mDatabase.deleteOldest((int)(this.mMaxFSCacheByteSize * 0.05f)); // Free 5% of cache
				}
			} catch (EmptyCacheException e) {
				if(DEBUGMODE)
					Log.d(DEBUGTAG, "Cache empty", e);
			}
		}
	}

	public void clearCurrentFSCache(){
		cutCurrentFSCacheBy(Integer.MAX_VALUE); // Delete all
	}
	
	public void cutCurrentFSCacheBy(final int bytesToCut){
		try {
			this.mDatabase.deleteOldest(Integer.MAX_VALUE); // Delete all
			this.mCurrentFSCacheByteSize = 0;
		} catch (EmptyCacheException e) {
			if(DEBUGMODE)
				Log.d(DEBUGTAG, "Cache empty", e);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class TileLoader extends OpenStreetMapAsyncTileProvider.TileLoader {

		public TileLoader(final IOpenStreetMapTileProviderCallback aTileProviderCallback) {
			super(aTileProviderCallback);
		}

		@Override
		public void loadTile(final OpenStreetMapTile aTile, final TileLoaderCallback pTileLoaderCallback) throws CantContinueException {
			mDatabase.incrementUse(aTile);
			final File tileFile = getOutputFile(aTile);
			try {
				if (tileFile.exists()) {
					if (DEBUGMODE)
						Log.d(DEBUGTAG, "Loaded tile: " + aTile);
					pTileLoaderCallback.tileLoaded(aTile, tileFile.getPath(), true);
				} else {
					if (DEBUGMODE)
						Log.d(DEBUGTAG, "Tile not exist, request for download: " + aTile);
					mTileDownloader.loadMapTileAsync(aTile, mTileProviderCallback);
					// don't refresh the screen because there's nothing new
					pTileLoaderCallback.tileLoaded(aTile, null, false);
				}
			} catch (final Throwable e) {
				Log.e(DEBUGTAG, "Error loading tile", e);
				pTileLoaderCallback.tileLoaded(aTile, null, false);
			}
		}
	}
}

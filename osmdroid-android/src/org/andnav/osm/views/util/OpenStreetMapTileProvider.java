// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import java.io.File;

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public abstract class OpenStreetMapTileProvider implements OpenStreetMapViewConstants {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapTileProvider.class);
	
	protected final OpenStreetMapTileCache mTileCache;
	protected final Handler mDownloadFinishedHandler;

	public OpenStreetMapTileProvider(final Handler pDownloadFinishedListener) {
		mTileCache = new OpenStreetMapTileCache();
		mDownloadFinishedHandler = pDownloadFinishedListener;
	}

	public void mapTileRequestCompleted(final OpenStreetMapTile pTile, final String pTilePath) {

		// if the tile path has been returned, add the tile to the cache
		if (pTilePath != null) {
			try {
				final Bitmap bitmap = BitmapFactory.decodeFile(pTilePath);
				if (bitmap != null) {
					mTileCache.putTile(pTile, bitmap);
				} else {
					// if we couldn't load it then it's invalid - delete it
					try {
						new File(pTilePath).delete();
					} catch (Throwable e) {
						logger.error(DEBUGTAG, "Error deleting invalid file: " + pTilePath, e);
					}
				}
			} catch (final OutOfMemoryError e) {
				logger.error(DEBUGTAG, "OutOfMemoryError putting tile in cache: " + pTile);
				mTileCache.clear();
				System.gc();
			}
		}
		
		// tell our caller we've finished and it should update its view
		mDownloadFinishedHandler.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);

		if (DEBUGMODE)
			logger.debug(DEBUGTAG, "MapTile request complete: " + pTile);
	}	
	
	public void ensureCapacity(final int aCapacity) {
		mTileCache.ensureCapacity(aCapacity);
	}

	public abstract Bitmap getMapTile(OpenStreetMapTile pTile);

	public abstract void detach();
	
}

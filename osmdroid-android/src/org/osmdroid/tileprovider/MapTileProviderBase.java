// Created by plusminus on 21:46:22 - 25.09.2008
package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;
import android.os.Handler;

/**
 * This is an abstract class. The tile provider is responsible for:
 * <ul>
 * <li>determining if a map tile is available,</li>
 * <li>notifying the client, via a callback handler</li>
 * </ul>
 * see {@link MapTile} for an overview of how tiles are served by this provider.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 *
 */
public abstract class MapTileProviderBase implements IMapTileProviderCallback,
		OpenStreetMapTileProviderConstants {

	private static final Logger logger = LoggerFactory.getLogger(MapTileProviderBase.class);

	protected final MapTileCache mTileCache;
	protected Handler mTileRequestCompleteHandler;
	protected boolean mUseDataConnection = true;

	private ITileSource mTileSource;

	public abstract Drawable getMapTile(MapTile pTile);

	public abstract void detach();

	/**
	 * Gets the minimum zoom level this tile provider can provide
	 *
	 * @return the minimum zoom level
	 */
	public abstract int getMinimumZoomLevel();

	/**
	 * Gets the maximum zoom level this tile provider can provide
	 *
	 * @return the maximum zoom level
	 */
	public abstract int getMaximumZoomLevel();

	/**
	 * Sets the tile source for this tile provider.
	 *
	 * @param pTileSource
	 *            the tile source
	 */
	public void setTileSource(final ITileSource pTileSource) {
		mTileSource = pTileSource;
		clearTileCache();
	}

	/**
	 * Gets the tile source for this tile provider.
	 *
	 * @return the tile source
	 */
	public ITileSource getTileSource() {
		return mTileSource;
	}

	public MapTileProviderBase(final ITileSource pTileSource) {
		this(pTileSource, null);
	}

	public MapTileProviderBase(final ITileSource pTileSource,
			final Handler pDownloadFinishedListener) {
		mTileCache = new MapTileCache();
		mTileRequestCompleteHandler = pDownloadFinishedListener;
		mTileSource = pTileSource;
	}

	/**
	 * Called by implementation class methods indicating that they have completed the request as
	 * best it can. The tile is added to the cache, and a MAPTILE_SUCCESS_ID message is sent.
	 *
	 * @param pState
	 *            the map tile request state object
	 * @param pDrawable
	 *            the Drawable of the map tile
	 */
	@Override
	public void mapTileRequestCompleted(final MapTileRequestState pState, final Drawable pDrawable) {
		final MapTile tile = pState.getMapTile();
		if (pDrawable != null) {
			mTileCache.putTile(tile, pDrawable);
		}

		// tell our caller we've finished and it should update its view
		if (mTileRequestCompleteHandler != null) {
			mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_SUCCESS_ID);
		}

		if (DEBUGMODE) {
			logger.debug("MapTile request complete: " + tile);
		}
	}

	/**
	 * Default implementation is to call mapTileRequestCompleted
	 *
	 * @param pState
	 *            the map tile request state object
	 * @param pDrawable
	 *            the Drawable of the map tile
	 */
	@Override
	public void mapTileRequestCandidate(final MapTileRequestState pState, final Drawable pDrawable) {
		mapTileRequestCompleted(pState, pDrawable);
	}

	/**
	 * Called by implementation class methods indicating that they have failed to retrieve the
	 * requested map tile. a MAPTILE_FAIL_ID message is sent.
	 *
	 * @param pState
	 *            the map tile request state object
	 */
	@Override
	public void mapTileRequestFailed(final MapTileRequestState pState) {
		final MapTile tile = pState.getMapTile();
		if (mTileRequestCompleteHandler != null) {
			mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_FAIL_ID);
		}

		if (DEBUGMODE) {
			logger.debug("MapTile request failed: " + tile);
		}
	}

	public void setTileRequestCompleteHandler(final Handler handler) {
		mTileRequestCompleteHandler = handler;
	}

	public void ensureCapacity(final int pCapacity) {
		mTileCache.ensureCapacity(pCapacity);
	}

	public void clearTileCache() {
		mTileCache.clear();
	}

	/**
	 * Whether to use the network connection if it's available.
	 */
	@Override
	public boolean useDataConnection() {
		return mUseDataConnection;
	}

	/**
	 * Set whether to use the network connection if it's available.
	 *
	 * @param pMode
	 *            if true use the network connection if it's available. if false don't use the
	 *            network connection even if it's available.
	 */
	public void setUseDataConnection(final boolean pMode) {
		mUseDataConnection = pMode;
	}

}

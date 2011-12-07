// Created by plusminus on 21:46:22 - 25.09.2008
package org.osmdroid.tileprovider;

import java.util.HashMap;

import microsoft.mappoint.TileSystem;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.TileLooper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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

	/**
	 * Recreate the cache using scaled versions of the tiles currently in it
	 * @param pNewZoomLevel the zoom level that we need now
	 * @param pOldZoomLevel the previous zoom level that we should get the tiles to rescale
	 * @param pViewPort the view port we need tiles for
	 */
	public void rescaleCache(final int pNewZoomLevel, final int pOldZoomLevel, final Rect pViewPort) {

		if (pNewZoomLevel == pOldZoomLevel) {
			return;
		}

		final long startMs = System.currentTimeMillis();

		logger.info("rescale tile cache from "+ pOldZoomLevel + " to " + pNewZoomLevel);

		final int tileSize = getTileSource().getTileSizePixels();
		final int worldSize_2 = TileSystem.MapSize(pNewZoomLevel) >> 1;
		final Rect viewPort = new Rect(pViewPort);
		viewPort.offset(worldSize_2, worldSize_2);

		final ScaleTileLooper tileLooper = pNewZoomLevel > pOldZoomLevel
				? new ZoomInTileLooper(pOldZoomLevel)
				: new ZoomOutTileLooper(pOldZoomLevel);
		tileLooper.loop(null, pNewZoomLevel, tileSize, viewPort);

		final long endMs = System.currentTimeMillis();
		logger.info("Finished rescale in " + (endMs - startMs) + "ms");
	}

	private abstract class ScaleTileLooper extends TileLooper {

		/** new (scaled) tiles to add to cache
		  * NB first generate all and then put all in cache,
		  * otherwise the ones we need will be pushed out */
		protected final HashMap<MapTile, Bitmap> mNewTiles;

		protected final int mOldZoomLevel;
		protected int mDiff;
		protected int mTileSize_2;
		protected Rect mSrcRect;
		protected Rect mDestRect;
		protected Paint mDebugPaint;

		public ScaleTileLooper(final int pOldZoomLevel) {
			mOldZoomLevel = pOldZoomLevel;
			mNewTiles = new HashMap<MapTile, Bitmap>();
			mSrcRect = new Rect();
			mDestRect = new Rect();
			mDebugPaint = new Paint();
		}

		@Override
		public void initialiseLoop(final int pZoomLevel, final int pTileSizePx) {
			mDiff = Math.abs(pZoomLevel - mOldZoomLevel);
			mTileSize_2 = pTileSizePx >> mDiff;
		}

		@Override
		public void handleTile(final Canvas pCanvas, final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

			// Get tile from cache.
			// If it's found then no need to created scaled version.
			// If not found (null) them we've initiated a new request for it,
			// and now we'll create a scaled version until the request completes.
			final Drawable requestedTile = getMapTile(pTile);
			if (requestedTile == null) {
				try {
					handleTile(pTileSizePx, pTile, pX, pY);
				} catch(final OutOfMemoryError e) {
					logger.error("OutOfMemoryError rescaling cache");
				}
			}
		}

		@Override
		public void finaliseLoop() {
			// now add the new ones, pushing out the old ones
			while(!mNewTiles.isEmpty()) {
				final MapTile tile = mNewTiles.keySet().iterator().next();
				final Bitmap bitmap = mNewTiles.remove(tile);
				mTileCache.putTile(tile, new ExpiredBitmapDrawable(bitmap));
			}
		}

		protected abstract void handleTile(int pTileSizePx, MapTile pTile, int pX, int pY);
	}

	private class ZoomInTileLooper extends ScaleTileLooper {
		public ZoomInTileLooper(final int pOldZoomLevel) {
			super(pOldZoomLevel);
		}
		@Override
		public void handleTile(final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {
			// get the correct fraction of the tile from cache and scale up

			final MapTile oldTile = new MapTile(mOldZoomLevel, pTile.getX() >> mDiff, pTile.getY() >> mDiff);
			final Drawable oldDrawable = mTileCache.getMapTile(oldTile);

			if (oldDrawable instanceof BitmapDrawable) {
				final Bitmap oldBitmap = ((BitmapDrawable)oldDrawable).getBitmap();
				final int xx = (pX % (1 << mDiff)) * mTileSize_2;
				final int yy = (pY % (1 << mDiff)) * mTileSize_2;
				mSrcRect.set(xx, yy, xx + mTileSize_2, yy + mTileSize_2);
				mDestRect.set(0, 0, pTileSizePx, pTileSizePx);
				final Bitmap bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx, Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);
				canvas.drawBitmap(oldBitmap, mSrcRect, mDestRect, null);
				if (DEBUGMODE) {
					logger.debug("Created scaled tile: " + pTile);
					mDebugPaint.setTextSize(40);
					canvas.drawText("scaled", 50, 50, mDebugPaint);
				}
				mNewTiles.put(pTile, bitmap);
			}
		}
	}

	private class ZoomOutTileLooper extends ScaleTileLooper {
		public ZoomOutTileLooper(final int pOldZoomLevel) {
			super(pOldZoomLevel);
		}
		@Override
		protected void handleTile(final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

			// get many tiles from cache and make one tile from them

			final int xx = pTile.getX() << mDiff;
			final int yy = pTile.getY() << mDiff;
			final int numTiles = 1 << mDiff;
			Bitmap bitmap = null;
			Canvas canvas = null;
			for(int x = 0; x < numTiles; x++) {
				for(int y = 0; y < numTiles; y++) {
					final MapTile oldTile = new MapTile(mOldZoomLevel, xx + x, yy + y);
					final Drawable oldDrawable = mTileCache.getMapTile(oldTile);
					if (oldDrawable instanceof BitmapDrawable) {
						final Bitmap oldBitmap = ((BitmapDrawable)oldDrawable).getBitmap();
						if (oldBitmap != null) {
							if (bitmap == null) {
								bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx, Bitmap.Config.ARGB_8888);
								canvas = new Canvas(bitmap);
								canvas.drawColor(Color.LTGRAY);
							}
							mDestRect.set(
									x * mTileSize_2, y * mTileSize_2,
									(x + 1) * mTileSize_2, (y + 1) * mTileSize_2);
							if (oldBitmap != null) {
								canvas.drawBitmap(oldBitmap, null, mDestRect, null);
								mTileCache.mCachedTiles.remove(oldBitmap);
							}
						}
					}
				}
			}

			if (bitmap != null) {
				mNewTiles.put(pTile, bitmap);
				if (DEBUGMODE) {
					logger.debug("Created scaled tile: " + pTile);
					mDebugPaint.setTextSize(40);
					canvas.drawText("scaled", 50, 50, mDebugPaint);
				}
			}
		}
	}

}

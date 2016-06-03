// Created by plusminus on 21:46:22 - 25.09.2008
package org.osmdroid.tileprovider;

import java.util.HashMap;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.TileLooper;
import org.osmdroid.views.Projection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import org.osmdroid.api.IMapView;

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
public abstract class MapTileProviderBase implements IMapTileProviderCallback {


	protected final MapTileCache mTileCache;
	protected Handler mTileRequestCompleteHandler;
	protected boolean mUseDataConnection = true;

	private ITileSource mTileSource;

	/**
	 * Attempts to get a Drawable that represents a {@link MapTile}. If the tile is not immediately
	 * available this will return null and attempt to get the tile from known tile sources for
	 * subsequent future requests. Note that this may return a {@link ReusableBitmapDrawable} in
	 * which case you should follow proper handling procedures for using that Drawable or it may
	 * reused while you are working with it.
	 *
	 * @see ReusableBitmapDrawable
	 */
	public abstract Drawable getMapTile(MapTile pTile);

	/**
	 * called when the MapView.detach is called. Implementations must clean up any cached resources
	 * to prevent leaks, such as the {@link MapTileCache#clear()} and the {@link LRUMapTileCache#clear()}
	 */
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

	/**
	 * Creates a {@link MapTileCache} to be used to cache tiles in memory.
	 */
	public MapTileCache createTileCache() {
		return new MapTileCache();
	}

	public MapTileProviderBase(final ITileSource pTileSource) {
		this(pTileSource, null);
	}

	public MapTileProviderBase(final ITileSource pTileSource,
			final Handler pDownloadFinishedListener) {
		mTileCache = this.createTileCache();
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
		// put the tile in the cache
		putTileIntoCache(pState, pDrawable);

		// tell our caller we've finished and it should update its view
		if (mTileRequestCompleteHandler != null) {
			mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_SUCCESS_ID);
		}

		if (OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS) {
               Log.d(IMapView.LOGTAG,"MapTileProviderBase.mapTileRequestCompleted(): " + pState.getMapTile());
		}
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
		if (mTileRequestCompleteHandler != null) {
			mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_FAIL_ID);
		}

		if (OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS) {
			Log.d(IMapView.LOGTAG,"MapTileProviderBase.mapTileRequestFailed(): " + pState.getMapTile());
		}
	}

	/**
	 * Called by implementation class methods indicating that they have produced an expired result
	 * that can be used but better results may be delivered later. The tile is added to the cache,
	 * and a MAPTILE_SUCCESS_ID message is sent.
	 *
	 * @param pState
	 *            the map tile request state object
	 * @param pDrawable
	 *            the Drawable of the map tile
	 */
	@Override
	public void mapTileRequestExpiredTile(MapTileRequestState pState, Drawable pDrawable) {
		// Put the expired tile into the cache
		putExpiredTileIntoCache(pState, pDrawable);

		// tell our caller we've finished and it should update its view
		if (mTileRequestCompleteHandler != null) {
			mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_SUCCESS_ID);
		}

		if (OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS) {
			Log.d(IMapView.LOGTAG,"MapTileProviderBase.mapTileRequestExpiredTile(): " + pState.getMapTile());
		}
	}

	protected void putTileIntoCache(MapTileRequestState pState, Drawable pDrawable) {
		final MapTile tile = pState.getMapTile();
		if (pDrawable != null) {
			mTileCache.putTile(tile, pDrawable);
		}
	}

	protected void putExpiredTileIntoCache(MapTileRequestState pState, Drawable pDrawable) {
		final MapTile tile = pState.getMapTile();
		if (pDrawable != null && !mTileCache.containsTile(tile)) {
			mTileCache.putTile(tile, pDrawable);
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
	public void rescaleCache(final Projection pProjection, final int pNewZoomLevel,
			final int pOldZoomLevel, final Rect pViewPort) {

		if (pNewZoomLevel == pOldZoomLevel) {
			return;
		}

		final long startMs = System.currentTimeMillis();

		Log.i(IMapView.LOGTAG,"rescale tile cache from "+ pOldZoomLevel + " to " + pNewZoomLevel);

		final int tileSize = getTileSource().getTileSizePixels();

		Point topLeftMercator = pProjection.toMercatorPixels(pViewPort.left, pViewPort.top, null);
		Point bottomRightMercator = pProjection.toMercatorPixels(pViewPort.right, pViewPort.bottom,
				null);
		final Rect viewPort = new Rect(topLeftMercator.x, topLeftMercator.y, bottomRightMercator.x,
				bottomRightMercator.y);

		final ScaleTileLooper tileLooper = pNewZoomLevel > pOldZoomLevel
				? new ZoomInTileLooper(pOldZoomLevel)
				: new ZoomOutTileLooper(pOldZoomLevel);
		tileLooper.loop(null, pNewZoomLevel, tileSize, viewPort);

		final long endMs = System.currentTimeMillis();
		Log.i(IMapView.LOGTAG,"Finished rescale in " + (endMs - startMs) + "ms");
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
					Log.e(IMapView.LOGTAG,"OutOfMemoryError rescaling cache");
				}
			}
		}

		@Override
		public void finaliseLoop() {
			// now add the new ones, pushing out the old ones
			while (!mNewTiles.isEmpty()) {
				final MapTile tile = mNewTiles.keySet().iterator().next();
				final Bitmap bitmap = mNewTiles.remove(tile);
				final ExpirableBitmapDrawable drawable = new ReusableBitmapDrawable(bitmap);
				ExpirableBitmapDrawable.setDrawableExpired(drawable);
				final Drawable existingTile = mTileCache.getMapTile(tile);
				if (existingTile == null || ExpirableBitmapDrawable.isDrawableExpired(existingTile))
					putExpiredTileIntoCache(new MapTileRequestState(tile,
							new MapTileModuleProviderBase[0], null), drawable);
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
				final int xx = (pX % (1 << mDiff)) * mTileSize_2;
				final int yy = (pY % (1 << mDiff)) * mTileSize_2;
				mSrcRect.set(xx, yy, xx + mTileSize_2, yy + mTileSize_2);
				mDestRect.set(0, 0, pTileSizePx, pTileSizePx);

				// Try to get a bitmap from the pool, otherwise allocate a new one
				Bitmap bitmap = BitmapPool.getInstance().obtainSizedBitmapFromPool(
						pTileSizePx, pTileSizePx);
				if (bitmap == null)
					bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx, Bitmap.Config.ARGB_8888);

				final Canvas canvas = new Canvas(bitmap);
				final boolean isReusable = oldDrawable instanceof ReusableBitmapDrawable;
				final ReusableBitmapDrawable reusableBitmapDrawable =
						isReusable ? (ReusableBitmapDrawable) oldDrawable : null;
				boolean success = false;
				if (isReusable)
					reusableBitmapDrawable.beginUsingDrawable();
				try {
					if (!isReusable || reusableBitmapDrawable.isBitmapValid()) {
						final BitmapDrawable bitmapDrawable = (BitmapDrawable) oldDrawable;
						final Bitmap oldBitmap = bitmapDrawable.getBitmap();
						canvas.drawBitmap(oldBitmap, mSrcRect, mDestRect, null);
						success = true;
						if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
							Log.d(IMapView.LOGTAG,"Created scaled tile: " + pTile);
							mDebugPaint.setTextSize(40);
							canvas.drawText("scaled", 50, 50, mDebugPaint);
						}
					}
				} finally {
					if (isReusable)
						reusableBitmapDrawable.finishUsingDrawable();
				}
				if (success)
					mNewTiles.put(pTile, bitmap);
			}
		}
	}

	private class ZoomOutTileLooper extends ScaleTileLooper {
		private static final int MAX_ZOOM_OUT_DIFF = 4;
		public ZoomOutTileLooper(final int pOldZoomLevel) {
			super(pOldZoomLevel);
		}
		@Override
		protected void handleTile(final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

			if (mDiff >= MAX_ZOOM_OUT_DIFF){
				return;
			}

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
								// Try to get a bitmap from the pool, otherwise allocate a new one
								bitmap = BitmapPool.getInstance().obtainSizedBitmapFromPool(
										pTileSizePx, pTileSizePx);
								if (bitmap == null)
									bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx,
										Bitmap.Config.ARGB_8888);
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
				if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
					Log.d(IMapView.LOGTAG,"Created scaled tile: " + pTile);
					mDebugPaint.setTextSize(40);
					canvas.drawText("scaled", 50, 50, mDebugPaint);
				}
			}
		}
	}

}

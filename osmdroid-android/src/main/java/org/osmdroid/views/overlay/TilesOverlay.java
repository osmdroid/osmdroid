package org.osmdroid.views.overlay;

import microsoft.mappoint.TileSystem;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.TileLooper;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

/**
 * These objects are the principle consumer of map tiles.
 *
 * see {@link MapTile} for an overview of how tiles are acquired by this overlay.
 *
 */

public class TilesOverlay extends Overlay implements IOverlayMenuProvider {

	private static final Logger logger = LoggerFactory.getLogger(TilesOverlay.class);

	public static final int MENU_MAP_MODE = getSafeMenuId();
	public static final int MENU_TILE_SOURCE_STARTING_ID = getSafeMenuIdSequence(TileSourceFactory
			.getTileSources().size());
	public static final int MENU_OFFLINE = getSafeMenuId();

	/** Current tile source */
	protected final MapTileProviderBase mTileProvider;

	/* to avoid allocations during draw */
	protected final Paint mDebugPaint = new Paint();
	private final Rect mTileRect = new Rect();
	private final Rect mViewPort = new Rect();

	private boolean mOptionsMenuEnabled = true;

	private int mWorldSize_2;

	/** A drawable loading tile **/
	private BitmapDrawable mLoadingTile = null;
	private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
	private int mLoadingLineColor = Color.rgb(200, 192, 192);

	/** For overshooting the tile cache **/
	private int mOvershootTileCache = 0;

	public TilesOverlay(final MapTileProviderBase aTileProvider, final Context aContext) {
		this(aTileProvider, new DefaultResourceProxyImpl(aContext));
	}

	public TilesOverlay(final MapTileProviderBase aTileProvider, final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
		if (aTileProvider == null) {
			throw new IllegalArgumentException(
					"You must pass a valid tile provider to the tiles overlay.");
		}
		this.mTileProvider = aTileProvider;
	}

	@Override
	public void onDetach(final MapView pMapView) {
		this.mTileProvider.detach();
	}

	public int getMinimumZoomLevel() {
		return mTileProvider.getMinimumZoomLevel();
	}

	public int getMaximumZoomLevel() {
		return mTileProvider.getMaximumZoomLevel();
	}

	/**
	 * Whether to use the network connection if it's available.
	 */
	public boolean useDataConnection() {
		return mTileProvider.useDataConnection();
	}

	/**
	 * Set whether to use the network connection if it's available.
	 *
	 * @param aMode
	 *            if true use the network connection if it's available. if false don't use the
	 *            network connection even if it's available.
	 */
	public void setUseDataConnection(final boolean aMode) {
		mTileProvider.setUseDataConnection(aMode);
	}

	@Override
	protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {

		if (DEBUGMODE) {
			logger.trace("onDraw(" + shadow + ")");
		}

		if (shadow) {
			return;
		}

		// Calculate the half-world size
		final Projection pj = osmv.getProjection();
		final int zoomLevel = pj.getZoomLevel();
		mWorldSize_2 = TileSystem.MapSize(zoomLevel) >> 1;

		// Get the area we are drawing to
		mViewPort.set(pj.getScreenRect());

		// Translate the Canvas coordinates into Mercator coordinates
		mViewPort.offset(mWorldSize_2, mWorldSize_2);

		// Draw the tiles!
		drawTiles(c, pj.getZoomLevel(), TileSystem.getTileSize(), mViewPort);
	}

	/**
	 * This is meant to be a "pure" tile drawing function that doesn't take into account
	 * osmdroid-specific characteristics (like osmdroid's canvas's having 0,0 as the center rather
	 * than the upper-left corner). Once the tile is ready to be drawn, it is passed to
	 * onTileReadyToDraw where custom manipulations can be made before drawing the tile.
	 */
	public void drawTiles(final Canvas c, final int zoomLevel, final int tileSizePx,
			final Rect viewPort) {

		mTileLooper.loop(c, zoomLevel, tileSizePx, viewPort);

		// draw a cross at center in debug mode
		if (DEBUGMODE) {
			// final GeoPoint center = osmv.getMapCenter();
			final Point centerPoint = new Point(viewPort.centerX() - mWorldSize_2,
					viewPort.centerY() - mWorldSize_2);
			c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9, mDebugPaint);
			c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y, mDebugPaint);
		}

	}

	private final TileLooper mTileLooper = new TileLooper() {
		@Override
		public void initialiseLoop(final int pZoomLevel, final int pTileSizePx) {
			// make sure the cache is big enough for all the tiles
			final int numNeeded = (mLowerRight.y - mUpperLeft.y + 1) * (mLowerRight.x - mUpperLeft.x + 1);
			mTileProvider.ensureCapacity(numNeeded + mOvershootTileCache);
		}
		@Override
		public void handleTile(final Canvas pCanvas, final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {
			Drawable currentMapTile = mTileProvider.getMapTile(pTile);
			if (currentMapTile == null) {
				currentMapTile = getLoadingTile();
			}

			if (currentMapTile != null) {
				mTileRect.set(pX * pTileSizePx, pY * pTileSizePx, pX * pTileSizePx + pTileSizePx, pY
						* pTileSizePx + pTileSizePx);
				onTileReadyToDraw(pCanvas, currentMapTile, mTileRect);
			}

			if (DEBUGMODE) {
				mTileRect.set(pX * pTileSizePx, pY * pTileSizePx, pX * pTileSizePx + pTileSizePx, pY
						* pTileSizePx + pTileSizePx);
				mTileRect.offset(-mWorldSize_2, -mWorldSize_2);
				pCanvas.drawText(pTile.toString(), mTileRect.left + 1,
						mTileRect.top + mDebugPaint.getTextSize(), mDebugPaint);
				pCanvas.drawLine(mTileRect.left, mTileRect.top, mTileRect.right, mTileRect.top,
						mDebugPaint);
				pCanvas.drawLine(mTileRect.left, mTileRect.top, mTileRect.left, mTileRect.bottom,
						mDebugPaint);
			}
		}
		@Override
		public void finaliseLoop() {
		}
	};

	protected void onTileReadyToDraw(final Canvas c, final Drawable currentMapTile,
			final Rect tileRect) {
		tileRect.offset(-mWorldSize_2, -mWorldSize_2);
		currentMapTile.setBounds(tileRect);
		currentMapTile.draw(c);
	}

	@Override
	public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled) {
		this.mOptionsMenuEnabled = pOptionsMenuEnabled;
	}

	@Override
	public boolean isOptionsMenuEnabled() {
		return this.mOptionsMenuEnabled;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
			final MapView pMapView) {
		final SubMenu mapMenu = pMenu.addSubMenu(0, MENU_MAP_MODE + pMenuIdOffset, Menu.NONE,
				mResourceProxy.getString(ResourceProxy.string.map_mode)).setIcon(
				mResourceProxy.getDrawable(ResourceProxy.bitmap.ic_menu_mapmode));

		for (int a = 0; a < TileSourceFactory.getTileSources().size(); a++) {
			final ITileSource tileSource = TileSourceFactory.getTileSources().get(a);
			mapMenu.add(MENU_MAP_MODE + pMenuIdOffset, MENU_TILE_SOURCE_STARTING_ID + a
					+ pMenuIdOffset, Menu.NONE, tileSource.localizedName(mResourceProxy));
		}
		mapMenu.setGroupCheckable(MENU_MAP_MODE + pMenuIdOffset, true, true);

		final String title = pMapView.getResourceProxy().getString(
				pMapView.useDataConnection() ? ResourceProxy.string.offline_mode
						: ResourceProxy.string.online_mode);
		final Drawable icon = pMapView.getResourceProxy().getDrawable(
				ResourceProxy.bitmap.ic_menu_offline);
		pMenu.add(0, MENU_OFFLINE + pMenuIdOffset, Menu.NONE, title).setIcon(icon);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
			final MapView pMapView) {
		final int index = TileSourceFactory.getTileSources().indexOf(
				pMapView.getTileProvider().getTileSource());
		if (index >= 0) {
			pMenu.findItem(MENU_TILE_SOURCE_STARTING_ID + index + pMenuIdOffset).setChecked(true);
		}

		pMenu.findItem(MENU_OFFLINE + pMenuIdOffset).setTitle(
				pMapView.getResourceProxy().getString(
						pMapView.useDataConnection() ? ResourceProxy.string.offline_mode
								: ResourceProxy.string.online_mode));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
			final MapView pMapView) {

		final int menuId = pItem.getItemId() - pMenuIdOffset;
		if ((menuId >= MENU_TILE_SOURCE_STARTING_ID)
				&& (menuId < MENU_TILE_SOURCE_STARTING_ID
						+ TileSourceFactory.getTileSources().size())) {
			pMapView.setTileSource(TileSourceFactory.getTileSources().get(
					menuId - MENU_TILE_SOURCE_STARTING_ID));
			return true;
		} else if (menuId == MENU_OFFLINE) {
			final boolean useDataConnection = !pMapView.useDataConnection();
			pMapView.setUseDataConnection(useDataConnection);
			return true;
		} else {
			return false;
		}
	}

	public int getLoadingBackgroundColor() {
		return mLoadingBackgroundColor;
	}

	/**
	 * Set the color to use to draw the background while we're waiting for the tile to load.
	 *
	 * @param pLoadingBackgroundColor
	 *            the color to use. If the value is {@link Color.TRANSPARENT} then there will be no
	 *            loading tile.
	 */
	public void setLoadingBackgroundColor(final int pLoadingBackgroundColor) {
		if (mLoadingBackgroundColor != pLoadingBackgroundColor) {
			mLoadingBackgroundColor = pLoadingBackgroundColor;
			clearLoadingTile();
		}
	}

	public int getLoadingLineColor() {
		return mLoadingLineColor;
	}

	public void setLoadingLineColor(final int pLoadingLineColor) {
		if (mLoadingLineColor != pLoadingLineColor) {
			mLoadingLineColor = pLoadingLineColor;
			clearLoadingTile();
		}
	}

	private Drawable getLoadingTile() {
		if (mLoadingTile == null && mLoadingBackgroundColor != Color.TRANSPARENT) {
			try {
				final int tileSize = mTileProvider.getTileSource() != null ? mTileProvider
						.getTileSource().getTileSizePixels() : 256;
				final Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize,
						Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);
				final Paint paint = new Paint();
				canvas.drawColor(mLoadingBackgroundColor);
				paint.setColor(mLoadingLineColor);
				paint.setStrokeWidth(0);
				final int lineSize = tileSize / 16;
				for (int a = 0; a < tileSize; a += lineSize) {
					canvas.drawLine(0, a, tileSize, a, paint);
					canvas.drawLine(a, 0, a, tileSize, paint);
				}
				mLoadingTile = new BitmapDrawable(bitmap);
			} catch (final OutOfMemoryError e) {
				logger.error("OutOfMemoryError getting loading tile");
				System.gc();
			}
		}
		return mLoadingTile;
	}

	private void clearLoadingTile() {
		final BitmapDrawable bitmapDrawable = mLoadingTile;
		mLoadingTile = null;
		if (bitmapDrawable != null) {
			bitmapDrawable.getBitmap().recycle();
		}
	}

	/**
	 * Set this to overshoot the tile cache. By default the TilesOverlay only creates a cache large
	 * enough to hold the minimum number of tiles necessary to draw to the screen. Setting this
	 * value will allow you to overshoot the tile cache and allow more tiles to be cached. This
	 * increases the memory usage, but increases drawing performance.
	 * 
	 * @param overshootTileCache
	 *            the number of tiles to overshoot the tile cache by
	 */
	public void setOvershootTileCache(int overshootTileCache) {
		mOvershootTileCache = overshootTileCache;
	}

	/**
	 * Get the tile cache overshoot value.
	 * 
	 * @return the number of tiles to overshoot tile cache
	 */
	public int getOvershootTileCache() {
		return mOvershootTileCache;
	}
}

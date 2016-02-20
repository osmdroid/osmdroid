package org.osmdroid.views.overlay;

import org.osmdroid.library.R;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.TileLooper;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import org.osmdroid.api.IMapView;

/**
 * These objects are the principle consumer of map tiles.
 *
 * see {@link MapTile} for an overview of how tiles are acquired by this overlay.
 *
 */

public class TilesOverlay extends Overlay implements IOverlayMenuProvider {


	public static final int MENU_MAP_MODE = getSafeMenuId();
	public static final int MENU_TILE_SOURCE_STARTING_ID = getSafeMenuIdSequence(TileSourceFactory
			.getTileSources().size());
	public static final int MENU_OFFLINE = getSafeMenuId();

	private Context ctx;
	/** Current tile source */
	protected final MapTileProviderBase mTileProvider;

	/* to avoid allocations during draw */
	protected final Paint mDebugPaint = new Paint();
	private final Rect mTileRect = new Rect();
	private final Point mTilePoint = new Point();
	private final Rect mViewPort = new Rect();
	private Point mTopLeftMercator = new Point();
	private Point mBottomRightMercator = new Point();
	private Point mTilePointMercator = new Point();

	private Projection mProjection;

	private boolean mOptionsMenuEnabled = true;

	/** A drawable loading tile **/
	private BitmapDrawable mLoadingTile = null;
	private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
	private int mLoadingLineColor = Color.rgb(200, 192, 192);

	/** For overshooting the tile cache **/
	private int mOvershootTileCache = 0;

	//Issue 133 night mode
	private ColorFilter currentColorFilter=null;
	final static float[] negate ={
		-1.0f,0,0,0,255,        //red
		0,-1.0f,0,0,255,//green
		0,0,-1.0f,0,255,//blue
		0,0,0,1.0f,0 //alpha
	};
	/**
	 * provides a night mode like affect by inverting the map tile colors
	 */
	public final static ColorFilter INVERT_COLORS = new ColorMatrixColorFilter(negate);


	public TilesOverlay(final MapTileProviderBase aTileProvider, final Context aContext) {
		super(aContext);
		this.ctx=aContext;
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
	protected void draw(Canvas c, MapView osmv, boolean shadow) {

		if (DEBUGMODE) {
               Log.d(IMapView.LOGTAG,"onDraw(" + shadow + ")");
		}

		if (shadow) {
			return;
		}

		Projection projection = osmv.getProjection();

		// Get the area we are drawing to
		Rect screenRect = projection.getScreenRect();
		projection.toMercatorPixels(screenRect.left, screenRect.top, mTopLeftMercator);
		projection.toMercatorPixels(screenRect.right, screenRect.bottom, mBottomRightMercator);
		mViewPort.set(mTopLeftMercator.x, mTopLeftMercator.y, mBottomRightMercator.x,
				mBottomRightMercator.y);

		// Draw the tiles!
		drawTiles(c, projection, projection.getZoomLevel(), TileSystem.getTileSize(), mViewPort);
	}

	/**
	 * This is meant to be a "pure" tile drawing function that doesn't take into account
	 * osmdroid-specific characteristics (like osmdroid's canvas's having 0,0 as the center rather
	 * than the upper-left corner). Once the tile is ready to be drawn, it is passed to
	 * onTileReadyToDraw where custom manipulations can be made before drawing the tile.
	 */
	public void drawTiles(final Canvas c, final Projection projection, final int zoomLevel,
			final int tileSizePx, final Rect viewPort) {

		mProjection = projection;
		mTileLooper.loop(c, zoomLevel, tileSizePx, viewPort);

		// draw a cross at center in debug mode
		if (DEBUGMODE) {
			// final GeoPoint center = osmv.getMapCenter();
			final Point centerPoint = new Point(viewPort.centerX(), viewPort.centerY());
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
			boolean isReusable = currentMapTile instanceof ReusableBitmapDrawable;
			final ReusableBitmapDrawable reusableBitmapDrawable =
					isReusable ? (ReusableBitmapDrawable) currentMapTile : null;
			if (currentMapTile == null) {
				currentMapTile = getLoadingTile();
			}

			if (currentMapTile != null) {
				mTilePoint.set(pX * pTileSizePx, pY * pTileSizePx);
				mTileRect.set(mTilePoint.x, mTilePoint.y, mTilePoint.x + pTileSizePx, mTilePoint.y
						+ pTileSizePx);
				if (isReusable) {
					reusableBitmapDrawable.beginUsingDrawable();
				}
				try {
					if (isReusable && !((ReusableBitmapDrawable) currentMapTile).isBitmapValid()) {
						currentMapTile = getLoadingTile();
						isReusable = false;
					}
					onTileReadyToDraw(pCanvas, currentMapTile, mTileRect);
				} finally {
					if (isReusable)
						reusableBitmapDrawable.finishUsingDrawable();
				}
			}

			if (DEBUGMODE) {
				mTileRect.set(pX * pTileSizePx, pY * pTileSizePx, pX * pTileSizePx + pTileSizePx, pY
						* pTileSizePx + pTileSizePx);
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
		currentMapTile.setColorFilter(currentColorFilter);
		mProjection.toPixelsFromMercator(tileRect.left, tileRect.top, mTilePointMercator);
		tileRect.offsetTo(mTilePointMercator.x, mTilePointMercator.y);
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
				R.string.map_mode).setIcon(R.drawable.ic_menu_mapmode);

		for (int a = 0; a < TileSourceFactory.getTileSources().size(); a++) {
			final ITileSource tileSource = TileSourceFactory.getTileSources().get(a);
			mapMenu.add(MENU_MAP_MODE + pMenuIdOffset, MENU_TILE_SOURCE_STARTING_ID + a
					+ pMenuIdOffset, Menu.NONE, tileSource.name());
		}
		mapMenu.setGroupCheckable(MENU_MAP_MODE + pMenuIdOffset, true, true);

		final String title = ctx.getString(
				pMapView.useDataConnection() ? R.string.set_mode_offline
						: R.string.set_mode_online);
		final Drawable icon = ctx.getResources().getDrawable(R.drawable.ic_menu_offline);
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
						pMapView.useDataConnection() ? R.string.set_mode_offline
								: R.string.set_mode_online);

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
	 *            the color to use. If the value is {@link Color#TRANSPARENT} then there will be no
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
				Log.e(IMapView.LOGTAG, "OutOfMemoryError getting loading tile");
				System.gc();
			} catch (final NullPointerException e) {
				Log.e(IMapView.LOGTAG, "NullPointerException getting loading tile");
				System.gc();
			}
		}
		return mLoadingTile;
	}

	private void clearLoadingTile() {
		final BitmapDrawable bitmapDrawable = mLoadingTile;
		mLoadingTile = null;
		// Only recycle if we are running on a project less than 2.3.3 Gingerbread.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			if (bitmapDrawable != null) {
				bitmapDrawable.getBitmap().recycle();
			}
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


	/**
	 * sets the current color filter, which is applied to tiles before being drawn to the screen.
	 * Use this to enable night mode or any other tile rendering adjustment as necessary. use null to clear.
	 * INVERT_COLORS provides color inversion for convenience and to support the previous night mode
	 * @param filter
	 * @since 5.1
     */
	public void setColorFilter(ColorFilter filter) {

		this.currentColorFilter=filter;
	}
}

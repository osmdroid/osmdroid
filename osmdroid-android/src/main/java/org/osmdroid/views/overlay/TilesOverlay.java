package org.osmdroid.views.overlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.R;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.RectL;
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
 * A {@link TilesOverlay} is responsible to display a {@link MapTile}.
 *
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

	protected Drawable userSelectedLoadingDrawable = null;
	/* to avoid allocations during draw */
	protected final Paint mDebugPaint = new Paint();
	private final Rect mTileRect = new Rect();
	protected final RectL mViewPort = new RectL();

	protected Projection mProjection;
	private boolean mOptionsMenuEnabled = true;

	/** A drawable loading tile **/
	private BitmapDrawable mLoadingTile = null;
	private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
	private int mLoadingLineColor = Color.rgb(200, 192, 192);

	/** For overshooting the tile cache **/
	private int mOvershootTileCache = 0;

	private boolean horizontalWrapEnabled = true;
	private boolean verticalWrapEnabled = true;

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
		this(aTileProvider, aContext, true, true);
	}

	public TilesOverlay(final MapTileProviderBase aTileProvider, final Context aContext, boolean horizontalWrapEnabled, boolean verticalWrapEnabled) {
		super();
		this.ctx=aContext;
		if (aTileProvider == null) {
			throw new IllegalArgumentException(
					"You must pass a valid tile provider to the tiles overlay.");
		}
		this.mTileProvider = aTileProvider;
		this.horizontalWrapEnabled = horizontalWrapEnabled;
		this.verticalWrapEnabled = verticalWrapEnabled;
		this.mTileLooper.setHorizontalWrapEnabled(horizontalWrapEnabled);
		this.mTileLooper.setVerticalWrapEnabled(verticalWrapEnabled);
	}

	/**
	 * See issue https://github.com/osmdroid/osmdroid/issues/330
	 * customizable override for the grey grid
	 * @since 5.2+
	 * @param drawable
     */
	public void setLoadingDrawable(final Drawable drawable){
		userSelectedLoadingDrawable = drawable;
	}

	@Override
	public void onDetach(final MapView pMapView) {
		this.mTileProvider.detach();
		ctx=null;
		if (mLoadingTile!=null) {
			// Only recycle if we are running on a project less than 2.3.3 Gingerbread.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				if (mLoadingTile instanceof BitmapDrawable) {
					final Bitmap bitmap = ((BitmapDrawable) mLoadingTile).getBitmap();
					if (bitmap != null) {
						bitmap.recycle();
					}
				}
			}
			if (mLoadingTile instanceof ReusableBitmapDrawable)
				BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) mLoadingTile);
		}
		mLoadingTile=null;
		if (userSelectedLoadingDrawable!=null){
			// Only recycle if we are running on a project less than 2.3.3 Gingerbread.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				if (userSelectedLoadingDrawable instanceof BitmapDrawable) {
					final Bitmap bitmap = ((BitmapDrawable) userSelectedLoadingDrawable).getBitmap();
					if (bitmap != null) {
						bitmap.recycle();
					}
				}
			}
			if (userSelectedLoadingDrawable instanceof ReusableBitmapDrawable)
				BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) userSelectedLoadingDrawable);
		}
		userSelectedLoadingDrawable=null;
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
	public void draw(Canvas c, MapView osmv, boolean shadow) {

		if (Configuration.getInstance().isDebugTileProviders()) {
               Log.d(IMapView.LOGTAG,"onDraw(" + shadow + ")");
		}

		if (shadow) {
			return;
		}

		setProjection(osmv.getProjection());

		// Get the area we are drawing to
		getProjection().getMercatorViewPort(mViewPort);

		// Draw the tiles!
		drawTiles(c, getProjection(), getProjection().getZoomLevel(), mViewPort);
	}

	/**
	 * This is meant to be a "pure" tile drawing function that doesn't take into account
	 * osmdroid-specific characteristics (like osmdroid's canvas's having 0,0 as the center rather
	 * than the upper-left corner). Once the tile is ready to be drawn, it is passed to
	 * onTileReadyToDraw where custom manipulations can be made before drawing the tile.
	 */
	public void drawTiles(final Canvas c, final Projection projection, final double zoomLevel, final RectL viewPort) {
		mProjection = projection;
		mTileLooper.loop(zoomLevel, viewPort, c);
	}

	/**
	 * @since 6.0
	 */
	protected class OverlayTileLooper extends TileLooper {

		private Canvas mCanvas;

		public OverlayTileLooper() {
		}

		public OverlayTileLooper(boolean horizontalWrapEnabled, boolean verticalWrapEnabled) {
			super(horizontalWrapEnabled, verticalWrapEnabled);
		}

		public void loop(final double pZoomLevel, final RectL pViewPort, final Canvas pCanvas) {
			mCanvas = pCanvas;
			loop(pZoomLevel, pViewPort);
		}

		@Override
		public void initialiseLoop() {
			// make sure the cache is big enough for all the tiles
			final int width = mTiles.right - mTiles.left + 1;
			final int height = mTiles.bottom - mTiles.top + 1;
			final int numNeeded = height * width;
			mTileProvider.ensureCapacity(numNeeded + mOvershootTileCache);
		}
		@Override
		public void handleTile(MapTile pTile, int pX, int pY) {
			Drawable currentMapTile = mTileProvider.getMapTile(pTile);
			boolean isReusable = currentMapTile instanceof ReusableBitmapDrawable;
			final ReusableBitmapDrawable reusableBitmapDrawable =
					isReusable ? (ReusableBitmapDrawable) currentMapTile : null;
			if (currentMapTile == null) {
				currentMapTile = getLoadingTile();
			}

			if (currentMapTile != null) {
				mProjection.getPixelFromTile(pX, pY, mTileRect);
				if (isReusable) {
					reusableBitmapDrawable.beginUsingDrawable();
				}
				try {
					if (isReusable && !((ReusableBitmapDrawable) currentMapTile).isBitmapValid()) {
						currentMapTile = getLoadingTile();
						isReusable = false;
					}
					onTileReadyToDraw(mCanvas, currentMapTile, mTileRect);
				} finally {
					if (isReusable)
						reusableBitmapDrawable.finishUsingDrawable();
				}
			}

			if (Configuration.getInstance().isDebugTileProviders()) {
				mProjection.getPixelFromTile(pX, pY, mTileRect);
				mCanvas.drawText(pTile.toString(), mTileRect.left + 1,
						mTileRect.top + mDebugPaint.getTextSize(), mDebugPaint);
				mCanvas.drawLine(mTileRect.left, mTileRect.top, mTileRect.right, mTileRect.top,
						mDebugPaint);
				mCanvas.drawLine(mTileRect.left, mTileRect.top, mTileRect.left, mTileRect.bottom,
						mDebugPaint);
			}
		}
	}

	private final OverlayTileLooper mTileLooper = new OverlayTileLooper();
	private final Rect mIntersectionRect = new Rect();

	private Rect mCanvasRect;

	protected void setCanvasRect(final Rect pCanvasRect) {
		mCanvasRect = pCanvasRect;
	}
	protected Rect getCanvasRect() {
		return mCanvasRect;
	}
	protected void setProjection(final Projection pProjection) {
		mProjection = pProjection;
	}
	protected Projection getProjection() {
		return mProjection;
	}


	protected void onTileReadyToDraw(final Canvas c, final Drawable currentMapTile, final Rect tileRect) {
		currentMapTile.setColorFilter(currentColorFilter);
		currentMapTile.setBounds(tileRect.left, tileRect.top, tileRect.right, tileRect.bottom);
		final Rect canvasRect = getCanvasRect();
		if (canvasRect == null) {
			currentMapTile.draw(c);
			return;
		}
		// Save the current clipping bounds
		c.save();
		// Check to see if the drawing area intersects with the minimap area
		if (mIntersectionRect.setIntersect(c.getClipBounds(), canvasRect)) {
			// If so, then clip that area
			c.clipRect(mIntersectionRect);

			// Draw the tile, which will be appropriately clipped
			currentMapTile.draw(c);
		}
		c.restore();
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

		if (ctx!=null) {
			final String title = ctx.getString(
					pMapView.useDataConnection() ? R.string.set_mode_offline
							: R.string.set_mode_online);
			final Drawable icon = ctx.getResources().getDrawable(R.drawable.ic_menu_offline);
			pMenu.add(0, MENU_OFFLINE + pMenuIdOffset, Menu.NONE, title).setIcon(icon);
		}
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
		if (userSelectedLoadingDrawable!=null)
			return userSelectedLoadingDrawable;
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

	public boolean isHorizontalWrapEnabled() {
		return horizontalWrapEnabled;
	}

	public void setHorizontalWrapEnabled(boolean horizontalWrapEnabled) {
		this.horizontalWrapEnabled = horizontalWrapEnabled;
		this.mTileLooper.setHorizontalWrapEnabled(horizontalWrapEnabled);

	}

	public boolean isVerticalWrapEnabled() {
		return verticalWrapEnabled;
	}

	public void setVerticalWrapEnabled(boolean verticalWrapEnabled) {
		this.verticalWrapEnabled = verticalWrapEnabled;
		this.mTileLooper.setVerticalWrapEnabled(verticalWrapEnabled);

	}
}

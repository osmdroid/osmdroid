package org.osmdroid.views.overlay;

import microsoft.mappoint.TileSystem;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.MyMath;
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
	protected final Paint mPaint = new Paint();
	private final Rect mTileRect = new Rect();
	private final Rect mViewPort = new Rect();
	private Point mUpperLeft;
	private Point mLowerRight;

	private boolean mOptionsMenuEnabled = true;

	private int mWorldSize_2;

	/** A drawable loading tile **/
	private BitmapDrawable mLoadingTile = null;
	private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
	private int mLoadingLineColor = Color.rgb(200, 192, 192);

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

	public void setAlpha(final int a) {
		this.mPaint.setAlpha(a);
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
		mWorldSize_2 = TileSystem.MapSize(zoomLevel) / 2;

		// Get the area we are drawing to
		mViewPort.set(pj.getScreenRect());

		// Translate the Canvas coordinates into Mercator coordinates
		mViewPort.offset(mWorldSize_2, mWorldSize_2);

		// Draw the tiles!
		drawTiles(c, pj.getZoomLevel(), TileSystem.getTileSize(), mViewPort);
	}

	/**
	 * This is meant to be a "pure" tile drawing function that doesn't take into account
	 * platform-specific characteristics (like Android's canvas's having 0,0 as the center rather
	 * than the upper-left corner).
	 */
	public void drawTiles(final Canvas c, final int zoomLevel, final int tileSizePx,
			final Rect viewPort) {

		// Calculate the amount of tiles needed for each side around the center one.
		mUpperLeft = TileSystem.PixelXYToTileXY(viewPort.left, viewPort.top, null);
		mUpperLeft.offset(-1, -1);
		mLowerRight = TileSystem.PixelXYToTileXY(viewPort.right, viewPort.bottom, null);

		final int mapTileUpperBound = 1 << zoomLevel;

		// make sure the cache is big enough for all the tiles
		final int numNeeded = (mLowerRight.y - mUpperLeft.y + 1) * (mLowerRight.x - mUpperLeft.x + 1);
		mTileProvider.ensureCapacity(numNeeded);

		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
			for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
				// Construct a MapTile to request from the tile provider.
				final int tileY = MyMath.mod(y, mapTileUpperBound);
				final int tileX = MyMath.mod(x, mapTileUpperBound);
				final MapTile tile = new MapTile(zoomLevel, tileX, tileY);

				Drawable currentMapTile = mTileProvider.getMapTile(tile);
				if (currentMapTile == null) {
					currentMapTile = getLoadingTile();
				}

				if (currentMapTile != null) {
					mTileRect.set(x * tileSizePx, y * tileSizePx, x * tileSizePx + tileSizePx, y
							* tileSizePx + tileSizePx);
					onTileReadyToDraw(c, currentMapTile, mTileRect);
				}

				if (DEBUGMODE) {
					mTileRect.set(x * tileSizePx, y * tileSizePx, x * tileSizePx + tileSizePx, y
							* tileSizePx + tileSizePx);
					c.drawText(tile.toString(), mTileRect.left + 1,
							mTileRect.top + mPaint.getTextSize(), mPaint);
					c.drawLine(mTileRect.left, mTileRect.top, mTileRect.right, mTileRect.top,
							mPaint);
					c.drawLine(mTileRect.left, mTileRect.top, mTileRect.left, mTileRect.bottom,
							mPaint);
				}
			}
		}

		// draw a cross at center in debug mode
		if (DEBUGMODE) {
			// final GeoPoint center = osmv.getMapCenter();
			final Point centerPoint = new Point(viewPort.centerX() - mWorldSize_2,
					viewPort.centerY() - mWorldSize_2);
			c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9, mPaint);
			c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y, mPaint);
		}

	}

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
}

package org.osmdroid.util;

import org.osmdroid.tileprovider.MapTile;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * A class that will loop around all the map tiles in the given viewport.
 */
public abstract class TileLooper {

	protected final Point mUpperLeft = new Point();
	protected final Point mLowerRight = new Point();

	private boolean wrapEnabled = true;

	public TileLooper() {
		this(false);
	}

	public TileLooper(boolean wrapEnabled) {
		this.wrapEnabled = wrapEnabled;
	}

	public final void loop(final Canvas pCanvas, final int pZoomLevel, final int pTileSizePx, final Rect pViewPort) {
		// Calculate the amount of tiles needed for each side around the center one.
		TileSystem.PixelXYToTileXY(pViewPort.left, pViewPort.top, mUpperLeft);
		mUpperLeft.offset(-1, -1);
		TileSystem.PixelXYToTileXY(pViewPort.right, pViewPort.bottom, mLowerRight);

		final int mapTileUpperBound = 1 << pZoomLevel;

		initialiseLoop(pZoomLevel, pTileSizePx);

		if(wrapEnabled) {
			/* Draw all the MapTiles (from the upper left to the lower right). */
			for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
				for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
					// Construct a MapTile to request from the tile provider.
					final int tileY = MyMath.mod(y, mapTileUpperBound);
					final int tileX = MyMath.mod(x, mapTileUpperBound);
					final MapTile tile = new MapTile(pZoomLevel, tileX, tileY);
					handleTile(pCanvas, pTileSizePx, tile, x, y);
				}
			}
		}
		else {
			for (int y = this.mUpperLeft.y; y <= this.mLowerRight.y; ++y) {
				for (int x = this.mUpperLeft.x; x <= this.mLowerRight.x; ++x) {
					int tileX = x;
					int tileY = y;
					if (tileY < mapTileUpperBound && tileY >= 0 && x < mapTileUpperBound && tileX >= 0) {
						MapTile tile = new MapTile(pZoomLevel, tileX, tileY);
						this.handleTile(pCanvas, pTileSizePx, tile, x, y);
					}
				}
			}
		}

		finaliseLoop();
	}

	public abstract void initialiseLoop(int pZoomLevel, int pTileSizePx);

	public abstract void handleTile(Canvas pCanvas, int pTileSizePx, MapTile pTile, int pX, int pY);

	public abstract void finaliseLoop();

	public boolean isWrapEnabled() {
		return wrapEnabled;
	}

	public void setWrapEnabled(boolean wrapEnabled) {
		this.wrapEnabled = wrapEnabled;
	}
}

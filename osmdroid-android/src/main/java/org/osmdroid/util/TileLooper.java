package org.osmdroid.util;

import android.graphics.Rect;

import org.osmdroid.tileprovider.MapTile;

/**
 * A class that will loop around all the map tiles in the given viewport.
 */
public abstract class TileLooper {

	protected final Rect mTiles = new Rect();
	protected int mTileZoomLevel;
	private boolean wrapEnabled = true;

	public TileLooper() {
		this(false);
	}

	public TileLooper(boolean wrapEnabled) {
		this.wrapEnabled = wrapEnabled;
	}

	protected void loop(final double pZoomLevel, final Rect pViewPort) {
		TileSystem.PixelXYToTileXY(pViewPort, TileSystem.getTileSize(pZoomLevel), mTiles);
		mTileZoomLevel = TileSystem.getInputTileZoomLevel(pZoomLevel);

		initialiseLoop();

		final int mapTileUpperBound = 1 << mTileZoomLevel;

		int width = mTiles.right - mTiles.left + 1; // handling the modulo
		if (width <= 0) {
			width += mapTileUpperBound;
		}
		int height = mTiles.bottom - mTiles.top + 1; // handling the modulo
		if (height <= 0) {
			height += mapTileUpperBound;
		}

		/* Draw all the MapTiles (from the upper left to the lower right). */
		if(wrapEnabled) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					final int x = mTiles.left + i;
					final int y = mTiles.top + j;
					final int tileX = MyMath.mod(x, mapTileUpperBound);
					final int tileY = MyMath.mod(y, mapTileUpperBound);
					final MapTile tile = new MapTile(mTileZoomLevel, tileX, tileY);
					handleTile(tile, x, y);
				}
			}
		} else {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					final int x = mTiles.left + i;
					final int y = mTiles.top + j;
					final int tileX = x;
					final int tileY = y;
					if (tileY < mapTileUpperBound && tileY >= 0 && x < mapTileUpperBound && tileX >= 0) {
						final MapTile tile = new MapTile(mTileZoomLevel, tileX, tileY);
						handleTile(tile, x, y);
					}
				}
			}
		}

		finaliseLoop();
	}

	public void initialiseLoop() {}

	public abstract void handleTile(final MapTile pTile, final int pX, final int pY);

	public void finaliseLoop() {}

	public boolean isWrapEnabled() {
		return wrapEnabled;
	}

	public void setWrapEnabled(boolean wrapEnabled) {
		this.wrapEnabled = wrapEnabled;
	}

}

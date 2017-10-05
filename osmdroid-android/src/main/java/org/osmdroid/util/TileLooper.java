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

	protected void loop(final double pZoomLevel, final RectL pMercatorViewPort) {
		TileSystem.getTileFromMercator(pMercatorViewPort, TileSystem.getTileSize(pZoomLevel), mTiles);
		mTileZoomLevel = TileSystem.getInputTileZoomLevel(pZoomLevel);

		initialiseLoop();

		final int mapTileUpperBound = 1 << mTileZoomLevel;

		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int i = mTiles.left ; i <= mTiles.right ; i ++) {
			for (int j = mTiles.top ; j <= mTiles.bottom ; j ++) {
				if(wrapEnabled || (i >= 0 && i < mapTileUpperBound && j >= 0 && j < mapTileUpperBound)) {
					final int tileX = wrapEnabled ? MyMath.mod(i, mapTileUpperBound) : i;
					final int tileY = wrapEnabled ? MyMath.mod(j, mapTileUpperBound) : j;
					final MapTile tile = new MapTile(mTileZoomLevel, tileX, tileY);
					handleTile(tile, i, j);
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

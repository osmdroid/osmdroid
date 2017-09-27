package org.osmdroid.util;

import android.graphics.Rect;

import org.osmdroid.tileprovider.MapTile;

/**
 * A class that will loop around all the map tiles in the given viewport.
 */
public abstract class TileLooper {

	protected final Rect mTiles = new Rect();
	protected int mTileZoomLevel;

	protected void loop(final double pZoomLevel, final RectL pMercatorViewPort) {
		TileSystem.getTileFromMercator(pMercatorViewPort, TileSystem.getTileSize(pZoomLevel), mTiles);
		mTileZoomLevel = TileSystem.getInputTileZoomLevel(pZoomLevel);

		initialiseLoop();

		final int mapTileUpperBound = 1 << mTileZoomLevel;

		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int i = mTiles.left ; i <= mTiles.right ; i ++) {
			for (int j = mTiles.top ; j <= mTiles.bottom ; j ++) {
				final int tileX = MyMath.mod(i, mapTileUpperBound);
				final int tileY = MyMath.mod(j, mapTileUpperBound);
				final MapTile tile = new MapTile(mTileZoomLevel, tileX, tileY);
				handleTile(tile, i, j);
			}
		}

		finaliseLoop();
	}

	public void initialiseLoop() {}

	public abstract void handleTile(final MapTile pTile, final int pX, final int pY);

	public void finaliseLoop() {}
}

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

	public final void loop(final Canvas pCanvas, final double pZoomLevel, final int pInputTileSizePx, final Rect pViewPort) {
		// Calculate the amount of tiles needed for each side around the center one.
		final double outputTileSizePx = TileSystem.getTileSize(pZoomLevel);
		TileSystem.PixelXYToTileXY(pViewPort.left, pViewPort.top, outputTileSizePx, mUpperLeft);
		mUpperLeft.offset(-1, -1);
		TileSystem.PixelXYToTileXY(pViewPort.right, pViewPort.bottom, outputTileSizePx, mLowerRight);

		final int tileZoomLevel = TileSystem.getInputTileZoomLevel(pZoomLevel);
		final int mapTileUpperBound = 1 << tileZoomLevel;

		initialiseLoop(pZoomLevel, pInputTileSizePx);

		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
			for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
				// Construct a MapTile to request from the tile provider.
				final int tileY = MyMath.mod(y, mapTileUpperBound);
				final int tileX = MyMath.mod(x, mapTileUpperBound);
				final MapTile tile = new MapTile(tileZoomLevel, tileX, tileY);
				handleTile(pCanvas, outputTileSizePx, tile, x, y);
			}
		}

		finaliseLoop();
	}

	public abstract void initialiseLoop(double pZoomLevel, int pInputTileSizePx);

	public abstract void handleTile(Canvas pCanvas, double pOutputTileSizePx, MapTile pTile, int pX, int pY);

	public abstract void finaliseLoop();
}

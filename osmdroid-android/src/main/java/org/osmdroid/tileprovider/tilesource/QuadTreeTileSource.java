package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.tileprovider.MapTile;

public class QuadTreeTileSource extends OnlineTileSourceBase {

	public QuadTreeTileSource(final String aName, 
			final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
			final String aImageFilenameEnding, final String[] aBaseUrl) {
		super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
				aImageFilenameEnding, aBaseUrl);
	}

	@Override
	public String getTileURLString(final MapTile aTile) {
		return getBaseUrl() + quadTree(aTile) + mImageFilenameEnding;
	}

	/**
	 * Converts TMS tile coordinates to QuadTree
	 * 
	 * @param aTile
	 *            The tile coordinates to convert
	 * @return The QuadTree as String.
	 */
	protected String quadTree(final MapTile aTile) {
		final StringBuilder quadKey = new StringBuilder();
		for (int i = aTile.getZoomLevel(); i > 0; i--) {
			int digit = 0;
			final int mask = 1 << (i - 1);
			if ((aTile.getX() & mask) != 0)
				digit += 1;
			if ((aTile.getY() & mask) != 0)
				digit += 2;
			quadKey.append("" + digit);
		}

		return quadKey.toString();
	}

}

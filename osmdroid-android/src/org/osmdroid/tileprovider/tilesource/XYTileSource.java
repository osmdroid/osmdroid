package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.OpenStreetMapTile;

class XYTileSource extends OnlineTileSourceBase {

	XYTileSource(final String aName, final string aResourceId, final int aZoomMinLevel,
			final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
			final String... aBaseUrl) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
				aImageFilenameEnding, aBaseUrl);
	}

	@Override
	public String getTileURLString(final OpenStreetMapTile aTile) {
		return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY()
				+ mImageFilenameEnding;
	}
}

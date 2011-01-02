package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.OpenStreetMapTile;

class XYRenderer extends OnlineTileSourceBase {

	XYRenderer(String aName, string aResourceId, int aZoomMinLevel,
			int aZoomMaxLevel, int aTileSizePixels,
			String aImageFilenameEnding, String... aBaseUrl) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel,
				aTileSizePixels, aImageFilenameEnding, aBaseUrl);
	}

	@Override
	public String getTileURLString(final OpenStreetMapTile aTile) {
		return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getX() + "/"
				+ aTile.getY() + mImageFilenameEnding;
	}
}

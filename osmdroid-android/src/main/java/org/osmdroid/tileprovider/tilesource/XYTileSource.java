package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.tileprovider.MapTile;

/**
 * An implementation of {@link org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase}
 */
public class XYTileSource extends OnlineTileSourceBase {

	public XYTileSource(final String aName, final int aZoomMinLevel,
			final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
			final String[] aBaseUrl) {
		this(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
				aImageFilenameEnding, aBaseUrl,null);
	}

	public XYTileSource(final String aName, final int aZoomMinLevel,
						final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
						final String[] aBaseUrl, final String copyright) {
		super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
			aImageFilenameEnding, aBaseUrl,copyright);
	}

	@Override
	public String getTileURLString(final MapTile aTile) {
		return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY()
				+ mImageFilenameEnding;
	}
}

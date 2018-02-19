package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.util.MapTileIndex;

/**
 * An implementation of {@link org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase}
 */
public class XYTileSource extends OnlineTileSourceBase {

	/**
	 *
	 * @param aName this is used for caching purposes, make sure it is consistent and unique
	 * @param aZoomMinLevel
	 * @param aZoomMaxLevel
	 * @param aTileSizePixels
	 * @param aImageFilenameEnding
	 * @param aBaseUrl
	 */
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
	public String toString(){
		return name();
	}
	@Override
	public String getTileURLString(final long pMapTileIndex) {
		return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex)
				+ mImageFilenameEnding;
	}
}

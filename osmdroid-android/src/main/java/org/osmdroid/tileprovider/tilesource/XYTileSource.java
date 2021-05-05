package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.util.MapTileIndex;

/**
 * An implementation of {@link org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase}
 */
public class XYTileSource extends OnlineTileSourceBase {

    public XYTileSource(final String aName, final int aZoomMinLevel,
                        final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
                        final String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, aBaseUrl);
    }

    public XYTileSource(final String aName, final int aZoomMinLevel,
                        final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
                        final String[] aBaseUrl, final String copyright) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, aBaseUrl, copyright);
    }

    /**
     * @param aName this is used for caching purposes, make sure it is consistent and unique
     * @since 6.1.0
     */
    public XYTileSource(final String aName, final int aZoomMinLevel,
                        final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
                        final String[] aBaseUrl, final String copyright,
                        final TileSourcePolicy pTileSourcePolicy) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, aBaseUrl, copyright, pTileSourcePolicy);
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public String getTileURLString(final long pMapTileIndex) {
        return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex)
                + mImageFilenameEnding;
    }
}

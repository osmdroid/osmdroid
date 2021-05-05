package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.util.MapTileIndex;

public class QuadTreeTileSource extends OnlineTileSourceBase {

    public QuadTreeTileSource(final String aName,
                              final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
                              final String aImageFilenameEnding, final String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, aBaseUrl);
    }

    @Override
    public String getTileURLString(final long pMapTileIndex) {
        return getBaseUrl() + quadTree(pMapTileIndex) + mImageFilenameEnding;
    }

    /**
     * Converts TMS tile coordinates to QuadTree
     *
     * @param pMapTileIndex The tile coordinates to convert
     * @return The QuadTree as String.
     */
    protected String quadTree(final long pMapTileIndex) {
        final StringBuilder quadKey = new StringBuilder();
        for (int i = MapTileIndex.getZoom(pMapTileIndex); i > 0; i--) {
            int digit = 0;
            final int mask = 1 << (i - 1);
            if ((MapTileIndex.getX(pMapTileIndex) & mask) != 0)
                digit += 1;
            if ((MapTileIndex.getY(pMapTileIndex) & mask) != 0)
                digit += 2;
            quadKey.append("" + digit);
        }

        return quadKey.toString();
    }

}

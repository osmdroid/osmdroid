package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.util.MapTileIndex;

/**
 * created on 12/24/2016.
 *
 * @author Alex O'Ree
 */

public abstract class TMSOnlineTileSourceBase extends OnlineTileSourceBase {
    /**
     * Constructor
     *
     * @param aName                a human-friendly name for this tile source
     * @param aZoomMinLevel        the minimum zoom level this tile source can provide
     * @param aZoomMaxLevel        the maximum zoom level this tile source can provide
     * @param aTileSizePixels      the tile size in pixels this tile source provides
     * @param aImageFilenameEnding the file name extension used when constructing the filename
     * @param aBaseUrl             the base url(s) of the tile server used when constructing the url to download the tiles
     */
    public TMSOnlineTileSourceBase(String aName, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
    }

    @Override
    public String getTileRelativeFilenameString(final long pMapTileIndex) {

        int y_tms = (1 << MapTileIndex.getZoom(pMapTileIndex)) - MapTileIndex.getY(pMapTileIndex) - 1;
        final StringBuilder sb = new StringBuilder();
        sb.append(pathBase());
        sb.append('/');
        sb.append(MapTileIndex.getZoom(pMapTileIndex));
        sb.append('/');
        sb.append(MapTileIndex.getX(pMapTileIndex));
        sb.append('/');
        sb.append(y_tms);
        sb.append(imageFilenameEnding());
        return sb.toString();
    }
}

package org.osmdroid.samplefragments.tilesources;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

/**
 * sample custom tile source
 * Created by alex on 6/20/16.
 */
public class USGSTileSource extends OnlineTileSourceBase {

    public USGSTileSource() {
        this("USGS Topo", 0, 18, 256, "",
                new String[]{"http://basemap.nationalmap.gov/ArcGIS/rest/services/USGSTopo/MapServer/tile/"});
    }

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
    public USGSTileSource(String aName, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl, "USGS");
    }

    @Override
    public String getTileURLString(final long pMapTileIndex) {
        return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
                + mImageFilenameEnding;
    }
}

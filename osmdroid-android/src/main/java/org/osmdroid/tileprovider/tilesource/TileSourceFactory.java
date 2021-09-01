package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.util.MapTileIndex;

import java.util.ArrayList;
import java.util.List;


/**
 * looking for mapquest? it's moved because they stopped supporting anonymous access to tiles
 *
 * @see MapQuestTileSource
 * @see org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource
 * @see CloudmadeTileSource
 * @see HEREWeGoTileSource
 * @see MapBoxTileSource
 * @see TMSOnlineTileSourceBase
 */
public class TileSourceFactory {

    /**
     * Get the tile source with the specified name. The tile source must be one of the registered sources
     * as defined in the static list mTileSources of this class.
     *
     * @param aName the tile source name
     * @return the tile source
     * @throws IllegalArgumentException if tile source not found
     */
    public static ITileSource getTileSource(final String aName) throws IllegalArgumentException {
        for (final ITileSource tileSource : mTileSources) {
            if (tileSource.name().equals(aName)) {
                return tileSource;
            }
        }
        throw new IllegalArgumentException("No such tile source: " + aName);
    }

    public static boolean containsTileSource(final String aName) {
        for (final ITileSource tileSource : mTileSources) {
            if (tileSource.name().equals(aName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the tile source at the specified position.
     *
     * @param aOrdinal
     * @return the tile source
     * @throws IllegalArgumentException if tile source not found
     */
    @Deprecated
    public static ITileSource getTileSource(final int aOrdinal) throws IllegalArgumentException {
        for (final ITileSource tileSource : mTileSources) {
            if (tileSource.ordinal() == aOrdinal) {
                return tileSource;
            }
        }
        throw new IllegalArgumentException("No tile source at position: " + aOrdinal);
    }

    /**
     * returns all predefined tiles sources that are generally free to use. be sure to check the usage
     * agreements yourself.
     *
     * @return
     */
    public static List<ITileSource> getTileSources() {
        return mTileSources;
    }

    /**
     * adds a new tile source to the list
     *
     * @param mTileSource
     */
    public static void addTileSource(final ITileSource mTileSource) {
        mTileSources.add(mTileSource);
    }

    /**
     * removes any tile sources whose name matches the regular expression
     *
     * @param aRegex regular expression
     * @return number of sources removed
     */
    public static int removeTileSources(final String aRegex) {
        int n = 0;
        for (int i = mTileSources.size() - 1; i >= 0; --i) {
            if (mTileSources.get(i).name().matches(aRegex)) {
                mTileSources.remove(i);
                ++n;
            }
        }
        return n;
    }

    public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
            0, 19, 256, ".png", new String[]{
            "https://a.tile.openstreetmap.org/",
            "https://b.tile.openstreetmap.org/",
            "https://c.tile.openstreetmap.org/"}, "© OpenStreetMap contributors",
            new TileSourcePolicy(2,
                    TileSourcePolicy.FLAG_NO_BULK
                            | TileSourcePolicy.FLAG_NO_PREVENTIVE
                            | TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                            | TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
            ));
    // max concurrent thread number is 2 (cf. https://operations.osmfoundation.org/policies/tiles/)

    // let's be restrictive here
    // see https://foundation.wikimedia.org/wiki/Maps_Terms_of_Use
    public static final OnlineTileSourceBase WIKIMEDIA = new XYTileSource("Wikimedia",
            1, 19, 256, ".png", new String[]{
            "https://maps.wikimedia.org/osm-intl/"},
            "Wikimedia maps | Map data © OpenStreetMap contributors",
            new TileSourcePolicy(1,
                    TileSourcePolicy.FLAG_NO_BULK
                            | TileSourcePolicy.FLAG_NO_PREVENTIVE
                            | TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                            | TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
            ));

    //they do not have ssl setup as of oct 2019
    public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new XYTileSource(
            "OSMPublicTransport", 0, 17, 256, ".png",
            new String[]{"http://openptmap.org/tiles/"}, "© OpenStreetMap contributors");


    public static final OnlineTileSourceBase DEFAULT_TILE_SOURCE = MAPNIK;

    // CloudMade tile sources are not in mTileSource because they are not free
    // and therefore not provided by default.

    public static final OnlineTileSourceBase CLOUDMADESTANDARDTILES = new CloudmadeTileSource(
            "CloudMadeStandardTiles", 0, 18, 256, ".png",
            new String[]{"http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
                    "http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
                    "http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s"});

    // FYI - This tile source has a tileSize of "6"
    public static final OnlineTileSourceBase CLOUDMADESMALLTILES = new CloudmadeTileSource(
            "CloudMadeSmallTiles", 0, 21, 64, ".png",
            new String[]{"http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
                    "http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
                    "http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s"});

    // The following tile sources are overlays, not standalone map views.
    // They are therefore not in mTileSources.

    public static final OnlineTileSourceBase FIETS_OVERLAY_NL = new XYTileSource("Fiets",
            3, 18, 256, ".png",
            new String[]{"https://overlay.openstreetmap.nl/openfietskaart-overlay/"}, "© OpenStreetMap contributors");

    public static final OnlineTileSourceBase BASE_OVERLAY_NL = new XYTileSource("BaseNL",
            0, 18, 256, ".png",
            new String[]{"https://overlay.openstreetmap.nl/basemap/"});

    public static final OnlineTileSourceBase ROADS_OVERLAY_NL = new XYTileSource("RoadsNL",
            0, 18, 256, ".png",
            new String[]{"https://overlay.openstreetmap.nl/roads/"}, "© OpenStreetMap contributors");

    /**
     * 2020.03.12 there is also a "http://(a|b|c).tiles.wmflabs.org/hikebike/" version
     */
    public static final OnlineTileSourceBase HIKEBIKEMAP = new XYTileSource("HikeBikeMap",
            0, 18, 256, ".png",
            new String[]{"https://tiles.wmflabs.org/hikebike/"});

    /**
     * This is actually another tile overlay
     *
     * @sunce 5.6.2
     */
    public static final OnlineTileSourceBase OPEN_SEAMAP = new XYTileSource("OpenSeaMap",
            3, 18, 256, ".png", new String[]{"https://tiles.openseamap.org/seamark/"}, "OpenSeaMap");


    public static final OnlineTileSourceBase USGS_TOPO = new OnlineTileSourceBase("USGS National Map Topo", 0, 15, 256, "",
            new String[]{"https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/"}, "USGS") {
        @Override
        public String getTileURLString(final long pMapTileIndex) {
            return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex);
        }
    };
    public static final OnlineTileSourceBase USGS_SAT = new OnlineTileSourceBase("USGS National Map Sat", 0, 15, 256, "",
            new String[]{"https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/"}, "USGS") {
        @Override
        public String getTileURLString(final long pMapTileIndex) {
            return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex);
        }
    };


    /**
     * Chart Bundle US Aeronautical Charts
     *
     * @since 5.6.2
     */
    public static final OnlineTileSourceBase ChartbundleWAC = new XYTileSource("ChartbundleWAC", 4, 12, 256, ".png?type=google",
            new String[]{"https://wms.chartbundle.com/tms/v1.0/wac/"}, "chartbundle.com");

    /**
     * Chart Bundle US Aeronautical Charts Enroute High
     *
     * @since 5.6.2
     */
    public static final OnlineTileSourceBase ChartbundleENRH = new XYTileSource("ChartbundleENRH", 4, 12, 256, ".png?type=google",
            new String[]{"https://wms.chartbundle.com/tms/v1.0/enrh/", "chartbundle.com"});
    /**
     * Chart Bundle US Aeronautical Charts Enroute Low
     *
     * @since 5.6.2
     */
    public static final OnlineTileSourceBase ChartbundleENRL = new XYTileSource("ChartbundleENRL", 4, 12, 256, ".png?type=google",
            new String[]{"https://wms.chartbundle.com/tms/v1.0/enrl/", "chartbundle.com"});

    /**
     * Open Topo Maps https://opentopomap.org
     *
     * @since 5.6.2
     */
    public static final OnlineTileSourceBase OpenTopo = new XYTileSource("OpenTopoMap", 0, 17, 256, ".png",
            new String[]{
                    "https://a.tile.opentopomap.org/",
                    "https://b.tile.opentopomap.org/",
                    "https://c.tile.opentopomap.org/"},
            "Kartendaten: © OpenStreetMap-Mitwirkende, SRTM | Kartendarstellung: © OpenTopoMap (CC-BY-SA)");

    private static List<ITileSource> mTileSources;

    static {
        mTileSources = new ArrayList<ITileSource>();
        mTileSources.add(MAPNIK);
        mTileSources.add(WIKIMEDIA);
        mTileSources.add(PUBLIC_TRANSPORT);
        mTileSources.add(HIKEBIKEMAP);
        mTileSources.add(USGS_TOPO);
        mTileSources.add(USGS_SAT);
        mTileSources.add(ChartbundleWAC);
        mTileSources.add(ChartbundleENRH);
        mTileSources.add(ChartbundleENRL);
        mTileSources.add(OpenTopo);
    }
}

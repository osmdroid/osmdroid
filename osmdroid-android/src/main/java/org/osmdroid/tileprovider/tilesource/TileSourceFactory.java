package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.tileprovider.MapTile;

import java.util.ArrayList;
import java.util.List;


/**
 * looking for mapquest? it's moved because they stopped supporting anonymous access to tiles
 * @see MapQuestTileSource
 */
public class TileSourceFactory {

	/**
	 * Get the tile source with the specified name. The tile source must be one of the registered sources
	 * as defined in the static list mTileSources of this class.
	 *
	 * @param aName
	 *            the tile source name
	 * @return the tile source
	 * @throws IllegalArgumentException
	 *             if tile source not found
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
	 * @throws IllegalArgumentException
	 *             if tile source not found
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
	 * @return
     */
	public static List<ITileSource> getTileSources() {
		return mTileSources;
	}

	/**
	 * adds a new tile source to the list
	 * @param mTileSource
     */
	public static void addTileSource(final ITileSource mTileSource) {
		mTileSources.add(mTileSource);
	}

        /**
         * removes any tile sources whose name matches the regular expression
         * @param aRegex regular expression
         * @return number of sources removed
         */
	public static int removeTileSources(final String aRegex) {
	        int n=0;
                for (int i=mTileSources.size()-1; i>=0; --i) {
                        if (mTileSources.get(i).name().matches(aRegex)) {
		                mTileSources.remove(i);
				++n;
		        }
                }
		return n;
	}

	public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
			0, 19, 256, ".png", new String[] {
					"http://a.tile.openstreetmap.org/",
					"http://b.tile.openstreetmap.org/",
					"http://c.tile.openstreetmap.org/" },"© OpenStreetMap contributors");

	public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new XYTileSource(
			"OSMPublicTransport", 0, 17, 256, ".png",
			new String[] { "http://openptmap.org/tiles/" },"© OpenStreetMap contributors");



	public static final OnlineTileSourceBase DEFAULT_TILE_SOURCE = MAPNIK;

	// CloudMade tile sources are not in mTileSource because they are not free
	// and therefore not provided by default.

	public static final OnlineTileSourceBase CLOUDMADESTANDARDTILES = new CloudmadeTileSource(
			"CloudMadeStandardTiles", 0, 18, 256, ".png",
			new String[] { "http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s" });

	// FYI - This tile source has a tileSize of "6"
	public static final OnlineTileSourceBase CLOUDMADESMALLTILES = new CloudmadeTileSource(
			"CloudMadeSmallTiles", 0, 21, 64, ".png",
			new String[] { "http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s" });

	// The following tile sources are overlays, not standalone map views.
	// They are therefore not in mTileSources.

	public static final OnlineTileSourceBase FIETS_OVERLAY_NL = new XYTileSource("Fiets",
			3, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/openfietskaart-overlay/" },"© OpenStreetMap contributors");

	public static final OnlineTileSourceBase BASE_OVERLAY_NL = new XYTileSource("BaseNL",
			0, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/basemap/" });

	public static final OnlineTileSourceBase ROADS_OVERLAY_NL = new XYTileSource("RoadsNL",
			0, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/roads/" },"© OpenStreetMap contributors");
     
     public static final OnlineTileSourceBase HIKEBIKEMAP = new XYTileSource("HikeBikeMap",
			 0, 18, 256, ".png",
			new String[] { "http://a.tiles.wmflabs.org/hikebike/",
                    "http://b.tiles.wmflabs.org/hikebike/",
                    "http://c.tiles.wmflabs.org/hikebike/"  });

	/**
	 * This is actually another tile overlay
	 * @sunce 5.6.2
	 */
	public static final OnlineTileSourceBase OPEN_SEAMAP = new XYTileSource("OpenSeaMap",
			3,18,256,".png", new String[] { "http://tiles.openseamap.org/seamark/"}, "OpenSeaMap");

     
     public static final OnlineTileSourceBase USGS_TOPO = new OnlineTileSourceBase("USGS National Map Topo",  0, 15, 256, "",
               new String[] { "https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/" },"USGS") {
               @Override
               public String getTileURLString(MapTile aTile) {
                    return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
               }
          };
	public static final OnlineTileSourceBase USGS_SAT = new OnlineTileSourceBase("USGS National Map Sat", 0, 15, 256, "",
			new String[]{"https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/"},"USGS") {
		@Override
		public String getTileURLString(MapTile aTile) {
			return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
		}
	};


	/**
	 * Chart Bundle US Aeronautical Charts
	 * @since 5.6.2
	 */
	public static final OnlineTileSourceBase ChartbundleWAC = new XYTileSource("ChartbundleWAC", 4, 12, 256, ".png?type=google",
		new String[]{"http://wms.chartbundle.com/tms/v1.0/wac/"}, "chartbundle.com");

	/**
	 * Chart Bundle US Aeronautical Charts Enroute High
	 * @since 5.6.2
	 */
	public static final OnlineTileSourceBase ChartbundleENRH = new XYTileSource("ChartbundleENRH", 4, 12, 256, ".png?type=google",
		new String[]{"http://wms.chartbundle.com/tms/v1.0/enrh/", "chartbundle.com"});
	/**
	 * Chart Bundle US Aeronautical Charts Enroute Low
	 * @since 5.6.2
	 */
	public static final OnlineTileSourceBase ChartbundleENRL = new XYTileSource("ChartbundleENRL", 4, 12, 256, ".png?type=google",
		new String[]{"http://wms.chartbundle.com/tms/v1.0/enrl/", "chartbundle.com"});

	/**
	 * Open Topo Maps https://opentopomap.org
	 * @since 5.6.2
	 */
	public static final OnlineTileSourceBase OpenTopo= new XYTileSource("OpenTopoMap", 0, 19, 256, ".png",
		new String[]{"https://opentopomap.org/"}, "Kartendaten: © OpenStreetMap-Mitwirkende, SRTM | Kartendarstellung: © OpenTopoMap (CC-BY-SA)");



	private static List<ITileSource> mTileSources;
	static {
		mTileSources = new ArrayList<ITileSource>();
		mTileSources.add(MAPNIK);
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

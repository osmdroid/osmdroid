package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.tileprovider.MapTile;

import java.util.ArrayList;
import java.util.List;

public class TileSourceFactory {

	// private static final Logger logger = LoggerFactory.getLogger(TileSourceFactory.class);

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

	public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
			0, 18, 256, ".png", new String[] {
					"http://a.tile.openstreetmap.org/",
					"http://b.tile.openstreetmap.org/",
					"http://c.tile.openstreetmap.org/" });

	public static final OnlineTileSourceBase CYCLEMAP = new XYTileSource("CycleMap",
			0, 17, 256, ".png", new String[] {
					"http://a.tile.opencyclemap.org/cycle/",
					"http://b.tile.opencyclemap.org/cycle/",
					"http://c.tile.opencyclemap.org/cycle/" });

	public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new XYTileSource(
			"OSMPublicTransport", 0, 17, 256, ".png",
			new String[] { "http://openptmap.org/tiles/" });

	public static final OnlineTileSourceBase MAPQUESTOSM = new XYTileSource("MapquestOSM",
			0, 18, 256, ".jpg", new String[] {
					"http://otile1.mqcdn.com/tiles/1.0.0/map/",
					"http://otile2.mqcdn.com/tiles/1.0.0/map/",
					"http://otile3.mqcdn.com/tiles/1.0.0/map/",
					"http://otile4.mqcdn.com/tiles/1.0.0/map/" });

	public static final OnlineTileSourceBase MAPQUESTAERIAL = new XYTileSource("MapquestAerial",
			0, 11, 256, ".jpg", new String[] {
					"http://otile1.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile2.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile3.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile4.mqcdn.com/tiles/1.0.0/sat/" });

	// From MapQuest documentation:
	// Please also note that global coverage is provided at zoom levels 0-11. Zoom Levels 12+ are
	// provided only in the United States (lower 48).
	public static final OnlineTileSourceBase MAPQUESTAERIAL_US = new XYTileSource(
			"MapquestAerialUSA",  0, 18, 256, ".jpg",
			new String[] { "http://otile1.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile2.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile3.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile4.mqcdn.com/tiles/1.0.0/sat/" });

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
			new String[] { "http://overlay.openstreetmap.nl/openfietskaart-overlay/" });

	public static final OnlineTileSourceBase BASE_OVERLAY_NL = new XYTileSource("BaseNL",
			0, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/basemap/" });

	public static final OnlineTileSourceBase ROADS_OVERLAY_NL = new XYTileSource("RoadsNL",
			0, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/roads/" });
     
     public static final OnlineTileSourceBase HIKEBIKEMAP = new XYTileSource("HikeBikeMap",
			 0, 18, 256, ".png",
			new String[] { "http://a.tiles.wmflabs.org/hikebike/",
                    "http://b.tiles.wmflabs.org/hikebike/",
                    "http://c.tiles.wmflabs.org/hikebike/"  });
     
     public static final OnlineTileSourceBase USGS_TOPO = new OnlineTileSourceBase("USGS National Map Topo",  0, 18, 256, "",
               new String[] { "http://basemap.nationalmap.gov/ArcGIS/rest/services/USGSTopo/MapServer/tile/" }) {
               @Override
               public String getTileURLString(MapTile aTile) {
                    return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
               }
          };
	public static final OnlineTileSourceBase USGS_SAT = new OnlineTileSourceBase("USGS National Map Sat", 0, 18, 256, "",
			new String[]{"http://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/"}) {
		@Override
		public String getTileURLString(MapTile aTile) {
			return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
		}
	};


	private static List<ITileSource> mTileSources;
	static {
		mTileSources = new ArrayList<ITileSource>();
		mTileSources.add(MAPNIK);
		mTileSources.add(CYCLEMAP);
		mTileSources.add(PUBLIC_TRANSPORT);
		mTileSources.add(MAPQUESTOSM);
		mTileSources.add(MAPQUESTAERIAL);
		mTileSources.add(HIKEBIKEMAP);
		mTileSources.add(USGS_TOPO);
		mTileSources.add(USGS_SAT);
	}
}

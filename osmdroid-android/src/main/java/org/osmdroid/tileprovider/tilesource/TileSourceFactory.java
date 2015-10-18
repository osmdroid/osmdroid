package org.osmdroid.tileprovider.tilesource;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;

public class TileSourceFactory {

	// private static final Logger logger = LoggerFactory.getLogger(TileSourceFactory.class);

	/**
	 * Get the tile source with the specified name.
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
	public static ITileSource getTileSource(final int aOrdinal) throws IllegalArgumentException {
		for (final ITileSource tileSource : mTileSources) {
			if (tileSource.ordinal() == aOrdinal) {
				return tileSource;
			}
		}
		throw new IllegalArgumentException("No tile source at position: " + aOrdinal);
	}

	public static ArrayList<ITileSource> getTileSources() {
		return mTileSources;
	}

	public static void addTileSource(final ITileSource mTileSource) {
		mTileSources.add(mTileSource);
	}

	public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
			ResourceProxy.string.mapnik, 0, 18, 256, ".png", new String[] {
					"http://a.tile.openstreetmap.org/",
					"http://b.tile.openstreetmap.org/",
					"http://c.tile.openstreetmap.org/" });

	public static final OnlineTileSourceBase CYCLEMAP = new XYTileSource("CycleMap",
			ResourceProxy.string.cyclemap, 0, 17, 256, ".png", new String[] {
					"http://a.tile.opencyclemap.org/cycle/",
					"http://b.tile.opencyclemap.org/cycle/",
					"http://c.tile.opencyclemap.org/cycle/" });

    public static final OnlineTileSourceBase HIKEBIKEMAP = new XYTileSource("HikeBikeMap",
            ResourceProxy.string.hikebikemap, 0, 18, 256, ".png", new String[] {
                    "http://a.tiles.wmflabs.org/hikebike/",
                    "http://b.tiles.wmflabs.org/hikebike/",
                    "http://c.tiles.wmflabs.org/hikebike/" });

    public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new XYTileSource(
			"OSMPublicTransport", ResourceProxy.string.public_transport, 0, 17, 256, ".png",
			new String[] { "http://openptmap.org/tiles/" });

	public static final OnlineTileSourceBase MAPQUESTOSM = new XYTileSource("MapquestOSM",
			ResourceProxy.string.mapquest_osm, 0, 18, 256, ".jpg", new String[] {
					"http://otile1.mqcdn.com/tiles/1.0.0/map/",
					"http://otile2.mqcdn.com/tiles/1.0.0/map/",
					"http://otile3.mqcdn.com/tiles/1.0.0/map/",
					"http://otile4.mqcdn.com/tiles/1.0.0/map/" });

	public static final OnlineTileSourceBase MAPQUESTAERIAL = new XYTileSource("MapquestAerial",
			ResourceProxy.string.mapquest_aerial, 0, 11, 256, ".jpg", new String[] {
					"http://otile1.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile2.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile3.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile4.mqcdn.com/tiles/1.0.0/sat/" });

	// From MapQuest documentation:
	// Please also note that global coverage is provided at zoom levels 0-11. Zoom Levels 12+ are
	// provided only in the United States (lower 48).
	public static final OnlineTileSourceBase MAPQUESTAERIAL_US = new XYTileSource(
			"MapquestAerialUSA", ResourceProxy.string.mapquest_aerial, 0, 18, 256, ".jpg",
			new String[] { "http://otile1.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile2.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile3.mqcdn.com/tiles/1.0.0/sat/",
					"http://otile4.mqcdn.com/tiles/1.0.0/sat/" });

	public static final OnlineTileSourceBase DEFAULT_TILE_SOURCE = MAPNIK;

	// CloudMade tile sources are not in mTileSource because they are not free
	// and therefore not provided by default.

	public static final OnlineTileSourceBase CLOUDMADESTANDARDTILES = new CloudmadeTileSource(
			"CloudMadeStandardTiles", ResourceProxy.string.cloudmade_standard, 0, 18, 256, ".png",
			new String[] { "http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s" });

	// FYI - This tile source has a tileSize of "6"
	public static final OnlineTileSourceBase CLOUDMADESMALLTILES = new CloudmadeTileSource(
			"CloudMadeSmallTiles", ResourceProxy.string.cloudmade_small, 0, 21, 64, ".png",
			new String[] { "http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
					"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s" });

	// The following tile sources are overlays, not standalone map views.
	// They are therefore not in mTileSources.

	public static final OnlineTileSourceBase FIETS_OVERLAY_NL = new XYTileSource("Fiets",
			ResourceProxy.string.fiets_nl, 3, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/openfietskaart-overlay/" });

	public static final OnlineTileSourceBase BASE_OVERLAY_NL = new XYTileSource("BaseNL",
			ResourceProxy.string.base_nl, 0, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/basemap/" });

	public static final OnlineTileSourceBase ROADS_OVERLAY_NL = new XYTileSource("RoadsNL",
			ResourceProxy.string.roads_nl, 0, 18, 256, ".png",
			new String[] { "http://overlay.openstreetmap.nl/roads/" });

	private static ArrayList<ITileSource> mTileSources;
	static {
		mTileSources = new ArrayList<ITileSource>();
		mTileSources.add(MAPNIK);
		mTileSources.add(CYCLEMAP);
        mTileSources.add(HIKEBIKEMAP);
		mTileSources.add(PUBLIC_TRANSPORT);
		mTileSources.add(MAPQUESTOSM);
		mTileSources.add(MAPQUESTAERIAL);
	}
}

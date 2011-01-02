package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.ResourceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileSourceFactory {

	private static final Logger logger = LoggerFactory.getLogger(TileSourceFactory.class);

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
			// TODO perhaps we should ignore case and white space
			if (tileSource.name().equals(aName)) {
				return tileSource;
			}
		}
		throw new IllegalArgumentException("No such tile source: " + aName);
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

	public static ITileSource[] getTileSources() {
		return mTileSources;
	}

	public static final OnlineTileSourceBase OSMARENDER = new XYTileSource("Osmarender",
			ResourceProxy.string.osmarender, 0, 17, 256, ".png",
			"http://tah.openstreetmap.org/Tiles/tile/");

	public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
			ResourceProxy.string.mapnik, 0, 18, 256, ".png", "http://tile.openstreetmap.org/");

	public static final OnlineTileSourceBase CYCLEMAP = new XYTileSource("CycleMap",
			ResourceProxy.string.cyclemap, 0, 17, 256, ".png",
			"http://a.andy.sandbox.cloudmade.com/tiles/cycle/",
			"http://b.andy.sandbox.cloudmade.com/tiles/cycle/",
			"http://c.andy.sandbox.cloudmade.com/tiles/cycle/");

	public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new XYTileSource(
			"OSMPublicTransport", ResourceProxy.string.public_transport, 0, 17, 256, ".png",
			"http://tile.xn--pnvkarte-m4a.de/tilegen/");

	public static final OnlineTileSourceBase BASE = new XYTileSource("Base",
			ResourceProxy.string.base, 4, 17, 256, ".png", "http://topo.openstreetmap.de/base/");

	public static final OnlineTileSourceBase TOPO = new XYTileSource("Topo",
			ResourceProxy.string.topo, 4, 17, 256, ".png", "http://topo.openstreetmap.de/topo/");

	public static final OnlineTileSourceBase HILLS = new XYTileSource("Hills",
			ResourceProxy.string.hills, 8, 17, 256, ".png", "http://topo.geofabrik.de/hills/");

	public static final OnlineTileSourceBase CLOUDMADESTANDARDTILES = new CloudmadeTileSource(
			"CloudMadeStandardTiles", ResourceProxy.string.cloudmade_standard, 0, 18, 256, ".png",
			"http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
			"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
			"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s");

	// FYI - This tile source has a tileSize of "6"
	public static final OnlineTileSourceBase CLOUDMADESMALLTILES = new CloudmadeTileSource(
			"CloudMadeSmallTiles", ResourceProxy.string.cloudmade_small, 0, 21, 64, ".png",
			"http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
			"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
			"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s");

	public static final OnlineTileSourceBase DEFAULT_TILE_SOURCE = MAPNIK;

	// The following tile sources are overlays, not standalone map views.
	// They are therefore not in mTileSources.

	public static final OnlineTileSourceBase FIETS_OVERLAY_NL = new XYTileSource("Fiets",
			ResourceProxy.string.fiets_nl, 3, 16, 256, ".png",
			"http://overlay.openstreetmap.nl/openfietskaart-overlay/");

	public static final OnlineTileSourceBase BASE_OVERLAY_NL = new XYTileSource("BaseNL",
			ResourceProxy.string.base_nl, 0, 18, 256, ".png",
			"http://overlay.openstreetmap.nl/basemap/");

	public static final OnlineTileSourceBase ROADS_OVERLAY_NL = new XYTileSource("RoadsNL",
			ResourceProxy.string.roads_nl, 0, 18, 256, ".png",
			"http://overlay.openstreetmap.nl/roads/");

	// FIXME the whole point of this implementation is that the list of tile sources should be
	// extensible, so that means making it possible to have a bigger or smaller list of tile sources
	// - there's a number of ways of doing that
	private static ITileSource[] mTileSources = new ITileSource[] { OSMARENDER, MAPNIK, CYCLEMAP,
			PUBLIC_TRANSPORT, BASE, TOPO, HILLS, CLOUDMADESTANDARDTILES, CLOUDMADESMALLTILES };
}

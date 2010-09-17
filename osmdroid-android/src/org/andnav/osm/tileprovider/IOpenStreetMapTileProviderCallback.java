package org.andnav.osm.tileprovider;

import java.io.InputStream;

public interface IOpenStreetMapTileProviderCallback {

	/**
	 * The map tile request has completed.
	 * @param aTile the tile request that has completed
	 * @param aTilePath the path of the requested tile, or null if request has completed without returning a tile path
	 */
	void mapTileRequestCompleted(OpenStreetMapTile aTile, String aTilePath);

	/**
	 * The map tile request has completed.
	 * @param aTile the tile request that has completed
	 * @param aTileInputStream the input stream of the requested tile, or null if request has completed without returning a tile
	 */
	void mapTileRequestCompleted(OpenStreetMapTile aTile, final InputStream aTileInputStream);

	/**
	 * The map tile request has completed but no tile has loaded.
	 * @param aTile the tile request that has completed
	 */
	void mapTileRequestCompleted(OpenStreetMapTile aTile);

	/**
	 * Get the API key for Cloudmade tiles.
	 * See http://developers.cloudmade.com/projects/show/auth
	 * @throws CloudmadeException if the key is not found
	 * @return
	 */
	String getCloudmadeKey() throws CloudmadeException;

	/**
	 * Whether to use the network connection if it's available.
	 */
	public boolean useDataConnection();
}

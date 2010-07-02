package org.andnav.osm.tileprovider;

public interface IOpenStreetMapTileProviderCallback {

	/**
	 * The map tile request has completed.
	 * @param pTile the tile request that has completed
	 * @param aTilePath the path of the requested tile, or null if request has completed without returning a tile path 
	 */
	void mapTileRequestCompleted(OpenStreetMapTile pTile, String aTilePath);

	/**
	 * Get the API key for Cloudmade tiles.
	 * See http://developers.cloudmade.com/projects/show/auth
	 * @throws CloudmadeException if the key is not found
	 * @return
	 */
	String getCloudmadeKey() throws CloudmadeException;
}

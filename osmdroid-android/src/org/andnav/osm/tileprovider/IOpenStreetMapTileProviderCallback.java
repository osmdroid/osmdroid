package org.andnav.osm.tileprovider;

public interface IOpenStreetMapTileProviderCallback {

	/**
	 * The map tile request has completed.
	 * @param pTile the tile request that has completed
	 * @param aTilePath the path of the requested tile, or null if request has completed without returning a tile path 
	 */
	void mapTileRequestCompleted(OpenStreetMapTile pTile, String aTilePath);

}

package org.andnav.osm.services;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;

interface IOpenStreetMapTileProviderService {

/**
 * Initiate a request for a map tile.
 * When the request has completed it will call callback.mapTileRequestComplete.
 * @param rendererID
 * @param zoomLevel
 * @param tileX
 * @param tileY
 * @param callback the callback to notify when the request completes 
 */
void requestMapTile(in int rendererID, in int zoomLevel, in int tileX, in int tileY, in IOpenStreetMapTileProviderCallback callback);

}

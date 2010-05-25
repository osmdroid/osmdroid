package org.andnav.osm.services;

import org.andnav.osm.services.IOpenStreetMapTileProviderServiceCallback;

interface IOpenStreetMapTileProviderService {

/**
 * Set the callback for tile completions.
 * @param callback the callback to notify when the request completes 
 */
void setCallback(in IOpenStreetMapTileProviderServiceCallback callback);

/**
 * Initiate a request for a map tile.
 * When the request has completed it will call the callback previously set in setCallback.
 * @param rendererID
 * @param zoomLevel
 * @param tileX
 * @param tileY
 * @param callback the callback to notify when the request completes 
 */
void requestMapTile(in int rendererID, in int zoomLevel, in int tileX, in int tileY);

}

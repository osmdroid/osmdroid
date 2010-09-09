package org.andnav.osm.services;

interface IOpenStreetMapTileProviderServiceCallback {

/**
 * The map tile request has completed.
 * @param rendererName
 * @param zoomLevel
 * @param tileX
 * @param tileY
 * @param aTilePath the path of the requested tile, or null if request has completed without returning a tile path
 */
void mapTileRequestCompleted(in String rendererName, in int zoomLevel, in int tileX, in int tileY, in String aTilePath);

}

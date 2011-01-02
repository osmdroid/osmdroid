package org.osmdroid.tileprovider;

import org.osmdroid.views.util.IOpenStreetMapRendererInfo;

/**
 * An interface that allows a class to register a tile source for file system cache access. This
 * allows that tile source to store data in the file system cache, and have it rendered by the tile
 * source later when it is needed.
 * 
 * @author Marc Kurtz
 * 
 */
@Deprecated
public interface IFilesystemCacheProvider {
	IFilesystemCache registerTileSourceForFilesystemAccess(
			IOpenStreetMapRendererInfo pTileSourceInfo, int minimumZoomLevel, int maximumZoomLevel);

	void unregisterTileSourceForFilesystemAccess(IOpenStreetMapRendererInfo pTileSourceInfo);
}

package org.andnav.osm.views.util;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCloudmadeTokenCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;

public interface IOpenStreetMapRendererInfo {

	int ordinal();
	String name();
	ResourceProxy.string resourceId();
	int maptileSizePx();
	int maptileZoom();
	int zoomMinLevel();
	int zoomMaxLevel();
	String imageFilenameEnding();
	String getTileURLString(OpenStreetMapTile aTile, IOpenStreetMapTileProviderCallback aMCallback, IOpenStreetMapTileProviderCloudmadeTokenCallback aCloudmadeTokenCallback) throws CloudmadeException;

}

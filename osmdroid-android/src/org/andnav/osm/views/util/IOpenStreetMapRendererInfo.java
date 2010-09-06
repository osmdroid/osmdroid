package org.andnav.osm.views.util;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCloudmadeTokenCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory.CodeScheme;

public interface IOpenStreetMapRendererInfo {

	String name();
	ResourceProxy.string resourceId();
	int ordinal();
	CodeScheme codeScheme();
	int maptileSizePx();
	int maptileZoom();
	int zoomMinLevel();
	int zoomMaxLevel();
	String imageFilenameEnding();
	void setCloudmadeStyle(int aStyle);
	String getTileURLString(OpenStreetMapTile aTile, IOpenStreetMapTileProviderCallback aMCallback, IOpenStreetMapTileProviderCloudmadeTokenCallback aCloudmadeTokenCallback) throws CloudmadeException;

}

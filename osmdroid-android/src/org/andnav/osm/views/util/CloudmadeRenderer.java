package org.andnav.osm.views.util;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.ResourceProxy.string;
import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCloudmadeTokenCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;

class CloudmadeRenderer extends OpenStreetMapRendererBase {

	private final ResourceProxy.string mResourceId;

	CloudmadeRenderer(String aName, string aResourceId, int aZoomMinLevel,
			int aZoomMaxLevel, int aMaptileZoom, String aImageFilenameEnding,
			String ...aBaseUrl) {
		super(aName, aZoomMinLevel, aZoomMaxLevel, aMaptileZoom, aImageFilenameEnding, aBaseUrl);
		mResourceId = aResourceId;
	}

	@Override
	public String pathBase() {
		if (cloudmadeStyle <= 1) {
			return mName;
		} else {
			return mName + cloudmadeStyle;
		}
	}

	@Override
	public String localizedName(ResourceProxy proxy) {
		return proxy.getString(mResourceId);
	}

	@Override
	public String getTileURLString(
			OpenStreetMapTile aTile,
			IOpenStreetMapTileProviderCallback aCallback,
			IOpenStreetMapTileProviderCloudmadeTokenCallback aCloudmadeTokenCallback)
			throws CloudmadeException {
		final String key = aCallback.getCloudmadeKey();
		final String token = aCloudmadeTokenCallback.getCloudmadeToken(key);
		return String.format(getBaseUrl(), key, cloudmadeStyle, mMaptileSizePx, aTile.getZoomLevel(), aTile.getX(), aTile.getY(), mImageFilenameEnding, token);
	}

}

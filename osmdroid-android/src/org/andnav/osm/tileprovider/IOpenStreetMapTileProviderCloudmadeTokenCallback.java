package org.andnav.osm.tileprovider;

public interface IOpenStreetMapTileProviderCloudmadeTokenCallback {

	/**
	 * Get the token for Cloudmade tiles.
	 * See http://developers.cloudmade.com/projects/show/auth
	 * @return
	 */
	String getCloudmadeToken(String key);
}

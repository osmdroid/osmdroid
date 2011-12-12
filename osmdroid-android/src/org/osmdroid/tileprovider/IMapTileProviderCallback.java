package org.osmdroid.tileprovider;

import android.graphics.drawable.Drawable;

public interface IMapTileProviderCallback {

	/**
	 * The map tile request has completed.
	 *
	 * @param aState
	 *            a state object
	 * @param aDrawable
	 *            a drawable
	 */
	void mapTileRequestCompleted(MapTileRequestState aState, final Drawable aDrawable);

	/**
	 * The map tile request has failed.
	 *
	 * @param aState
	 *            a state object
	 */
	void mapTileRequestFailed(MapTileRequestState aState);

	/**
	 * Returns true if the network connection should be used, false if not.
	 *
	 * @return true if data connection should be used, false otherwise
	 */
	public boolean useDataConnection();
}
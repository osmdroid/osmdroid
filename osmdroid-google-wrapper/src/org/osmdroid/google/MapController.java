package org.osmdroid.google;

import org.osmdroid.api.IMapController;

/**
 * A wrapper for the Google {@link com.google.android.maps.MapController} class.
 * This implements {@link IMapController}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.MapController}.
 *
 * @author Neil Boyd
 *
 */
public class MapController implements IMapController {

	private final com.google.android.maps.MapController mController;

	public MapController(final com.google.android.maps.MapController pController) {
		mController = pController;
	}

}

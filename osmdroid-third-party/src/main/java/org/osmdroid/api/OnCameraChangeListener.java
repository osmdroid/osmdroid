package org.osmdroid.api;

/**
 * this is only used by the Google Wrapper/3rd party library
 */
public interface OnCameraChangeListener {

	/**
	 * Called after the map view has changed.
	 */
	void onCameraChange (IPosition position);

}

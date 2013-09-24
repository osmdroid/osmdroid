package org.osmdroid.api;

/**
 * An interface that is used for simultaneously setting several properties of the map
 */
public interface IPosition {

	/**
	 * The latitude where to position the center of the map
	 */
	double getLatitude();

	/**
	 * The longitude where to position the center of the map
	 */
	double getLongitude();

	/**
	 * Whether this position has a bearing
	 */
	boolean hasBearing();

	/**
	 * The bearing to set the map to
	 */
	float getBearing();

	/**
	 * Whether this position has a zoom level
	 */
	boolean hasZoomLevel();

	/**
	 * The zoom level to set the map to
	 */
	float getZoomLevel();
}

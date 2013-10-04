package org.osmdroid.api;

/**
 * An interface that is used for simultaneously accessing several properties of the map
 */
public interface IPosition {

	/**
	 * The latitude of the center of the map
	 */
	double getLatitude();

	/**
	 * The longitude of the center of the map
	 */
	double getLongitude();

	/**
	 * Whether this position has a bearing
	 */
	boolean hasBearing();

	/**
	 * The bearing of the map
	 */
	float getBearing();

	/**
	 * Whether this position has a zoom level
	 */
	boolean hasZoomLevel();

	/**
	 * The zoom level of the map
	 */
	float getZoomLevel();
}

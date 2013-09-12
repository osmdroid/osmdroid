package org.osmdroid.api;

/**
 * An interface that contains the common features of osmdroid and Google Maps v2.
 */
public interface IMap {
	void setZoom(int zoomLevel);

	void setCenter(int latitudeE6, int longitudeE6);

	void setZoomAndCenter(int zoomLevel, int latitudeE6, int longitudeE6);

	void setMyLocationEnabled(boolean enabled);

	boolean isMyLocationEnabled();
}

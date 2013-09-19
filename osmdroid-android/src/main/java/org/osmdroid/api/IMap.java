package org.osmdroid.api;

/**
 * An interface that contains the common features of osmdroid and Google Maps v2.
 */
public interface IMap {

	void setZoom(float zoomLevel);

	float getZoomLevel();

	IGeoPoint getCenter();

	void setCenter(int latitudeE6, int longitudeE6);

	void setZoomAndCenter(float zoomLevel, int latitudeE6, int longitudeE6);

	boolean zoomIn();

	boolean zoomOut();

	void setMyLocationEnabled(boolean enabled);

	boolean isMyLocationEnabled();

	IProjection getProjection();
}

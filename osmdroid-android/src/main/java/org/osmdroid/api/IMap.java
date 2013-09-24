package org.osmdroid.api;

/**
 * An interface that contains the common features of osmdroid and Google Maps v2.
 */
public interface IMap {

	float getZoomLevel();

	void setZoom(float zoomLevel);

	IGeoPoint getCenter();

	void setCenter(double latitude, double longitude);

	float getBearing();

	void setBearing(float bearing);

	void setPosition(IPosition position);

	boolean zoomIn();

	boolean zoomOut();

	void setMyLocationEnabled(boolean enabled);

	boolean isMyLocationEnabled();

	IProjection getProjection();
}

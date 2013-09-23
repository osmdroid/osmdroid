package org.osmdroid.api;

/**
 * An interface that contains the common features of osmdroid and Google Maps v2.
 */
public interface IMap {

	float getZoomLevel();

	void setZoom(float zoomLevel);

	IGeoPoint getCenter();

	void setCenter(int latitudeE6, int longitudeE6);

	float getBearing();

	void setBearing(float bearing);

	void setBearingAndCenter(float bearing, int latitudeE6, int longitudeE6);

	void setZoomAndCenter(float zoomLevel, int latitudeE6, int longitudeE6);

	boolean zoomIn();

	boolean zoomOut();

	void setMyLocationEnabled(boolean enabled);

	boolean isMyLocationEnabled();

	IProjection getProjection();
}

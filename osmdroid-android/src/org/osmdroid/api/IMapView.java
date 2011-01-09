package org.osmdroid.api;

import org.osmdroid.views.MapView;

/**
 * An interface that resembles the Google Maps API MapView class
 * and is implemented by the osmdroid {@link MapView} class.
 *
 * @author Neil Boyd
 *
 */
public interface IMapView {

	IMapController getController();
	IProjection getProjection();
	int getZoomLevel();
	int getLatitudeSpan();
	int getLongitudeSpan();
	IGeoPoint getMapCenter();
}

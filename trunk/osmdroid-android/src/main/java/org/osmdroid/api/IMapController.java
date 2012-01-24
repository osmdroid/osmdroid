package org.osmdroid.api;

import org.osmdroid.views.MapController;

/**
 * An interface that resembles the Google Maps API MapController class
 * and is implemented by the osmdroid {@link MapController} class.
 *
 * @author Neil Boyd
 *
 */
public interface IMapController {

	void animateTo(IGeoPoint geoPoint);
	void setCenter(IGeoPoint point);
	int setZoom(int zoomLevel);
	boolean zoomIn();
	boolean zoomInFixing(int xPixel, int yPixel);
	boolean zoomOut();
	boolean zoomOutFixing(int xPixel, int yPixel);
	void zoomToSpan(int latSpanE6, int lonSpanE6);

}

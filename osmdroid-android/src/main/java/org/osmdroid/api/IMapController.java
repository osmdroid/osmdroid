package org.osmdroid.api;

import org.osmdroid.views.MapController;

/**
 * An interface that resembles the Google Maps API MapController class and is implemented by the
 * osmdroid {@link MapController} class.
 * 
 * @author Neil Boyd
 * 
 */
public interface IMapController {
	void animateTo(IGeoPoint geoPoint);
	void scrollBy(int x, int y);
	void setCenter(IGeoPoint point);
	int setZoom(int zoomLevel);
	void stopAnimation(boolean jumpToFinish);
	void stopPanning();
	boolean zoomIn();
	boolean zoomInFixing(int xPixel, int yPixel);
	boolean zoomOut();
	boolean zoomOutFixing(int xPixel, int yPixel);
	void zoomToSpan(double latSpan, double lonSpan);
}

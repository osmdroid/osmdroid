package org.osmdroid.api;

import android.graphics.Point;

/**
 * An interface that resembles the Google Maps API MapController class and is implemented by the
 * osmdroid {@link org.osmdroid.views.MapController} class.
 * 
 * @author Neil Boyd
 * 
 */
public interface IMapController {
	void animateTo(IGeoPoint geoPoint);

     /**
      * this function is a user contribution to OSMdroid is not part of the google api set for gmaps. as such, it's an osmdroid specific extension
      * @param geoPoint
      * @param screenPoint
      * @param animationDuration
      * @since 5.0
      */
	void animateTo(IGeoPoint geoPoint, Point screenPoint, int animationDuration);
	void scrollBy(int x, int y);
	void setCenter(IGeoPoint point);
	int setZoom(int zoomLevel);
	void stopAnimation(boolean jumpToFinish);
	void stopPanning();
	boolean zoomIn();
	boolean zoomInFixing(int xPixel, int yPixel);
	boolean zoomOut();
	boolean zoomOutFixing(int xPixel, int yPixel);
	boolean zoomTo(int zoomLevel);
	boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel);
	void zoomToSpan(int latSpanE6, int lonSpanE6);
}

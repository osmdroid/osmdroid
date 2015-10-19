package org.osmdroid.api;

import android.graphics.Point;

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
	void zoomToSpan(int latSpanE6, int lonSpanE6);
	
	/**
      * this function is a user contribution to OSMdroid is not part of the google api set for gmaps. as such, it's an osmdroid specific extension
	 * returns true if the map tiles are currently being color inverted
	 * @return 
      * @since 5.0
	 */
	boolean isInvertedTiles();
     /**
      * this function is a user contribution to OSMdroid is not part of the google api set for gmaps. as such, it's an osmdroid specific extension
      * @param value 
      * @since 5.0
      */
	void setInvertedTiles(boolean value);
}

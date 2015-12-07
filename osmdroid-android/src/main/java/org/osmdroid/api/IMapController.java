package org.osmdroid.api;

/**
 * An interface that resembles the Google Maps API MapController class and is implemented by the
 * osmdroid {@link org.osmdroid.views.MapController} class.
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
	@Deprecated
	boolean zoomInFixing(int xPixel, int yPixel);
	boolean zoomOut();
	@Deprecated
	boolean zoomOutFixing(int xPixel, int yPixel);
	boolean zoomTo(int zoomLevel);
	boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel);
	void zoomToSpan(int latSpanE6, int lonSpanE6);
	
	/**
	 * returns true if the map tiles are currently being color inverted
	 * @return 
	 */
	boolean isInvertedTiles();
	void setInvertedTiles(boolean value);
}

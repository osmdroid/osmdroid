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
	@Deprecated
	int setZoom(int zoomLevel);
	double setZoom(final double pZoomLevel);
	void stopAnimation(boolean jumpToFinish);
	void stopPanning();
	boolean zoomIn();
	boolean zoomInFixing(int xPixel, int yPixel);
	boolean zoomOut();
	boolean zoomOutFixing(int xPixel, int yPixel);
	@Deprecated
	boolean zoomTo(int zoomLevel);
	boolean zoomTo(final double pZoomLevel);
	@Deprecated
	boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel);
	boolean zoomToFixing(final double pZoomLevel, final int pXPixel, final int pYPixel);
	@Deprecated
	void zoomToSpan(int latSpanE6, int lonSpanE6);
	void zoomToSpan(double latSpan, double lonSpan);
}

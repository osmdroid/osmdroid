package org.osmdroid.api;


/**
 * An interface that resembles the Google Maps API MapView class
 * and is implemented by the osmdroid {@link org.osmdroid.views.MapView} class.
 *
 * @author Neil Boyd
 *
 */
public interface IMapView {
     public static String LOGTAG="OsmDroid";
	IMapController getController();
	IProjection getProjection();
	int getZoomLevel();
	int getMaxZoomLevel();
     @Deprecated
	int getLatitudeSpan();
     @Deprecated
	int getLongitudeSpan();
	double getLatitudeSpanDouble();
	double getLongitudeSpanDouble();
	IGeoPoint getMapCenter();

	// some methods from View
	// (well, just one for now)
	void setBackgroundColor(int color);

}

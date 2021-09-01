package org.osmdroid.api;


/**
 * An interface that resembles the Google Maps API MapView class
 * and is implemented by the osmdroid {@link org.osmdroid.views.MapView} class.
 *
 * @author Neil Boyd
 */
public interface IMapView {
    public final static String LOGTAG = "OsmDroid";

    IMapController getController();

    IProjection getProjection();

    @Deprecated
    int getZoomLevel();

    /**
     * @since 6.0
     */
    double getZoomLevelDouble();

    double getMaxZoomLevel();

    double getLatitudeSpanDouble();

    double getLongitudeSpanDouble();

    IGeoPoint getMapCenter();

    // some methods from View
    // (well, just one for now)
    void setBackgroundColor(int color);

}

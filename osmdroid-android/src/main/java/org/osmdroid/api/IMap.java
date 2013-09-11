package org.osmdroid.api;

import org.osmdroid.util.GeoPoint;

/**
 * An interface that contains the common features of osmdroid and Google Maps v2.
 */
public interface IMap {
    void setZoom(int pZoomLevel);

    void setCenter(int pLatitudeE6, int pLongitudeE6);

    void disableMyLocation();
}

package org.osmdroid.api;

import org.osmdroid.util.GeoPoint;

/**
 * An interface that contains the common features of osmdroid and Google Maps v2.
 */
public interface IMap {
    void setZoom(int aZoomLevel);

    void setCenter(int aLatitudeE6, int aLongitudeE6);

    void disableMyLocation();
}

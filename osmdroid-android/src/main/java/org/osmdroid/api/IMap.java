package org.osmdroid.api;

import org.osmdroid.util.GeoPoint;

/**
 * An interface that contains the common features of osmdroid and Google Maps v2.
 */
public interface IMap {
    void setZoom(int zoomLevel);

    void setCenter(int latitudeE6, int longitudeE6);

    void disableMyLocation();
}

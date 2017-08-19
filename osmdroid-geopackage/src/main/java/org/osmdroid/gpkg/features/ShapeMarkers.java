package org.osmdroid.gpkg.features;


import org.osmdroid.views.overlay.Marker;

import java.util.List;

/**
 * Shape markers interface for handling marker changes
 *
 * @author osbornb
 */
public interface ShapeMarkers {

    /**
     * Get all markers
     *
     * @return
     */
    public List<Marker> getMarkers();


    /**
     * Add the marker
     *
     * @param marker
     */
    public void addNew(Marker marker);

    /**
     * Updates visibility of all objects
     *
     * @param visible visible flag
     * @since 1.3.2
     */
    public void setVisible(boolean visible);

    /**
     * Updates visibility of the shape representing markers
     *
     * @param visible visible flag
     * @since 1.3.2
     */
    public void setVisibleMarkers(boolean visible);

}

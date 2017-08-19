package org.osmdroid.gpkg.features;



import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Marker object
 *
 * @author osbornb
 */
public class MultiMarker implements ShapeMarkers {

    private List<Marker> markers = new ArrayList<Marker>();

    public void add(Marker marker) {
        markers.add(marker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    /**
     * Remove from the map

    public void remove() {
        for (Marker marker : markers) {
            marker.remove();
        }
    }
     */
    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        setVisibleMarkers(visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisibleMarkers(boolean visible) {
        for (Marker marker : markers) {
            marker.setVisible(visible);
        }
    }

    /**
     * {@inheritDoc}

    @Override
    public void delete(Marker marker) {
        if (markers.remove(marker)) {
            marker.remove();
        }
    }   */

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNew(Marker marker) {
        add(marker);
    }

}

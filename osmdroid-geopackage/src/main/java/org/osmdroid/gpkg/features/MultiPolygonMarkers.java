package org.osmdroid.gpkg.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Polygon Markers object
 *
 * @author osbornb
 */
public class MultiPolygonMarkers {

    private List<PolygonMarkers> polygonMarkers = new ArrayList<PolygonMarkers>();

    public void add(PolygonMarkers polygonMarker) {
        polygonMarkers.add(polygonMarker);
    }

    public List<PolygonMarkers> getPolygonMarkers() {
        return polygonMarkers;
    }

    public void setPolygonMarkers(List<PolygonMarkers> polygonMarkers) {
        this.polygonMarkers = polygonMarkers;
    }

    /**
     * Update based upon marker changes

    public void update() {
        for (PolygonMarkers polygonMarker : polygonMarkers) {
            polygonMarker.update();
        }
    }*/

    /**
     * Remove the polygon and points

    public void remove() {
        for (PolygonMarkers polygonMarker : polygonMarkers) {
            polygonMarker.remove();
        }
    }   */

    /**
     * Set visibility on the map
     *
     * @param visible visibility flag
     * @since 1.3.2
     */
    public void setVisible(boolean visible) {
        for (PolygonMarkers polygonMarker : polygonMarkers) {
            polygonMarker.setVisible(visible);
        }
    }

    /**
     * Is it valid
     *
     * @return
     */
    public boolean isValid() {
        boolean valid = true;
        for (PolygonMarkers polygon : polygonMarkers) {
            valid = polygon.isValid();
            if (!valid) {
                break;
            }
        }
        return valid;
    }

    /**
     * Is it deleted
     *
     * @return
     */
    public boolean isDeleted() {
        boolean deleted = true;
        for (PolygonMarkers polygon : polygonMarkers) {
            deleted = polygon.isDeleted();
            if (!deleted) {
                break;
            }
        }
        return deleted;
    }

}

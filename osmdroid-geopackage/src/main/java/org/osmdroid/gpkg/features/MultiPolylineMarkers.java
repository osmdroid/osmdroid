package org.osmdroid.gpkg.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Polyline Markers object
 *
 * @author osbornb
 */
public class MultiPolylineMarkers {

    private List<PolylineMarkers> polylineMarkers = new ArrayList<PolylineMarkers>();

    public void add(PolylineMarkers polylineMarker) {
        polylineMarkers.add(polylineMarker);
    }

    public List<PolylineMarkers> getPolylineMarkers() {
        return polylineMarkers;
    }

    public void setPolylineMarkers(List<PolylineMarkers> polylineMarkers) {
        this.polylineMarkers = polylineMarkers;
    }

    /**
     * Update based upon marker changes

    public void update() {
        for (PolylineMarkers polylineMarker : polylineMarkers) {
            polylineMarker.update();
        }
    }  */

    /**
     * Remove the polyline and points

    public void remove() {
        for (PolylineMarkers polylineMarker : polylineMarkers) {
            polylineMarker.remove();
        }
    }   */

    /**
     * Set visibility on the map
     *
     * @param visible visibility flag
     * @since 1.3.2
     */
    public void setVisible(boolean visible) {
        for (PolylineMarkers polylineMarker : polylineMarkers) {
            polylineMarker.setVisible(visible);
        }
    }

    /**
     * Is it valid
     *
     * @return
     */
    public boolean isValid() {
        boolean valid = true;
        for (PolylineMarkers polyline : polylineMarkers) {
            valid = polyline.isValid();
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
        for (PolylineMarkers polyline : polylineMarkers) {
            deleted = polyline.isDeleted();
            if (!deleted) {
                break;
            }
        }
        return deleted;
    }

}

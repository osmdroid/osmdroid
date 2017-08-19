package org.osmdroid.gpkg.features;


import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Polygon object
 *
 * @author osbornb
 */
public class MultiPolygon {

    private List<Polygon> polygons = new ArrayList<Polygon>();

    public void add(Polygon polygon) {
        polygons.add(polygon);
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    /**
     * Remove from the map

    public void remove() {
        for (Polygon polygon : polygons) {
            polygon.remove();
        }
    }  */

    /**
     * Set visibility on the map
     *
     * @param visible visibility flag
     * @since 1.3.2
     */
    public void setVisible(boolean visible) {
        for (Polygon polygon : polygons) {
            polygon.setVisible(visible);
        }
    }

}

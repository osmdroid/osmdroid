package org.osmdroid.gpkg.features;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Polygon with Markers object
 *
 * @author osbornb
 */
public class PolygonMarkers implements ShapeWithChildrenMarkers {

    private final OsmMapShapeConverter converter;

    private Polygon polygon;

    private List<Marker> markers = new ArrayList<Marker>();

    private List<PolygonHoleMarkers> holes = new ArrayList<PolygonHoleMarkers>();

    /**
     * Constructor
     *
     * @param converter
     */
    public PolygonMarkers(OsmMapShapeConverter converter) {
        this.converter = converter;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

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

    public void addHole(PolygonHoleMarkers hole) {
        holes.add(hole);
    }

    public List<PolygonHoleMarkers> getHoles() {
        return holes;
    }

    public void setHoles(List<PolygonHoleMarkers> holes) {
        this.holes = holes;
    }

    /**
     * Update based upon marker changes

    public void update() {
        if (polygon != null) {
            if (isDeleted()) {
                remove();
            } else {

                List<GeoPoint> points = converter.getPointsFromMarkers(markers);
                polygon.setPoints(points);

                List<List<GeoPoint>> holePointList = new ArrayList<List<GeoPoint>>();
                for (PolygonHoleMarkers hole : holes) {
                    if (!hole.isDeleted()) {
                        List<GeoPoint> holePoints = converter
                                .getPointsFromMarkers(hole.getMarkers());
                        holePointList.add(holePoints);
                    }
                }
                polygon.setHoles(holePointList);
            }
        }
    }    */

    /**
     * Remove from the map

    public void remove() {
        if (polygon != null) {
            polygon.remove();
            polygon = null;
        }
        for (Marker marker : markers) {
            marker.remove();
        }
        for (PolygonHoleMarkers hole : holes) {
            hole.remove();
        }
    }    */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        if (polygon != null) {
            polygon.setVisible(visible);
        }
        for (Marker marker : markers) {
            marker.setVisible(visible);
        }
        for (PolygonHoleMarkers hole : holes) {
            hole.setVisible(visible);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisibleMarkers(boolean visible) {
        for (Marker marker : markers) {
            marker.setVisible(visible);
        }
        for (PolygonHoleMarkers hole : holes) {
            hole.setVisibleMarkers(visible);
        }
    }

    /**
     * Is it valid
     *
     * @return
     */
    public boolean isValid() {
        boolean valid = markers.isEmpty() || markers.size() >= 3;
        if (valid) {
            for (PolygonHoleMarkers hole : holes) {
                valid = hole.isValid();
                if (!valid) {
                    break;
                }
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
        return markers.isEmpty();
    }

    /**
     * {@inheritDoc}

    @Override
    public void delete(Marker marker) {
        if (markers.remove(marker)) {
            marker.remove();
            update();
        }
    }  */

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNew(Marker marker) {
        OsmdroidShapeMarkers.addMarkerAsPolygon(marker, markers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShapeMarkers createChild() {
        PolygonHoleMarkers hole = new PolygonHoleMarkers(this);
        holes.add(hole);
        return hole;
    }

}

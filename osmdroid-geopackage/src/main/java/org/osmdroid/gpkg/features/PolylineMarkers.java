package org.osmdroid.gpkg.features;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Polyline with Markers object
 *
 * @author osbornb
 */
public class PolylineMarkers implements ShapeMarkers {

    private final OsmMapShapeConverter converter;

    private Polyline polyline;

    private List<Marker> markers = new ArrayList<Marker>();

    /**
     * Constructor
     *
     * @param converter
     */
    public PolylineMarkers(OsmMapShapeConverter converter) {
        this.converter = converter;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
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

    /**
     * Update based upon marker changes

    public void update() {
        if (polyline != null) {
            if (isDeleted()) {
                remove();
            } else {
                List<GeoPoint> points = converter.getPointsFromMarkers(markers);
                polyline.setPoints(points);
            }
        }
    }  */

    /**
     * Remove from the map

    public void remove() {
        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }
        for (Marker marker : markers) {
            marker.remove();
        }
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        if (polyline != null) {
            polyline.setVisible(visible);
        }
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
     * Is it valid
     *
     * @return
     */
    public boolean isValid() {
        return markers.isEmpty() || markers.size() >= 2;
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
    }
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public void addNew(Marker marker) {
        OsmdroidShapeMarkers.addMarkerAsPolyline(marker, markers);
    }

}

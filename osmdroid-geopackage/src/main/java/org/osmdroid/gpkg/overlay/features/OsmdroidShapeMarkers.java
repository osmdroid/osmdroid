/**
 * The MIT License
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * This code was sourced from the National Geospatial Intelligency Agency and was
 * originally licensed under the MIT license. It has been modified to support
 * osmdroid's APIs.
 * <p>
 * You can find the original code base here:
 * https://github.com/ngageoint/geopackage-android-map
 * https://github.com/ngageoint/geopackage-android
 */

package org.osmdroid.gpkg.overlay.features;


import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Map Shape with markers
 *
 * @author osbornb
 */
public class OsmdroidShapeMarkers {

    /**
     * Shape
     */
    private OsmDroidMapShape shape;

    /**
     * Map between marker ids and shape markers they belong to (or null for non
     * shapes)
     */
    private Map<String, ShapeMarkers> shapeMarkersMap = new HashMap<String, ShapeMarkers>();

    /**
     * Add the marker to the shape
     *
     * @param marker
     * @param shapeMarkers
     */
    public void add(Marker marker, ShapeMarkers shapeMarkers) {
        add(marker.getId(), shapeMarkers);
    }

    /**
     * Add the marker id to the shape
     *
     * @param markerId
     * @param shapeMarkers
     */
    public void add(String markerId, ShapeMarkers shapeMarkers) {
        shapeMarkersMap.put(markerId, shapeMarkers);
    }

    /**
     * Add all markers in the shape
     *
     * @param shapeMarkers
     */
    public void add(ShapeMarkers shapeMarkers) {
        for (Marker marker : shapeMarkers.getMarkers()) {
            add(marker, shapeMarkers);
        }
    }

    /**
     * Add a marker with no shape
     *
     * @param marker
     */
    public void add(Marker marker) {
        add(marker, null);
    }

    /**
     * Add a list of markers with no shape
     *
     * @param markers
     */
    public void add(List<Marker> markers) {
        for (Marker marker : markers) {
            add(marker);
        }
    }

    /**
     * Add an embedded shape markers
     *
     * @param googleShapeMarkers
     */
    public void add(OsmdroidShapeMarkers googleShapeMarkers) {
        shapeMarkersMap.putAll(googleShapeMarkers.shapeMarkersMap);
    }

    /**
     * Get the map shape
     *
     * @return map shape
     */
    public OsmDroidMapShape getShape() {
        return shape;
    }

    /**
     * Set the map shape
     *
     * @param shape map shape
     */
    public void setShape(OsmDroidMapShape shape) {
        this.shape = shape;
    }

    /**
     * Get the shape markers map
     *
     * @return shape markers map
     * @since 1.3.2
     */
    public Map<String, ShapeMarkers> getShapeMarkersMap() {
        return shapeMarkersMap;
    }

    /**
     * Check if contains the marker
     *
     * @param marker
     * @return
     */
    public boolean contains(Marker marker) {
        return contains(marker.getId());
    }

    /**
     * Check if contains the marker id
     *
     * @param markerId
     * @return
     */
    public boolean contains(String markerId) {
        return shapeMarkersMap.containsKey(markerId);
    }

    /**
     * Get the shape markers for a marker, only returns a value of shapes that
     * can be edited
     *
     * @param marker
     * @return
     */
    public ShapeMarkers getShapeMarkers(Marker marker) {
        return getShapeMarkers(marker.getId());
    }

    /**
     * Get the shape markers for a marker id, only returns a value of shapes
     * that can be edited
     *
     * @param markerId
     * @return
     */
    public ShapeMarkers getShapeMarkers(String markerId) {
        return shapeMarkersMap.get(markerId);
    }

    /**
     * Determines if the shape is in a valid state
     */
    public boolean isValid() {
        boolean valid = true;
        if (shape != null) {
            valid = shape.isValid();
        }
        return valid;
    }

    /**
     * Polygon add a marker in the list of markers to where it is closest to the
     * the surrounding points
     *
     * @param marker
     * @param markers
     */
    public static void addMarkerAsPolygon(Marker marker, List<Marker> markers) {
        IGeoPoint position = marker.getPosition();
        int insertLocation = markers.size();
        if (markers.size() > 2) {
            double[] distances = new double[markers.size()];
            insertLocation = 0;
            distances[0] = SphericalUtil.computeDistanceBetween(position,
                    markers.get(0).getPosition());
            for (int i = 1; i < markers.size(); i++) {
                distances[i] = SphericalUtil.computeDistanceBetween(position,
                        markers.get(i).getPosition());
                if (distances[i] < distances[insertLocation]) {
                    insertLocation = i;
                }
            }

            int beforeLocation = insertLocation > 0 ? insertLocation - 1
                    : distances.length - 1;
            int afterLocation = insertLocation < distances.length - 1 ? insertLocation + 1
                    : 0;

            if (distances[beforeLocation] > distances[afterLocation]) {
                insertLocation = afterLocation;
            }

        }
        markers.add(insertLocation, marker);
    }

    /**
     * Polyline add a marker in the list of markers to where it is closest to
     * the the surrounding points
     *
     * @param marker
     * @param markers
     */
    public static void addMarkerAsPolyline(Marker marker, List<Marker> markers) {
        GeoPoint position = marker.getPosition();
        int insertLocation = markers.size();
        if (markers.size() > 1) {
            double[] distances = new double[markers.size()];
            insertLocation = 0;
            distances[0] = SphericalUtil.computeDistanceBetween(position,
                    markers.get(0).getPosition());
            for (int i = 1; i < markers.size(); i++) {
                distances[i] = SphericalUtil.computeDistanceBetween(position,
                        markers.get(i).getPosition());
                if (distances[i] < distances[insertLocation]) {
                    insertLocation = i;
                }
            }

            Integer beforeLocation = insertLocation > 0 ? insertLocation - 1
                    : null;
            Integer afterLocation = insertLocation < distances.length - 1 ? insertLocation + 1
                    : null;

            if (beforeLocation != null && afterLocation != null) {
                if (distances[beforeLocation] > distances[afterLocation]) {
                    insertLocation = afterLocation;
                }
            } else if (beforeLocation != null) {
                if (distances[beforeLocation] >= SphericalUtil
                        .computeDistanceBetween(markers.get(beforeLocation)
                                .getPosition(), markers.get(insertLocation)
                                .getPosition())) {
                    insertLocation++;
                }
            } else {
                if (distances[afterLocation] < SphericalUtil
                        .computeDistanceBetween(markers.get(afterLocation)
                                .getPosition(), markers.get(insertLocation)
                                .getPosition())) {
                    insertLocation++;
                }
            }

        }
        markers.add(insertLocation, marker);
    }

    /**
     * Updates visibility of all objects
     *
     * @param visible visible flag
     * @since 1.3.2
     */
    public void setVisible(boolean visible) {
        setVisibleMarkers(visible);
    }

    /**
     * Updates visibility of the shape representing markers
     *
     * @param visible visible flag
     * @since 1.3.2
     */
    public void setVisibleMarkers(boolean visible) {
        for (ShapeMarkers shapeMarkers : shapeMarkersMap.values()) {
            shapeMarkers.setVisibleMarkers(visible);
        }
    }

    /**
     * Get the shape markers size
     *
     * @return size
     * @since 1.3.2
     */
    public int size() {
        return shapeMarkersMap.size();
    }

    /**
     * Check if the shape markers is empty
     *
     * @return true if empty
     * @since 1.3.2
     */
    public boolean isEmpty() {
        return shapeMarkersMap.isEmpty();
    }

}

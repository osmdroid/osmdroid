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

import org.osmdroid.gpkg.overlay.OsmMapShapeConverter;
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

     @Override public void delete(Marker marker) {
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

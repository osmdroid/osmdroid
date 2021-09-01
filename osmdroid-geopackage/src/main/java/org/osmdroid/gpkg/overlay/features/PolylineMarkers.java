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

     @Override public void delete(Marker marker) {
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

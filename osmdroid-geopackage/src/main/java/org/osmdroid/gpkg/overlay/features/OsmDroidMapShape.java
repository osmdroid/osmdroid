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


import java.util.List;

import mil.nga.wkb.geom.GeometryType;

/**
 * Google Map Shape
 *
 * @author osbornb
 */
public class OsmDroidMapShape {

    /**
     * Geometry type
     */
    private GeometryType geometryType;

    /**
     * Shape type
     */
    private OsmMapShapeType shapeType;

    /**
     * Shape objects
     */
    private Object shape;

    /**
     * Constructor
     *
     * @param geometryType
     * @param shapeType
     * @param shape
     */
    public OsmDroidMapShape(GeometryType geometryType,
                            OsmMapShapeType shapeType, Object shape) {
        this.geometryType = geometryType;
        this.shapeType = shapeType;
        this.shape = shape;
    }

    /**
     * Get the geometry type
     *
     * @return
     */
    public GeometryType getGeometryType() {
        return geometryType;
    }

    /**
     * Set the geometry type
     *
     * @param geometryType
     */
    public void setGeometryType(GeometryType geometryType) {
        this.geometryType = geometryType;
    }

    /**
     * Get the shape type
     *
     * @return
     */
    public OsmMapShapeType getShapeType() {
        return shapeType;
    }

    /**
     * Set the shape type
     *
     * @param shapeType
     */
    public void setShapeType(OsmMapShapeType shapeType) {
        this.shapeType = shapeType;
    }

    /**
     * Get the shape
     *
     * @return
     */
    public Object getShape() {
        return shape;
    }

    /**
     * Set the shape
     *
     * @param shape
     */
    public void setShape(Object shape) {
        this.shape = shape;
    }

    /**
     * Removes all objects added to the map

     public void remove() {

     switch (shapeType) {

     case MARKER:
     ((Marker) shape).remove();
     break;
     case POLYGON:
     ((Polygon) shape).remove();
     break;
     case POLYLINE:
     ((Polyline) shape).remove();
     break;
     case MULTI_MARKER:
     ((MultiMarker) shape).remove();
     break;
     case MULTI_POLYLINE:
     ((MultiPolyline) shape).remove();
     break;
     case MULTI_POLYGON:
     ((MultiPolygon) shape).remove();
     break;
     case POLYLINE_MARKERS:
     ((PolylineMarkers) shape).remove();
     break;
     case POLYGON_MARKERS:
     ((PolygonMarkers) shape).remove();
     break;
     case MULTI_POLYLINE_MARKERS:
     ((MultiPolylineMarkers) shape).remove();
     break;
     case MULTI_POLYGON_MARKERS:
     ((MultiPolygonMarkers) shape).remove();
     break;
     case COLLECTION:
     @SuppressWarnings("unchecked") List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
     for (GoogleMapShape shapeListItem : shapeList) {
     shapeListItem.remove();
     }
     break;
     default:
     }

     }
     */
    /**
     * Updates visibility of all objects
     *
     * @param visible visible flag
     * @since 1.3.2

    public void setVisible(boolean visible) {

    switch (shapeType) {

    case MARKER:
    ((Marker) shape).setVisible(visible);
    break;
    case POLYGON:
    ((Polygon) shape).setVisible(visible);
    break;
    case POLYLINE:
    ((Polyline) shape).setVisible(visible);
    break;
    case MULTI_MARKER:
    ((MultiMarker) shape).setVisible(visible);
    break;
    case MULTI_POLYLINE:
    ((MultiPolyline) shape).setVisible(visible);
    break;
    case MULTI_POLYGON:
    ((MultiPolygon) shape).setVisible(visible);
    break;
    case POLYLINE_MARKERS:
    ((PolylineMarkers) shape).setVisible(visible);
    break;
    case POLYGON_MARKERS:
    ((PolygonMarkers) shape).setVisible(visible);
    break;
    case MULTI_POLYLINE_MARKERS:
    ((MultiPolylineMarkers) shape).setVisible(visible);
    break;
    case MULTI_POLYGON_MARKERS:
    ((MultiPolygonMarkers) shape).setVisible(visible);
    break;
    case COLLECTION:
     @SuppressWarnings("unchecked") List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
     for (GoogleMapShape shapeListItem : shapeList) {
     shapeListItem.setVisible(visible);
     }
     break;
     default:
     }

     }
     */
    /**
     * Updates all objects that could have changed from moved markers

     public void update() {

     switch (shapeType) {

     case POLYLINE_MARKERS:
     ((PolylineMarkers) shape).update();
     break;
     case POLYGON_MARKERS:
     ((PolygonMarkers) shape).update();
     break;
     case MULTI_POLYLINE_MARKERS:
     ((MultiPolylineMarkers) shape).update();
     break;
     case MULTI_POLYGON_MARKERS:
     ((MultiPolygonMarkers) shape).update();
     break;
     case COLLECTION:
     @SuppressWarnings("unchecked") List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
     for (GoogleMapShape shapeListItem : shapeList) {
     shapeListItem.update();
     }
     break;
     default:
     }

     } */

    /**
     * Determines if the shape is in a valid state
     */
    public boolean isValid() {

        boolean valid = true;

        switch (shapeType) {

            case POLYLINE_MARKERS:
                valid = ((PolylineMarkers) shape).isValid();
                break;
            case POLYGON_MARKERS:
                valid = ((PolygonMarkers) shape).isValid();
                break;
            case MULTI_POLYLINE_MARKERS:
                valid = ((MultiPolylineMarkers) shape).isValid();
                break;
            case MULTI_POLYGON_MARKERS:
                valid = ((MultiPolygonMarkers) shape).isValid();
                break;
            case COLLECTION:
                @SuppressWarnings("unchecked")
                List<OsmDroidMapShape> shapeList = (List<OsmDroidMapShape>) shape;
                for (OsmDroidMapShape shapeListItem : shapeList) {
                    valid = shapeListItem.isValid();
                    if (!valid) {
                        break;
                    }
                }
                break;
            default:
        }

        return valid;
    }


    public void setVisible(boolean visible) {

    }
}

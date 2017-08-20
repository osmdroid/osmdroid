/**
 The MIT License

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 This code was sourced from the National Geospatial Intelligency Agency and was
 originally licensed under the MIT license. It has been modified to support
 osmdroid's APIs.

 You can find the original code base here:
 https://github.com/ngageoint/geopackage-android-map
 https://github.com/ngageoint/geopackage-android
 */

package org.osmdroid.gpkg.features;


import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.projection.ProjectionConstants;
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
                @SuppressWarnings("unchecked")
                List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
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
                @SuppressWarnings("unchecked")
                List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
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
                @SuppressWarnings("unchecked")
                List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
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

    /**
     * Get a bounding box that includes the shape
     *
     * @return
     */
    public BoundingBox boundingBox() {
        BoundingBox boundingBox = new BoundingBox(Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE);
        expandBoundingBox(boundingBox);
        return boundingBox;
    }

    /**
     * Expand the bounding box to include the shape
     *
     * @param boundingBox
     */
    public void expandBoundingBox(BoundingBox boundingBox) {

        switch (shapeType) {

            case LAT_LNG:
                expandBoundingBox(boundingBox, (GeoPoint) shape);
                break;
            case MARKER_OPTIONS:
                expandBoundingBox(boundingBox,
                        ((MarkerOptions) shape).getPosition());
                break;
            case POLYLINE_OPTIONS:
                expandBoundingBox(boundingBox,
                        ((PolylineOptions) shape).getPoints());
                break;
            case POLYGON_OPTIONS:
                expandBoundingBox(boundingBox, ((PolygonOptions) shape).getPoints());
                break;
            case MULTI_LAT_LNG:
                expandBoundingBox(boundingBox, ((MultiLatLng) shape).getLatLngs());
                break;
            case MULTI_POLYLINE_OPTIONS:
                MultiPolylineOptions multiPolylineOptions = (MultiPolylineOptions) shape;
                for (PolylineOptions polylineOptions : multiPolylineOptions
                        .getPolylineOptions()) {
                    expandBoundingBox(boundingBox, polylineOptions.getPoints());
                }
                break;
            case MULTI_POLYGON_OPTIONS:
                MultiPolygonOptions multiPolygonOptions = (MultiPolygonOptions) shape;
                for (PolygonOptions polygonOptions : multiPolygonOptions
                        .getPolygonOptions()) {
                    expandBoundingBox(boundingBox, polygonOptions.getPoints());
                }
                break;
            case MARKER:
                expandBoundingBox(boundingBox, ((Marker) shape).getPosition());
                break;
            case POLYLINE:
                expandBoundingBox(boundingBox, ((Polyline) shape).getPoints());
                break;
            case POLYGON:
                expandBoundingBox(boundingBox, ((Polygon) shape).getPoints());
                break;
            case MULTI_MARKER:
                expandBoundingBoxMarkers(boundingBox,
                        ((MultiMarker) shape).getMarkers());
                break;
            case MULTI_POLYLINE:
                MultiPolyline multiPolyline = (MultiPolyline) shape;
                for (Polyline polyline : multiPolyline.getPolylines()) {
                    expandBoundingBox(boundingBox, polyline.getPoints());
                }
                break;
            case MULTI_POLYGON:
                MultiPolygon multiPolygon = (MultiPolygon) shape;
                for (Polygon polygon : multiPolygon.getPolygons()) {
                    expandBoundingBox(boundingBox, polygon.getPoints());
                }
                break;
            case POLYLINE_MARKERS:
                expandBoundingBoxMarkers(boundingBox,
                        ((PolylineMarkers) shape).getMarkers());
                break;
            case POLYGON_MARKERS:
                expandBoundingBoxMarkers(boundingBox,
                        ((PolygonMarkers) shape).getMarkers());
                break;
            case MULTI_POLYLINE_MARKERS:
                MultiPolylineMarkers multiPolylineMarkers = (MultiPolylineMarkers) shape;
                for (PolylineMarkers polylineMarkers : multiPolylineMarkers
                        .getPolylineMarkers()) {
                    expandBoundingBoxMarkers(boundingBox,
                            polylineMarkers.getMarkers());
                }
                break;
            case MULTI_POLYGON_MARKERS:
                MultiPolygonMarkers multiPolygonMarkers = (MultiPolygonMarkers) shape;
                for (PolygonMarkers polygonMarkers : multiPolygonMarkers
                        .getPolygonMarkers()) {
                    expandBoundingBoxMarkers(boundingBox,
                            polygonMarkers.getMarkers());
                }
                break;
            case COLLECTION:
                @SuppressWarnings("unchecked")
                List<OsmDroidMapShape> shapeList = (List<OsmDroidMapShape>) shape;
                for (OsmDroidMapShape shapeListItem : shapeList) {
                    shapeListItem.expandBoundingBox(boundingBox);
                }
                break;
        }

    }

    /**
     * Expand the bounding box by the LatLng
     *
     * @param boundingBox
     * @param latLng
     */
    private void expandBoundingBox(BoundingBox boundingBox, GeoPoint latLng) {

        double latitude = latLng.getLatitude();
        double longitude = latLng.getLongitude();

        if (boundingBox.getMinLongitude() <= 3 * ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH && boundingBox.getMaxLongitude() >= 3 * -ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH) {
            if (longitude < boundingBox.getMinLongitude()) {
                if (boundingBox.getMinLongitude()
                        - longitude > (longitude + (2 * ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH)) - boundingBox.getMaxLongitude()) {
                    longitude += (2 * ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH);
                }
            } else if (longitude > boundingBox.getMaxLongitude()) {
                if (longitude - boundingBox.getMaxLongitude() > boundingBox.getMinLongitude()
                        - (longitude - (2 * ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH))) {
                    longitude -= (2 * ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH);
                }
            }
        }

        if (latitude < boundingBox.getMinLatitude()) {
            boundingBox.setMinLatitude(latitude);
        }
        if (latitude > boundingBox.getMaxLatitude()) {
            boundingBox.setMaxLatitude(latitude);
        }
        if (longitude < boundingBox.getMinLongitude()) {
            boundingBox.setMinLongitude(longitude);
        }
        if (longitude > boundingBox.getMaxLongitude()) {
            boundingBox.setMaxLongitude(longitude);
        }

    }

    /**
     * Expand the bounding box by the LatLngs
     *
     * @param boundingBox
     * @param latLngs
     */
    private void expandBoundingBox(BoundingBox boundingBox, List<GeoPoint> latLngs) {
        for (GeoPoint latLng : latLngs) {
            expandBoundingBox(boundingBox, latLng);
        }
    }

    /**
     * Expand the bounding box by the markers
     *
     * @param boundingBox
     * @param markers
     */
    private void expandBoundingBoxMarkers(BoundingBox boundingBox,
                                          List<Marker> markers) {
        for (Marker marker : markers) {
            expandBoundingBox(boundingBox, marker.getPosition());
        }
    }

    public void setVisible(boolean visible) {

    }
}

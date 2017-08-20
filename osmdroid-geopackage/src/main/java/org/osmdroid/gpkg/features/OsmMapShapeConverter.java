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
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.wkb.geom.CircularString;
import mil.nga.wkb.geom.CompoundCurve;
import mil.nga.wkb.geom.Curve;
import mil.nga.wkb.geom.CurvePolygon;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryCollection;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import mil.nga.wkb.geom.PolyhedralSurface;
import mil.nga.wkb.geom.TIN;
import mil.nga.wkb.geom.Triangle;
/**
 * created on 8/19/2017.
 *
 * @author Alex O'Ree
 */

/**
 * Provides conversions methods between geometry object and Google Maps Android
 * API v2 Shapes
 *
 * @author osbornb
 */
public class OsmMapShapeConverter {


    /**
     * Projection
     */
    private final Projection projection;

    /**
     * Transformation to WGS 84
     */
    private final ProjectionTransform toWgs84;

    /**
     * Transformation from WGS 84
     */
    private final ProjectionTransform fromWgs84;

    /**
     * Convert polygon exteriors to specified orientation
     */
    private PolygonOrientation exteriorOrientation = PolygonOrientation.COUNTERCLOCKWISE;

    /**
     * Convert polygon holes to specified orientation
     */
    private PolygonOrientation holeOrientation = PolygonOrientation.CLOCKWISE;

    /**
     * Constructor
     *
     * @since 1.3.2
     */
    public OsmMapShapeConverter() {
        this(null);
    }

    /**
     * Constructor with specified projection, see
     * {@link FeatureDao#getProjection}
     *
     * @param projection
     */
    public OsmMapShapeConverter(Projection projection) {
        this.projection = projection;
        if (projection != null) {
            toWgs84 = projection
                .getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            Projection wgs84 = toWgs84.getToProjection();
            fromWgs84 = wgs84.getTransformation(projection);
        } else {
            toWgs84 = null;
            fromWgs84 = null;
        }
    }


    /**
     * Get the projection
     *
     * @return
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * Get exterior orientation for conversions. Defaults to PolygonOrientation.COUNTERCLOCKWISE
     *
     * @return exterior orientation
     * @since 1.3.2
     */
    public PolygonOrientation getExteriorOrientation() {
        return exteriorOrientation;
    }

    /**
     * Set the exterior orientation for conversions, set to null to maintain orientation
     *
     * @param exteriorOrientation orientation
     * @since 1.3.2
     */
    public void setExteriorOrientation(PolygonOrientation exteriorOrientation) {
        this.exteriorOrientation = exteriorOrientation;
    }

    /**
     * Get polygon hole orientation for conversions. Defaults to PolygonOrientation.CLOCKWISE
     *
     * @return hole orientation
     * @since 1.3.2
     */
    public PolygonOrientation getHoleOrientation() {
        return holeOrientation;
    }

    /**
     * Set the polygon hole orientation for conversions, set to null to maintain orientation
     *
     * @param holeOrientation orientation
     * @since 1.3.2
     */
    public void setHoleOrientation(PolygonOrientation holeOrientation) {
        this.holeOrientation = holeOrientation;
    }

    /**
     * Transform a projection point to WGS84
     *
     * @param point
     * @return
     */
    public Point toWgs84(Point point) {
        if (projection != null) {
            point = toWgs84.transform(point);
        }
        return point;
    }

    /**
     * Transform a WGS84 point to the projection
     *
     * @param point
     * @return
     */
    public Point toProjection(Point point) {
        if (projection != null) {
            point = fromWgs84.transform(point);
        }
        return point;
    }

    /**
     * Convert a {@link Point} to a {@link GeoPoint}
     *
     * @param point
     * @return
     */
    public GeoPoint toLatLng2(Point point) {
        point = toWgs84(point);
        GeoPoint latLng = new GeoPoint(point.getY(), point.getX());
        return latLng;
    }

    public GeoPoint toLatLng(Point point) {
        point = toWgs84(point);
        GeoPoint latLng = new GeoPoint(point.getY(), point.getX());
        return latLng;
    }


    /**
     * Convert a {@link GeoPoint} to a {@link Point}
     *
     * @param latLng
     * @return
     */
    public Point toPoint(GeoPoint latLng) {
        return toPoint(latLng, false, false);
    }

    /**
     * Convert a {@link GeoPoint} to a {@link Point}
     *
     * @param latLng
     * @param hasZ
     * @param hasM
     * @return
     */
    public Point toPoint(GeoPoint latLng, boolean hasZ, boolean hasM) {
        double y = latLng.getLatitude();
        double x = latLng.getLongitude();
        Point point = new Point(hasZ, hasM, x, y);
        point = toProjection(point);
        return point;
    }

    /**
     * Convert a {@link LineString} to a {@link PolylineOptions}
     *
     * @param lineString
     * @return
     */
    public PolylineOptions toPolyline(LineString lineString) {

        PolylineOptions polylineOptions = new PolylineOptions();
        Double z = null;

        for (Point point : lineString.getPoints()) {
            GeoPoint latLng = toLatLng(point);
            polylineOptions.add(latLng);
            if (point.hasZ()) {
                z = (z == null) ? point.getZ() : Math.max(z, point.getZ());
            }
        }

        if (lineString.hasZ() && z != null) {
            polylineOptions.zIndex(z.floatValue());
        }

        return polylineOptions;
    }

    /**
     * Convert a {@link Polyline} to a {@link LineString}
     *
     * @param polyline
     * @return
     */
    public LineString toLineString(Polyline polyline) {
        return toLineString(polyline, false, false);
    }

    /**
     * Convert a {@link Polyline} to a {@link LineString}
     *
     * @param polyline
     * @param hasZ
     * @param hasM
     * @return
     */
    public LineString toLineString(Polyline polyline, boolean hasZ, boolean hasM) {
        return toLineString2(polyline.getPoints(), hasZ, hasM);
    }

    /**
     * Convert a {@link PolylineOptions} to a {@link LineString}
     *
     * @param polyline
     * @return
     */
    public LineString toLineString(PolylineOptions polyline) {
        return toLineString(polyline, false, false);
    }

    /**
     * Convert a {@link PolylineOptions} to a {@link LineString}
     *
     * @param polyline
     * @param hasZ
     * @param hasM
     * @return
     */
    public LineString toLineString(PolylineOptions polyline, boolean hasZ,
                                   boolean hasM) {
        return toLineString2(polyline.getPoints(), hasZ, hasM);
    }

    /**
     * Convert a list of {@link GeoPoint} to a {@link LineString}
     *
     * @param latLngs
     * @return
     */
    public LineString toLineString(List<GeoPoint> latLngs) {
        return toLineString(latLngs, false, false);
    }

    public LineString toLineString2(List<GeoPoint> latLngs) {
        return toLineString2(latLngs, false, false);
    }

    /**
     * Convert a list of {@link GeoPoint} to a {@link LineString}
     *
     * @param latLngs
     * @param hasZ
     * @param hasM
     * @return
     */
    public LineString toLineString(List<GeoPoint> latLngs, boolean hasZ,
                                   boolean hasM) {

        LineString lineString = new LineString(hasZ, hasM);

        populateLineString(lineString, latLngs);

        return lineString;
    }

    public LineString toLineString2(List<GeoPoint> latLngs, boolean hasZ,
                                   boolean hasM) {

        LineString lineString = new LineString(hasZ, hasM);

        populateLineString2(lineString, latLngs);

        return lineString;
    }

    /**
     * Convert a list of {@link GeoPoint} to a {@link CircularString}
     *
     * @param latLngs
     * @return
     */
    public CircularString toCircularString(List<GeoPoint> latLngs) {
        return toCircularString(latLngs, false, false);
    }

    /**
     * Convert a list of {@link GeoPoint} to a {@link CircularString}
     *
     * @param latLngs
     * @param hasZ
     * @param hasM
     * @return
     */
    public CircularString toCircularString(List<GeoPoint> latLngs, boolean hasZ,
                                           boolean hasM) {

        CircularString circularString = new CircularString(hasZ, hasM);

        populateLineString(circularString, latLngs);

        return circularString;
    }

    public void populateLineString2(LineString lineString, List<GeoPoint> latLngs) {

        for (GeoPoint latLng : latLngs) {
            Point point = toPoint(latLng, lineString.hasZ(), lineString.hasM());
            lineString.addPoint(point);
        }
    }

    /**
     * Convert a list of {@link GeoPoint} to a {@link LineString}
     *
     * @param lineString
     * @param latLngs
     */
    public void populateLineString(LineString lineString, List<GeoPoint> latLngs) {

        for (GeoPoint latLng : latLngs) {
            Point point = toPoint(latLng, lineString.hasZ(), lineString.hasM());
            lineString.addPoint(point);
        }
    }

    /**
     * Convert a {@link Polygon} to a {@link PolygonOptions}
     *
     * @param polygon
     * @return
     */
    public PolygonOptions toPolygon(Polygon polygon) {

        PolygonOptions polygonOptions = new PolygonOptions();

        List<LineString> rings = polygon.getRings();

        if (!rings.isEmpty()) {

            Double z = null;

            // Add the polygon points
            LineString polygonLineString = rings.get(0);
            for (Point point : polygonLineString.getPoints()) {
                GeoPoint latLng = toLatLng(point);
                polygonOptions.add(latLng);
                if (point.hasZ()) {
                    z = (z == null) ? point.getZ() : Math.max(z, point.getZ());
                }
            }

            // Add the holes
            for (int i = 1; i < rings.size(); i++) {
                LineString hole = rings.get(i);
                List<GeoPoint> holeLatLngs = new ArrayList<GeoPoint>();
                for (Point point : hole.getPoints()) {
                    GeoPoint latLng = toLatLng(point);
                    holeLatLngs.add(latLng);
                    if (point.hasZ()) {
                        z = (z == null) ? point.getZ() : Math.max(z,
                            point.getZ());
                    }
                }
                polygonOptions.addHole(holeLatLngs);
            }

            if (polygon.hasZ() && z != null) {
                polygonOptions.zIndex(z.floatValue());
            }
        }

        return polygonOptions;
    }

    /**
     * Convert a {@link CurvePolygon} to a {@link PolygonOptions}
     *
     * @param curvePolygon curve polygon
     * @return polygon options
     * @since 1.4.1
     */
    public PolygonOptions toCurvePolygon(CurvePolygon curvePolygon) {

        PolygonOptions polygonOptions = new PolygonOptions();

        List<Curve> rings = curvePolygon.getRings();

        if (!rings.isEmpty()) {

            Double z = null;

            // Add the polygon points
            Curve curve = rings.get(0);
            if (curve instanceof CompoundCurve) {
                CompoundCurve compoundCurve = (CompoundCurve) curve;
                for (LineString lineString : compoundCurve.getLineStrings()) {
                    for (Point point : lineString.getPoints()) {
                        GeoPoint latLng = toLatLng(point);
                        polygonOptions.add(latLng);
                        if (point.hasZ()) {
                            z = (z == null) ? point.getZ() : Math.max(z, point.getZ());
                        }
                    }
                }
            } else if (curve instanceof LineString) {
                LineString lineString = (LineString) curve;
                for (Point point : lineString.getPoints()) {
                    GeoPoint latLng = toLatLng(point);
                    polygonOptions.add(latLng);
                    if (point.hasZ()) {
                        z = (z == null) ? point.getZ() : Math.max(z, point.getZ());
                    }
                }
            } else {
                throw new GeoPackageException("Unsupported Curve Type: "
                    + curve.getClass().getSimpleName());
            }

            // Add the holes
            for (int i = 1; i < rings.size(); i++) {
                Curve hole = rings.get(i);
                List<GeoPoint> holeLatLngs = new ArrayList<GeoPoint>();
                if (hole instanceof CompoundCurve) {
                    CompoundCurve holeCompoundCurve = (CompoundCurve) hole;
                    for (LineString holeLineString : holeCompoundCurve.getLineStrings()) {
                        for (Point point : holeLineString.getPoints()) {
                            GeoPoint latLng = toLatLng(point);
                            holeLatLngs.add(latLng);
                            if (point.hasZ()) {
                                z = (z == null) ? point.getZ() : Math.max(z,
                                    point.getZ());
                            }
                        }
                    }
                } else if (hole instanceof LineString) {
                    LineString holeLineString = (LineString) hole;
                    for (Point point : holeLineString.getPoints()) {
                        GeoPoint latLng = toLatLng(point);
                        holeLatLngs.add(latLng);
                        if (point.hasZ()) {
                            z = (z == null) ? point.getZ() : Math.max(z,
                                point.getZ());
                        }
                    }
                } else {
                    throw new GeoPackageException("Unsupported Curve Hole Type: "
                        + hole.getClass().getSimpleName());
                }

                polygonOptions.addHole(holeLatLngs);
            }

            if (curvePolygon.hasZ() && z != null) {
                polygonOptions.zIndex(z.floatValue());
            }
        }

        return polygonOptions;
    }

    /**
     * Convert a {@link org.osmdroid.views.overlay.Polygon} to a
     * {@link Polygon}
     *
     * @param polygon
     * @return
     */
    public Polygon toPolygon(org.osmdroid.views.overlay.Polygon polygon) {
        return toPolygon(polygon, false, false);
    }

    /**
     * Convert a {@link org.osmdroid.views.overlay.Polygon} to a
     * {@link Polygon}
     *
     * @param polygon
     * @param hasZ
     * @param hasM
     * @return
     */
    public Polygon toPolygon(org.osmdroid.views.overlay.Polygon polygon,
                             boolean hasZ, boolean hasM) {
        return toPolygon(polygon.getPoints(), polygon.getHoles(), hasZ, hasM);
    }

    /**
     * Convert a {@link org.osmdroid.views.overlay.Polygon} to a
     * {@link Polygon}
     *
     * @param polygon
     * @return
     */
    public Polygon toPolygon(PolygonOptions polygon) {
        return toPolygon(polygon, false, false);
    }

    /**
     * Convert a {@link org.osmdroid.views.overlay.Polygon} to a
     * {@link Polygon}
     *
     * @param polygon
     * @param hasZ
     * @param hasM
     * @return
     */
    public Polygon toPolygon(PolygonOptions polygon, boolean hasZ, boolean hasM) {
        return toPolygon(polygon.getPoints(), polygon.getHoles(), hasZ, hasM);
    }

    /**
     * Convert a list of {@link GeoPoint} and list of hole list {@link GeoPoint} to
     * a {@link Polygon}
     *
     * @param latLngs
     * @param holes
     * @return
     */
    public Polygon toPolygon(List<GeoPoint> latLngs, List<List<GeoPoint>> holes) {
        return toPolygon(latLngs, holes, false, false);
    }

    /**
     * Convert a list of {@link GeoPoint} and list of hole list {@link GeoPoint} to
     * a {@link Polygon}
     *
     * @param latLngs
     * @param holes
     * @param hasZ
     * @param hasM
     * @return
     */
    public Polygon toPolygon(List<GeoPoint> latLngs, List<List<GeoPoint>> holes,
                             boolean hasZ, boolean hasM) {

        Polygon polygon = new Polygon(hasZ, hasM);

        // Close the ring if needed and determine orientation
        closePolygonRing(latLngs);
        PolygonOrientation ringOrientation = getOrientation(latLngs);

        // Add the polygon points
        LineString polygonLineString = new LineString(hasZ, hasM);
        for (GeoPoint latLng : latLngs) {
            Point point = toPoint(latLng);
            // Add exterior in desired orientation order
            if (exteriorOrientation == null || exteriorOrientation == ringOrientation) {
                polygonLineString.addPoint(point);
            } else {
                polygonLineString.getPoints().add(0, point);
            }
        }
        polygon.addRing(polygonLineString);

        // Add the holes
        if (holes != null) {
            for (List<GeoPoint> hole : holes) {

                // Close the hole if needed and determine orientation
                closePolygonRing(hole);
                PolygonOrientation ringHoleOrientation = getOrientation(hole);

                LineString holeLineString = new LineString(hasZ, hasM);
                for (GeoPoint latLng : hole) {
                    Point point = toPoint(latLng);
                    // Add holes in desired orientation order
                    if (holeOrientation == null || holeOrientation == ringHoleOrientation) {
                        holeLineString.addPoint(point);
                    } else {
                        holeLineString.getPoints().add(0, point);
                    }
                }
                polygon.addRing(holeLineString);
            }
        }

        return polygon;
    }

    /**
     * Close the polygon ring (exterior or hole) points if needed
     *
     * @param points ring points
     * @since 1.3.2
     */
    public void closePolygonRing(List<GeoPoint> points) {
        if (!PolyUtil.isClosedPolygon(points)) {
            GeoPoint first = points.get(0);
            points.add(new GeoPoint(first.getLatitude(), first.getLongitude()));
        }
    }

    /**
     * Determine the closed points orientation
         * @param points closed points
     * @return orientation
     * @since 1.3.2
        */
    public PolygonOrientation getOrientation(List<GeoPoint> points) {
        return SphericalUtil.computeSignedArea(points) >= 0 ? PolygonOrientation.COUNTERCLOCKWISE : PolygonOrientation.CLOCKWISE;
    }

    /**
     * Convert a {@link MultiPoint} to a {@link MultiLatLng}
     *
     * @param multiPoint
     * @return
     */
    public MultiLatLng toLatLngs(MultiPoint multiPoint) {

        MultiLatLng multiLatLng = new MultiLatLng();

        for (Point point : multiPoint.getPoints()) {
            GeoPoint latLng = toLatLng2(point);
            multiLatLng.add(latLng);
        }

        return multiLatLng;
    }

    /**
     * Convert a {@link MultiLatLng} to a {@link MultiPoint}
     *
     * @param latLngs
     * @return
     */
    public MultiPoint toMultiPoint(MultiLatLng latLngs) {
        return toMultiPoint(latLngs, false, false);
    }

    /**
     * Convert a {@link MultiLatLng} to a {@link MultiPoint}
     *
     * @param latLngs
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiPoint toMultiPoint(MultiLatLng latLngs, boolean hasZ,
                                   boolean hasM) {
        return toMultiPoint(latLngs.getLatLngs(), hasZ, hasM);
    }

    /**
     * Convert a {@link MultiLatLng} to a {@link MultiPoint}
     *
     * @param latLngs
     * @return
     */
    public MultiPoint toMultiPoint(List<GeoPoint> latLngs) {
        return toMultiPoint(latLngs, false, false);
    }

    /**
     * Convert a {@link MultiLatLng} to a {@link MultiPoint}
     *
     * @param latLngs
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiPoint toMultiPoint(List<GeoPoint> latLngs, boolean hasZ,
                                   boolean hasM) {

        MultiPoint multiPoint = new MultiPoint(hasZ, hasM);

        for (GeoPoint latLng : latLngs) {
            Point point = toPoint(latLng);
            multiPoint.addPoint(point);
        }

        return multiPoint;
    }

    /**
     * Convert a {@link MultiLineString} to a {@link MultiPolylineOptions}
     *
     * @param multiLineString
     * @return
     */
    public MultiPolylineOptions toPolylines(MultiLineString multiLineString) {

        MultiPolylineOptions polylines = new MultiPolylineOptions();

        for (LineString lineString : multiLineString.getLineStrings()) {
            PolylineOptions polyline = toPolyline(lineString);
            polylines.add(polyline);
        }

        return polylines;
    }

    /**
     * Convert a list of {@link Polyline} to a {@link MultiLineString}
     *
     * @param polylineList
     * @return
     */
    public MultiLineString toMultiLineString(List<Polyline> polylineList) {
        return toMultiLineString(polylineList, false, false);
    }

    /**
     * Convert a list of {@link Polyline} to a {@link MultiLineString}
     *
     * @param polylineList
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiLineString toMultiLineString(List<Polyline> polylineList,
                                             boolean hasZ, boolean hasM) {

        MultiLineString multiLineString = new MultiLineString(hasZ, hasM);

        for (Polyline polyline : polylineList) {
            LineString lineString = toLineString(polyline);
            multiLineString.addLineString(lineString);
        }

        return multiLineString;
    }

    /**
     * Convert a list of List<GeoPoint> to a {@link MultiLineString}
     *
     * @param polylineList
     * @return
     */
    public MultiLineString toMultiLineStringFromList(
        List<List<GeoPoint>> polylineList) {
        return toMultiLineStringFromList(polylineList, false, false);
    }

    /**
     * Convert a list of List<GeoPoint> to a {@link MultiLineString}
     *
     * @param polylineList
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiLineString toMultiLineStringFromList(
        List<List<GeoPoint>> polylineList, boolean hasZ, boolean hasM) {

        MultiLineString multiLineString = new MultiLineString(hasZ, hasM);

        for (List<GeoPoint> polyline : polylineList) {
            LineString lineString = toLineString(polyline);
            multiLineString.addLineString(lineString);
        }

        return multiLineString;
    }

    /**
     * Convert a list of List<GeoPoint> to a {@link CompoundCurve}
     *
     * @param polylineList
     * @return
     */
    public CompoundCurve toCompoundCurveFromList(List<List<GeoPoint>> polylineList) {
        return toCompoundCurveFromList(polylineList, false, false);
    }

    /**
     * Convert a list of List<GeoPoint> to a {@link CompoundCurve}
     *
     * @param polylineList
     * @param hasZ
     * @param hasM
     * @return
     */
    public CompoundCurve toCompoundCurveFromList(
        List<List<GeoPoint>> polylineList, boolean hasZ, boolean hasM) {

        CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

        for (List<GeoPoint> polyline : polylineList) {
            LineString lineString = toLineString(polyline);
            compoundCurve.addLineString(lineString);
        }

        return compoundCurve;
    }

    /**
     * Convert a {@link MultiPolylineOptions} to a {@link MultiLineString}
     *
     * @param multiPolylineOptions
     * @return
     */
    public MultiLineString toMultiLineStringFromOptions(
        MultiPolylineOptions multiPolylineOptions) {
        return toMultiLineStringFromOptions(multiPolylineOptions, false, false);
    }

    /**
     * Convert a {@link MultiPolylineOptions} to a {@link MultiLineString}
     *
     * @param multiPolylineOptions
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiLineString toMultiLineStringFromOptions(
        MultiPolylineOptions multiPolylineOptions, boolean hasZ,
        boolean hasM) {

        MultiLineString multiLineString = new MultiLineString(hasZ, hasM);

        for (PolylineOptions polyline : multiPolylineOptions
            .getPolylineOptions()) {
            LineString lineString = toLineString(polyline);
            multiLineString.addLineString(lineString);
        }

        return multiLineString;
    }

    /**
     * Convert a {@link MultiPolylineOptions} to a {@link CompoundCurve}
     *
     * @param multiPolylineOptions
     * @return
     */
    public CompoundCurve toCompoundCurveFromOptions(
        MultiPolylineOptions multiPolylineOptions) {
        return toCompoundCurveFromOptions(multiPolylineOptions, false, false);
    }

    /**
     * Convert a {@link MultiPolylineOptions} to a {@link CompoundCurve}
     *
     * @param multiPolylineOptions
     * @param hasZ
     * @param hasM
     * @return
     */
    public CompoundCurve toCompoundCurveFromOptions(
        MultiPolylineOptions multiPolylineOptions, boolean hasZ,
        boolean hasM) {

        CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

        for (PolylineOptions polyline : multiPolylineOptions
            .getPolylineOptions()) {
            LineString lineString = toLineString(polyline);
            compoundCurve.addLineString(lineString);
        }

        return compoundCurve;
    }

    /**
     * Convert a {@link MultiPolygon} to a {@link MultiPolygonOptions}
     *
     * @param multiPolygon
     * @return
     */
    public MultiPolygonOptions toPolygons(MultiPolygon multiPolygon) {

        MultiPolygonOptions polygons = new MultiPolygonOptions();

        for (Polygon polygon : multiPolygon.getPolygons()) {
            PolygonOptions polygonOptions = toPolygon(polygon);
            polygons.add(polygonOptions);
        }

        return polygons;
    }

    /**
     * Convert a list of {@link org.osmdroid.views.overlay.Polygon} to a
     * {@link MultiPolygon}
     *
     * @param polygonList
     * @return
     */
    public MultiPolygon toMultiPolygon(
        List<org.osmdroid.views.overlay.Polygon> polygonList) {
        return toMultiPolygon(polygonList, false, false);
    }

    /**
     * Convert a list of {@link org.osmdroid.views.overlay.Polygon} to a
     * {@link MultiPolygon}
     *
     * @param polygonList
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiPolygon toMultiPolygon(
        List<org.osmdroid.views.overlay.Polygon> polygonList,
        boolean hasZ, boolean hasM) {

        MultiPolygon multiPolygon = new MultiPolygon(hasZ, hasM);

        for (org.osmdroid.views.overlay.Polygon mapPolygon : polygonList) {
            Polygon polygon = toPolygon(mapPolygon);
            multiPolygon.addPolygon(polygon);
        }

        return multiPolygon;
    }

    /**
     * Convert a list of {@link Polygon} to a {@link MultiPolygon}
     *
     * @param polygonList
     * @return
     */
    public MultiPolygon createMultiPolygon(List<Polygon> polygonList) {
        return createMultiPolygon(polygonList, false, false);
    }

    /**
     * Convert a list of {@link Polygon} to a {@link MultiPolygon}
     *
     * @param polygonList
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiPolygon createMultiPolygon(List<Polygon> polygonList,
                                           boolean hasZ, boolean hasM) {

        MultiPolygon multiPolygon = new MultiPolygon(hasZ, hasM);

        for (Polygon polygon : polygonList) {
            multiPolygon.addPolygon(polygon);
        }

        return multiPolygon;
    }

    /**
     * Convert a {@link MultiPolygonOptions} to a {@link MultiPolygon}
     *
     * @param multiPolygonOptions
     * @return
     */
    public MultiPolygon toMultiPolygonFromOptions(
        MultiPolygonOptions multiPolygonOptions) {
        return toMultiPolygonFromOptions(multiPolygonOptions, false, false);
    }

    /**
     * Convert a list of {@link PolygonOptions} to a {@link MultiPolygon}
     *
     * @param multiPolygonOptions
     * @param hasZ
     * @param hasM
     * @return
     */
    public MultiPolygon toMultiPolygonFromOptions(
        MultiPolygonOptions multiPolygonOptions, boolean hasZ, boolean hasM) {

        MultiPolygon multiPolygon = new MultiPolygon(hasZ, hasM);

        for (PolygonOptions mapPolygon : multiPolygonOptions
            .getPolygonOptions()) {
            Polygon polygon = toPolygon(mapPolygon);
            multiPolygon.addPolygon(polygon);
        }

        return multiPolygon;
    }

    /**
     * Convert a {@link CompoundCurve} to a {@link MultiPolylineOptions}
     *
     * @param compoundCurve
     * @return
     */
    public MultiPolylineOptions toPolylines(CompoundCurve compoundCurve) {

        MultiPolylineOptions polylines = new MultiPolylineOptions();

        for (LineString lineString : compoundCurve.getLineStrings()) {
            PolylineOptions polyline = toPolyline(lineString);
            polylines.add(polyline);
        }

        return polylines;
    }

    /**
     * Convert a list of {@link Polyline} to a {@link CompoundCurve}
     *
     * @param polylineList
     * @return
     */
    public CompoundCurve toCompoundCurve(List<Polyline> polylineList) {
        return toCompoundCurve(polylineList, false, false);
    }

    /**
     * Convert a list of {@link Polyline} to a {@link CompoundCurve}
     *
     * @param polylineList
     * @param hasZ
     * @param hasM
     * @return
     */
    public CompoundCurve toCompoundCurve(List<Polyline> polylineList,
                                         boolean hasZ, boolean hasM) {

        CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

        for (Polyline polyline : polylineList) {
            LineString lineString = toLineString(polyline);
            compoundCurve.addLineString(lineString);
        }

        return compoundCurve;
    }

    /**
     * Convert a {@link MultiPolylineOptions} to a {@link CompoundCurve}
     *
     * @param multiPolylineOptions
     * @return
     */
    public CompoundCurve toCompoundCurveWithOptions(
        MultiPolylineOptions multiPolylineOptions) {
        return toCompoundCurveWithOptions(multiPolylineOptions, false, false);
    }

    /**
     * Convert a {@link MultiPolylineOptions} to a {@link CompoundCurve}
     *
     * @param multiPolylineOptions
     * @param hasZ
     * @param hasM
     * @return
     */
    public CompoundCurve toCompoundCurveWithOptions(
        MultiPolylineOptions multiPolylineOptions, boolean hasZ,
        boolean hasM) {

        CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

        for (PolylineOptions polyline : multiPolylineOptions
            .getPolylineOptions()) {
            LineString lineString = toLineString(polyline);
            compoundCurve.addLineString(lineString);
        }

        return compoundCurve;
    }

    /**
     * Convert a {@link PolyhedralSurface} to a {@link MultiPolygonOptions}
     *
     * @param polyhedralSurface
     * @return
     */
    public MultiPolygonOptions toPolygons(PolyhedralSurface polyhedralSurface) {

        MultiPolygonOptions polygons = new MultiPolygonOptions();

        for (Polygon polygon : polyhedralSurface.getPolygons()) {
            PolygonOptions polygonOptions = toPolygon(polygon);
            polygons.add(polygonOptions);
        }

        return polygons;
    }

    /**
     * Convert a list of {@link Polygon} to a {@link PolyhedralSurface}
     *
     * @param polygonList
     * @return
     */
    public PolyhedralSurface toPolyhedralSurface(
        List<org.osmdroid.views.overlay.Polygon> polygonList) {
        return toPolyhedralSurface(polygonList, false, false);
    }

    /**
     * Convert a list of {@link Polygon} to a {@link PolyhedralSurface}
     *
     * @param polygonList
     * @param hasZ
     * @param hasM
     * @return
     */
    public PolyhedralSurface toPolyhedralSurface(
        List<org.osmdroid.views.overlay.Polygon> polygonList,
        boolean hasZ, boolean hasM) {

        PolyhedralSurface polyhedralSurface = new PolyhedralSurface(hasZ, hasM);

        for (org.osmdroid.views.overlay.Polygon mapPolygon : polygonList) {
            Polygon polygon = toPolygon(mapPolygon);
            polyhedralSurface.addPolygon(polygon);
        }

        return polyhedralSurface;
    }

    /**
     * Convert a {@link MultiPolygonOptions} to a {@link PolyhedralSurface}
     *
     * @param multiPolygonOptions
     * @return
     */
    public PolyhedralSurface toPolyhedralSurfaceWithOptions(
        MultiPolygonOptions multiPolygonOptions) {
        return toPolyhedralSurfaceWithOptions(multiPolygonOptions, false, false);
    }

    /**
     * Convert a {@link MultiPolygonOptions} to a {@link PolyhedralSurface}
     *
     * @param multiPolygonOptions
     * @param hasZ
     * @param hasM
     * @return
     */
    public PolyhedralSurface toPolyhedralSurfaceWithOptions(
        MultiPolygonOptions multiPolygonOptions, boolean hasZ, boolean hasM) {

        PolyhedralSurface polyhedralSurface = new PolyhedralSurface(hasZ, hasM);

        for (PolygonOptions mapPolygon : multiPolygonOptions
            .getPolygonOptions()) {
            Polygon polygon = toPolygon(mapPolygon);
            polyhedralSurface.addPolygon(polygon);
        }

        return polyhedralSurface;
    }

    /**
     * Convert a {@link Geometry} to a Map shape
     *
     * @param geometry
     * @return
     */

    @SuppressWarnings("unchecked")
    public OsmDroidMapShape toShape(Geometry geometry) {

        OsmDroidMapShape shape = null;

        GeometryType geometryType = geometry.getGeometryType();
        switch (geometryType) {
            case POINT:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.LAT_LNG, toLatLng((Point) geometry));
                break;
            case LINESTRING:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYLINE_OPTIONS,
                    toPolyline((LineString) geometry));
                break;
            case POLYGON:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYGON_OPTIONS,
                    toPolygon((Polygon) geometry));
                break;
            case MULTIPOINT:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_LAT_LNG,
                    toLatLngs((MultiPoint) geometry));
                break;
            case MULTILINESTRING:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYLINE_OPTIONS,
                    toPolylines((MultiLineString) geometry));
                break;
            case MULTIPOLYGON:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYGON_OPTIONS,
                    toPolygons((MultiPolygon) geometry));
                break;
            case CIRCULARSTRING:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYLINE_OPTIONS,
                    toPolyline((CircularString) geometry));
                break;
            case COMPOUNDCURVE:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYLINE_OPTIONS,
                    toPolylines((CompoundCurve) geometry));
                break;
            case CURVEPOLYGON:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYGON_OPTIONS,
                    toCurvePolygon((CurvePolygon) geometry));
                break;
            case POLYHEDRALSURFACE:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYGON_OPTIONS,
                    toPolygons((PolyhedralSurface) geometry));
                break;
            case TIN:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYGON_OPTIONS,
                    toPolygons((TIN) geometry));
                break;
            case TRIANGLE:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYGON_OPTIONS,
                    toPolygon((Triangle) geometry));
                break;
            case GEOMETRYCOLLECTION:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.COLLECTION,
                    toShapes((GeometryCollection<Geometry>) geometry));
                break;
            default:
                throw new GeoPackageException("Unsupported Geometry Type: "
                    + geometryType.getName());
        }

        return shape;
    }

    /**
     * Convert a {@link GeometryCollection} to a list of Map shapes
     *
     * @param geometryCollection
     * @return
     */
    public List<OsmDroidMapShape> toShapes(
        GeometryCollection<Geometry> geometryCollection) {

        List<OsmDroidMapShape> shapes = new ArrayList<OsmDroidMapShape>();

        for (Geometry geometry : geometryCollection.getGeometries()) {
            OsmDroidMapShape shape = toShape(geometry);
            shapes.add(shape);
        }

        return shapes;
    }

    /**
     * Convert a {@link Geometry} to a Map shape and add it
     *
     * @param map
     * @param geometry
     * @return
     */
    @SuppressWarnings("unchecked")
    public OsmDroidMapShape addToMap(MapView map, Geometry geometry) {

        OsmDroidMapShape shape = null;

        GeometryType geometryType = geometry.getGeometryType();
        switch (geometryType) {
            case POINT:
                shape = new OsmDroidMapShape(geometryType, OsmMapShapeType.MARKER,
                    addLatLngToMap(map, toLatLng2((Point) geometry)));
                break;
            case LINESTRING:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYLINE, addPolylineToMap(map,
                    toPolyline((LineString) geometry)));
                break;
            case POLYGON:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYGON, addPolygonToMap(map,
                    toPolygon((Polygon) geometry)));
                break;
            case MULTIPOINT:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_MARKER, addLatLngsToMap(map,
                    toLatLngs((MultiPoint) geometry)));
                break;
            case MULTILINESTRING:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYLINE, addPolylinesToMap(map,
                    toPolylines((MultiLineString) geometry)));
                break;
            case MULTIPOLYGON:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYGON, addPolygonsToMap(map,
                    toPolygons((MultiPolygon) geometry)));
                break;
            case CIRCULARSTRING:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYLINE, addPolylineToMap(map,
                    toPolyline((CircularString) geometry)));
                break;
            case COMPOUNDCURVE:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYLINE, addPolylinesToMap(map,
                    toPolylines((CompoundCurve) geometry)));
                break;
            case CURVEPOLYGON:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYGON, addPolygonToMap(map,
                    toCurvePolygon((CurvePolygon) geometry)));
                break;
            case POLYHEDRALSURFACE:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYGON, addPolygonsToMap(map,
                    toPolygons((PolyhedralSurface) geometry)));
                break;
            case TIN:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.MULTI_POLYGON, addPolygonsToMap(map,
                    toPolygons((TIN) geometry)));
                break;
            case TRIANGLE:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.POLYGON, addPolygonToMap(map,
                    toPolygon((Triangle) geometry)));
                break;
            case GEOMETRYCOLLECTION:
                shape = new OsmDroidMapShape(geometryType,
                    OsmMapShapeType.COLLECTION, addToMap(map,
                    (GeometryCollection<Geometry>) geometry));
                break;
            default:
                throw new GeoPackageException("Unsupported Geometry Type: "
                    + geometryType.getName());
        }

        return shape;
    }

    /**
     * Add a shape to the map
     *
     * @param map
     * @param shape
     * @return
     */
    public static OsmDroidMapShape addShapeToMap(MapView map,
                                               OsmDroidMapShape shape) {

        OsmDroidMapShape addedShape = null;

        switch (shape.getShapeType()) {

            case LAT_LNG:
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MARKER, addLatLngToMap(map,
                    (GeoPoint) shape.getShape()));
                break;
            case MARKER_OPTIONS:
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MARKER, addMarkerOptionsToMap(map,
                    (MarkerOptions) shape.getShape()));
                break;
            case POLYLINE_OPTIONS:
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.POLYLINE, addPolylineToMap(map,
                    (PolylineOptions) shape.getShape()));
                break;
            case POLYGON_OPTIONS:
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.POLYGON, addPolygonToMap(map,
                    (PolygonOptions) shape.getShape()));
                break;
            case MULTI_LAT_LNG:
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MULTI_MARKER, addLatLngsToMap(map,
                    (MultiLatLng) shape.getShape()));
                break;
            case MULTI_POLYLINE_OPTIONS:
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MULTI_POLYLINE, addPolylinesToMap(map,
                    (MultiPolylineOptions) shape.getShape()));
                break;
            case MULTI_POLYGON_OPTIONS:
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MULTI_POLYGON, addPolygonsToMap(map,
                    (MultiPolygonOptions) shape.getShape()));
                break;
            case COLLECTION:
                List<OsmDroidMapShape> addedShapeList = new ArrayList<OsmDroidMapShape>();
                @SuppressWarnings("unchecked")
                List<OsmDroidMapShape> shapeList = (List<OsmDroidMapShape>) shape
                    .getShape();
                for (OsmDroidMapShape shapeListItem : shapeList) {
                    addedShapeList.add(addShapeToMap(map, shapeListItem));
                }
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.COLLECTION, addedShapeList);
                break;
            default:
                throw new GeoPackageException("Unsupported Shape Type: "
                    + shape.getShapeType());

        }

        return addedShape;
    }

    /**
     * Add a LatLng to the map
     *
     * @param map
     * @param latLng
     * @return
     */
    public static Marker addLatLngToMap(MapView map, GeoPoint latLng) {
        return addLatLngToMap(map, latLng, new MarkerOptions());
    }

    /**
     * Add MarkerOptions to the map
     *
     * @param map
     * @param options
     * @return
     */
    public static Marker addMarkerOptionsToMap(MapView map,
                                               MarkerOptions options) {
        Marker m = new Marker(map);
        m.setPosition(options.position);
        map.getOverlayManager().add(m);
        return m;
    }

    /**
     * Add a LatLng to the map
     *
     * @param map
     * @param latLng
     * @param options
     * @return
     */
    public static Marker addLatLngToMap(MapView map, GeoPoint latLng,
                                        MarkerOptions options) {
        Marker m = new Marker(map);
        m.setPosition(latLng);
        map.getOverlayManager().add(m);
        return m;
    }

    /**
     * Add a Polyline to the map
     *
     * @param map
     * @param polyline
     * @return
     */
    public static Polyline addPolylineToMap(MapView map,
                                            PolylineOptions polyline) {
        Polyline line = new Polyline();
        line.setPoints(polyline.getPoints());
        map.getOverlayManager().add(line);
        return line;
    }

    /**
     * Add a Polygon to the map
     *
     * @param map
     * @param polygon
     * @return
     */
    public static org.osmdroid.views.overlay.Polygon addPolygonToMap(
        MapView map, PolygonOptions polygon) {
        org.osmdroid.views.overlay.Polygon polygon1 = new org.osmdroid.views.overlay.Polygon();
        polygon1.setPoints(polygon.getPoints());
        map.getOverlayManager().add(polygon1);
        return polygon1;
    }

    /**
     * Add a list of LatLngs to the map
     *
     * @param map
     * @param latLngs
     * @return
     */
    public static MultiMarker addLatLngsToMap(MapView map, MultiLatLng latLngs) {
        MultiMarker multiMarker = new MultiMarker();
        for (GeoPoint latLng : latLngs.getLatLngs()) {
            MarkerOptions markerOptions = new MarkerOptions();
            if (latLngs.getMarkerOptions() != null) {
                //FIXME arkerOptions.drawable = (latLngs.getMarkerOptions().getIcon());
                //markerOptions.anchor(latLngs.getMarkerOptions().getAnchorU(),
                  //  markerOptions.getAnchorV());
                //markerOptions.draggable(latLngs.getMarkerOptions()
                  //  .isDraggable());
            }
            Marker marker = addLatLngToMap(map, latLng, markerOptions);
            multiMarker.add(marker);
        }
        return multiMarker;
    }

    /**
     * Add a list of Polylines to the map
     *
     * @param map
     * @param polylines
     * @return
     */
    public static MultiPolyline addPolylinesToMap(MapView map,
                                                  MultiPolylineOptions polylines) {
        MultiPolyline multiPolyline = new MultiPolyline();
        for (PolylineOptions polylineOption : polylines.getPolylineOptions()) {
            if (polylines.getOptions() != null) {
                //FIXME   polylineOption.color(polylines.getOptions().getColor());
                //FIXME   polylineOption.geodesic(polylines.getOptions().isGeodesic());
            }
            Polyline polyline = addPolylineToMap(map, polylineOption);
            multiPolyline.add(polyline);
        }
        return multiPolyline;
    }

    /**
     * Add a list of Polygons to the map
     *
     * @param map
     * @param polygons
     * @return
     */
    public static  org.osmdroid.gpkg.features.MultiPolygon  addPolygonsToMap(
        MapView map, MultiPolygonOptions polygons) {
        org.osmdroid.gpkg.features.MultiPolygon multiPolygon = new org.osmdroid.gpkg.features.MultiPolygon();
        for (PolygonOptions polygonOption : polygons.getPolygonOptions()) {
            org.osmdroid.views.overlay.Polygon polygon = addPolygonToMap(
                map, polygonOption);
            if (polygons.getOptions() != null) {
                //FIXME      polygonOption.fillColor(polygons.getOptions().getFillColor());
                //FIXME      polygonOption.strokeColor(polygons.getOptions()
                //FIXME          .getStrokeColor());
                //FIXME      polygonOption.geodesic(polygons.getOptions().isGeodesic());
            }
            multiPolygon.add(polygon);
        }
        return multiPolygon;
    }

    /**
     * Convert a {@link GeometryCollection} to a list of Map shapes and add to
     * the map
     *
     * @param map
     * @param geometryCollection
     * @return
     */
    public List<OsmDroidMapShape> addToMap(MapView map,
                                         GeometryCollection<Geometry> geometryCollection) {

        List<OsmDroidMapShape> shapes = new ArrayList<OsmDroidMapShape>();

        for (Geometry geometry : geometryCollection.getGeometries()) {
            OsmDroidMapShape shape = addToMap(map, geometry);
            shapes.add(shape);
        }

        return shapes;
    }

    /**
     * Add a shape to the map as markers
     *
     * @param map
     * @param shape
     * @param markerOptions
     * @param polylineMarkerOptions
     * @param polygonMarkerOptions
     * @param polygonMarkerHoleOptions
     * @param globalPolylineOptions
     * @param globalPolygonOptions
     * @return
     */
    public OsmdroidShapeMarkers addShapeToMapAsMarkers(MapView map,
                                                       OsmDroidMapShape shape, MarkerOptions markerOptions,
                                                       MarkerOptions polylineMarkerOptions,
                                                       MarkerOptions polygonMarkerOptions,
                                                       MarkerOptions polygonMarkerHoleOptions,
                                                       PolylineOptions globalPolylineOptions,
                                                       PolygonOptions globalPolygonOptions) {

        OsmdroidShapeMarkers shapeMarkers = new OsmdroidShapeMarkers();
        OsmDroidMapShape addedShape = null;

        switch (shape.getShapeType()) {

            case LAT_LNG:
                if (markerOptions == null) {
                    markerOptions = new MarkerOptions();
                }
                Marker latLngMarker = addLatLngToMap(map,
                    (GeoPoint) shape.getShape(), markerOptions);
                shapeMarkers.add(latLngMarker);
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MARKER, latLngMarker);
                break;
            case MARKER_OPTIONS:
                MarkerOptions shapeMarkerOptions = (MarkerOptions) shape.getShape();
                if (markerOptions != null) {
                   //FIXME shapeMarkerOptions.icon(markerOptions.getIcon());
                    //FIXME shapeMarkerOptions.anchor(markerOptions.getAnchorU(),
                    //FIXMEmarkerOptions.getAnchorV());
                    //FIXMEshapeMarkerOptions.draggable(markerOptions.isDraggable());
                }
                Marker markerOptionsMarker = addMarkerOptionsToMap(map,
                    shapeMarkerOptions);
                shapeMarkers.add(markerOptionsMarker);
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MARKER, markerOptionsMarker);
                break;
            case POLYLINE_OPTIONS:
                PolylineMarkers polylineMarkers = addPolylineToMapAsMarkers(map,
                    (PolylineOptions) shape.getShape(), polylineMarkerOptions,
                    globalPolylineOptions);
                shapeMarkers.add(polylineMarkers);
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.POLYLINE_MARKERS, polylineMarkers);
                break;
            case POLYGON_OPTIONS:
                PolygonMarkers polygonMarkers = addPolygonToMapAsMarkers(
                    shapeMarkers, map, (PolygonOptions) shape.getShape(),
                    polygonMarkerOptions, polygonMarkerHoleOptions,
                    globalPolygonOptions);
                shapeMarkers.add(polygonMarkers);
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.POLYGON_MARKERS, polygonMarkers);
                break;
            case MULTI_LAT_LNG:
                MultiLatLng multiLatLng = (MultiLatLng) shape.getShape();
                if (markerOptions != null) {
                    multiLatLng.setMarkerOptions(markerOptions);
                }
                MultiMarker multiMarker = addLatLngsToMap(map, multiLatLng);
                shapeMarkers.add(multiMarker);
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MULTI_MARKER, multiMarker);
                break;
            case MULTI_POLYLINE_OPTIONS:
                MultiPolylineMarkers multiPolylineMarkers = addMultiPolylineToMapAsMarkers(
                    shapeMarkers, map, (MultiPolylineOptions) shape.getShape(),
                    polylineMarkerOptions, globalPolylineOptions);
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MULTI_POLYLINE_MARKERS,
                    multiPolylineMarkers);
                break;
            case MULTI_POLYGON_OPTIONS:
                MultiPolygonMarkers multiPolygonMarkers = addMultiPolygonToMapAsMarkers(
                    shapeMarkers, map, (MultiPolygonOptions) shape.getShape(),
                    polygonMarkerOptions, polygonMarkerHoleOptions,
                    globalPolygonOptions);
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.MULTI_POLYGON_MARKERS,
                    multiPolygonMarkers);
                break;
            case COLLECTION:
                List<OsmDroidMapShape> addedShapeList = new ArrayList<OsmDroidMapShape>();
                @SuppressWarnings("unchecked")
                List<OsmDroidMapShape> shapeList = (List<OsmDroidMapShape>) shape
                    .getShape();
                for (OsmDroidMapShape shapeListItem : shapeList) {
                    OsmdroidShapeMarkers shapeListItemMarkers = addShapeToMapAsMarkers(
                        map, shapeListItem, markerOptions,
                        polylineMarkerOptions, polygonMarkerOptions,
                        polygonMarkerHoleOptions, globalPolylineOptions,
                        globalPolygonOptions);
                    shapeMarkers.add(shapeListItemMarkers);
                    addedShapeList.add(shapeListItemMarkers.getShape());
                }
                addedShape = new OsmDroidMapShape(shape.getGeometryType(),
                    OsmMapShapeType.COLLECTION, addedShapeList);
                break;
            default:
                throw new GeoPackageException("Unsupported Shape Type: "
                    + shape.getShapeType());

        }

        shapeMarkers.setShape(addedShape);

        return shapeMarkers;
    }

    /**
     * Add the list of points as markers
     *
     * @param map
     * @param points
     * @param customMarkerOptions
     * @param ignoreIdenticalEnds
     * @return
     */
    public List<Marker> addPointsToMapAsMarkers(MapView map,
                                                List<GeoPoint> points, MarkerOptions customMarkerOptions,
                                                boolean ignoreIdenticalEnds) {

        List<Marker> markers = new ArrayList<Marker>();
        for (int i = 0; i < points.size(); i++) {
            GeoPoint latLng = points.get(i);

            if (points.size() > 1 && i + 1 == points.size() && ignoreIdenticalEnds) {
                GeoPoint firstLatLng = points.get(0);
                if (latLng.getLatitude() == firstLatLng.getLatitude()
                    && latLng.getLongitude()== firstLatLng.getLongitude()) {
                    break;
                }
            }

            MarkerOptions markerOptions = new MarkerOptions();
            if (customMarkerOptions != null) {
                //FIXMEmarkerOptions.icon(customMarkerOptions.getIcon());
                //FIXMEmarkerOptions.anchor(customMarkerOptions.getAnchorU(),
                //FIXMEcustomMarkerOptions.getAnchorV());
                //FIXMEmarkerOptions.draggable(customMarkerOptions.isDraggable());
            }
            Marker marker = addLatLngToMap(map, latLng, markerOptions);
            markers.add(marker);
        }
        return markers;
    }

    /**
     * Add a Polyline to the map as markers
     *
     * @param map
     * @param polylineOptions
     * @param polylineMarkerOptions
     * @param globalPolylineOptions
     * @return
     */
    public PolylineMarkers addPolylineToMapAsMarkers(MapView map,
                                                     PolylineOptions polylineOptions,
                                                     MarkerOptions polylineMarkerOptions,
                                                     PolylineOptions globalPolylineOptions) {

        PolylineMarkers polylineMarkers = new PolylineMarkers(this);

        if (globalPolylineOptions != null) {
            //FIXMEpolylineOptions.color(globalPolylineOptions.getColor());
            //FIXMEpolylineOptions.geodesic(globalPolylineOptions.isGeodesic());
        }

        Polyline polyline = addPolylineToMap(map, polylineOptions);
        polylineMarkers.setPolyline(polyline);

        List<Marker> markers = addPointsToMapAsMarkers(map,
            polylineOptions.getPoints(), polylineMarkerOptions, false);
        polylineMarkers.setMarkers(markers);

        return polylineMarkers;
    }

    /**
     * Add a Polygon to the map as markers
     *
     * @param shapeMarkers
     * @param map
     * @param polygonOptions
     * @param polygonMarkerOptions
     * @param polygonMarkerHoleOptions
     * @param globalPolygonOptions
     * @return
     */
    public PolygonMarkers addPolygonToMapAsMarkers(
        OsmdroidShapeMarkers shapeMarkers, MapView map,
        PolygonOptions polygonOptions, MarkerOptions polygonMarkerOptions,
        MarkerOptions polygonMarkerHoleOptions,
        PolygonOptions globalPolygonOptions) {

        PolygonMarkers polygonMarkers = new PolygonMarkers(this);

        if (globalPolygonOptions != null) {
            //FIXME polygonOptions.fillColor(globalPolygonOptions.getFillColor());
            //FIXME polygonOptions.strokeColor(globalPolygonOptions.getStrokeColor());
            //FIXME polygonOptions.geodesic(globalPolygonOptions.isGeodesic());
        }

        org.osmdroid.views.overlay.Polygon polygon = addPolygonToMap(
            map, polygonOptions);
        polygonMarkers.setPolygon(polygon);

        List<Marker> markers = addPointsToMapAsMarkers(map,
            polygon.getPoints(), polygonMarkerOptions, true);
        polygonMarkers.setMarkers(markers);

        for (List<GeoPoint> holes : polygon.getHoles()) {
            List<Marker> holeMarkers = addPointsToMapAsMarkers(map, holes,
                polygonMarkerHoleOptions, true);
            PolygonHoleMarkers polygonHoleMarkers = new PolygonHoleMarkers(
                polygonMarkers);
            polygonHoleMarkers.setMarkers(holeMarkers);
            shapeMarkers.add(polygonHoleMarkers);
            polygonMarkers.addHole(polygonHoleMarkers);
        }

        return polygonMarkers;
    }

    /**
     * Add a MultiPolylineOptions to the map as markers
     *
     * @param shapeMarkers
     * @param map
     * @param multiPolyline
     * @param polylineMarkerOptions
     * @param globalPolylineOptions
     * @return
     */
    public MultiPolylineMarkers addMultiPolylineToMapAsMarkers(
        OsmdroidShapeMarkers shapeMarkers, MapView map,
        MultiPolylineOptions multiPolyline,
        MarkerOptions polylineMarkerOptions,
        PolylineOptions globalPolylineOptions) {
        MultiPolylineMarkers polylines = new MultiPolylineMarkers();
        for (PolylineOptions polylineOptions : multiPolyline
            .getPolylineOptions()) {
            PolylineMarkers polylineMarker = addPolylineToMapAsMarkers(map,
                polylineOptions, polylineMarkerOptions,
                globalPolylineOptions);
            shapeMarkers.add(polylineMarker);
            polylines.add(polylineMarker);
        }
        return polylines;
    }

    /**
     * Add a MultiPolygonOptions to the map as markers
     *
     * @param shapeMarkers
     * @param map
     * @param multiPolygon
     * @param polygonMarkerOptions
     * @param polygonMarkerHoleOptions
     * @param globalPolygonOptions
     * @return
     */
    public MultiPolygonMarkers addMultiPolygonToMapAsMarkers(
        OsmdroidShapeMarkers shapeMarkers, MapView map,
        MultiPolygonOptions multiPolygon,
        MarkerOptions polygonMarkerOptions,
        MarkerOptions polygonMarkerHoleOptions,
        PolygonOptions globalPolygonOptions) {
        MultiPolygonMarkers multiPolygonMarkers = new MultiPolygonMarkers();
        for (PolygonOptions polygon : multiPolygon.getPolygonOptions()) {
            PolygonMarkers polygonMarker = addPolygonToMapAsMarkers(
                shapeMarkers, map, polygon, polygonMarkerOptions,
                polygonMarkerHoleOptions, globalPolygonOptions);
            shapeMarkers.add(polygonMarker);
            multiPolygonMarkers.add(polygonMarker);
        }
        return multiPolygonMarkers;
    }

    /**
     * Get a list of points as LatLng from a list of Markers
     *
     * @param markers
     * @return
     */
    public ArrayList<GeoPoint> getPointsFromMarkers(List<Marker> markers) {
        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
        for (Marker marker : markers) {
            points.add(marker.getPosition());
        }
        return points;
    }

    /**
     * Convert a OsmDroidMapShape to a Geometry
     *
     * @param shape
     * @return

    public Geometry toGeometry(OsmDroidMapShape shape) {

        Geometry geometry = null;
        Object shapeObject = shape.getShape();

        switch (shape.getGeometryType()) {

            case POINT:
                GeoPoint point = null;
                switch (shape.getShapeType()) {
                    case LAT_LNG:
                        point = (GeoPoint) shapeObject;
                        break;
                    case MARKER_OPTIONS:
                        MarkerOptions markerOptions = (MarkerOptions) shapeObject;
                        point = markerOptions.getPosition();
                        break;
                    case MARKER:
                        Marker marker = (Marker) shapeObject;
                        point = marker.getPosition();
                        break;
                    default:
                        throw new GeoPackageException("Not a valid "
                            + shape.getGeometryType().getName() + " shape type: "
                            + shape.getShapeType());
                }
                if (point != null) {
                    geometry = toPoint(point);
                }

                break;
            case LINESTRING:
            case CIRCULARSTRING:
                List<GeoPoint> lineStringPoints = null;
                switch (shape.getShapeType()) {
                    case POLYLINE_OPTIONS:
                        PolylineOptions polylineOptions = (PolylineOptions) shapeObject;
                        lineStringPoints = polylineOptions.getPoints();
                        break;
                    case POLYLINE:
                        Polyline polyline = (Polyline) shapeObject;
                        lineStringPoints = polyline.getPoints();
                        break;
                    case POLYLINE_MARKERS:
                        PolylineMarkers polylineMarkers = (PolylineMarkers) shapeObject;
                        if (!polylineMarkers.isValid()) {
                            throw new GeoPackageException(
                                PolylineMarkers.class.getSimpleName()
                                    + " is not valid to create "
                                    + shape.getGeometryType().getName());
                        }
                        if (!polylineMarkers.isDeleted()) {
                            lineStringPoints = getPointsFromMarkers(polylineMarkers
                                .getMarkers());
                        }
                        break;
                    default:
                        throw new GeoPackageException("Not a valid "
                            + shape.getGeometryType().getName() + " shape type: "
                            + shape.getShapeType());
                }
                if (lineStringPoints != null) {
                    switch (shape.getGeometryType()) {
                        case LINESTRING:
                            geometry = toLineString(lineStringPoints);
                            break;
                        case CIRCULARSTRING:
                            geometry = toCircularString(lineStringPoints);
                            break;
                        default:
                            throw new GeoPackageException("Unhandled "
                                + shape.getGeometryType().getName());
                    }
                }

                break;
            case POLYGON:
                List<GeoPoint> polygonPoints = null;
                List<List<GeoPoint>> holePointList = null;
                switch (shape.getShapeType()) {
                    case POLYGON_OPTIONS:
                        PolygonOptions polygonOptions = (PolygonOptions) shapeObject;
                        polygonPoints = polygonOptions.getPoints();
                        holePointList = polygonOptions.getHoles();
                        break;
                    case POLYGON:
                        org.osmdroid.views.overlay.Polygon polygon = (org.osmdroid.views.overlay.Polygon) shapeObject;
                        polygonPoints = polygon.getPoints();
                        holePointList = polygon.getHoles();
                        break;
                    case POLYGON_MARKERS:
                        PolygonMarkers polygonMarkers = (PolygonMarkers) shapeObject;
                        if (!polygonMarkers.isValid()) {
                            throw new GeoPackageException(
                                PolygonMarkers.class.getSimpleName()
                                    + " is not valid to create "
                                    + shape.getGeometryType().getName());
                        }
                        if (!polygonMarkers.isDeleted()) {
                            polygonPoints = getPointsFromMarkers(polygonMarkers
                                .getMarkers());
                            holePointList = new ArrayList<List<GeoPoint>>();
                            for (PolygonHoleMarkers hole : polygonMarkers.getHoles()) {
                                if (!hole.isDeleted()) {
                                    List<GeoPoint> holePoints = getPointsFromMarkers(hole
                                        .getMarkers());
                                    holePointList.add(holePoints);
                                }
                            }
                        }
                        break;
                    default:
                        throw new GeoPackageException("Not a valid "
                            + shape.getGeometryType().getName() + " shape type: "
                            + shape.getShapeType());
                }
                if (polygonPoints != null) {
                    geometry = toPolygon(polygonPoints, holePointList);
                }

                break;
            case MULTIPOINT:
                List<GeoPoint> multiPoints = null;
                switch (shape.getShapeType()) {
                    case MULTI_LAT_LNG:
                        MultiLatLng multiLatLng = (MultiLatLng) shapeObject;
                        multiPoints = multiLatLng.getLatLngs();
                        break;
                    case MULTI_MARKER:
                        MultiMarker multiMarker = (MultiMarker) shapeObject;
                        multiPoints = getPointsFromMarkers(multiMarker.getMarkers());
                        break;
                    default:
                        throw new GeoPackageException("Not a valid "
                            + shape.getGeometryType().getName() + " shape type: "
                            + shape.getShapeType());
                }
                if (multiPoints != null) {
                    geometry = toMultiPoint(multiPoints);
                }

                break;
            case MULTILINESTRING:
            case COMPOUNDCURVE:
                switch (shape.getShapeType()) {
                    case MULTI_POLYLINE_OPTIONS:
                        MultiPolylineOptions multiPolylineOptions = (MultiPolylineOptions) shapeObject;
                        switch (shape.getGeometryType()) {
                            case MULTILINESTRING:
                                geometry = toMultiLineStringFromOptions(multiPolylineOptions);
                                break;
                            case COMPOUNDCURVE:
                                geometry = toCompoundCurveFromOptions(multiPolylineOptions);
                                break;
                            default:
                                throw new GeoPackageException("Unhandled "
                                    + shape.getGeometryType().getName());
                        }
                        break;
                    case MULTI_POLYLINE:
                        MultiPolyline multiPolyline = (MultiPolyline) shapeObject;
                        switch (shape.getGeometryType()) {
                            case MULTILINESTRING:
                                geometry = toMultiLineString(multiPolyline.getPolylines());
                                break;
                            case COMPOUNDCURVE:
                                geometry = toCompoundCurve(multiPolyline.getPolylines());
                                break;
                            default:
                                throw new GeoPackageException("Unhandled "
                                    + shape.getGeometryType().getName());
                        }
                        break;
                    case MULTI_POLYLINE_MARKERS:
                        MultiPolylineMarkers multiPolylineMarkers = (MultiPolylineMarkers) shapeObject;
                        if (!multiPolylineMarkers.isValid()) {
                            throw new GeoPackageException(
                                MultiPolylineMarkers.class.getSimpleName()
                                    + " is not valid to create "
                                    + shape.getGeometryType().getName());
                        }
                        if (!multiPolylineMarkers.isDeleted()) {
                            List<List<GeoPoint>> multiPolylineMarkersList = new ArrayList<List<GeoPoint>>();
                            for (PolylineMarkers polylineMarkers : multiPolylineMarkers
                                .getPolylineMarkers()) {
                                if (!polylineMarkers.isDeleted()) {
                                    multiPolylineMarkersList
                                        .add(getPointsFromMarkers(polylineMarkers
                                            .getMarkers()));
                                }
                            }
                            switch (shape.getGeometryType()) {
                                case MULTILINESTRING:
                                    geometry = toMultiLineStringFromList(multiPolylineMarkersList);
                                    break;
                                case COMPOUNDCURVE:
                                    geometry = toCompoundCurveFromList(multiPolylineMarkersList);
                                    break;
                                default:
                                    throw new GeoPackageException("Unhandled "
                                        + shape.getGeometryType().getName());
                            }
                        }
                        break;
                    default:
                        throw new GeoPackageException("Not a valid "
                            + shape.getGeometryType().getName() + " shape type: "
                            + shape.getShapeType());
                }

                break;
            case MULTIPOLYGON:
                switch (shape.getShapeType()) {
                    case MULTI_POLYGON_OPTIONS:
                        MultiPolygonOptions multiPolygonOptions = (MultiPolygonOptions) shapeObject;
                        geometry = toMultiPolygonFromOptions(multiPolygonOptions);
                        break;
                    case MULTI_POLYGON:
                        MultiPolygon multiPolygon = (MultiPolygon) shapeObject;
                        geometry = toMultiPolygon(multiPolygon.getPolygons());
                        break;
                    case MULTI_POLYGON_MARKERS:
                        MultiPolygonMarkers multiPolygonMarkers = (MultiPolygonMarkers) shapeObject;
                        if (!multiPolygonMarkers.isValid()) {
                            throw new GeoPackageException(
                                MultiPolygonMarkers.class.getSimpleName()
                                    + " is not valid to create "
                                    + shape.getGeometryType().getName());
                        }
                        if (!multiPolygonMarkers.isDeleted()) {
                            List<Polygon> multiPolygonMarkersList = new ArrayList<Polygon>();
                            for (PolygonMarkers polygonMarkers : multiPolygonMarkers
                                .getPolygonMarkers()) {

                                if (!polygonMarkers.isDeleted()) {

                                    List<GeoPoint> multiPolygonPoints = getPointsFromMarkers(polygonMarkers
                                        .getMarkers());
                                    ArrayList<ArrayList<GeoPoint>> multiPolygonHolePoints = new ArrayList<ArrayList<GeoPoint>>();
                                    for (PolygonHoleMarkers hole : polygonMarkers
                                        .getHoles()) {
                                        if (!hole.isDeleted()) {
                                            ArrayList<GeoPoint> holePoints = getPointsFromMarkers(hole
                                                .getMarkers());
                                            multiPolygonHolePoints.add(holePoints);
                                        }
                                    }

                                    multiPolygonMarkersList
                                        .add(toPolygon(multiPolygonPoints,
                                            multiPolygonHolePoints));
                                }

                            }
                            geometry = createMultiPolygon(multiPolygonMarkersList);
                        }
                        break;
                    default:
                        throw new GeoPackageException("Not a valid "
                            + shape.getGeometryType().getName() + " shape type: "
                            + shape.getShapeType());
                }
                break;

            case POLYHEDRALSURFACE:
            case TIN:
            case TRIANGLE:
                throw new GeoPackageException("Unsupported GeoPackage type: "
                    + shape.getGeometryType());
            case GEOMETRYCOLLECTION:
                @SuppressWarnings("unchecked")
                List<OsmDroidMapShape> shapeList = (List<OsmDroidMapShape>) shapeObject;
                GeometryCollection<Geometry> geometryCollection = new GeometryCollection<Geometry>(
                    false, false);
                for (OsmDroidMapShape shapeListItem : shapeList) {
                    Geometry subGeometry = toGeometry(shapeListItem);
                    if (subGeometry != null) {
                        geometryCollection.addGeometry(subGeometry);
                    }
                }
                if (geometryCollection.numGeometries() > 0) {
                    geometry = geometryCollection;
                }
                break;
            default:
        }

        return geometry;
    }  */
}
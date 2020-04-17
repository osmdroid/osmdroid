package org.osmdroid.shape;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import net.iryndin.jdbf.core.DbfRecord;
import net.iryndin.jdbf.reader.DbfReader;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointPlainShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/osmdroid/osmdroid/issues/906
 * A simple utility class to convert a shape file into osmdroid overlays
 * created on 1/28/2018.
 *
 * @author Alex O'Ree
 * @since 6.1.0
 */

public class GridShapeConverter {

    /**
     *
     * @param map the MapView to which these overlays will be added.
     * @param file the shape file to be converted.
     * @param prefs  allows the client to relax the level of validation when reading a shape file.
     * @param fillColor fill color of polygon.
     * @param outLineColor out line color of polyOverlay.
     * @param width out line width of polyOverlay.
     * @param getBubbleText customize titles, snippets, and sub-descriptions of bubbles.
     *                      remember to handling bounding box if your shape file has both points and poly-overlays.
     *                      please also mind that getSnippet is called before getTitle, and before getSubDescription.
     * @return
     * @throws Exception
     */
    public static List<Overlay> convert(MapView map, File file, ValidationPreferences prefs,
                                        int fillColor, int outLineColor, int width, GetBubbleText getBubbleText) throws Exception {
        List<Overlay> folder=new ArrayList<>();

        FileInputStream is = null;
        FileInputStream dbfInputStream = null;
        DbfReader dbfReader = null;
        ShapeFileReader r = null;
        try {
            File dbase = new File(file.getParentFile(), file.getName().replace(".shp", ".dbf"));
            if (dbase.exists()) {
                dbfInputStream = new FileInputStream(dbase);
                dbfReader = new DbfReader(dbfInputStream);
            }
            is = new FileInputStream(file);
            r = new ShapeFileReader(is, prefs);


            AbstractShape s;
            while ((s = r.next()) != null) {
                DbfRecord metadata = null;
                if (dbfReader != null)
                    metadata = dbfReader.read();

                switch (s.getShapeType()) {
                    case POINT: {
                        PointShape aPoint = (PointShape) s;
                        Marker m = new Marker(map);
                        m.setPosition(fixOutOfRange(new GeoPoint(aPoint.getY(), aPoint.getX())));
                        if (metadata != null) {
                            metadata.setStringCharset(Charset.defaultCharset());
                            String metaString = metadata.toMap().toString();

                            m.setSnippet(getBubbleText.getSnippet(metaString, ""));
                            m.setTitle(getBubbleText.getTitle(metaString, ""));
                        }
                        folder.add(m);
                    }
                    break;

                    case POLYGON: {
                        PolygonShape aPolygon = (PolygonShape) s;


                        for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
                            Polygon polygon = new Polygon(map);

                            PointData[] points = aPolygon.getPointsOfPart(i);
                            List<GeoPoint> pts = new ArrayList<>();

                            for (PointData p : points) {
                                GeoPoint pt = fixOutOfRange(new GeoPoint(p.getY(), p.getX()));
                                pts.add(pt);
                            }
                            pts.add(pts.get(0));    //force the polygon to close

                            polygon.setPoints(pts); //points out of range should be fixed before this line.
                            final BoundingBox boundingBox = polygon.getBounds();
                            String boxString = boundingBox.toString();
                            if (metadata != null) {
                                metadata.setStringCharset(Charset.defaultCharset());
                                String metaString = metadata.toMap().toString();

                                polygon.setSnippet(getBubbleText.getSnippet(metaString, boxString));
                                polygon.setTitle(getBubbleText.getTitle(metaString, boxString));
                                polygon.setSubDescription(getBubbleText.getSubDescription(metaString, boxString));
                            }
                            polygon.getFillPaint().setColor(fillColor); //set fill color
                            Paint paint = polygon.getOutlinePaint();
                            paint.setStrokeWidth(width);
                            paint.setColor(outLineColor);
                            folder.add(polygon);
                        }
                    }

                    break;
                    case POLYLINE: {
                        PolylineShape polylineShape = (PolylineShape) s;
                        for (int i = 0; i < polylineShape.getNumberOfParts(); i++) {
                            Polyline line = new Polyline(map);

                            PointData[] points = polylineShape.getPointsOfPart(i);
                            List<GeoPoint> pts = new ArrayList<>();

                            for (PointData p : points) {
                                GeoPoint pt = fixOutOfRange(new GeoPoint(p.getY(), p.getX()));
                                pts.add(pt);
                            }

                            line.setPoints(pts);//points out of range should be fixed before this line.
                            final BoundingBox boundingBox = line.getBounds();
                            String boxString = boundingBox.toString();
                            if (metadata != null) {
                                metadata.setStringCharset(Charset.defaultCharset());
                                String metaString = metadata.toMap().toString();

                                line.setSnippet(getBubbleText.getSnippet(metaString, boxString));
                                line.setTitle(getBubbleText.getTitle(metaString, boxString));
                                line.setSubDescription(getBubbleText.getSubDescription(metaString, boxString));
                            }
                            Paint paint = line.getOutlinePaint();
                            paint.setStrokeWidth(width);
                            paint.setColor(outLineColor);
                            folder.add(line);
                        }
                    }
                    break;
                    case MULTIPOINT: {
                        MultiPointPlainShape aPoint = (MultiPointPlainShape) s;

                        PointData[] points = aPoint.getPoints();
                        for (PointData p : points) {
                            Marker m = new Marker(map);
                            m.setPosition(fixOutOfRange(new GeoPoint(p.getY(), p.getX())));
                            if (metadata != null) {
                                metadata.setStringCharset(Charset.defaultCharset());
                                String metaString = metadata.toMap().toString();

                                m.setSnippet(getBubbleText.getSnippet(metaString, ""));
                                m.setTitle(getBubbleText.getTitle(metaString, ""));
                            }
                            folder.add(m);
                        }
                    }
                    break;
                    default:
                        Log.w(IMapView.LOGTAG, s.getShapeType() + " was unhandled! " + s.getClass().getCanonicalName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
            try {
                dbfReader.close();
            } catch (Exception ex) {
            }
            try {
                dbfInputStream.close();
            } catch (Exception ex) {
            }
        }
        return folder;
    }

    private static GetBubbleText defaultBubbleText = new GetBubbleText() {
        @Override
        public String getTitle(String metadata, String boundingBox) {
            String snippet = getSnippet(metadata, boundingBox);
            if (snippet.length() > 100) {
                return snippet.substring(0, 96) + "...";
            }
            return snippet;
        }

        @Override
        public String getSubDescription(String metadata, String boundingBox) {
            return boundingBox;
        }

        @Override
        public String getSnippet(String metadata, String boundingBox) {
            return metadata;
        }
    };

    public static List<Overlay>  convert(MapView map, File file) throws Exception {
        ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return convert(map, file, pref);
    }

    public static List<Overlay> convert(MapView map, File file, ValidationPreferences pref) throws Exception {
        return convert(map, file, pref, Color.TRANSPARENT, Color.BLACK, 5, defaultBubbleText);
    }

    public static List<Overlay> convert(MapView map, File file, int fillColor, int outLineColor, int width) throws Exception {
        ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return convert(map, file, pref, fillColor, outLineColor, width, defaultBubbleText);
    }

    public static List<Overlay> convert(MapView map, File file, GetBubbleText getBubbleText) throws Exception {
        ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return convert(map, file, pref, Color.TRANSPARENT, Color.BLACK, 5, getBubbleText);
    }

    public static List<Overlay> convert(MapView map, File file, int fillColor, int outLineColor, int width, GetBubbleText getBubbleText) throws Exception {
        ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return convert(map, file, pref, fillColor, outLineColor, width, getBubbleText);
    }

    private static GeoPoint fixOutOfRange(GeoPoint point){
        if (point.getLatitude()>90.00)
            point.setLatitude(90.00);
        else if (point.getLatitude()<-90.00)
            point.setLatitude(-90.00);

        if (point.getLongitude()>180.00){
            double longitude = point.getLongitude() -360.00;
            point.setLongitude(longitude);
        } else if (point.getLongitude()<-180.00){
            double longitude = point.getLongitude() +360.00;
            point.setLongitude(longitude);
        }
        return point;
    }
}

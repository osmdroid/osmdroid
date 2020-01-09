package org.osmdroid.shape;

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

public class ShapeConverter {

    public static List<Overlay> convert(MapView map, File file, ValidationPreferences prefs) throws Exception {
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
                        m.setPosition(new GeoPoint(aPoint.getY(), aPoint.getX()));
                        if (metadata != null) {
                            metadata.setStringCharset(Charset.defaultCharset());
                            m.setSnippet(metadata.toMap().toString());
                            m.setTitle(getSensibleTitle(m.getSnippet()));

                        }
                        folder.add(m);
                    }
                    break;

                    case POLYGON: {
                        PolygonShape aPolygon = (PolygonShape) s;


                        for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
                            Polygon polygon = new Polygon(map);
                            if (metadata != null) {
                                metadata.setStringCharset(Charset.defaultCharset());

                                polygon.setSnippet(metadata.toMap().toString());
                                polygon.setTitle(getSensibleTitle(polygon.getSnippet()));
                            }

                            PointData[] points = aPolygon.getPointsOfPart(i);
                            List<GeoPoint> pts = new ArrayList<>();

                            for (PointData p : points) {
                                GeoPoint pt = new GeoPoint(p.getY(), p.getX());
                                pts.add(pt);
                            }
                            pts.add(pts.get(0));    //force the polygon to close

                            polygon.setPoints(pts);
                            final BoundingBox boundingBox = polygon.getBounds();
                            polygon.setSubDescription(boundingBox.toString());
                            folder.add(polygon);


                        }
                    }

                    break;
                    case POLYLINE: {
                        PolylineShape polylineShape = (PolylineShape) s;
                        for (int i = 0; i < polylineShape.getNumberOfParts(); i++) {
                            Polyline line = new Polyline(map);
                            if (metadata != null) {
                                metadata.setStringCharset(Charset.defaultCharset());
                                line.setSnippet(metadata.toMap().toString());
                                line.setTitle(getSensibleTitle(line.getSnippet()));
                            }
                            PointData[] points = polylineShape.getPointsOfPart(i);
                            List<GeoPoint> pts = new ArrayList<>();

                            for (PointData p : points) {
                                GeoPoint pt = new GeoPoint(p.getY(), p.getX());
                                pts.add(pt);
                            }
                            line.setPoints(pts);
                            folder.add(line);
                        }

                    }
                    break;
                    case MULTIPOINT: {
                        MultiPointPlainShape aPoint = (MultiPointPlainShape) s;

                        PointData[] points = aPoint.getPoints();
                        for (PointData p : points) {
                            Marker m = new Marker(map);
                            m.setPosition(new GeoPoint(p.getY(), p.getX()));
                            if (metadata != null) {
                                metadata.setStringCharset(Charset.defaultCharset());
                                m.setSnippet(metadata.toMap().toString());
                                m.setTitle(getSensibleTitle(m.getSnippet()));


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

    private static String getSensibleTitle(String snippet) {
        if (snippet.length() > 100) {
            return snippet.substring(0, 96) + "...";
        }
        return snippet;
    }

    public static List<Overlay>  convert(MapView map, File file) throws Exception {
        ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return convert(map, file, pref);
    }
}

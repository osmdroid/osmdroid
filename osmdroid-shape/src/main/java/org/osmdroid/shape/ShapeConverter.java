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
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * https://github.com/osmdroid/osmdroid/issues/906
 * A simple utility class to convert a shape file into osmdroid overlays
 * created on 1/28/2018.
 *
 * @author Alex O'Ree
 * @since 6.1.0
 */

public class ShapeConverter {

    /**
     *
     * @param map the MapView to which these overlays will be added.
     * @param file the shape file to be converted.
     * @param prefs  allows the client to relax the level of validation when reading a shape file.
     * @param fillColor fill color of polygon.
     * @param strokeColor out line color of polyOverlay.
     * @param strokeWidth out line width of polyOverlay.
     * @param shapeMetadataReader read metadata from file, and customize titles, snippets, and sub-descriptions of bubbles.
     * @return an arraylist of all overlays from the shapefile.
     * @throws Exception
     */
    public static List<Overlay> convert(MapView map, File file, ValidationPreferences prefs,
                                        int fillColor, int strokeColor, int strokeWidth, ShapeMetadataReader shapeMetadataReader) throws Exception {
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
                            shapeMetadataReader.read(metadata);
                        }
                        m.setSnippet(shapeMetadataReader.getSnippet());
                        m.setTitle(shapeMetadataReader.getTitle());
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
                            if (metadata != null) {
                                shapeMetadataReader.read(metadata);
                            }
                            polygon.setSnippet(shapeMetadataReader.getSnippet());
                            polygon.setTitle(shapeMetadataReader.getTitle());

                            shapeMetadataReader.setBoundingBox(polygon.getBounds());
                            polygon.setSubDescription(shapeMetadataReader.getSubDescription());

                            polygon.getFillPaint().setColor(fillColor); //set fill color
                            Paint paint = polygon.getOutlinePaint();
                            paint.setStrokeWidth(strokeWidth);
                            paint.setColor(strokeColor);
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
                                shapeMetadataReader.read(metadata);
                            }
                            line.setSnippet(shapeMetadataReader.getSnippet());
                            line.setTitle(shapeMetadataReader.getTitle());

                            shapeMetadataReader.setBoundingBox(line.getBounds());
                            line.setSubDescription(shapeMetadataReader.getSubDescription());

                            Paint paint = line.getOutlinePaint();
                            paint.setStrokeWidth(strokeWidth);
                            paint.setColor(strokeColor);
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
                                shapeMetadataReader.read(metadata);
                            }
                            m.setSnippet(shapeMetadataReader.getSnippet());
                            m.setTitle(shapeMetadataReader.getTitle());
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

    public static ValidationPreferences getDefaultValidationPreferences() {
        final ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return pref;
    }

    public static List<Overlay>  convert(MapView map, File file) throws Exception {
        return convert(map, file, getDefaultValidationPreferences());
    }

    public static List<Overlay> convert(MapView map, File file, ValidationPreferences pref) throws Exception {
        return convert(map, file, pref, Color.TRANSPARENT, Color.BLACK, 5, new DefaultShapeMetadataReader());
    }


    private static GeoPoint fixOutOfRange(GeoPoint point){
        if (point.getLatitude()>90.00)
            point.setLatitude(90.00);
        else if (point.getLatitude()<-90.00)
            point.setLatitude(-90.00);

        if (abs(point.getLongitude())>180.00){
            double longitude = point.getLongitude();
            double diff = longitude > 0 ? -360 : 360;
            while (abs(longitude) > 180)
                longitude += diff;
            point.setLongitude(longitude);
        }

        return point;
    }
}

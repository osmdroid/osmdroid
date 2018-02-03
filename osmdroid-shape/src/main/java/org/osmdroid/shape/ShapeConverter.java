package org.osmdroid.shape;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/osmdroid/osmdroid/issues/906
 * A simple utility class to convert a shape file into osmdroid overlays
 * created on 1/28/2018.
 *
 * @author Alex O'Ree
 */

public class ShapeConverter {

    public static FolderOverlay convert(MapView map, File file, ValidationPreferences prefs) throws Exception {
        FolderOverlay folder = new FolderOverlay();

        FileInputStream is = null;

        is = new FileInputStream(file);

        ShapeFileReader r = new ShapeFileReader(is, prefs);

        ShapeFileHeader h = r.getHeader();
        System.out.println("The shape type of this files is " + h.getShapeType());


        AbstractShape s;
        while ((s = r.next()) != null) {

            switch (s.getShapeType()) {
                case POINT:
                    PointShape aPoint = (PointShape) s;
                    Marker m = new Marker(map);
                    m.setPosition(new GeoPoint(aPoint.getY(), aPoint.getX()));
                    folder.add(m);

                    break;
                case MULTIPOINT_Z:

                    MultiPointZShape aMultiPointZ = (MultiPointZShape) s;
                    // Do something with the MultiPointZ shape...
                    break;
                case POLYGON:

                    PolygonShape aPolygon = (PolygonShape) s;

                    System.out.println("I read a Polygon with "
                        + aPolygon.getNumberOfParts() + " parts and "
                        + aPolygon.getNumberOfPoints() + " points");
                    Polygon polygon = new Polygon(map);
                    List<List<GeoPoint>> holes = new ArrayList<>();
                    for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {

                        PointData[] points = aPolygon.getPointsOfPart(i);
                        List<GeoPoint> pts = new ArrayList<>();

                        for (PointData p : points) {
                            GeoPoint pt = new GeoPoint(p.getY(), p.getX());
                            pts.add(pt);
                        }
                        if (i==0) {
                            polygon.setPoints(pts);
                        } else {
                            holes.add(pts);
                        }
                        System.out.println("- part " + i + " has " + points.length
                            + " points.");
                    }
                    polygon.setHoles(holes);
                    folder.add(polygon);

                    break;
                default:
                    System.out.println("Read other type of shape.");
            }
        }
        is.close();
        return folder;
    }
    public static FolderOverlay convert(MapView map, File file) throws Exception {
        ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return convert(map,file,pref);
    }
}

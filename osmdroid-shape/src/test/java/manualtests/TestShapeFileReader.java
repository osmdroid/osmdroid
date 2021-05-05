package manualtests;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointPlainShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestShapeFileReader {

    public static void main(final String[] args) throws IOException,
            InvalidShapeFileException {

        String filename;

//		 filename = "testdata/freeworld/10m-coastline/10m_coastline.shp";

//		 filename = "testdata/freefiles/pointfiles/prop_text.shp";
//		 filename = "testdata/freefiles/pointfiles/roadtext.shp";
//		 filename = "testdata/freefiles/pointfiles/sbuild.shp";

//		 filename = "testdata/freefiles/polygonfiles/lbuild.shp";
//		 filename = "testdata/freefiles/polygonfiles/property.shp";
//		 filename = "testdata/freefiles/polygonfiles/subdiv.shp";
//		 filename = "testdata/freefiles/polygonfiles/water.shp";

//		 filename = "testdata/freefiles/polylinefiles/roadcl.shp";
//		 filename = "testdata/freefiles/polylinefiles/roadeop.shp";

        filename = "src/test/resources/freefiles/multipoint/admin_font_point.shp";

        // filename =
        // "data/testdata/badfiles/multipoint-markedas-multipointz.shp";

        // filename ="";
        // filename ="";
        // filename ="";

        FileInputStream is = new FileInputStream(filename);

//		 ValidationPreferences prefs = new ValidationPreferences();
//		 prefs.setMaxNumberOfPointsPerShape(16650);
////		 prefs.setForceType(ShapeType.MULTIPOINT);
//		 ShapeFileReader r = new ShapeFileReader(is, prefs);

        ShapeFileReader r = new ShapeFileReader(is);

        ShapeFileHeader h = r.getHeader();
        display("header: " + h);

        Map<ShapeType, Integer> counters = new HashMap<ShapeType, Integer>();
        // for (ShapeType t : ShapeType.values()) {
        // counters.put(t, 0);
        // }

        int maxShapes = 1000000000;
        int total = 0;
        int totalPoints = 0;
        int totalParts = 0;
        AbstractShape s;
        while ((s = r.next()) != null && (total < maxShapes)) {
            if (s.getShapeType() == ShapeType.POLYLINE) {
                PolylineShape ps = (PolylineShape) s;
                if (ps.getNumberOfParts() > 1) {
                    display(total + ": " + s.getShapeType() + " (parts:"
                            + ps.getNumberOfParts() + ", points:"
                            + ps.getNumberOfPoints() + ")");
                    for (int i = 0; i < ps.getNumberOfParts(); i++) {
                        PointData[] points = ps.getPointsOfPart(i);
                        display("- part " + i + " (" + points.length
                                + " points)");
                    }
                }
                totalParts += ps.getNumberOfParts();
                totalPoints += ps.getNumberOfPoints();
            } else {
                display(s.getHeader().getRecordNumber()
                        + " ["
                        + s.getHeader().getContentLength()
                        + " w]: "
                        + s.getShapeType()
                        + " ("
                        + s.getShapeType().getId()
                        + ")"
                        + (s.getShapeType() == ShapeType.MULTIPOINT_Z ? " - "
                        + ((MultiPointPlainShape) s)
                        .getNumberOfPoints() + " points" : ""));
            }

            Integer typeCounter = counters.get(s.getShapeType());
            if (typeCounter == null) {
                counters.put(s.getShapeType(), 1);
            } else {
                counters.put(s.getShapeType(), 1 + typeCounter);
            }

            total++;
        }
        display("=========================");
        display("Total points: " + totalPoints + ", total parts: " + totalParts);
        display("");
        for (ShapeType t : counters.keySet()) {
            display("  " + counters.get(t) + " " + t);
        }
        display("  ==============");
        display("  " + total + " Total shapes");

        is.close();
    }

    private static void display(final String txt) {
        System.out.println(txt);
    }

}

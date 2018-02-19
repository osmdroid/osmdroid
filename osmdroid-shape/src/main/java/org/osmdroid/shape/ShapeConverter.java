package org.osmdroid.shape;

import android.util.Log;

//FIXME import net.iryndin.jdbf.core.DbfRecord;
//FIXME import net.iryndin.jdbf.reader.DbfReader;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

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
 */

public class ShapeConverter {

    public static FolderOverlay convert(MapView map, File file, ValidationPreferences prefs) throws Exception {
        FolderOverlay folder = new FolderOverlay();

        FileInputStream is = null;
        FileInputStream dbfInputStream = null;
        //FIXME DbfReader dbfReader = null;
        ShapeFileReader r = null;
        try {
            //FIXME File dbase = new File(file.getParentFile(), file.getName().replace(".shp", ".dbf"));
            //FIXME  if (dbase.exists()) {
            //FIXME     dbfInputStream = new FileInputStream(dbase);
                //FIXME dbfReader = new DbfReader(dbfInputStream);
            //FIXME }
            is = new FileInputStream(file);
            r = new ShapeFileReader(is, prefs);


            AbstractShape s;
            while ((s = r.next()) != null) {
                //FIXME  DbfRecord metadata = null;
                //FIXME  if (dbfReader != null)
                //FIXME      metadata = dbfReader.read();

                switch (s.getShapeType()) {
                    case POINT:
                        PointShape aPoint = (PointShape) s;
                        Marker m = new Marker(map);
                        m.setPosition(new GeoPoint(aPoint.getY(), aPoint.getX()));
                        //FIXME  if (metadata != null) {
                        //FIXME      metadata.setStringCharset(Charset.defaultCharset());
                        //FIXME      m.setSnippet(metadata.toMap().toString());

                        //FIXME }
                        folder.add(m);

                        break;

                    case POLYGON:

                        PolygonShape aPolygon = (PolygonShape) s;


                        for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
                            Polygon polygon = new Polygon(map);
                            //FIXME if (metadata != null) {
                            //FIXME     metadata.setStringCharset(Charset.defaultCharset());
                            //FIXME     polygon.setSnippet(metadata.toMap().toString());
                            //FIXME }

                            PointData[] points = aPolygon.getPointsOfPart(i);
                            List<GeoPoint> pts = new ArrayList<>();

                            for (PointData p : points) {
                                GeoPoint pt = new GeoPoint(p.getY(), p.getX());
                                pts.add(pt);
                            }
                            pts.add(pts.get(0));    //force the polygon to close

                            polygon.setPoints(pts);
                            BoundingBox boundingBox = BoundingBox.fromGeoPoints(polygon.getPoints());
                            polygon.setSubDescription(boundingBox.toString());

                            folder.add(polygon);


                        }


                        break;
                    default:
                        Log.w(IMapView.LOGTAG, s.getShapeType() + " was unhandled!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
            //FIXME try {
            //FIXME dbfReader.close();
            //FIXME } catch (Exception ex) {
            //FIXME }
            try {
                dbfInputStream.close();
            } catch (Exception ex) {
            }
        }
        return folder;
    }

    public static FolderOverlay convert(MapView map, File file) throws Exception {
        ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return convert(map, file, pref);
    }
}

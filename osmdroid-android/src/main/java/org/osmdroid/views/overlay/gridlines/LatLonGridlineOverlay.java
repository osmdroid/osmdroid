package org.osmdroid.views.overlay.gridlines;

import android.content.Context;
import android.graphics.Color;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Latitude/Longitude gridline overlay
 * <p>
 * It's not perfect and has issues with osmdroid's global wrap around (where north pole turns into the south pole).
 * There's probably room for more optimizations too, pull requests are welcome.
 *
 * @see LatLonGridlineOverlay2
 * @since 5.2+
 * Created by alex on 12/15/15.
 * @deprecated see {@link LatLonGridlineOverlay2}
 */
@Deprecated
public class LatLonGridlineOverlay {
    final static DecimalFormat df = new DecimalFormat("#.#####");
    public static int lineColor = Color.BLACK;
    public static int fontColor = Color.WHITE;
    public static short fontSizeDp = 24;
    public static int backgroundColor = Color.BLACK;
    public static float lineWidth = 1f;
    //extra debugging options
    public static boolean DEBUG = false;
    public static boolean DEBUG2 = false;

    //used to adjust the number of grid lines displayed on screen
    private static float multiplier = 1f;

    private static void applyMarkerAttributes(Marker m) {
        m.setTextLabelBackgroundColor(backgroundColor);
        m.setTextLabelFontSize(fontSizeDp);
        m.setTextLabelForegroundColor(fontColor);
    }

    public static FolderOverlay getLatLonGrid(Context ctx, MapView mapView) {
        BoundingBox box = mapView.getBoundingBox();
        int zoom = mapView.getZoomLevel();

        if (DEBUG) {
            System.out.println("######### getLatLonGrid ");
        }
        FolderOverlay gridlines = new FolderOverlay();
        if (zoom < 2) {
          /*  commented out for performance reasons
          the calculations due to wrap around screw things up because the bounds is more than 1 globe.
            for (int i = -90; i <= 90; i = i + 45) {
                Polyline p = new Polyline(ctx);
                p.setColor(mLineColor);
                p.setWidth(lineWidth);
                List<GeoPoint> pts = new ArrayList<GeoPoint>();

                GeoPoint x = new GeoPoint((double) i, 180);
                pts.add(x);
                x = new GeoPoint((double) i, 0);
                pts.add(x);
                x = new GeoPoint((double) i, -180);
                pts.add(x);

                p.setPoints(pts);
                gridlines.add(p);
            }

            //vertical lines

            for (int i = -180; i < 180; i = i + 45) {
                Polyline p = new Polyline(ctx);
                p.setColor(mLineColor);
                p.setWidth(lineWidth);
                List<GeoPoint> pts = new ArrayList<GeoPoint>();

                GeoPoint x = new GeoPoint((double) 90, (double) i);
                pts.add(x);
                x = new GeoPoint((double) -90, (double) i);
                pts.add(x);
                p.setPoints(pts);
                gridlines.add(p);
            }*/
        } else {
            double north = box.getLatNorth();
            double south = box.getLatSouth();
            double east = box.getLonEast();
            double west = box.getLonWest();

            double north_south_delta = 0d;

            if (north < south) {
                //we're vertically wrapping, abort.
                return gridlines;
            }
            if (DEBUG) {
                System.out.println("N " + north + " S " + south + ", " + north_south_delta);
            }

            boolean dateLineVisible = false;
            if (east < 0 && west > 0) {
                //we're at the date line
                dateLineVisible = true;
            }

            if (DEBUG) {
                System.out.println("delta " + north_south_delta);
            }
            //drop a line every this many degrees

            double incrementor = getIncrementor(zoom);

            //this should be starting south at the nearest logical value, 90,45, 15, 10, 5, 1, 0.5, 0.25, 0.125, based on the incrementer,
            //that way doesn't look like the lines are dancing everywhere
            //FIXME also draw 2x as wide as the screen, to support rotation?


            double[] startend = getStartEndPointsNS(north, south, zoom);
            double sn_start_point = startend[0];
            double sn_stop_point = startend[1];


            for (double i = sn_start_point; i <= sn_stop_point; i = i + incrementor) {
                Polyline p = new Polyline();
                p.getOutlinePaint().setStrokeWidth(lineWidth);
                p.getOutlinePaint().setColor(lineColor);
                List<GeoPoint> pts = new ArrayList<GeoPoint>();


                GeoPoint gx = new GeoPoint((double) i, east);
                pts.add(gx);
                gx = new GeoPoint((double) i, west);
                pts.add(gx);
                if (DEBUG) {
                    System.out.println("drawing NS " + (double) i + "," + east + " to " + (double) i + "," + west + ", zoom " + zoom);
                }

                p.setPoints(pts);

                gridlines.add(p);


                Marker m = new Marker(mapView);
                applyMarkerAttributes(m);
                final String title = df.format(i) + (i > 0 ? "N" : "S");
                m.setTitle(title);
                m.setTextIcon(title);
                m.setPosition(new GeoPoint(i, west + incrementor));
                gridlines.add(m);
            }

            double[] ew = getStartEndPointsWE(west, east, zoom);
            double we_startpoint = ew[1];
            double ws_stoppoint = ew[0];


            for (double i = we_startpoint; i <= ws_stoppoint; i = i + incrementor) {
                Polyline p = new Polyline();
                p.getOutlinePaint().setStrokeWidth(lineWidth);
                p.getOutlinePaint().setColor(lineColor);
                List<GeoPoint> pts = new ArrayList<GeoPoint>();
                GeoPoint gx = new GeoPoint((double) north, i);
                pts.add(gx);
                gx = new GeoPoint((double) south, i);
                pts.add(gx);
                p.setPoints(pts);

                if (DEBUG) {
                    System.err.println("drawing EW " + (double) south + "," + i + " to " + (double) north + "," + i + ", zoom " + zoom);
                }
                gridlines.add(p);


                Marker m = new Marker(mapView);
                applyMarkerAttributes(m);
                m.setRotation(-90f);
                final String title = df.format(i) + (i > 0 ? "E" : "W");
                m.setTitle(title);
                m.setTextIcon(title);
                m.setPosition(new GeoPoint(south + (incrementor), i));
                gridlines.add(m);
            }
            if (dateLineVisible) {

                if (DEBUG)
                    System.out.println("DATELINE zoom " + zoom + " " + we_startpoint + " " + ws_stoppoint);

                //special case to ensure that vertical lines are visible when the date line is visible.
                //in this case western point is very positive and eastern part is very negative
                for (double i = we_startpoint; i <= 180; i = i + incrementor) {
                    Polyline p = new Polyline();
                    p.getOutlinePaint().setStrokeWidth(lineWidth);
                    p.getOutlinePaint().setColor(lineColor);
                    List<GeoPoint> pts = new ArrayList<GeoPoint>();
                    GeoPoint gx = new GeoPoint((double) north, i);
                    pts.add(gx);
                    gx = new GeoPoint((double) south, i);
                    pts.add(gx);
                    p.setPoints(pts);

                    if (DEBUG2) {
                        System.out.println("DATELINE drawing NS" + (double) south + "," + i + " to " + (double) north + "," + i + ", zoom " + zoom);
                    }

                    gridlines.add(p);

                }
                for (double i = -180; i <= ws_stoppoint; i = i + incrementor) {
                    Polyline p = new Polyline();
                    p.getOutlinePaint().setStrokeWidth(lineWidth);
                    p.getOutlinePaint().setColor(lineColor);
                    List<GeoPoint> pts = new ArrayList<GeoPoint>();
                    GeoPoint gx = new GeoPoint((double) north, i);
                    pts.add(gx);
                    gx = new GeoPoint((double) south, i);
                    pts.add(gx);
                    p.setPoints(pts);

                    if (DEBUG2) {
                        System.out.println("DATELINE drawing EW" + (double) south + "," + i + " to " + (double) north + "," + i + ", zoom " + zoom);
                    }

                    gridlines.add(p);

                    Marker m = new Marker(mapView);
                    applyMarkerAttributes(m);
                    m.setRotation(-90f);
                    final String title = df.format(i) + (i > 0 ? "E" : "W");
                    m.setTitle(title);
                    m.setTextIcon(title);
                    m.setPosition(new GeoPoint(south + (incrementor), i));
                    gridlines.add(m);
                }


                for (double i = we_startpoint; i < 180; i = i + incrementor) {

                    Marker m = new Marker(mapView);

                    applyMarkerAttributes(m);
                    m.setRotation(-90f);
                    final String title = df.format(i) + (i > 0 ? "E" : "W");
                    m.setTitle(title);
                    m.setTextIcon(title);
                    m.setPosition(new GeoPoint(south + (incrementor), i));
                    gridlines.add(m);
                }
            }


        }
        return gridlines;
    }

    /**
     * gets the start and end points for a latitude line
     *
     * @param north
     * @param south
     * @param zoom
     * @return
     */
    private static double[] getStartEndPointsNS(double north, double south, int zoom) {
        //brute force when zoom is less than 10
        if (zoom < 10) {
            double sn_start_point = Math.floor(south);
            double incrementor = getIncrementor(zoom);


            double x = -90;
            while (x < sn_start_point)
                x = x + incrementor;
            sn_start_point = x;

            double sn_stop_point = Math.ceil(north);
            x = 90;
            while (x > sn_stop_point)
                x = x - incrementor;
            sn_stop_point = x;

            if (sn_stop_point > 90) {
                sn_stop_point = 90;
            }
            if (sn_start_point < -90) {
                sn_start_point = -90;
            }
            return new double[]{sn_start_point, sn_stop_point};
        } else {
            //hmm start at origin, add inc until we go too far, then back off, go to the next zoom level
            double sn_start_point = -90;
            if (south > 0) {
                sn_start_point = 0;
            }
            double sn_stop_point = 90;
            if (north < 0) {
                sn_stop_point = 0;
            }

            for (int xx = 2; xx <= zoom; xx++) {
                double inc = getIncrementor(xx);
                while (sn_start_point < south - inc) {
                    sn_start_point += inc;
                    if (DEBUG) {
                        System.out.println("south " + sn_start_point);
                    }
                }

                while (sn_stop_point > north + inc) {
                    sn_stop_point -= inc;
                    if (DEBUG) {
                        System.out.println("north " + sn_stop_point);
                    }
                }
            }

            return new double[]{sn_start_point, sn_stop_point};
        }
    }


    /**
     * gets the start and stop point for a longitude line
     *
     * @param west
     * @param east
     * @param zoom
     * @return
     */
    private static double[] getStartEndPointsWE(double west, double east, int zoom) {

        double incrementor = getIncrementor(zoom);
        //brute force when zoom is less than 10
        if (zoom < 10) {
            double we_startpoint = Math.floor(west);
            double x = 180;
            while (x > we_startpoint)
                x = x - incrementor;
            we_startpoint = x;
            //System.out.println("WS " + we_startpoint);
            double ws_stoppoint = Math.ceil(east);
            x = -180;
            while (x < ws_stoppoint)
                x = x + incrementor;
            if (we_startpoint < -180) {
                we_startpoint = -180;
            }
            if (ws_stoppoint > 180) {
                ws_stoppoint = 180;
            }
            return new double[]{ws_stoppoint, we_startpoint};
        } else {
            //hmm start at origin, add inc until we go too far, then back off, go to the next zoom level
            double west_start_point = -180;
            if (west > 0) {
                west_start_point = 0;
            }
            double easter_stop_point = 180;
            if (east < 0) {
                easter_stop_point = 0;
            }

            for (int xx = 2; xx <= zoom; xx++) {
                double inc = getIncrementor(xx);
                while (easter_stop_point > east + inc) {
                    easter_stop_point -= inc;
                    //System.out.println("east " + easter_stop_point);
                }

                while (west_start_point < west - inc) {
                    west_start_point += inc;
                    if (DEBUG) {
                        System.out.println("west " + west_start_point);
                    }
                }
            }
            if (DEBUG) {
                System.out.println("return EW set as " + west_start_point + " " + easter_stop_point);
            }
            return new double[]{easter_stop_point, west_start_point};
        }
    }

    /**
     * this gets the distance in decimal degrees in between each line on the grid based on zoom level.
     * i had had it at more logical increments (90, 45, 30, etc) but changing to factors of 90 helps visualization
     * (i.e. when you zoom in on a particular crosshair, the crosshair is still there at the next zoom level, for the most part
     *
     * @param zoom mapview's osm zoom level
     * @return a double indicating the distance in degrees/decimal from which to place the gridlines on screen
     */
    private static double getIncrementor(int zoom) {

        switch (zoom) {
            case 0:
            case 1:
                return 30d * multiplier;
            case 2:
                return 15d * multiplier;
            case 3:
                return 9d * multiplier;
            case 4:
                return 6d * multiplier;
            case 5:
                return 3d * multiplier;
            case 6:

                return 2d * multiplier;
            case 7:
                return 1d * multiplier;
            case 8:
                return 0.5d * multiplier;
            case 9:
                return 0.25d * multiplier;
          /*  default:
                return 0.1d * (1/(Math.pow(2, (10-zoom))));*/
            case 10:
                return 0.1d * multiplier;
            case 11:
                return 0.05d * multiplier;
            case 12:
                return 0.025d * multiplier;
            case 13:
                return 0.0125d * multiplier;
            case 14:
                return 0.00625d * multiplier;
            case 15:
                return 0.003125d * multiplier;
            case 16:
                return 0.0015625 * multiplier;
            case 17:
                return 0.00078125 * multiplier;
            case 18:
                return 0.000390625 * multiplier;
            case 19:
                return 0.0001953125 * multiplier;
            case 20:
                return 0.00009765625 * multiplier;
            case 21:
                return 0.000048828125 * multiplier;
            default:
                return 0.0000244140625 * multiplier;
        }
    }

    /**
     * resets the settings
     *
     * @since 5.6.3
     */
    public static void setDefaults() {

        lineColor = Color.BLACK;
        fontColor = Color.WHITE;
        backgroundColor = Color.BLACK;
        lineWidth = 1f;
        fontSizeDp = 32;
        DEBUG = false;
        DEBUG2 = false;
    }
}

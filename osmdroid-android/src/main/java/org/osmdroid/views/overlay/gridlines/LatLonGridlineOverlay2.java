package org.osmdroid.views.overlay.gridlines;

import android.graphics.Canvas;
import android.graphics.Color;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * created on 2/7/2018.
 * @since 6.0.0
 * @author Alex O'Ree
 */

public class LatLonGridlineOverlay2 extends Overlay {

    protected DecimalFormat mDecimalFormatter = new DecimalFormat("#.#####");
    protected int mLineColor = Color.BLACK;
    protected int mFontColor = Color.WHITE;
    protected short mFontSizeDp = 24;
    protected int mFontBackgroundColor = Color.BLACK;
    protected float mLineWidth = 1f;
    //used to adjust the number of grid lines displayed on screen
    protected float mMultiplier = 1f;
    protected FolderOverlay mLastOverlay = null;

    public LatLonGridlineOverlay2() {

        mLineColor = Color.BLACK;
        mFontColor = Color.WHITE;
        mFontBackgroundColor = Color.BLACK;
        mLineWidth = 1f;
        mFontSizeDp = 32;
    }

    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        if (shadow) return;
        if (!isEnabled()) return;

        if (mLastOverlay != null)
            mLastOverlay.onDetach(osmv);
        mLastOverlay = getLatLonGrid(osmv);
        mLastOverlay.draw(c, osmv, shadow);
    }

    public void setDecimalFormatter(DecimalFormat df) {
        this.mDecimalFormatter = df;
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
    }

    public void setFontColor(int fontColor) {
        this.mFontColor = fontColor;
    }

    public void setFontSizeDp(short fontSizeDp) {
        this.mFontSizeDp = fontSizeDp;
    }

    /**
     * background color for the text labels
     *
     * @param backgroundColor
     */
    public void setBackgroundColor(int backgroundColor) {
        this.mFontBackgroundColor = backgroundColor;
    }

    public void setLineWidth(float lineWidth) {
        this.mLineWidth = lineWidth;
    }

    /**
     * default is 1, larger number = more lines on screen. This comes at a performance penalty though
     *
     * @param multiplier
     */
    public void setMultiplier(float multiplier) {
        this.mMultiplier = multiplier;
    }

    protected void applyMarkerAttributes(Marker m) {
        m.setTextLabelBackgroundColor(mFontBackgroundColor);
        m.setTextLabelFontSize(mFontSizeDp);
        m.setTextLabelForegroundColor(mFontColor);
    }

    protected FolderOverlay getLatLonGrid(MapView mapView) {
        BoundingBox box = mapView.getBoundingBox();
        int zoom = mapView.getZoomLevel();

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

            if (north < south) {
                //we're vertically wrapping, abort.
                return gridlines;
            }


            boolean dateLineVisible = false;
            if (east < 0 && west > 0) {
                //we're at the date line
                dateLineVisible = true;
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
                p.setWidth(mLineWidth);
                p.setColor(mLineColor);
                List<GeoPoint> pts = new ArrayList<GeoPoint>();


                GeoPoint gx = new GeoPoint((double) i, east);
                pts.add(gx);
                gx = new GeoPoint((double) i, west);
                pts.add(gx);


                p.setPoints(pts);

                gridlines.add(p);


                Marker m = new Marker(mapView);
                applyMarkerAttributes(m);
                final String title = mDecimalFormatter.format(i) + (i > 0 ? "N" : "S");
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
                p.setWidth(mLineWidth);
                p.setColor(mLineColor);
                List<GeoPoint> pts = new ArrayList<GeoPoint>();
                GeoPoint gx = new GeoPoint((double) north, i);
                pts.add(gx);
                gx = new GeoPoint((double) south, i);
                pts.add(gx);
                p.setPoints(pts);


                gridlines.add(p);


                Marker m = new Marker(mapView);
                applyMarkerAttributes(m);
                m.setRotation(-90f);
                final String title = mDecimalFormatter.format(i) + (i > 0 ? "E" : "W");
                m.setTitle(title);
                m.setTextIcon(title);
                m.setPosition(new GeoPoint(south + (incrementor), i));
                gridlines.add(m);
            }
            if (dateLineVisible) {


                //special case to ensure that vertical lines are visible when the date line is visible.
                //in this case western point is very positive and eastern part is very negative
                for (double i = we_startpoint; i <= 180; i = i + incrementor) {
                    Polyline p = new Polyline();
                    p.setWidth(mLineWidth);
                    p.setColor(mLineColor);
                    List<GeoPoint> pts = new ArrayList<GeoPoint>();
                    GeoPoint gx = new GeoPoint((double) north, i);
                    pts.add(gx);
                    gx = new GeoPoint((double) south, i);
                    pts.add(gx);
                    p.setPoints(pts);

                    gridlines.add(p);

                }
                for (double i = -180; i <= ws_stoppoint; i = i + incrementor) {
                    Polyline p = new Polyline();
                    p.setWidth(mLineWidth);
                    p.setColor(mLineColor);
                    List<GeoPoint> pts = new ArrayList<GeoPoint>();
                    GeoPoint gx = new GeoPoint((double) north, i);
                    pts.add(gx);
                    gx = new GeoPoint((double) south, i);
                    pts.add(gx);
                    p.setPoints(pts);

                    gridlines.add(p);

                    Marker m = new Marker(mapView);
                    applyMarkerAttributes(m);
                    m.setRotation(-90f);
                    final String title = mDecimalFormatter.format(i) + (i > 0 ? "E" : "W");
                    m.setTitle(title);
                    m.setTextIcon(title);
                    m.setPosition(new GeoPoint(south + (incrementor), i));
                    gridlines.add(m);
                }


                for (double i = we_startpoint; i < 180; i = i + incrementor) {

                    Marker m = new Marker(mapView);

                    applyMarkerAttributes(m);
                    m.setRotation(-90f);
                    final String title = mDecimalFormatter.format(i) + (i > 0 ? "E" : "W");
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
    protected double[] getStartEndPointsNS(double north, double south, int zoom) {
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
                }

                while (sn_stop_point > north + inc) {
                    sn_stop_point -= inc;
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
    protected double[] getStartEndPointsWE(double west, double east, int zoom) {

        double incrementor = getIncrementor(zoom);
        //brute force when zoom is less than 10
        if (zoom < 10) {
            double we_startpoint = Math.floor(west);
            double x = 180;
            while (x > we_startpoint)
                x = x - incrementor;
            we_startpoint = x;
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

                }
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
    protected double getIncrementor(int zoom) {

        switch (zoom) {
            case 0:
            case 1:
                return 30d * mMultiplier;
            case 2:
                return 15d * mMultiplier;
            case 3:
                return 9d * mMultiplier;
            case 4:
                return 6d * mMultiplier;
            case 5:
                return 3d * mMultiplier;
            case 6:

                return 2d * mMultiplier;
            case 7:
                return 1d * mMultiplier;
            case 8:
                return 0.5d * mMultiplier;
            case 9:
                return 0.25d * mMultiplier;
          /*  default:
                return 0.1d * (1/(Math.pow(2, (10-zoom))));*/
            case 10:
                return 0.1d * mMultiplier;
            case 11:
                return 0.05d * mMultiplier;
            case 12:
                return 0.025d * mMultiplier;
            case 13:
                return 0.0125d * mMultiplier;
            case 14:
                return 0.00625d * mMultiplier;
            case 15:
                return 0.003125d * mMultiplier;
            case 16:
                return 0.0015625 * mMultiplier;
            case 17:
                return 0.00078125 * mMultiplier;
            case 18:
                return 0.000390625 * mMultiplier;
            case 19:
                return 0.0001953125 * mMultiplier;
            case 20:
                return 0.00009765625 * mMultiplier;
            case 21:
                return 0.000048828125 * mMultiplier;
            default:
                return 0.0000244140625 * mMultiplier;
        }
    }

}

package org.osmdroid.samplefragments.milstd2525;

import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointReducer;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.graphics2d.Point2D;
import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.MilStdSymbol;
import armyc2.c2sd.renderer.utilities.ShapeInfo;
import sec.web.render.SECWebRenderer;

/**
 * This overlay does a few things that are unique to milstd graphics
 * <p>
 * The GeoPoints provided are the symbol's control points. These control
 * points are then converted into the graphic and then added to the map using a
 * folder overlay + markers, polylines and polygons.
 * <p>
 * <p>
 * created on 1/30/2018.
 *
 * @author Alex O'Ree
 */

public class MilStdMultipointOverlay extends Overlay {
    SimpleSymbol symbol;
    ArrayList<GeoPoint> inputGeoPoints;
    private float mCurrentMapRotation = 0;
    private double mCurrentMapZoom = 0d;
    private IGeoPoint mCurrentCenter = null;
    protected FolderOverlay lastOverlay = null;

    public MilStdMultipointOverlay(SimpleSymbol symbol, ArrayList<GeoPoint> inputs) {
        this.symbol = symbol;
        this.inputGeoPoints = inputs;
    }

    @Override
    public void draw(Canvas c, MapView map, boolean shadow) {
        if (shadow)
            return;
        //prevent looping forever for rendering
        //get the bounds,zoom, and rotation. if it's different, proceed
        boolean render = false;
        if (mCurrentMapRotation != map.getMapOrientation())
            render = true;
        if (mCurrentCenter == null)
            render = true;
        else if (!mCurrentCenter.equals(map.getMapCenter()))
            render = true;
        if (mCurrentMapZoom != map.getZoomLevelDouble())
            render = true;

        if (!render) return;

        //ok we are going to make a new symbol

        DisplayMetrics dm = map.getContext().getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;

        //remove the last plotted configuration


        //Log.d(IMapView.LOGTAG, "point size before " + inputGeoPoints.size());
        //get the screen bounds
        BoundingBox boundingBox = map.getBoundingBox();
        final double latSpanDegrees = boundingBox.getLatitudeSpan();
        //get the degree difference, divide by dpi
        double tolerance = latSpanDegrees / densityDpi;
        //each degree on screen is represented by this many dip

        StringBuilder controlPts = new StringBuilder();
        //run the douglas pucker algorithm to reduce the points for performance reasons
        ArrayList<GeoPoint> inputGeoPoints = PointReducer.reduceWithTolerance(
                this.inputGeoPoints,
                tolerance
        );
        //Log.d(IMapView.LOGTAG, "point size after " + inputGeoPoints.size());

        for (GeoPoint iGeoPoint : inputGeoPoints) {
            controlPts.append(iGeoPoint.getLongitude()).append(",").append(iGeoPoint.getLatitude()).append(" ");
        }


        String id = "id";   //TODO
        String name = symbol.getSymbolCode();
        String description = symbol.getDescription();
        String symbolCode = symbol.getSymbolCode();

        String controlPoints = controlPts.toString();
        String altitudeMode = "absolute";
        //the ground scale
        double scale = TileSystem.GroundResolution(map.getMapCenter().getLatitude(), map.getZoomLevelDouble());
        //"lowerLeftX,lowerLeftY,upperRightX,upperRightY."
        String bbox = boundingBox.getLonWest() + "," +
                boundingBox.getLatSouth() + "," +
                boundingBox.getLonEast() + "," +
                boundingBox.getLatNorth();


        SparseArray<String> modifiers = symbol.getModifiers();

        if (symbolCode.charAt(0) == 'G') {
            //set the echleon to something meaningful
            symbolCode = symbolCode.substring(0, 10) + "-F" + symbolCode.substring(12);
            symbolCode = symbolCode.substring(0, 3) + "P" + symbolCode.substring(4);
        }
        //TODO country code is index 13-14
        //TODO X is 15

        SparseArray<String> attributes = new SparseArray<String>();
        //TODO user defined drawing overides
        // attributes.put(MilStdAttributes.LineColor, "ffff0000");

        int symStd = 0;

        //produce the symbol
        MilStdSymbol flot = SECWebRenderer.RenderMultiPointAsMilStdSymbol(id, name, description, symbolCode, controlPoints, altitudeMode, scale, bbox, modifiers, attributes, symStd);

        //convert the symbol into osmdroid's data structures
        if (lastOverlay != null) {
            lastOverlay.onDetach(map);
        }
        lastOverlay = new FolderOverlay();
        for (int i = 0; i < flot.getSymbolShapes().size(); i++) {
            ShapeInfo info = flot.getSymbolShapes().get(i);

            if (info != null) {
                if (info.getFillColor() != null) {
                    ArrayList<ArrayList<Point2D>> polylines = info.getPolylines();
                    if (polylines != null)
                        for (ArrayList<Point2D> list : polylines) {
                            Polygon line = new Polygon(map);
                            List<GeoPoint> geoPoints = new ArrayList<>();
                            for (Point2D p : list) {
                                geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                            }
                            line.setPoints(geoPoints);
                            if (info.getLineColor() != null)
                                line.getOutlinePaint().setColor(info.getLineColor().toInt());
                            if (info.getFillColor() != null)
                                line.getFillPaint().setColor(info.getFillColor().toInt());
                            line.getOutlinePaint().setStrokeWidth(flot.getLineWidth());
                            line.setId(id);
                            line.setTitle(name);
                            line.setSubDescription(description);
                            line.setSnippet(symbolCode);
                            line.setVisible(true);
                            lastOverlay.getItems().add(line);

                        }
                } else {

                    ArrayList<ArrayList<Point2D>> polylines = info.getPolylines();
                    if (polylines != null)
                        for (ArrayList<Point2D> list : polylines) {
                            Polyline line = new Polyline(map);
                            List<GeoPoint> geoPoints = new ArrayList<>();
                            for (Point2D p : list) {
                                geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                            }
                            line.setPoints(geoPoints);
                            if (info.getLineColor() != null)
                                line.getOutlinePaint().setColor(info.getLineColor().toInt());
                            line.setGeodesic(true);
                            line.setId(id);
                            line.setTitle(name);
                            line.getOutlinePaint().setStrokeWidth(flot.getLineWidth());
                            line.setSubDescription(description);
                            line.setSnippet(symbolCode);
                            line.setVisible(true);
                            lastOverlay.getItems().add(line);

                        }
                }
            }
        }
        for (int i = 0; i < flot.getModifierShapes().size(); i++) {
            ShapeInfo info = flot.getModifierShapes().get(i);
            if (info != null) {

                if (info.getPolylines() != null) {
                    ArrayList<ArrayList<Point2D>> polylines = info.getPolylines();
                    if (info.getFillColor() != null) {
                        for (ArrayList<Point2D> list : polylines) {
                            Polygon line = new Polygon(map);
                            List<GeoPoint> geoPoints = new ArrayList<>();
                            for (Point2D p : list) {
                                geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                            }
                            line.setPoints(geoPoints);
                            if (info.getLineColor() != null)
                                line.getOutlinePaint().setColor(info.getLineColor().toInt());
                            if (info.getFillColor() != null)
                                line.getFillPaint().setColor(info.getFillColor().toInt());
                            line.setId(id);
                            line.setTitle(name);
                            line.getOutlinePaint().setStrokeWidth(flot.getLineWidth());
                            line.setSubDescription(description);
                            line.setSnippet(symbolCode);
                            line.setVisible(true);
                            lastOverlay.getItems().add(line);
                        }
                    } else {
                        //it's a line
                        for (ArrayList<Point2D> list : polylines) {
                            Polyline line = new Polyline(map);
                            List<GeoPoint> geoPoints = new ArrayList<>();
                            for (Point2D p : list) {
                                geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                            }
                            line.setPoints(geoPoints);
                            line.getOutlinePaint().setStrokeWidth(flot.getLineWidth());
                            if (info.getLineColor() != null)
                                line.getOutlinePaint().setColor(info.getLineColor().toInt());
                            line.setGeodesic(true);
                            line.setVisible(true);
                            lastOverlay.getItems().add(line);

                        }
                    }
                } else {
                    //not a line or a polygon
                    Marker m = new Marker(map);
                    m.setTextLabelBackgroundColor(Color.WHITE.toInt());
                    m.setTextLabelFontSize(14);
                    m.setTextLabelForegroundColor(Color.BLACK.toInt());
                    m.setTitle(info.getModifierString());
                    m.setRotation((float) info.getModifierStringAngle());
                    m.setTextIcon(info.getModifierString());
                    m.setPosition(new GeoPoint(info.getModifierStringPosition().getY(), info.getModifierStringPosition().getX()));
                    lastOverlay.getItems().add(m);
                }
            }
        }

        lastOverlay.draw(c, map, false);
    }
}

package org.osmdroid.samplefragments.milstd2525;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.graphics2d.Point2D;
import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.MilStdSymbol;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.ShapeInfo;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import sec.web.render.SECWebRenderer;

/**
 * A very simple borrowed from Android's "Finger Page" example, modified to generate polylines that
 * are geopoint bound after finger up.
 * created on 1/13/2017.
 *
 * @author Alex O'Ree
 */

public class MilStdCustomPaintingSurface extends View {
    private SimpleSymbol symbol;

    public void setSymbol(SimpleSymbol symbol) {
        this.symbol = symbol;
    }

    public enum Mode {
        Polyline,
        Polygon,
        PolygonHole
    }

    protected boolean withArrows = false;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private MapView map;
    private List<Point> pts = new ArrayList<>();
    private Paint mPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    transient FolderOverlay lastOverlay = null;


    public MilStdCustomPaintingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPath = new Path();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        mCanvas = new Canvas(mBitmap);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        canvas.drawPath(mPath, mPaint);
    }

    public void init(MapView mapView) {
        map = mapView;
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
        if (map != null) {
            Projection projection = map.getProjection();

            //TODO run the douglas pucker algorithm to reduce the points for performance reasons
            if (symbol != null && symbol.getMinPoints() <= pts.size()) {

                //ok we are going to make a new symbol
                map.getOverlayManager().remove(lastOverlay);
                StringBuilder controlPts = new StringBuilder();
                final Point unrotatedPoint = new Point();
                for (int i = 0; i < pts.size(); i++) {
                    projection.unrotateAndScalePoint(pts.get(i).x, pts.get(i).y, unrotatedPoint);
                    GeoPoint iGeoPoint = (GeoPoint) projection.fromPixels(unrotatedPoint.x, unrotatedPoint.y);
                    controlPts.append(iGeoPoint.getLongitude()).append(",").append(iGeoPoint.getLatitude()).append(" ");
                }
                String id = "id";
                String name = symbol.getSymbolCode();
                String description = symbol.getDescription();
                String symbolCode = symbol.getSymbolCode();

                String controlPoints = controlPts.toString();
                String altitudeMode = "absolute";
                //FIXME have to get the ground scale
                double scale = TileSystem.GroundResolution(map.getMapCenter().getLatitude(), map.getZoomLevelDouble());
                //"lowerLeftX,lowerLeftY,upperRightX,upperRightY."
                BoundingBox boundingBox = map.getBoundingBox();
                String bbox = boundingBox.getLonWest() + "," +
                    boundingBox.getLatSouth() + "," +
                    boundingBox.getLonEast() + "," +
                    boundingBox.getLatNorth();


                SparseArray<String> modifiers = new SparseArray<String>();
                SymbolDef symbolDefinition = SymbolDefTable.getInstance().getSymbolDef(symbol.getBasicSymbolId(), RendererSettings.getInstance().getSymbologyStandard());

                if (symbolCode.charAt(0) == 'G') {
                    //set the echloen to something meaningful
                    symbolCode = symbolCode.substring(0, 10) + "-F" + symbolCode.substring(12);
                    symbolCode = symbolCode.substring(0, 3) + "P" + symbolCode.substring(4);
                }
                //TODO country code is index 13-14
                //TODO X is 15

                modifiers.put(ModifiersTG.N_HOSTILE, "BL");
                modifiers.put(ModifiersTG.T1_UNIQUE_DESIGNATION_2, "T1");
                modifiers.put(ModifiersTG.W_DTG_1, "DTG1");
                modifiers.put(ModifiersTG.W1_DTG_2, "DTG2");
                modifiers.put(ModifiersTG.LENGTH, "100");
                modifiers.put(ModifiersTG.RADIUS, "100");
                modifiers.put(ModifiersTG.T_UNIQUE_DESIGNATION_1, "T");
                modifiers.put(ModifiersTG.Q_DIRECTION_OF_MOVEMENT, "45");
                modifiers.put(ModifiersTG.C_QUANTITY, "3");
                modifiers.put(ModifiersTG.AM_DISTANCE, "100");
                modifiers.put(ModifiersTG.X_ALTITUDE_DEPTH, "100");
                modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1, "H");
                modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2, "H1");
                modifiers.put(ModifiersTG.AN_AZIMUTH, "15");
                modifiers.put(ModifiersTG.H2_ADDITIONAL_INFO_3, "H2");
                //TODO user defined modifiers

                SparseArray<String> attributes = new SparseArray<String>();
                //TODO user defined drawing overides
                // attributes.put(MilStdAttributes.LineColor, "ffff0000");

                int symStd = 0;


                MilStdSymbol flot = SECWebRenderer.RenderMultiPointAsMilStdSymbol(id, name, description, symbolCode, controlPoints, altitudeMode, scale, bbox, modifiers, attributes, symStd);
                lastOverlay = new FolderOverlay();

                //ArrayList<Point2D> milStdSymbol.getSymbolShapes.get(index).getPolylines()
                //* ShapeInfo = milStdSymbol.getModifierShapes.get(index).

                for (int i = 0; i < flot.getSymbolShapes().size(); i++) {
                    ShapeInfo info = flot.getSymbolShapes().get(i);

                    if (info != null) {
                        if (info.getFillColor() != null) {
                            ArrayList<ArrayList<Point2D>> polylines = info.getPolylines();
                            if (polylines != null)
                                for (ArrayList<Point2D> list : polylines) {
                                    Polygon line = new Polygon();
                                    List<GeoPoint> geoPoints = new ArrayList<>();
                                    for (Point2D p : list) {
                                        geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                                    }
                                    line.setPoints(geoPoints);
                                    if (info.getLineColor() != null)
                                        line.setStrokeColor(info.getLineColor().toInt());
                                    if (info.getFillColor() != null)
                                        line.setFillColor(info.getFillColor().toInt());
                                    line.setStrokeWidth(flot.getLineWidth());
                                    line.setId(id);
                                    line.setTitle(name);
                                    line.setSubDescription(description);
                                    line.setSnippet(symbolCode);
                                    line.setVisible(true);
                                    lastOverlay.getItems().add(line);

                                }


                            //TODO polygon?
                        } else {

                            ArrayList<ArrayList<Point2D>> polylines = info.getPolylines();
                            if (polylines != null)
                                for (ArrayList<Point2D> list : polylines) {
                                    Polyline line = new Polyline();
                                    List<GeoPoint> geoPoints = new ArrayList<>();
                                    for (Point2D p : list) {
                                        geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                                    }
                                    line.setPoints(geoPoints);
                                    if (info.getLineColor() != null)
                                        line.setColor(info.getLineColor().toInt());
                                    line.setGeodesic(true);
                                    line.setId(id);
                                    line.setTitle(name);
                                    line.setWidth(flot.getLineWidth());
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
                                    Polygon line = new Polygon();
                                    List<GeoPoint> geoPoints = new ArrayList<>();
                                    for (Point2D p : list) {
                                        geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                                    }
                                    line.setPoints(geoPoints);
                                    if (info.getLineColor() != null)
                                        line.setStrokeColor(info.getLineColor().toInt());
                                    if (info.getFillColor() != null)
                                        line.setFillColor(info.getFillColor().toInt());
                                    line.setId(id);
                                    line.setTitle(name);
                                    line.setStrokeWidth(flot.getLineWidth());
                                    line.setSubDescription(description);
                                    line.setSnippet(symbolCode);
                                    line.setVisible(true);
                                    lastOverlay.getItems().add(line);
                                }
                            } else {
                                //it's a line
                                for (ArrayList<Point2D> list : polylines) {
                                    Polyline line = new Polyline();
                                    List<GeoPoint> geoPoints = new ArrayList<>();
                                    for (Point2D p : list) {
                                        geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                                    }
                                    line.setPoints(geoPoints);
                                    line.setWidth(flot.getLineWidth());
                                    if (info.getLineColor() != null)
                                        line.setColor(info.getLineColor().toInt());
                                    line.setGeodesic(true);
                                    line.setVisible(true);
                                    lastOverlay.getItems().add(line);

                                }
                            }
                        } else {
                            //not a line or a polygon

                            Marker.ENABLE_TEXT_LABELS_WHEN_NO_IMAGE=true;
                            Marker m = new Marker(map);
                            m.setTextLabelBackgroundColor(Color.WHITE.toInt());
                            m.setTextLabelFontSize(14);
                            m.setTextLabelForegroundColor(Color.BLACK.toInt());
                            m.setTitle(info.getModifierString());
                            m.setRotation((float)info.getModifierStringAngle());
                            m.setIcon(null);
                            m.setPosition(new GeoPoint(info.getModifierStringPosition().getY(),info.getModifierStringPosition().getX()));
                            lastOverlay.getItems().add(m);
                        }
                    }
                }

                map.getOverlayManager().add(lastOverlay);


                map.invalidate();
            }
        }

        pts.clear();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        pts.add(new Point((int) x, (int) y));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

}

package org.osmdroid.samplefragments.milstd2525;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.StringBuilderPrinter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.graphics2d.Point2D;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.MilStdSymbol;
import armyc2.c2sd.renderer.utilities.ShapeInfo;
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

    transient Polygon lastPolygon = null;


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
                double scale = 5869879.2;
                //"lowerLeftX,lowerLeftY,upperRightX,upperRightY."
                BoundingBox boundingBox = map.getBoundingBox();
                String bbox = boundingBox.getLonWest() + "," +
                    boundingBox.getLatSouth()+","+
                    boundingBox.getLonEast()+"," +
                    boundingBox.getLatNorth();


                SparseArray<String> modifiers = new SparseArray<String>();
                SparseArray<String> attributes = new SparseArray<String>();
                attributes.put(MilStdAttributes.LineColor, "ffff0000");

                int symStd = 0;

                MilStdSymbol flot = SECWebRenderer.RenderMultiPointAsMilStdSymbol(id, name, description, symbolCode, controlPoints, altitudeMode, scale, bbox, modifiers, attributes, symStd);
                FolderOverlay folder = new FolderOverlay();

                //FIXME this part needs a lot of work...
                for (ShapeInfo info: flot.getSymbolShapes()){
                    ArrayList<ArrayList<Point2D>> polylines = info.getPolylines();
                    for (ArrayList<Point2D> list: polylines) {
                        Polyline line = new Polyline();
                        List<GeoPoint> geoPoints = new ArrayList<>();
                        for (Point2D p: list) {
                            geoPoints.add(new GeoPoint(p.getY(), p.getX()));
                        }
                        line.setPoints(geoPoints);
                        line.setColor(info.getFillColor().toInt());
                        folder.getItems().add(line);

                    }
                }

                map.getOverlayManager().add(folder);
                lastPolygon = null;


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

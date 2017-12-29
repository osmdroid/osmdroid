package org.osmdroid.samplefragments.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MilestoneBitmapDisplayer;
import org.osmdroid.views.overlay.MilestoneManager;
import org.osmdroid.views.overlay.MilestonePathDisplayer;
import org.osmdroid.views.overlay.MilestonePixelDistanceLister;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * A very simple borrowed from Android's "Finger Page" example, modified to generate polylines that
 * are geopoint bound after finger up.
 * created on 1/13/2017.
 *
 * @author Alex O'Ree
 */

public class CustomPaintingSurface extends View {
    public void setMode(Mode mode) {
        this.drawingMode=mode;
    }
    private Mode drawingMode=Mode.Polyline;

    public enum Mode{
        Polyline,
        Polygon,
        PolygonHole
    }
    protected boolean withArrows=false;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private MapView map;
    private List<Point> pts = new ArrayList<>();
    private Paint mPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    transient Polygon lastPolygon=null;


    public CustomPaintingSurface(Context context, AttributeSet attrs) {
        super(context,attrs);
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
        map=mapView;
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
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
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
        if (map!=null){
            Projection projection = map.getProjection();
            List<GeoPoint> geoPoints = new ArrayList<>();
            for (int i=0; i < pts.size(); i++) {
                GeoPoint iGeoPoint = (GeoPoint) projection.fromPixels(pts.get(i).x, pts.get(i).y);
                geoPoints.add(iGeoPoint);
            }
            //TODO run the douglas pucker algorithm to reduce the points for performance reasons
            if (geoPoints.size() > 2) {
                //only plot a line unless there's at least one item
                switch (drawingMode) {
                    case Polyline:
                        final int color = Color.BLACK;
                        Polyline line = new Polyline();
                        line.setPoints(geoPoints);
                        line.setOnClickListener(new Polyline.OnClickListener() {
                            @Override
                            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                                return false;
                            }
                        });

                        line.setColor(color);

                        if (withArrows) {
                            final Paint arrowPaint = new Paint();
                            arrowPaint.setColor(color);
                            arrowPaint.setStrokeWidth(10.0f);
                            arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                            arrowPaint.setAntiAlias(true);
                            final Path arrowPath = new Path(); // a simple arrow towards the right
                            arrowPath.moveTo(- 10, - 10);
                            arrowPath.lineTo(10, 0);
                            arrowPath.lineTo(- 10, 10);
                            arrowPath.close();
                            final List<MilestoneManager> managers = new ArrayList<>();
                            managers.add(new MilestoneManager(
                                    new MilestonePixelDistanceLister(50, 50),
                                    new MilestonePathDisplayer(0, true, arrowPath, arrowPaint)
                            ));
                            line.setMilestoneManagers(managers);
                        }
                        map.getOverlayManager().add(line);
                        lastPolygon=null;
                        break;
                    case Polygon:
                        Polygon polygon = new Polygon();
                        polygon.setFillColor(Color.argb(75, 255,0,0));
                        polygon.setPoints(geoPoints);
                        polygon.setTitle("A sample polygon");
                        if (withArrows) {
                            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), org.osmdroid.library.R.drawable.direction_arrow);
                            final List<MilestoneManager> managers = new ArrayList<>();
                            managers.add(new MilestoneManager(
                                    new MilestonePixelDistanceLister(20, 200),
                                    new MilestoneBitmapDisplayer(90, true, bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2)
                            ));
                            polygon.setMilestoneManagers(managers);
                        }
                        map.getOverlayManager().add(polygon);
                        lastPolygon=polygon;
                        break;
                    case PolygonHole:
                        if (lastPolygon!=null) {
                            List<List<GeoPoint>> holes = new ArrayList<>();
                            holes.add(geoPoints);
                            lastPolygon.setHoles(holes);
                        }
                        break;
                }

                map.invalidate();
            }
        }

        pts.clear();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        pts.add(new Point((int)x,(int)y));
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

package org.osmdroid.views.overlay.simplefastpoint;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.Arrays;

/**
 * Overlay to draw a layer of clickable simple points, optimized for rendering speed. Nice
 * performance up to 100k points. Does not support styling each point individually, the style
 * applies to all points. Does not support rotated maps.
 * There are three rendering algorithms:
 * NO_OPTIMIZATION: all points all drawn in every draw event
 * MEDIUM_OPTIMIZATION: not recommended for >10k points. Recalculates the grid index on each draw
 *     event and only draws one point per grid cell.
 * MAXIMUM_OPTIMIZATION: for >10k points, only recalculates the grid on touch up, hence much faster.
 *
 * TODO: support for rotated maps!
 * TODO: a quadtree index would improve rendering speed!
 * Created by Miguel Porto on 25-10-2016.
 */
public class SimpleFastPointOverlay extends Overlay {
    private final SimpleFastPointOverlayOptions mStyle;
    private final PointAdapter mPointList;
    private final BoundingBox mBoundingBox;
    private Integer mSelectedPoint;
    private OnClickListener clickListener;
    // grid index for optimizing drawing k's of points
    private LabelledPoint grid[][];
    private boolean gridBool[][];
    private int gridWid, gridHei, viewWid, viewHei;
    private float startX, startY, curX, curY, offsetX, offsetY;
    private int prevNumPointers, numLabels;
    private BoundingBox prevBoundingBox = new BoundingBox(0, 0, 0, 0);

    public class LabelledPoint extends Point {
        private String mlabel;

        public LabelledPoint(Point point, String label) {
            super(point);
            this.mlabel = label;
        }
    }

    public interface PointAdapter extends Iterable<IGeoPoint> {
        int size();
        IGeoPoint get(int i);

        /**
         * Whether this adapter has labels
         * @return
         */
        boolean isLabelled();
    }

    public interface OnClickListener {
        void onClick(PointAdapter points, Integer point);
    }

    public SimpleFastPointOverlay(PointAdapter pointList, SimpleFastPointOverlayOptions style) {
        mStyle = style;
        mPointList = pointList;

        Double east = null, west = null, north = null, south = null;
        for(IGeoPoint p : mPointList) {
            if(p == null) continue;
            if(east == null || p.getLongitude() > east) east = p.getLongitude();
            if(west == null || p.getLongitude() < west) west = p.getLongitude();
            if(north == null || p.getLatitude() > north) north = p.getLatitude();
            if(south == null || p.getLatitude() < south) south = p.getLatitude();
        }

        if(east != null)
            mBoundingBox = new BoundingBox(north, east, south, west);
        else
            mBoundingBox = null;
    }

    public SimpleFastPointOverlay(PointAdapter pointList) {
        this(pointList, SimpleFastPointOverlayOptions.getDefaultStyle());
    }

    private void updateGrid(MapView mapView) {
        viewWid = mapView.getWidth();
        viewHei = mapView.getHeight();
        gridWid = (int) Math.floor((float) viewWid / mStyle.mCellSize) + 1;
        gridHei = (int) Math.floor((float) viewHei / mStyle.mCellSize) + 1;
        if(mStyle.mAlgorithm ==
                SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
            grid = new LabelledPoint[gridWid][gridHei];
        else
            gridBool = new boolean[gridWid][gridHei];

        // TODO the measures on first draw are not the final values.
        // MapView should propagate onLayout to overlays
    }

    /**
     * Re-calculates which points to be shown and their coordinates
     * TODO: this could be further optimized for speed, for example, the grid could be calculated
     * in geographic coordinates, instead of projected. N.B. the speed bottleneck is pj.toPixels()
     * @param pMapView
     */
    private void computeGrid(final MapView pMapView) {
        // TODO: 15-11-2016 should take map orientation into account in the BBox!
        BoundingBox viewBBox = pMapView.getBoundingBox();

        // do not compute grid if BBox is the same
        if(viewBBox.getLatNorth() != prevBoundingBox.getLatNorth()
                || viewBBox.getLatSouth() != prevBoundingBox.getLatSouth()
                || viewBBox.getLonWest() != prevBoundingBox.getLonWest()
                || viewBBox.getLonEast() != prevBoundingBox.getLonEast()) {

            prevBoundingBox = new BoundingBox(viewBBox.getLatNorth(), viewBBox.getLonEast()
                    , viewBBox.getLatSouth(), viewBBox.getLonWest());

            if (grid == null || viewHei != pMapView.getHeight() || viewWid != pMapView.getWidth()) {
                updateGrid(pMapView);
            } else {
                // empty grid.
                // TODO: we might leave the grid as it was before to avoid the "flickering"?
                for (Point[] row : grid)
                    Arrays.fill(row, null);
            }

            int gridX, gridY;
            final Point mPositionPixels = new Point();
            final Projection pj = pMapView.getProjection();
            numLabels = 0;

            for (IGeoPoint pt1 : mPointList) {
                if (pt1 == null) continue;
                if (pt1.getLatitude() > viewBBox.getLatSouth()
                        && pt1.getLatitude() < viewBBox.getLatNorth()
                        && pt1.getLongitude() > viewBBox.getLonWest()
                        && pt1.getLongitude() < viewBBox.getLonEast()) {
                    pj.toPixels(pt1, mPositionPixels);
                    // test whether in this grid cell there is already a point, skip if yes
                    gridX = (int) Math.floor((float) mPositionPixels.x / mStyle.mCellSize);
                    gridY = (int) Math.floor((float) mPositionPixels.y / mStyle.mCellSize);
                    if (gridX >= gridWid || gridY >= gridHei || gridX < 0 || gridY < 0
                        || grid[gridX][gridY] != null)
                        continue;
                    grid[gridX][gridY] = new LabelledPoint(mPositionPixels
                            , mPointList.isLabelled() ? ((LabelledGeoPoint) pt1).getLabel() : null);
                    numLabels++;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if(mStyle.mAlgorithm !=
                SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevNumPointers = event.getPointerCount();
                startX = event.getX(0);
                startY = event.getY(0);
                for (int i = 1; i < prevNumPointers; i++) {
                    startX += event.getX(i);
                    startY += event.getY(i);
                }
                startX /= prevNumPointers;
                startY /= prevNumPointers;
                break;

            // TODO: this isn't quite well synchronized with MultitouchController in zoom...
            case MotionEvent.ACTION_MOVE:
                curX = event.getX(0);
                curY = event.getY(0);
                for (int i = 1; i < event.getPointerCount(); i++) {
                    curX += event.getX(i);
                    curY += event.getY(i);
                }
                curX /= event.getPointerCount();
                curY /= event.getPointerCount();

                if(event.getPointerCount() != prevNumPointers) {
                    computeGrid(mapView);
                    prevNumPointers = event.getPointerCount();
                    offsetX = curX - startX;
                    offsetY = curY - startY;
                }
                break;

            case MotionEvent.ACTION_UP:
                startX = 0;
                startY = 0;
                curX = 0;
                curY = 0;
                offsetX = 0;
                offsetY = 0;
                mapView.invalidate();
                break;
        }
        return false;
    }

    /**
     * Default action on tap is to select the nearest point.
     */
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
        if(!mStyle.mClickable) return false;
        float hyp;
        Float minHyp = null;
        int closest = -1;
        Point tmp = new Point();
        Projection pj = mapView.getProjection();

        for(int i = 0; i < mPointList.size(); i++) {
            if(mPointList.get(i) == null) continue;
            // TODO avoid projecting coordinates, do a test before calling next line
            pj.toPixels(mPointList.get(i), tmp);
            if(Math.abs(event.getX() - tmp.x) > 50 || Math.abs(event.getY() - tmp.y) > 50) continue;
            hyp = (event.getX() - tmp.x) * (event.getX() - tmp.x)
                    + (event.getY() - tmp.y) * (event.getY() - tmp.y);
            if(minHyp == null || hyp < minHyp) {
                minHyp = hyp;
                closest = i;
            }
        }
        if(minHyp == null) return false;
        setSelectedPoint(closest);
        mapView.invalidate();
        if(clickListener != null) clickListener.onClick(mPointList, closest);
        return true;
    }

    /**
     * Sets the highlighted point. App must invalidate the MapView.
     * @param toSelect The index of the point (zero-based) in the original list.
     */
    public void setSelectedPoint(Integer toSelect) {
        if(toSelect == null || toSelect < 0 || toSelect >= mPointList.size())
            mSelectedPoint = null;
        else
            mSelectedPoint = toSelect;
    }

    public Integer getSelectedPoint() {
        return mSelectedPoint;
    }

    public BoundingBox getBoundingBox() {
        return mBoundingBox;
    }

    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean b) {
        final BoundingBox viewBBox;
        if (b) return;
        final Point mPositionPixels = new Point();
        final Projection pj = mapView.getProjection();
        String tmpLabel;
        boolean showLabels;

        if(mStyle.mPointStyle != null) {
            switch (mStyle.mAlgorithm) {
                case MAXIMUM_OPTIMIZATION:
                    // optimized for speed, recommended for > 10k points
                    // recompute grid only on specific events - only onDraw but when not animating
                    // and not in the middle of a touch scroll gesture
                    if (grid == null
                            || (curX == 0 && curY == 0 && !mapView.isAnimating()))
                        computeGrid(mapView);

                    showLabels =
                            ((mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.DENSITY_THRESHOLD
                                    && numLabels <= mStyle.mMaxNShownLabels)
                                    || (mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.ZOOM_THRESHOLD
                                    && mapView.getZoomLevel() >= mStyle.mMinZoomShowLabels));
                    // draw points
                    float offX = curX - startX;
                    float offY = curY - startY;
                    for (int x = 0; x < gridWid; x++) {
                        for (int y = 0; y < gridHei; y++) {
                            if (grid[x][y] != null) {
                                if(mStyle.mSymbol == SimpleFastPointOverlayOptions.Shape.CIRCLE)
                                    canvas.drawCircle(grid[x][y].x + offX - offsetX
                                            , grid[x][y].y + offY - offsetY
                                            , mStyle.mCircleRadius, mStyle.mPointStyle);
                                else
                                    canvas.drawRect(
                                            grid[x][y].x + offX - offsetX - mStyle.mCircleRadius
                                            , grid[x][y].y + offY - offsetY - mStyle.mCircleRadius
                                            , grid[x][y].x + offX - offsetX + mStyle.mCircleRadius
                                            , grid[x][y].y + offY - offsetY + mStyle.mCircleRadius
                                            , mStyle.mPointStyle);

                                if(mPointList.isLabelled() && showLabels &&
                                        (tmpLabel = grid[x][y].mlabel) != null)
                                    canvas.drawText(tmpLabel
                                            , grid[x][y].x + offX - offsetX
                                            , grid[x][y].y + offY - offsetY - mStyle.mCircleRadius
                                                    - 5
                                            , mStyle.mTextStyle);

                            }
                        }
                    }
                    break;

                case MEDIUM_OPTIMIZATION:
                    // recompute grid index on every draw
                    if (grid == null || viewHei != mapView.getHeight() ||
                            viewWid != mapView.getWidth())
                        updateGrid(mapView);
                    else
                        for (boolean[] row : gridBool)
                            Arrays.fill(row, false);

                    showLabels = (mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.ZOOM_THRESHOLD
                            && mapView.getZoomLevel() >= mStyle.mMinZoomShowLabels);

                    int gridX, gridY;
                    viewBBox = mapView.getBoundingBox();
                    for (IGeoPoint pt1 : mPointList) {
                        if (pt1 == null) continue;
                        if (pt1.getLatitude() > viewBBox.getLatSouth()
                                && pt1.getLatitude() < viewBBox.getLatNorth()
                                && pt1.getLongitude() > viewBBox.getLonWest()
                                && pt1.getLongitude() < viewBBox.getLonEast()) {
                            pj.toPixels(pt1, mPositionPixels);
                            // test whether in this grid cell there is already a point, skip if yes
                            // this makes a lot of difference in rendering speed
                            gridX = (int) Math.floor((float) mPositionPixels.x / mStyle.mCellSize);
                            gridY = (int) Math.floor((float) mPositionPixels.y / mStyle.mCellSize);
                            if (gridX >= gridWid || gridY >= gridHei || gridX < 0 || gridY < 0
                                    || gridBool[gridX][gridY])
                                continue;
                            gridBool[gridX][gridY] = true;

                            if(mStyle.mSymbol == SimpleFastPointOverlayOptions.Shape.CIRCLE)
                                canvas.drawCircle((float) mPositionPixels.x
                                        , (float) mPositionPixels.y
                                        , mStyle.mCircleRadius, mStyle.mPointStyle);
                            else
                                canvas.drawRect((float) mPositionPixels.x - mStyle.mCircleRadius
                                        , (float) mPositionPixels.y - mStyle.mCircleRadius
                                        , (float) mPositionPixels.x + mStyle.mCircleRadius
                                        , (float) mPositionPixels.y + mStyle.mCircleRadius
                                        , mStyle.mPointStyle);

                            if(mPointList.isLabelled() && showLabels &&
                                    (tmpLabel = ((LabelledGeoPoint) pt1).getLabel()) != null)
                                canvas.drawText(tmpLabel
                                        , (float) mPositionPixels.x
                                        , (float) mPositionPixels.y - mStyle.mCircleRadius - 5
                                        , mStyle.mTextStyle);
                        }
                    }
                    break;

                case NO_OPTIMIZATION:
                    // draw all points
                    showLabels = (mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.ZOOM_THRESHOLD
                            && mapView.getZoomLevel() >= mStyle.mMinZoomShowLabels);
                    viewBBox = mapView.getBoundingBox();
                    for (IGeoPoint pt1 : mPointList) {
                        if (pt1 == null) continue;
                        if (pt1.getLatitude() > viewBBox.getLatSouth()
                                && pt1.getLatitude() < viewBBox.getLatNorth()
                                && pt1.getLongitude() > viewBBox.getLonWest()
                                && pt1.getLongitude() < viewBBox.getLonEast()) {
                            pj.toPixels(pt1, mPositionPixels);
                            if(mStyle.mSymbol == SimpleFastPointOverlayOptions.Shape.CIRCLE)
                                canvas.drawCircle((float) mPositionPixels.x
                                        , (float) mPositionPixels.y
                                        , mStyle.mCircleRadius, mStyle.mPointStyle);
                            else
                                canvas.drawRect((float) mPositionPixels.x - mStyle.mCircleRadius
                                        , (float) mPositionPixels.y - mStyle.mCircleRadius
                                        , (float) mPositionPixels.x + mStyle.mCircleRadius
                                        , (float) mPositionPixels.y + mStyle.mCircleRadius
                                        , mStyle.mPointStyle);

                            if(mPointList.isLabelled() && showLabels &&
                                    (tmpLabel = ((LabelledGeoPoint) pt1).getLabel()) != null)
                                canvas.drawText(tmpLabel
                                        , (float) mPositionPixels.x
                                        , (float) mPositionPixels.y - mStyle.mCircleRadius - 5
                                        , mStyle.mTextStyle);

                        }
                    }
                    break;
            }
        }

        if(mSelectedPoint != null && mSelectedPoint < mPointList.size() &&
                mPointList.get(mSelectedPoint) != null
                && mStyle.mSelectedPointStyle != null) {
            pj.toPixels(mPointList.get(mSelectedPoint), mPositionPixels);
            if(mStyle.mSymbol == SimpleFastPointOverlayOptions.Shape.CIRCLE)
                canvas.drawCircle(mPositionPixels.x, mPositionPixels.y
                        , mStyle.mSelectedCircleRadius, mStyle.mSelectedPointStyle);
            else
                canvas.drawRect((float) mPositionPixels.x - mStyle.mSelectedCircleRadius
                        , (float) mPositionPixels.y - mStyle.mSelectedCircleRadius
                        , (float) mPositionPixels.x + mStyle.mSelectedCircleRadius
                        , (float) mPositionPixels.y + mStyle.mSelectedCircleRadius
                        , mStyle.mSelectedPointStyle);
        }
    }
}

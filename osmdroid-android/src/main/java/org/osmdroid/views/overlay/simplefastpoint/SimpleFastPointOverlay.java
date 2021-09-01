package org.osmdroid.views.overlay.simplefastpoint;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Overlay to draw a layer of clickable simple points, optimized for rendering speed. Nice
 * performance up to 100k points. Supports styling each point and label individually, or shared style
 * for all points in a theme. Does not support rotated maps.
 * There are three rendering algorithms:
 * NO_OPTIMIZATION: all points all drawn in every draw event
 * MEDIUM_OPTIMIZATION: not recommended for >10k points. Recalculates the grid index on each draw
 * event and only draws one point per grid cell.
 * MAXIMUM_OPTIMIZATION: for >10k points, only recalculates the grid on touch up, hence much faster.
 * Performs well for 100k points.
 * <p>
 * TODO: a quadtree index would improve rendering speed!
 * TODO: an alternative to the CIRCLE shape, cause this is slow to render
 * Created by Miguel Porto on 25-10-2016.
 */
public class SimpleFastPointOverlay extends Overlay {
    private final SimpleFastPointOverlayOptions mStyle;
    private final PointAdapter mPointList;
    private final BoundingBox mBoundingBox;
    private Integer mSelectedPoint;
    private OnClickListener clickListener;
    private List<StyledLabelledPoint> gridIndex;
    // grid index for optimizing drawing k's of points
    private boolean gridBool[][];
    private int gridWid, gridHei, viewWid, viewHei;
    private boolean hasMoved = false;
    private int numLabels;
    private BoundingBox startBoundingBox;
    private Projection startProjection;
    private BoundingBox prevBoundingBox = new BoundingBox();

    /**
     * Just a light internal class for storing point data
     */
    public class StyledLabelledPoint extends Point {
        private String mlabel;
        private Paint mPointStyle, mTextStyle;

        public StyledLabelledPoint(Point point, String label, Paint pointStyle, Paint textStyle) {
            super(point);
            this.mlabel = label;
            this.mPointStyle = pointStyle;
            this.mTextStyle = textStyle;
        }
    }

    /**
     * @return fast point overlay options (applied to all points)
     */
    public SimpleFastPointOverlayOptions getStyle() {
        return mStyle;
    }

    public interface PointAdapter extends Iterable<IGeoPoint> {
        int size();

        IGeoPoint get(int i);

        /**
         * Whether this adapter has labels
         *
         * @return
         */
        boolean isLabelled();

        /**
         * Whether the points are individually styled
         *
         * @return
         */
        boolean isStyled();
    }

    public interface OnClickListener {
        void onClick(PointAdapter points, Integer point);
    }

    public SimpleFastPointOverlay(PointAdapter pointList, SimpleFastPointOverlayOptions style) {
        mStyle = style;
        mPointList = pointList;

        Double east = null, west = null, north = null, south = null;
        for (IGeoPoint p : mPointList) {
            if (p == null) continue;
            if (east == null || p.getLongitude() > east) east = p.getLongitude();
            if (west == null || p.getLongitude() < west) west = p.getLongitude();
            if (north == null || p.getLatitude() > north) north = p.getLatitude();
            if (south == null || p.getLatitude() < south) south = p.getLatitude();
        }

        if (east != null)
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
        gridBool = new boolean[gridWid][gridHei];

        // TODO the measures on first draw are not the final values.
        // MapView should propagate onLayout to overlays
    }

    /**
     * Re-calculates which points to be shown and their coordinates
     * TODO: this could be further optimized for speed, for example, the grid could be calculated
     * in geographic coordinates, instead of projected. N.B. the speed bottleneck is pj.toPixels()
     *
     * @param pMapView
     */
    private void computeGrid(final MapView pMapView) {
        // TODO: 15-11-2016 should take map orientation into account in the BBox!
        BoundingBox viewBBox = pMapView.getBoundingBox();

        startBoundingBox = viewBBox;
        startProjection = pMapView.getProjection();

        // do not compute grid if BBox is the same
        if (viewBBox.getLatNorth() != prevBoundingBox.getLatNorth()
                || viewBBox.getLatSouth() != prevBoundingBox.getLatSouth()
                || viewBBox.getLonWest() != prevBoundingBox.getLonWest()
                || viewBBox.getLonEast() != prevBoundingBox.getLonEast()) {

            prevBoundingBox = new BoundingBox(viewBBox.getLatNorth(), viewBBox.getLonEast()
                    , viewBBox.getLatSouth(), viewBBox.getLonWest());

            if (gridBool == null || viewHei != pMapView.getHeight() || viewWid != pMapView.getWidth()) {
                updateGrid(pMapView);
            } else {
                // empty grid.
                // TODO: we might leave the grid as it was before to avoid the "flickering"?
                for (boolean[] row : gridBool)
                    Arrays.fill(row, false);
            }

            int gridX, gridY;
            final Point mPositionPixels = new Point();
            final Projection pj = pMapView.getProjection();
            gridIndex = new ArrayList<>();
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
                            || gridBool[gridX][gridY])
                        continue;
                    gridBool[gridX][gridY] = true;
                    gridIndex.add(new StyledLabelledPoint(mPositionPixels
                            , mPointList.isLabelled() ? ((LabelledGeoPoint) pt1).getLabel() : null
                            , mPointList.isStyled() ? ((StyledLabelledGeoPoint) pt1).getPointStyle() : null
                            , mPointList.isStyled() ? ((StyledLabelledGeoPoint) pt1).getTextStyle() : null
                    ));
                    numLabels++;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, final MapView mapView) {
        if (mStyle.mAlgorithm !=
                SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startBoundingBox = mapView.getBoundingBox();
                startProjection = mapView.getProjection();
                break;

            case MotionEvent.ACTION_MOVE:
                hasMoved = true;
                break;

            case MotionEvent.ACTION_UP:
                hasMoved = false;
                startBoundingBox = mapView.getBoundingBox();
                startProjection = mapView.getProjection();
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
        if (!mStyle.mClickable) return false;
        float hyp;
        Float minHyp = null;
        int closest = -1;
        Point tmp = new Point();
        Projection pj = mapView.getProjection();

        for (int i = 0; i < mPointList.size(); i++) {
            if (mPointList.get(i) == null) continue;
            // TODO avoid projecting coordinates, do a test before calling next line
            pj.toPixels(mPointList.get(i), tmp);
            if (Math.abs(event.getX() - tmp.x) > 50 || Math.abs(event.getY() - tmp.y) > 50) continue;
            hyp = (event.getX() - tmp.x) * (event.getX() - tmp.x)
                    + (event.getY() - tmp.y) * (event.getY() - tmp.y);
            if (minHyp == null || hyp < minHyp) {
                minHyp = hyp;
                closest = i;
            }
        }
        if (minHyp == null) return false;
        setSelectedPoint(closest);
        mapView.invalidate();
        if (clickListener != null) clickListener.onClick(mPointList, closest);
        return true;
    }

    /**
     * Sets the highlighted point. App must invalidate the MapView.
     *
     * @param toSelect The index of the point (zero-based) in the original list.
     */
    public void setSelectedPoint(Integer toSelect) {
        if (toSelect == null || toSelect < 0 || toSelect >= mPointList.size())
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
        boolean showLabels;
        Paint textStyle;

        if (mStyle.mPointStyle != null || mPointList.isStyled()) {
            switch (mStyle.mAlgorithm) {
                case MAXIMUM_OPTIMIZATION:
                    // optimized for speed, recommended for > 10k points
                    // recompute grid only on specific events - only onDraw but when not animating
                    // and not in the middle of a touch scroll gesture

                    if (gridBool == null || (!hasMoved && !mapView.isAnimating()))
                        computeGrid(mapView);

                    // compute the coordinates of each visible point in the new viewbox
                    IGeoPoint nw = new GeoPoint(startBoundingBox.getLatNorth(), startBoundingBox.getLonWest());
                    IGeoPoint se = new GeoPoint(startBoundingBox.getLatSouth(), startBoundingBox.getLonEast());
                    Point pNw = pj.toPixels(nw, null);
                    Point pSe = pj.toPixels(se, null);
                    Point pStartSe = startProjection.toPixels(se, null);
                    Point dGz = new Point(pSe.x - pStartSe.x, pSe.y - pStartSe.y);
                    Point dd = new Point(dGz.x - pNw.x, dGz.y - pNw.y);
                    float tx, ty;

                    showLabels =
                            ((mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.DENSITY_THRESHOLD
                                    && numLabels <= mStyle.mMaxNShownLabels)
                                    || (mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.ZOOM_THRESHOLD
                                    && mapView.getZoomLevelDouble() >= mStyle.mMinZoomShowLabels));
                    // draw points
                    for (StyledLabelledPoint slp : gridIndex) {
                        tx = (slp.x * dd.x) / pStartSe.x;
                        ty = (slp.y * dd.y) / pStartSe.y;

                        drawPointAt(canvas, slp.x + pNw.x + tx, slp.y + pNw.y + ty
                                , mPointList.isLabelled() && showLabels, slp.mlabel
                                , (mPointList.isStyled() && slp.mPointStyle != null) ? slp.mPointStyle : mStyle.mPointStyle
                                , (mPointList.isStyled() && (textStyle = slp.mTextStyle) != null)
                                        ? textStyle : mStyle.mTextStyle, mapView);
                    }
                    break;

                case MEDIUM_OPTIMIZATION:
                    // recompute grid index on every draw
                    if (gridBool == null || viewHei != mapView.getHeight() ||
                            viewWid != mapView.getWidth())
                        updateGrid(mapView);
                    else
                        for (boolean[] row : gridBool)
                            Arrays.fill(row, false);

                    showLabels = (mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.ZOOM_THRESHOLD
                            && mapView.getZoomLevelDouble() >= mStyle.mMinZoomShowLabels);

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

                            // style may come individually or from the whole theme setting
                            drawPointAt(canvas, mPositionPixels.x, mPositionPixels.y, mPointList.isLabelled() && showLabels
                                    , mPointList.isLabelled() ? ((LabelledGeoPoint) pt1).getLabel() : null
                                    , (mPointList.isStyled() && ((StyledLabelledGeoPoint) pt1).getPointStyle() != null)
                                            ? ((StyledLabelledGeoPoint) pt1).getPointStyle() : mStyle.mPointStyle
                                    , (mPointList.isStyled() && (textStyle = ((StyledLabelledGeoPoint) pt1).getTextStyle()) != null)
                                            ? textStyle : mStyle.mTextStyle, mapView);
                        }
                    }
                    break;

                case NO_OPTIMIZATION:
                    // draw all points
                    showLabels = (mStyle.mLabelPolicy == SimpleFastPointOverlayOptions.LabelPolicy.ZOOM_THRESHOLD
                            && mapView.getZoomLevelDouble() >= mStyle.mMinZoomShowLabels);
                    viewBBox = mapView.getBoundingBox();
                    for (IGeoPoint pt1 : mPointList) {
                        if (pt1 == null) continue;
                        if (pt1.getLatitude() > viewBBox.getLatSouth()
                                && pt1.getLatitude() < viewBBox.getLatNorth()
                                && pt1.getLongitude() > viewBBox.getLonWest()
                                && pt1.getLongitude() < viewBBox.getLonEast()) {
                            pj.toPixels(pt1, mPositionPixels);

                            // style may come individually or from the whole theme setting

                            drawPointAt(canvas, mPositionPixels.x, mPositionPixels.y, mPointList.isLabelled() && showLabels
                                    , mPointList.isLabelled() ? ((LabelledGeoPoint) pt1).getLabel() : null
                                    , (mPointList.isStyled() && ((StyledLabelledGeoPoint) pt1).getPointStyle() != null)
                                            ? ((StyledLabelledGeoPoint) pt1).getPointStyle() : mStyle.mPointStyle
                                    , (mPointList.isStyled() && (textStyle = ((StyledLabelledGeoPoint) pt1).getTextStyle()) != null)
                                            ? textStyle : mStyle.mTextStyle, mapView);
                        }
                    }
                    break;
            }
        }

        if (mSelectedPoint != null && mSelectedPoint < mPointList.size() &&
                mPointList.get(mSelectedPoint) != null
                && mStyle.mSelectedPointStyle != null) {
            pj.toPixels(mPointList.get(mSelectedPoint), mPositionPixels);
            if (mStyle.mSymbol == SimpleFastPointOverlayOptions.Shape.CIRCLE)
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

    protected void drawPointAt(Canvas canvas, float x, float y, boolean showLabel, String label, Paint pointStyle, Paint textStyle, MapView pMapView) {
        canvas.save();
        canvas.rotate(-pMapView.getMapOrientation(), x, y);
        if (mStyle.mSymbol == SimpleFastPointOverlayOptions.Shape.CIRCLE)
            canvas.drawCircle(x, y, mStyle.mCircleRadius, pointStyle);
        else
            canvas.drawRect(x - mStyle.mCircleRadius, y - mStyle.mCircleRadius
                    , x + mStyle.mCircleRadius, y + mStyle.mCircleRadius
                    , pointStyle);

        if (showLabel && label != null)
            canvas.drawText(label, x, y - mStyle.mCircleRadius - 5, textStyle);
        canvas.restore();
    }
}

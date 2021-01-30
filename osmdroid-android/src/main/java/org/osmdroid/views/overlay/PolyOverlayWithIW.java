package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.Distance;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository of common methods for Polyline and Polygon
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public abstract class PolyOverlayWithIW extends OverlayWithIW {

    protected LinearRing mOutline;
    protected List<LinearRing> mHoles = new ArrayList<>();
    protected Paint mOutlinePaint = new Paint();
    protected Paint mFillPaint;
    private final List<PaintList> mOutlinePaintLists = new ArrayList<>();
    private List<MilestoneManager> mMilestoneManagers = new ArrayList<>();
    private GeoPoint mInfoWindowLocation;

    private LineDrawer mLineDrawer;
    protected Path mPath;
    protected float mDensity = 1.0f;

    /**
     * @since 6.2.0
     */
    private boolean mIsPaintOrPaintList = true;
    private final PointL mVisibilityProjectedCenter = new PointL();
    private final PointL mVisibilityProjectedCorner = new PointL();
    private final PointL mVisibilityRectangleCenter = new PointL();
    private final PointL mVisibilityRectangleCorner = new PointL();

    /**
     * @since 6.2.0
     */
    private int mDowngradeMaximumPixelSize;
    private int mDowngradeMaximumRectanglePixelSize;
    private boolean mDowngradeDisplay;
    private final Point mDowngradeTopLeft = new Point();
    private final Point mDowngradeBottomRight = new Point();
    private final PointL mDowngradeCenter = new PointL();
    private final PointL mDowngradeOffset = new PointL();
    private float[] mDowngradeSegments;

    /**
     * @since 6.2.0
     * Used to be in {@link Polyline}
     */
    private float mDensityMultiplier = 1.0f;
    private final boolean mClosePath;

    protected PolyOverlayWithIW(final MapView pMapView, final boolean pUsePath, final boolean pClosePath) {
        super();
        mClosePath = pClosePath;
        if (pMapView != null) {
            setInfoWindow(pMapView.getRepository().getDefaultPolylineInfoWindow());
            mDensity = pMapView.getContext().getResources().getDisplayMetrics().density;
        }
        usePath(pUsePath);
    }

    /**
     * @since 6.2.0
     * Use Path or not for the display
     * drawPath can be notoriously slower than drawLines, therefore when relevant "Polygon"s
     * would be better off if displayed with drawLines.
     * On the other hand, drawPath sometimes looks better, therefore when relevant "Polyline"s
     * would be better off if displayed with drawPath
     */
    public void usePath(final boolean pUsePath) {
        final ArrayList<GeoPoint> previousPoints = mOutline == null ? null : mOutline.getPoints();
        if (pUsePath) {
            mPath = new Path();
            mLineDrawer = null;
            mOutline = new LinearRing(mPath, mClosePath);
        } else {
            mPath = null;
            mLineDrawer = new LineDrawer(256);
            mOutline = new LinearRing(mLineDrawer, mClosePath);
            mLineDrawer.setPaint(mOutlinePaint);
        }
        if (previousPoints != null) {
            setPoints(previousPoints);
        }
    }

    public void setVisible(boolean visible) {
        setEnabled(visible);
    }

    public boolean isVisible() {
        return isEnabled();
    }

    /**
     * @return the Paint used for the outline. This allows to set advanced Paint settings.
     */
    public Paint getOutlinePaint() {
        mIsPaintOrPaintList = true;
        return mOutlinePaint;
    }

    /**
     * assuming if someone uses this method, someone wants to use List<PaintList>
     * instead of mere Paint
     */
    public List<PaintList> getOutlinePaintLists() {
        mIsPaintOrPaintList = false;
        return mOutlinePaintLists;
    }

    /**
     * @return the Paint used for the filling. This allows to set advanced Paint settings.
     */
    protected Paint getFillPaint() {
        return mFillPaint;
    }

    /**
     * Sets whether to draw each segment of the line as a geodesic or not.
     * Warning: it takes effect only if set before setting the points in the Polyline.
     */
    public void setGeodesic(boolean geodesic) {
        mOutline.setGeodesic(geodesic);
    }

    public boolean isGeodesic() {
        return mOutline.isGeodesic();
    }

    /**
     * Set the InfoWindow to be used.
     * Default is a BasicInfoWindow, with the layout named "bonuspack_bubble".
     * You can use this method either to use your own layout, or to use your own sub-class of InfoWindow.
     * If you don't want any InfoWindow to open, you can set it to null.
     */
    public void setInfoWindow(InfoWindow infoWindow) {
        if (mInfoWindow != null) {
            if (mInfoWindow.getRelatedObject() == this)
                mInfoWindow.setRelatedObject(null);
        }
        mInfoWindow = infoWindow;
    }

    /**
     * Show the infowindow, if any. It will be opened either at the latest location, if any,
     * or to a default location computed by setDefaultInfoWindowLocation method.
     * Note that you can manually set this location with: setInfoWindowLocation
     */
    public void showInfoWindow() {
        if (mInfoWindow != null && mInfoWindowLocation != null)
            mInfoWindow.open(this, mInfoWindowLocation, 0, 0);
    }

    /**
     * Sets the info window anchor point to a geopoint location
     */
    public void setInfoWindowLocation(GeoPoint location) {
        mInfoWindowLocation = location;
    }

    /**
     * @return the geopoint location where the infowindow should point at.
     * Doesn't matter if the infowindow is currently opened or not.
     */
    public GeoPoint getInfoWindowLocation() {
        return mInfoWindowLocation;
    }

    public void setMilestoneManagers(final List<MilestoneManager> pMilestoneManagers) {
        if (pMilestoneManagers == null) {
            if (mMilestoneManagers.size() > 0) {
                mMilestoneManagers.clear();
            }
        } else {
            mMilestoneManagers = pMilestoneManagers;
        }
    }

    /**
     * @return aggregate distance (in meters)
     */
    public double getDistance() {
        return mOutline.getDistance();
    }

    /**
     * Internal method used to ensure that the infowindow will have a default position in all cases,
     * so that the user can call showInfoWindow even if no tap occured before.
     * Currently, set the position on the center of the polygon bounding box.
     */
    protected void setDefaultInfoWindowLocation() {
        int s = mOutline.getPoints().size();
        if (s == 0) {
            mInfoWindowLocation = new GeoPoint(0.0, 0.0);
            return;
        }
        if (mInfoWindowLocation == null) {
            mInfoWindowLocation = new GeoPoint(0., 0);
        }
        mOutline.getCenter(mInfoWindowLocation);
    }

    @Override
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        if (!isVisible(pProjection)) {
            return;
        }

        if (mDowngradeMaximumPixelSize > 0) {
            if (!isWorthDisplaying(pProjection)) {
                if (mDowngradeDisplay) {
                    displayDowngrade(pCanvas, pProjection);
                }
                return;
            }
        }

        if (mPath != null) {
            drawWithPath(pCanvas, pProjection);
        } else {
            drawWithLines(pCanvas, pProjection);
        }
    }

    /**
     * @since 6.2.0
     * We pre-check if it's worth computing and drawing a poly.
     * How do we do that? As an approximation, we consider both the poly and the screen as disks.
     * Then, we compute the distance between both centers: if it's greater than the sum of the radii
     * the poly won't be visible.
     */
    private boolean isVisible(final Projection pProjection) {
        // projecting the center and a corner of the bounding box to the screen, close to the screen center
        final BoundingBox boundingBox = getBounds();
        pProjection.toProjectedPixels(boundingBox.getCenterLatitude(), boundingBox.getCenterLongitude(),
                mVisibilityProjectedCenter);
        pProjection.toProjectedPixels(boundingBox.getLatNorth(), boundingBox.getLonEast(),
                mVisibilityProjectedCorner);
        pProjection.getLongPixelsFromProjected(mVisibilityProjectedCenter,
                pProjection.getProjectedPowerDifference(), true, mVisibilityRectangleCenter);
        pProjection.getLongPixelsFromProjected(mVisibilityProjectedCorner,
                pProjection.getProjectedPowerDifference(), true, mVisibilityRectangleCorner);

        // computing the distance and the radii
        final int screenCenterX = pProjection.getWidth() / 2;
        final int screenCenterY = pProjection.getHeight() / 2;
        final double radius = Math.sqrt(Distance.getSquaredDistanceToPoint(
                mVisibilityRectangleCenter.x, mVisibilityRectangleCenter.y,
                mVisibilityRectangleCorner.x, mVisibilityRectangleCorner.y));
        final double distanceBetweenCenters = Math.sqrt(Distance.getSquaredDistanceToPoint(
                mVisibilityRectangleCenter.x, mVisibilityRectangleCenter.y,
                screenCenterX, screenCenterY));
        final double screenRadius = Math.sqrt(Distance.getSquaredDistanceToPoint(
                0, 0,
                screenCenterX, screenCenterY));

        return distanceBetweenCenters <= radius + screenRadius;
    }

    private void drawWithPath(final Canvas pCanvas, final Projection pProjection) {
        mPath.rewind();

        mOutline.setClipArea(pProjection);
        final PointL offset = mOutline.buildPathPortion(pProjection, null, mMilestoneManagers.size() > 0);
        for (final MilestoneManager milestoneManager : mMilestoneManagers) {
            milestoneManager.init();
            milestoneManager.setDistances(mOutline.getDistances());
            for (final PointL point : mOutline.getPointsForMilestones()) {
                milestoneManager.add(point.x, point.y);
            }
            milestoneManager.end();
        }

        if (mHoles != null) {
            for (final LinearRing hole : mHoles) {
                hole.setClipArea(pProjection);
                hole.buildPathPortion(pProjection, offset, mMilestoneManagers.size() > 0);
            }
            mPath.setFillType(Path.FillType.EVEN_ODD); //for correct support of holes
        }

        if (isVisible(mFillPaint)) {
            pCanvas.drawPath(mPath, mFillPaint);
        }
        if (isVisible(mOutlinePaint)) {
            pCanvas.drawPath(mPath, mOutlinePaint);
        }

        for (final MilestoneManager milestoneManager : mMilestoneManagers) {
            milestoneManager.draw(pCanvas);
        }

        if (isInfoWindowOpen() && mInfoWindow != null && mInfoWindow.getRelatedObject() == this) {
            mInfoWindow.draw();
        }
    }

    private void drawWithLines(final Canvas pCanvas, final Projection pProjection) {
        mLineDrawer.setCanvas(pCanvas);
        mOutline.setClipArea(pProjection);
        boolean storePoints = mMilestoneManagers.size() > 0;
        if (mIsPaintOrPaintList) {
            final Paint paint = getOutlinePaint();
            mLineDrawer.setPaint(paint);
            mOutline.buildLinePortion(pProjection, storePoints);
        } else {
            for (final PaintList paintList : getOutlinePaintLists()) {
                mLineDrawer.setPaint(paintList);
                mOutline.buildLinePortion(pProjection, storePoints);
                storePoints = false;
            }
        }
        for (final MilestoneManager milestoneManager : mMilestoneManagers) {
            milestoneManager.init();
            milestoneManager.setDistances(mOutline.getDistances());
            for (final PointL point : mOutline.getPointsForMilestones()) {
                milestoneManager.add(point.x, point.y);
            }
            milestoneManager.end();
        }

        for (final MilestoneManager milestoneManager : mMilestoneManagers) {
            milestoneManager.draw(pCanvas);
        }
        if (isInfoWindowOpen() && mInfoWindow != null && mInfoWindow.getRelatedObject() == this) {
            mInfoWindow.draw();
        }
    }

    @Override
    public void onDetach(MapView mapView) {
        if (mOutline != null) {
            mOutline.clear();
            mOutline = null;
        }
        mHoles.clear();
        mMilestoneManagers.clear();
        onDestroy();
    }

    /**
     * @since 6.2.0
     */
    @Override
    public BoundingBox getBounds() {
        return mOutline.getBoundingBox();
    }

    /**
     * @since 6.2.0
     * Used to be in {@link Polygon} and {@link Polyline}
     * Set the points of the outline.
     * Note that a later change in the original points List will have no effect.
     * To remove/change points, you must call setPoints again.
     * If geodesic mode has been set, the long segments will follow the earth "great circle".
     */
    public void setPoints(final List<GeoPoint> points) {
        mOutline.setPoints(points);
        setDefaultInfoWindowLocation();
    }

    /**
     * @since 6.2.0
     * Used to be in {@link Polygon} and {@link Polyline}
     * Add the point at the end of the outline
     * If geodesic mode has been set, the long segments will follow the earth "great circle".
     */
    public void addPoint(GeoPoint p) {
        mOutline.addPoint(p);
    }

    /**
     * @return a direct link to the list of polygon's vertices,
     * which are the original points if we didn't use the geodesic feature
     * Warning: changes on this list may cause strange results on the display.
     * @since 6.2.0
     */
    public List<GeoPoint> getActualPoints() {
        return mOutline.getPoints();
    }

    /**
     * @since 6.2.0
     * See {@link #setDowngradePixelSizes(int, int)}
     */
    public void setDowngradeDisplay(final boolean pDowngradeDisplay) {
        mDowngradeDisplay = pDowngradeDisplay;
    }

    /**
     * @param pPolySize      Size in pixels below which we will display an optimized list of segments
     * @param pRectangleSize Size in pixels below which we will display a mere rectangle (faster);
     *                       supposed to be lower than pPolySize
     * @since 6.2.0
     * If the size of the Poly (width or height) projected on the screen is lower than the parameter,
     * the Poly won't be displayed as a real Poly, but downgraded to an optimized list of segments
     * or an even more optimized rectangle
     * (or not displayed at all, depending on {@link #setDowngradeDisplay(boolean)}
     */
    public void setDowngradePixelSizes(final int pPolySize, final int pRectangleSize) {
        mDowngradeMaximumRectanglePixelSize = pRectangleSize;
        mDowngradeMaximumPixelSize = Math.max(pPolySize, pRectangleSize);
    }

    /**
     * @since 6.2.0
     */
    private boolean isWorthDisplaying(final Projection pProjection) {
        final BoundingBox boundingBox = getBounds();
        pProjection.toPixels(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()), mDowngradeTopLeft);
        pProjection.toPixels(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest()), mDowngradeBottomRight);
        final double worldSize = pProjection.getWorldMapSize();
        final long right = Math.round(mOutline.getCloserValue(mDowngradeTopLeft.x, mDowngradeBottomRight.x, worldSize));
        final long bottom = Math.round(mOutline.getCloserValue(mDowngradeTopLeft.y, mDowngradeBottomRight.y, worldSize));
        if (Math.abs(mDowngradeTopLeft.x - mDowngradeBottomRight.x) < mDowngradeMaximumPixelSize) {
            return false;
        }
        if (Math.abs(mDowngradeTopLeft.x - right) < mDowngradeMaximumPixelSize) {
            return false;
        }
        if (Math.abs(mDowngradeTopLeft.y - mDowngradeBottomRight.y) < mDowngradeMaximumPixelSize) {
            return false;
        }
        if (Math.abs(mDowngradeTopLeft.y - bottom) < mDowngradeMaximumPixelSize) {
            return false;
        }
        return true;
    }

    /**
     * @since 6.2.0
     */
    private boolean isVisible(final Paint pPaint) {
        return pPaint != null && pPaint.getColor() != Color.TRANSPARENT;
    }

    /**
     * @since 6.2.0
     */
    private void displayDowngrade(final Canvas pCanvas, final Projection pProjection) {
        final BoundingBox boundingBox = mOutline.getBoundingBox();
        pProjection.toPixels(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()), mDowngradeTopLeft);
        pProjection.toPixels(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest()), mDowngradeBottomRight);
        final double worldSize = pProjection.getWorldMapSize();
        long left = mDowngradeTopLeft.x;
        long top = mDowngradeTopLeft.y;
        final long right = Math.round(mOutline.getCloserValue(left, mDowngradeBottomRight.x, worldSize));
        final long bottom = Math.round(mOutline.getCloserValue(top, mDowngradeBottomRight.y, worldSize));
        final long width;
        if (left == right) {
            width = 1;
        } else if (left > right) {
            width = left - right;
            left = right;
        } else {
            width = right - left;
        }
        final long height;
        if (top == bottom) {
            height = 1;
        } else if (top > bottom) {
            height = top - bottom;
            top = bottom;
        } else {
            height = bottom - top;
        }
        mDowngradeCenter.set(left + width / 2, top + height / 2);
        mOutline.getBestOffset(pProjection, mDowngradeOffset, mDowngradeCenter);
        left += mDowngradeOffset.x;
        top += mDowngradeOffset.y;

        Paint paint = null;
        if (mIsPaintOrPaintList) {
            paint = getOutlinePaint();
        } else if (getOutlinePaintLists().size() > 0) {
            final PaintList paintList = getOutlinePaintLists().get(0);
            paint = paintList.getPaint();
            if (paint == null) { // polychromatic
                paint = paintList.getPaint(0, left, top, left + width, top + height);
            }
        }
        if (!isVisible(paint)) {
            return;
        }

        final long maxWidthHeight = width > height ? width : height;
        if (maxWidthHeight <= mDowngradeMaximumRectanglePixelSize) {
            pCanvas.drawRect(left, top, left + width, top + height, paint);
            return;
        }

        final float[] downgradeList = mOutline.computeDowngradePointList(mDowngradeMaximumPixelSize);
        if (downgradeList == null || downgradeList.length == 0) {
            return;
        }
        final int size = downgradeList.length * 2;
        if (mDowngradeSegments == null || mDowngradeSegments.length < size) {
            mDowngradeSegments = new float[size];
        }
        final float factor = maxWidthHeight * 1f / mDowngradeMaximumPixelSize;
        int index = 0;
        float firstX = 0;
        float firstY = 0;
        for (int i = 0; i < downgradeList.length; ) {
            float currentX = mDowngradeCenter.x + downgradeList[i++] * factor;
            float currentY = mDowngradeCenter.y + downgradeList[i++] * factor;
            if (index == 0) {
                firstX = currentX;
                firstY = currentY;
            } else {
                mDowngradeSegments[index++] = currentX;
                mDowngradeSegments[index++] = currentY;
            }
            mDowngradeSegments[index++] = currentX;
            mDowngradeSegments[index++] = currentY;
        }
        // close
        mDowngradeSegments[index++] = firstX;
        mDowngradeSegments[index++] = firstY;
        if (index <= 4) {
            return;
        }
        pCanvas.drawLines(mDowngradeSegments, 0, index, paint);
    }

    /**
     * @since 6.2.0
     */
    protected abstract boolean click(final MapView pMapView, final GeoPoint pEventPos);

    /**
     * @since 6.2.0
     * Used to be in {@link Polyline}
     */
    public void setDensityMultiplier(final float pDensityMultiplier) {
        mDensityMultiplier = pDensityMultiplier;
    }

    /**
     * Used to be if {@link Polygon}
     * Important note: this function returns correct results only if the Poly has been drawn before,
     * and if the MapView positioning has not changed.
     *
     * @return true if the Poly contains the event position.
     */
    public boolean contains(final MotionEvent pEvent) {
        if (mPath.isEmpty()) {
            return false;
        }
        final RectF bounds = new RectF(); //bounds of the Path
        mPath.computeBounds(bounds, true);
        final Region region = new Region();
        //Path has been computed in #draw (we assume that if it can be clicked, it has been drawn before).
        region.setPath(mPath, new Region((int) bounds.left, (int) bounds.top,
                (int) (bounds.right), (int) (bounds.bottom)));
        return region.contains((int) pEvent.getX(), (int) pEvent.getY());
    }

    /**
     * @param pTolerance in pixels
     * @return true if the Poly is close enough to the point.
     * @since 6.2.0
     * Used to be in {@link Polyline}
     * Detection is done is screen coordinates.
     */
    public boolean isCloseTo(final GeoPoint pPoint, final double pTolerance, final MapView pMapView) {
        return getCloseTo(pPoint, pTolerance, pMapView) != null;
    }

    /**
     * @param pTolerance in pixels
     * @return the first GeoPoint of the Poly close enough to the point
     * @since 6.2.0
     * Used to be in {@link Polyline}
     * Detection is done is screen coordinates.
     */
    public GeoPoint getCloseTo(final GeoPoint pPoint, final double pTolerance, final MapView pMapView) {
        return mOutline.getCloseTo(pPoint, pTolerance, pMapView.getProjection(), mClosePath);
    }

    /**
     * Used to be in both {@link Polyline} and {@link Polygon}
     * Default listener for a single tap event on a Poly:
     * set the infowindow at the tapped position, and open the infowindow (if any).
     *
     * @return true if tapped
     */
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent pEvent, final MapView pMapView) {
        final Projection projection = pMapView.getProjection();
        final GeoPoint eventPos = (GeoPoint) projection.fromPixels((int) pEvent.getX(), (int) pEvent.getY());
        final GeoPoint geoPoint;
        if (mPath != null) {
            final boolean tapped = contains(pEvent);
            if (tapped) {
                geoPoint = eventPos;
            } else {
                geoPoint = null;
            }
        } else {
            final double tolerance = mOutlinePaint.getStrokeWidth() * mDensity * mDensityMultiplier;
            geoPoint = getCloseTo(eventPos, tolerance, pMapView);
        }
        if (geoPoint != null) {
            return click(pMapView, geoPoint);
        }
        return false;
    }
}
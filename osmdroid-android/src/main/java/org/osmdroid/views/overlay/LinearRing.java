package org.osmdroid.views.overlay;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.Distance;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.IntegerAccepter;
import org.osmdroid.util.LineBuilder;
import org.osmdroid.util.ListPointAccepter;
import org.osmdroid.util.ListPointL;
import org.osmdroid.util.PathBuilder;
import org.osmdroid.util.PointAccepter;
import org.osmdroid.util.PointL;
import org.osmdroid.util.SegmentClipper;
import org.osmdroid.util.SideOptimizationPointAccepter;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.util.constants.MathConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding one ring: the polygon outline, or a hole inside the polygon
 * Used to be an inner class of {@link Polygon} and {@link Polyline}
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */
public class LinearRing {

    /**
     * We build a virtual area [mClipMin, mClipMin, mClipMax, mClipMax]
     * used to clip our Path in order to cope with
     * - very high pixel values (that go beyond the int values, for instance on zoom 29)
     * - some kind of Android bug related to hardware acceleration
     * "One size fits all" clip area values cannot be determined
     * as there's not explicit value given by Android to avoid Path drawing issues.
     * If the size is too big (magnitude around Integer.MAX_VALUE), the Path won't show properly.
     * If it's small (just above the size of the screen), the approximations of the clipped Path
     * may look gross, particularly if you zoom out in animation.
     * The smaller it is, the better it is for performances because the clip
     * will then often approximate consecutive Path segments as identical, and we only add
     * distinct points to the Path as an optimization.
     * The best idea so far is to compute the clip area border values
     * from the current MapView's characteristics (width, height, scale, orientation)
     */

    private final ArrayList<GeoPoint> mOriginalPoints = new ArrayList<>();
    private double[] mDistances;
    private long[] mProjectedPoints;
    private final PointL mProjectedCenter = new PointL();
    private final SegmentClipper mSegmentClipper = new SegmentClipper();
    private final Path mPath;
    private final BoundingBox mBoundingBox = new BoundingBox();
    private boolean mProjectedPrecomputed;
    private boolean mDistancesPrecomputed;
    private boolean isHorizontalRepeating = true;
    private boolean isVerticalRepeating = true;
    private final ListPointL mPointsForMilestones = new ListPointL();
    private final PointAccepter mPointAccepter;
    private final IntegerAccepter mIntegerAccepter;
    private boolean mGeodesic = false;

    /**
     * @since 6.1.0
     */
    private final boolean mClosed;

    /**
     * @since 6.2.0
     */
    private float[] mDowngradePointList;
    private int mDowngradePixelSize;
    private long mProjectedWidth;
    private long mProjectedHeight;

    /**
     * Dedicated to `Path`
     */
    public LinearRing(final Path pPath) {
        this(pPath, true);
    }

    /**
     * Dedicated to lines
     *
     * @since 6.2.0
     */
    public LinearRing(final LineBuilder pLineBuilder, final boolean pClosePath) {
        mPath = null;
        mPointAccepter = pLineBuilder;
        if (pLineBuilder instanceof LineDrawer) {
            mIntegerAccepter = new IntegerAccepter(pLineBuilder.getLines().length / 2);
            ((LineDrawer) pLineBuilder).setIntegerAccepter(mIntegerAccepter);
        } else {
            mIntegerAccepter = null;
        }
        mClosed = pClosePath;
    }

    /**
     * Dedicated to lines
     *
     * @since 6.0.0
     */
    public LinearRing(final LineBuilder pLineBuilder) {
        this(pLineBuilder, false);
    }

    /**
     * @since 6.1.0
     */
    public LinearRing(final Path pPath, final boolean pClosed) {
        mPath = pPath;
        mPointAccepter = new SideOptimizationPointAccepter(new PathBuilder(pPath));
        mIntegerAccepter = null;
        mClosed = pClosed;
    }

    void clearPath() {
        mOriginalPoints.clear();
        mProjectedPoints = null;
        mDistances = null;
        resetPrecomputations();
        mPointAccepter.init();
    }

    protected void addGreatCircle(final GeoPoint startPoint, final GeoPoint endPoint, final int numberOfPoints) {
        //	adapted from page http://compastic.blogspot.co.uk/2011/07/how-to-draw-great-circle-on-map-in.html
        //	which was adapted from page http://maps.forum.nu/gm_flight_path.html

        // convert to radians
        final double lat1 = startPoint.getLatitude() * MathConstants.DEG2RAD;
        final double lon1 = startPoint.getLongitude() * MathConstants.DEG2RAD;
        final double lat2 = endPoint.getLatitude() * MathConstants.DEG2RAD;
        final double lon2 = endPoint.getLongitude() * MathConstants.DEG2RAD;

        final double d = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2) / 2), 2) + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin((lon1 - lon2) / 2), 2)));
		/*
		double bearing = Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2),
				Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2))
				/ -MathConstants.DEG2RAD;
		bearing = bearing < 0 ? 360 + bearing : bearing;
		*/

        for (int i = 1; i <= numberOfPoints; i++) {
            final double f = 1.0 * i / (numberOfPoints + 1);
            final double A = Math.sin((1 - f) * d) / Math.sin(d);
            final double B = Math.sin(f * d) / Math.sin(d);
            final double x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
            final double y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
            final double z = A * Math.sin(lat1) + B * Math.sin(lat2);

            final double latN = Math.atan2(z, Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
            final double lonN = Math.atan2(y, x);
            GeoPoint p = new GeoPoint(latN * MathConstants.RAD2DEG, lonN * MathConstants.RAD2DEG);
            mOriginalPoints.add(p);
        }
    }

    public void addPoint(final GeoPoint p) {
        if (mGeodesic && mOriginalPoints.size() > 0) {
            //add potential intermediate points:
            GeoPoint prev = mOriginalPoints.get(mOriginalPoints.size() - 1);
            final int greatCircleLength = (int) prev.distanceToAsDouble(p);
            //add one point for every 100kms of the great circle path
            final int numberOfPoints = greatCircleLength / 100000;
            addGreatCircle(prev, p, numberOfPoints);
        }
        mOriginalPoints.add(p);
        resetPrecomputations();
    }

    /**
     * @since 6.2.0
     */
    private void resetPrecomputations() {
        mProjectedPrecomputed = false;
        mDistancesPrecomputed = false;
        mDowngradePixelSize = 0;
        mDowngradePointList = null;
    }

    public void setPoints(final List<GeoPoint> points) {
        clearPath();
        for (GeoPoint p : points) {
            addPoint(p);
        }
    }

    public ArrayList<GeoPoint> getPoints() {
        return mOriginalPoints;
    }

    double[] getDistances() {
        computeDistances();
        return mDistances;
    }

    /**
     * @since 6.0.3
     */
    public double getDistance() {
        double result = 0;
        for (final double distance : getDistances()) {
            result += distance;
        }
        return result;
    }

    public void setGeodesic(boolean geodesic) {
        mGeodesic = geodesic;
    }

    public boolean isGeodesic() {
        return mGeodesic;
    }

    /**
     * Feed the path with the segments corresponding to the GeoPoint pairs
     * projected using pProjection and clipped into a "reasonable" clip area
     * In most cases (Polygon without holes, Polyline) the offset parameter will be null.
     * In the case of a Polygon with holes, the first path will use a null offset.
     * Then this method will return the pixel offset computed for this path so that
     * the path is in the best possible place on the map:
     * the center of all pixels is as close to the screen center as possible
     * Then, this computed offset must be injected into the buildPathPortion for each hole,
     * in order to have the main polygon and its holes at the same place on the map.
     *
     * @return the initial offset if not null, or the computed offset
     */
    PointL buildPathPortion(final Projection pProjection,
                            final PointL pOffset,
                            final boolean pStorePoints) {
        final int size = mOriginalPoints.size();
        if (size < 2) { // nothing to paint
            return pOffset;
        }
        computeProjected();
        computeDistances();
        final PointL offset;
        if (pOffset != null) {
            offset = pOffset;
        } else {
            offset = new PointL();
            getBestOffset(pProjection, offset);
        }
        mSegmentClipper.init();
        clipAndStore(pProjection, offset, mClosed, pStorePoints, mSegmentClipper);
        mSegmentClipper.end();
        if (mClosed) {
            mPath.close();
        }
        return offset;
    }

    /**
     * Dedicated to Polyline, as they can run much faster with drawLine than through a Path
     *
     * @since 6.0.0
     */
    void buildLinePortion(final Projection pProjection,
                          final boolean pStorePoints) {
        final int size = mOriginalPoints.size();
        if (size < 2) { // nothing to paint
            return;
        }
        computeProjected();
        computeDistances();
        final PointL offset = new PointL();
        getBestOffset(pProjection, offset);
        mSegmentClipper.init();
        clipAndStore(pProjection, offset, mClosed, pStorePoints, mSegmentClipper);
        mSegmentClipper.end();
    }

    /**
     * @since 6.0.0
     */
    public ListPointL getPointsForMilestones() {
        return mPointsForMilestones;
    }

    /**
     * Compute the pixel offset so that a list of pixel segments display in the best possible way:
     * the center of all pixels is as close to the screen center as possible
     * This notion of pixel offset only has a meaning on very low zoom level,
     * when a GeoPoint can be projected on different places on the screen.
     */
    private void getBestOffset(final Projection pProjection, final PointL pOffset) {
        final double powerDifference = pProjection.getProjectedPowerDifference();
        final PointL center = pProjection.getLongPixelsFromProjected(
                mProjectedCenter, powerDifference, false, null);
        getBestOffset(pProjection, pOffset, center);
    }

    /**
     * @since 6.2.0
     */
    public void getBestOffset(final Projection pProjection, final PointL pOffset, final PointL pPixel) {
        final Rect screenRect = pProjection.getIntrinsicScreenRect();
        final double screenCenterX = (screenRect.left + screenRect.right) / 2.;
        final double screenCenterY = (screenRect.top + screenRect.bottom) / 2.;
        final double worldSize = pProjection.getWorldMapSize();
        getBestOffset(pPixel.x, pPixel.y, screenCenterX, screenCenterY, worldSize, pOffset);
    }

    /**
     * @since 6.0.0
     */
    private void getBestOffset(final double pPolyCenterX, final double pPolyCenterY,
                               final double pScreenCenterX, final double pScreenCenterY,
                               final double pWorldSize, final PointL pOffset) {
        final long worldSize = Math.round(pWorldSize);
        int deltaPositive;
        int deltaNegative;
        if (!isVerticalRepeating) {
            deltaPositive = 0;
            deltaNegative = 0;
        } else {
            deltaPositive = getBestOffset(
                    pPolyCenterX, pPolyCenterY, pScreenCenterX, pScreenCenterY, 0, worldSize);
            deltaNegative = getBestOffset(
                    pPolyCenterX, pPolyCenterY, pScreenCenterX, pScreenCenterY, 0, -worldSize);

        }
        pOffset.y = worldSize * (deltaPositive > deltaNegative ? deltaPositive : -deltaNegative);
        if (!isHorizontalRepeating) {
            deltaPositive = 0;
            deltaNegative = 0;
        } else {
            deltaPositive = getBestOffset(
                    pPolyCenterX, pPolyCenterY, pScreenCenterX, pScreenCenterY, worldSize, 0);
            deltaNegative = getBestOffset(
                    pPolyCenterX, pPolyCenterY, pScreenCenterX, pScreenCenterY, -worldSize, 0);

        }
        pOffset.x = worldSize * (deltaPositive > deltaNegative ? deltaPositive : -deltaNegative);
    }

    /**
     * @since 6.0.0
     */
    private int getBestOffset(final double pPolyCenterX, final double pPolyCenterY,
                              final double pScreenCenterX, final double pScreenCenterY,
                              final long pDeltaX, final long pDeltaY) {
        double squaredDistance = 0;
        int i = 0;
        while (true) {
            final double tmpSquaredDistance = Distance.getSquaredDistanceToPoint(
                    pPolyCenterX + i * pDeltaX, pPolyCenterY + i * pDeltaY,
                    pScreenCenterX, pScreenCenterY);
            if (i == 0 || squaredDistance > tmpSquaredDistance) {
                squaredDistance = tmpSquaredDistance;
                i++;
            } else {
                break;
            }
        }
        return i - 1;
    }

    /**
     * @since 6.0.0
     */
    private void clipAndStore(final Projection pProjection, final PointL pOffset,
                              final boolean pClosePath, final boolean pStorePoints,
                              final SegmentClipper pSegmentClipper) {
        mPointsForMilestones.clear();
        final double powerDifference = pProjection.getProjectedPowerDifference();
        final PointL projected = new PointL();
        final PointL point = new PointL();
        final PointL first = new PointL();
        for (int i = 0; i < mProjectedPoints.length; i += 2) {
            projected.set(mProjectedPoints[i], mProjectedPoints[i + 1]);
            pProjection.getLongPixelsFromProjected(projected, powerDifference, false, point);
            final long x = point.x + pOffset.x;
            final long y = point.y + pOffset.y;
            if (pStorePoints) {
                mPointsForMilestones.add(x, y);
            }
            if (pSegmentClipper != null) {
                pSegmentClipper.add(x, y);
            }
            if (i == 0) {
                first.set(x, y);
            }
        }
        if (pClosePath) {
            if (pSegmentClipper != null) {
                pSegmentClipper.add(first.x, first.y);
            }
            if (pStorePoints) {
                mPointsForMilestones.add(first.x, first.y);
            }
        }
    }

    /**
     * @since 6.2.0
     */
    public static double getCloserValue(final double pPrevious, double pNext, final double pWorldSize) {
        while (Math.abs(pNext - pWorldSize - pPrevious) < Math.abs(pNext - pPrevious)) {
            pNext -= pWorldSize;
        }
        while (Math.abs(pNext + pWorldSize - pPrevious) < Math.abs(pNext - pPrevious)) {
            pNext += pWorldSize;
        }
        return pNext;
    }

    /**
     * We want consecutive projected points to be as close as possible,
     * and not a world away (typically when dealing with very low zoom levels)
     */
    private void setCloserPoint(final PointL pPrevious, final PointL pNext,
                                final double pWorldSize) {
        if (isHorizontalRepeating) {
            pNext.x = Math.round(getCloserValue(pPrevious.x, pNext.x, pWorldSize));
        }
        if (isVerticalRepeating) {
            pNext.y = Math.round(getCloserValue(pPrevious.y, pNext.y, pWorldSize));
        }
    }

    /**
     * Detection is done in screen coordinates.
     *
     * @param tolerance in pixels
     * @return true if the Polyline is close enough to the point.
     */
    boolean isCloseTo(final GeoPoint pPoint, final double tolerance,
                      final Projection pProjection, final boolean pClosePath) {
        return getCloseTo(pPoint, tolerance, pProjection, pClosePath) != null;
    }

    /**
     * @param tolerance in pixels
     * @return the first GeoPoint of the Polyline close enough to the point
     * @since 6.0.3
     * Detection is done in screen coordinates.
     */
    GeoPoint getCloseTo(final GeoPoint pPoint, final double tolerance,
                        final Projection pProjection, final boolean pClosePath) {
        computeProjected();
        final Point pixel = pProjection.toPixels(pPoint, null);
        final PointL offset = new PointL();
        getBestOffset(pProjection, offset);
        clipAndStore(pProjection, offset, pClosePath, true, null);
        final double mapSize = pProjection.getWorldMapSize();
        final Rect screenRect = pProjection.getIntrinsicScreenRect();
        final int screenWidth = screenRect.width();
        final int screenHeight = screenRect.height();
        double startX = pixel.x; // in order to deal with world replication
        while (startX - mapSize >= 0) {
            startX -= mapSize;
        }
        double startY = pixel.y;
        while (startY - mapSize >= 0) {
            startY -= mapSize;
        }
        final double squaredTolerance = tolerance * tolerance;
        final PointL point0 = new PointL();
        final PointL point1 = new PointL();
        boolean first = true;
        int index = 0;
        for (final PointL point : mPointsForMilestones) {
            point1.set(point);
            if (first) {
                first = false;
            } else {
                for (double x = startX; x < screenWidth; x += mapSize) {
                    for (double y = startY; y < screenHeight; y += mapSize) {
                        final double projectionFactor = Distance.getProjectionFactorToSegment(x, y, point0.x, point0.y, point1.x, point1.y);
                        final double squaredDistance = Distance.getSquaredDistanceToProjection(x, y, point0.x, point0.y, point1.x, point1.y, projectionFactor);
                        if (squaredTolerance > squaredDistance) {
                            final long pointAX = mProjectedPoints[2 * (index - 1)];
                            final long pointAY = mProjectedPoints[2 * (index - 1) + 1];
                            final long pointBX = mProjectedPoints[2 * index];
                            final long pointBY = mProjectedPoints[2 * index + 1];
                            final long projectionX = (long) (pointAX + (pointBX - pointAX) * projectionFactor);
                            final long projectionY = (long) (pointAY + (pointBY - pointAY) * projectionFactor);
                            return MapView.getTileSystem().getGeoFromMercator(
                                    projectionX, projectionY, Projection.mProjectedMapSize,
                                    null, false, false);
                        }
                    }
                }
            }
            point0.set(point1);
            index++;
        }
        return null;
    }

    /**
     * @since 6.0.0
     * Mandatory use before clipping.
     */
    public void setClipArea(final long pXMin, final long pYMin, final long pXMax, final long pYMax) {
        mSegmentClipper.set(pXMin, pYMin, pXMax, pYMax, mPointAccepter, mIntegerAccepter, mPath != null);
    }

    /**
     * @since 6.0.0
     * Mandatory use before clipping.
     */
    public void setClipArea(final Projection pProjection) {
        final double border = .1;
        final Rect rect = pProjection.getIntrinsicScreenRect();
        final int halfWidth = rect.width() / 2;
        final int halfHeight = rect.height() / 2;
        // People less lazy than me would do more refined computations for width and height
        // that include the map orientation: the covered area would be smaller but still big enough
        // Now we use the circle which contains the `MapView`'s 4 corners
        final double radius = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
        // cf. https://github.com/osmdroid/osmdroid/issues/1528
        // People less lazy than me would not double the radius and rather fix the pixel coordinates:
        // using Projection.getScreenCenterX() and Y() would certainly make sense
        // instead of halfWidth and halfHeight, and that could improve performances
        // to have a smaller radius (in that case, not doubled)
        final double doubleRadius = 2 * radius;
        final int scaledRadius = (int) (doubleRadius * (1 + border));
        setClipArea(
                halfWidth - scaledRadius, halfHeight - scaledRadius,
                halfWidth + scaledRadius, halfHeight + scaledRadius
        );
        // TODO: Not sure if this is the correct approach
        this.isHorizontalRepeating = pProjection.isHorizontalWrapEnabled();
        this.isVerticalRepeating = pProjection.isVerticalWrapEnabled();
    }

    /**
     * @since 6.0.2
     */
    public GeoPoint getCenter(final GeoPoint pReuse) {
        final GeoPoint out = pReuse != null ? pReuse : new GeoPoint(0., 0);
        final BoundingBox boundingBox = getBoundingBox();
        out.setLatitude(boundingBox.getCenterLatitude());
        out.setLongitude(boundingBox.getCenterLongitude());
        return out;
    }

    /**
     * @since 6.0.3
     * Code comes from now gone method computeProjectedAndDistances
     */
    private void computeProjected() {
        if (mProjectedPrecomputed) {
            return;
        }
        mProjectedPrecomputed = true;
        if (mProjectedPoints == null || mProjectedPoints.length != mOriginalPoints.size() * 2) {
            mProjectedPoints = new long[mOriginalPoints.size() * 2];
        }
        long minX = 0;
        long maxX = 0;
        long minY = 0;
        long maxY = 0;
        double north = 0;
        double east = 0;
        double south = 0;
        double west = 0;
        int index = 0;
        final PointL previous = new PointL();
        final PointL current = new PointL();
        final TileSystem tileSystem = MapView.getTileSystem();
        final double projectedMapSize = Projection.mProjectedMapSize;
        for (final GeoPoint currentGeo : mOriginalPoints) {
            final double latitude = currentGeo.getLatitude();
            final double longitude = currentGeo.getLongitude();
            tileSystem.getMercatorFromGeo(latitude, longitude, projectedMapSize, current, false);
            if (index == 0) {
                minX = maxX = current.x;
                minY = maxY = current.y;
                north = south = latitude;
                east = west = longitude;
            } else {
                setCloserPoint(previous, current, projectedMapSize);
                if (minX > current.x) {
                    minX = current.x;
                    west = longitude;
                }
                if (maxX < current.x) {
                    maxX = current.x;
                    east = longitude;
                }
                if (minY > current.y) {
                    minY = current.y;
                    north = latitude;
                }
                if (maxY < current.y) {
                    maxY = current.y;
                    south = latitude;
                }
            }
            mProjectedPoints[2 * index] = current.x;
            mProjectedPoints[2 * index + 1] = current.y;
            previous.set(current.x, current.y);
            index++;
        }
        mProjectedWidth = maxX - minX;
        mProjectedHeight = maxY - minY;
        mProjectedCenter.set((minX + maxX) / 2, (minY + maxY) / 2);
        mBoundingBox.set(north, east, south, west);
    }

    /**
     * @since 6.0.3
     * Code comes from now gone method computeProjectedAndDistances
     */
    private void computeDistances() {
        if (mDistancesPrecomputed) {
            return;
        }
        mDistancesPrecomputed = true;
        if (mDistances == null || mDistances.length != mOriginalPoints.size()) {
            mDistances = new double[mOriginalPoints.size()];
        }
        int index = 0;
        final GeoPoint previousGeo = new GeoPoint(0., 0);
        for (final GeoPoint currentGeo : mOriginalPoints) {
            if (index == 0) {
                mDistances[index] = 0;
            } else {
                mDistances[index] = currentGeo.distanceToAsDouble(previousGeo);
            }
            previousGeo.setCoords(currentGeo.getLatitude(), currentGeo.getLongitude());
            index++;
        }
    }

    /**
     * @since 6.2.0
     */
    public BoundingBox getBoundingBox() {
        if (!mProjectedPrecomputed) {
            computeProjected();
        }
        return mBoundingBox;
    }

    /**
     * @since 6.2.0
     */
    public void clear() {
        mOriginalPoints.clear();
        if (mPath != null) {
            mPath.reset();
        }
        ;
        mPointsForMilestones.clear();
    }

    /**
     * Computes the list of points of a polyline that would be the projection of the GeoPoints
     * on a centered size*size square
     *
     * @since 6.2.0
     */
    float[] computeDowngradePointList(final int pSize) {
        if (pSize == 0) {
            return null;
        }
        if (mDowngradePixelSize == pSize) {
            return mDowngradePointList;
        }
        computeProjected();
        final long projectedSize = mProjectedWidth > mProjectedHeight ? mProjectedWidth : mProjectedHeight;
        if (projectedSize == 0) {
            return null;
        }
        final ListPointAccepter listPointAccepter = new ListPointAccepter(true);
        final PointAccepter pointAccepter = new SideOptimizationPointAccepter(listPointAccepter);
        final double factor = (projectedSize * 1.) / pSize;
        for (int i = 0; i < mProjectedPoints.length; ) {
            final long x = mProjectedPoints[i++];
            final long y = mProjectedPoints[i++];
            final long squareX = Math.round((x - mProjectedCenter.x) / factor);
            final long squareY = Math.round((y - mProjectedCenter.y) / factor);
            pointAccepter.add(squareX, squareY);
        }
        mDowngradePixelSize = pSize;
        mDowngradePointList = new float[listPointAccepter.getList().size()];
        for (int i = 0; i < mDowngradePointList.length; i++) {
            mDowngradePointList[i] = listPointAccepter.getList().get(i);
        }
        return mDowngradePointList;
    }
}

package org.osmdroid.views;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.GeometryMath;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.TileSystem;

/**
 * A Projection serves to translate between the coordinate system of x/y on-screen pixel coordinates
 * and that of latitude/longitude points on the surface of the earth. You obtain a Projection from
 * MapView.getProjection(). You should not hold on to this object for more than one draw, since the
 * projection of the map could change. <br>
 * <br>Uses the web mercator projection
 * <b>Note:</b> This class will "wrap" all pixel and lat/long values that overflow their bounds
 * (rather than clamping to their bounds).
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 * @author Fabrice Fontaine
 */
public class Projection implements IProjection {

    /**
     * The size in pixels of a VERY large map, the "projected" map.
     * For optimization purpose, we may compute only once the projection of the GeoPoints
     * on this large map, and then just divide in order to get the projection on a corresponding
     * smaller map / smaller zoom
     */
    public static final double mProjectedMapSize = 1L << 60;
    private long mOffsetX;
    private long mOffsetY;
    private long mScrollX;
    private long mScrollY;

    private final Matrix mRotateAndScaleMatrix = new Matrix();
    private final Matrix mUnrotateAndScaleMatrix = new Matrix();
    private final float[] mRotateScalePoints = new float[2];

    private final BoundingBox mBoundingBoxProjection = new BoundingBox();
    private final double mZoomLevelProjection;
    private final Rect mScreenRectProjection = new Rect();
    private final Rect mIntrinsicScreenRectProjection;

    private boolean horizontalWrapEnabled;
    private boolean verticalWrapEnabled;

    private final double mMercatorMapSize;
    private final double mTileSize;
    private final float mOrientation;
    private final GeoPoint mCurrentCenter = new GeoPoint(0., 0);

    private final TileSystem mTileSystem;

    /**
     * @since 6.1.1
     */
    private final int mMapCenterOffsetX;
    private final int mMapCenterOffsetY;

    Projection(MapView mapView) {
        this(
                mapView.getZoomLevelDouble(), mapView.getIntrinsicScreenRect(null),
                mapView.getExpectedCenter(),
                mapView.getMapScrollX(), mapView.getMapScrollY(),
                mapView.getMapOrientation(),
                mapView.isHorizontalMapRepetitionEnabled(), mapView.isVerticalMapRepetitionEnabled(),
                MapView.getTileSystem(),
                mapView.getMapCenterOffsetX(),
                mapView.getMapCenterOffsetY());
    }

    /**
     * @since 6.0.0
     */
    public Projection(
            final double pZoomLevel, final Rect pScreenRect,
            final GeoPoint pCenter,
            final long pScrollX, final long pScrollY,
            final float pOrientation,
            final boolean pHorizontalWrapEnabled, final boolean pVerticalWrapEnabled,
            final TileSystem pTileSystem,
            final int pMapCenterOffsetX, final int pMapCenterOffsetY) {
        mMapCenterOffsetX = pMapCenterOffsetX;
        mMapCenterOffsetY = pMapCenterOffsetY;
        mZoomLevelProjection = pZoomLevel;
        horizontalWrapEnabled = pHorizontalWrapEnabled;
        verticalWrapEnabled = pVerticalWrapEnabled;
        mTileSystem = pTileSystem;
        mMercatorMapSize = TileSystem.MapSize(mZoomLevelProjection);
        mTileSize = TileSystem.getTileSize(mZoomLevelProjection);
        mIntrinsicScreenRectProjection = pScreenRect;
        final GeoPoint center = pCenter != null ? pCenter : new GeoPoint(0., 0);
        mScrollX = pScrollX;
        mScrollY = pScrollY;
        mOffsetX = getScreenCenterX() - mScrollX - mTileSystem.getMercatorXFromLongitude(center.getLongitude(), mMercatorMapSize, this.horizontalWrapEnabled);
        mOffsetY = getScreenCenterY() - mScrollY - mTileSystem.getMercatorYFromLatitude(center.getLatitude(), mMercatorMapSize, this.verticalWrapEnabled);
        mOrientation = pOrientation;
        mRotateAndScaleMatrix.preRotate(mOrientation, getScreenCenterX(), getScreenCenterY());
        mRotateAndScaleMatrix.invert(mUnrotateAndScaleMatrix);
        refresh();
    }

    /**
     * @since 6.1.0
     */
    public Projection(
            final double pZoomLevel, final int pWidth, final int pHeight,
            final GeoPoint pCenter,
            final float pOrientation,
            final boolean pHorizontalWrapEnabled, final boolean pVerticalWrapEnabled,
            final int pMapCenterOffsetX, final int pMapCenterOffsetY) {
        this(
                pZoomLevel, new Rect(0, 0, pWidth, pHeight),
                pCenter,
                0, 0,
                pOrientation,
                pHorizontalWrapEnabled, pVerticalWrapEnabled,
                MapView.getTileSystem(),
                pMapCenterOffsetX, pMapCenterOffsetY);
    }

    /**
     * @since 6.0.0
     */
    public Projection getOffspring(final double pZoomLevel, final Rect pScreenRect) {
        return new Projection(
                pZoomLevel, pScreenRect,
                mCurrentCenter, 0, 0,
                mOrientation,
                horizontalWrapEnabled, verticalWrapEnabled,
                mTileSystem,
                0, 0); // 0 looks like the most relevant value
    }

    public double getZoomLevel() {
        return mZoomLevelProjection;
    }

    public BoundingBox getBoundingBox() {
        return mBoundingBoxProjection;
    }

    public Rect getScreenRect() {
        return mScreenRectProjection;
    }

    public Rect getIntrinsicScreenRect() {
        return mIntrinsicScreenRectProjection;
    }

    @Override
    public IGeoPoint fromPixels(int x, int y) {
        return fromPixels(x, y, null, false);
    }

    /**
     * note: if {@link MapView#setHorizontalMapRepetitionEnabled(boolean)} or
     * {@link MapView#setVerticalMapRepetitionEnabled(boolean)} is false, then this
     * can return values that beyond the max extents of the world. This may or may not be
     * desired. <a href="https://github.com/osmdroid/osmdroid/pull/722">https://github.com/osmdroid/osmdroid/pull/722</a>
     * for more information and the discussion associated with this.
     *
     * @param pPixelX
     * @param pPixelY
     * @param pReuse
     * @return
     */
    public IGeoPoint fromPixels(final int pPixelX, final int pPixelY, final GeoPoint pReuse) {
        return fromPixels(pPixelX, pPixelY, pReuse, false);
    }

    /**
     * * note: if {@link MapView#setHorizontalMapRepetitionEnabled(boolean)} or
     * {@link MapView#setVerticalMapRepetitionEnabled(boolean)} is false, then this
     * can return values that beyond the max extents of the world. This may or may not be
     * desired. <a href="https://github.com/osmdroid/osmdroid/pull/722">https://github.com/osmdroid/osmdroid/pull/722</a>
     * for more information and the discussion associated with this.
     *
     * @param pPixelX
     * @param pPixelY
     * @param pReuse
     * @param forceWrap
     * @return
     */
    public IGeoPoint fromPixels(final int pPixelX, final int pPixelY, final GeoPoint pReuse, boolean forceWrap) {
        //reverting https://github.com/osmdroid/osmdroid/issues/459
        //due to relapse of https://github.com/osmdroid/osmdroid/issues/507
        //reverted functionality is now on the method fromPixelsRotationSensitive
        return mTileSystem.getGeoFromMercator(getCleanMercator(getMercatorXFromPixel(pPixelX), horizontalWrapEnabled),
                getCleanMercator(getMercatorYFromPixel(pPixelY), verticalWrapEnabled), mMercatorMapSize, pReuse,
                horizontalWrapEnabled || forceWrap, verticalWrapEnabled || forceWrap);
    }

    @Override
    public Point toPixels(final IGeoPoint in, final Point reuse) {
        return toPixels(in, reuse, false);
    }

    public Point toPixels(final IGeoPoint in, final Point reuse, boolean forceWrap) {
        final Point out = reuse != null ? reuse : new Point();
        out.x = TileSystem.truncateToInt(getLongPixelXFromLongitude(in.getLongitude(), forceWrap));
        out.y = TileSystem.truncateToInt(getLongPixelYFromLatitude(in.getLatitude(), forceWrap));
        return out;
    }

    /**
     * @since 6.0.0
     * TODO refactor
     */
    public long getLongPixelXFromLongitude(final double pLongitude, boolean forceWrap) {
        return getLongPixelXFromMercator(mTileSystem.getMercatorXFromLongitude(pLongitude, mMercatorMapSize, horizontalWrapEnabled || forceWrap), horizontalWrapEnabled);
    }

    /**
     * @since 6.0.0
     * TODO refactor
     */
    public long getLongPixelXFromLongitude(final double pLongitude) {
        return getLongPixelXFromMercator(mTileSystem.getMercatorXFromLongitude(pLongitude, mMercatorMapSize, false), false);
    }

    /**
     * @since 6.0.0
     * TODO refactor
     */
    public long getLongPixelYFromLatitude(final double pLatitude, boolean forceWrap) {
        return getLongPixelYFromMercator(mTileSystem.getMercatorYFromLatitude(pLatitude, mMercatorMapSize, verticalWrapEnabled || forceWrap), verticalWrapEnabled);
    }

    /**
     * @since 6.0.0
     * TODO refactor
     */
    public long getLongPixelYFromLatitude(final double pLatitude) {
        return getLongPixelYFromMercator(mTileSystem.getMercatorYFromLatitude(pLatitude, mMercatorMapSize, false), false);
    }

    /**
     * A wrapper for {@link #toProjectedPixels(double, double, PointL)}
     */
    public PointL toProjectedPixels(final GeoPoint geoPoint, final PointL reuse) {
        return toProjectedPixels(geoPoint.getLatitude(), geoPoint.getLongitude(), reuse);
    }

    /**
     * Performs only the first computationally heavy part of the projection. Call
     * {@link #getLongPixelsFromProjected(PointL, double, boolean, PointL)} to get the final position.
     *
     * @param latituteE6  the latitute of the point
     * @param longitudeE6 the longitude of the point
     * @param reuse       just pass null if you do not have a PointL to be 'recycled'.
     * @return intermediate value to be stored and passed to toMapPixelsTranslated.
     * @deprecated Use {@link #toProjectedPixels(double, double, PointL)} instead
     */
    @Deprecated
    public PointL toProjectedPixels(final long latituteE6, final long longitudeE6, final PointL reuse) {
        return toProjectedPixels(latituteE6 * 1E-6, longitudeE6 * 1E-6, reuse);
    }

    /**
     * Performs only the first computationally heavy part of the projection. Call
     * {@link #getLongPixelsFromProjected(PointL, double, boolean, PointL)} to get the final position.
     *
     * @param latitude  the latitute of the point
     * @param longitude the longitude of the point
     * @param reuse     just pass null if you do not have a PointL to be 'recycled'.
     * @return intermediate value to be stored and passed to toMapPixelsTranslated.
     */
    public PointL toProjectedPixels(final double latitude, final double longitude, final PointL reuse) {
        return toProjectedPixels(latitude, longitude, true, reuse);
    }

    /**
     * @since 6.0.0
     */
    public PointL toProjectedPixels(final double latitude, final double longitude, final boolean pWrapEnabled, final PointL reuse) {
        return mTileSystem.getMercatorFromGeo(latitude, longitude, mProjectedMapSize, reuse, pWrapEnabled);
    }

    /**
     * Performs the second computationally light part of the projection.
     *
     * @param in    the PointL calculated by the {@link #toProjectedPixels(double, double, PointL)}
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the coordinates of the initial GeoPoint passed to the
     * {@link #toProjectedPixels(double, double, PointL)}.
     * @deprecated Use {@link #getLongPixelsFromProjected(PointL, double, boolean, PointL)} instead
     */
    @Deprecated
    public Point toPixelsFromProjected(final PointL in, final Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        final double power = getProjectedPowerDifference();
        final PointL tmp = new PointL();
        getLongPixelsFromProjected(in, power, true, tmp);
        out.x = TileSystem.truncateToInt(tmp.x);
        out.y = TileSystem.truncateToInt(tmp.y);
        return out;
    }

    /**
     * @deprecated Use {@link #getLongPixelsFromProjected(PointL, double, boolean, PointL)} instead
     */
    @Deprecated
    public Point toPixelsFromMercator(final long pMercatorX, final long pMercatorY, final Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        out.x = TileSystem.truncateToInt(getLongPixelXFromMercator(pMercatorX, true));
        out.y = TileSystem.truncateToInt(getLongPixelYFromMercator(pMercatorY, true));
        return out;
    }

    public PointL toMercatorPixels(final int pPixelX, final int pPixelY, final PointL reuse) {
        final PointL out = reuse != null ? reuse : new PointL();
        out.x = getCleanMercator(getMercatorXFromPixel(pPixelX), horizontalWrapEnabled);
        out.y = getCleanMercator(getMercatorYFromPixel(pPixelY), verticalWrapEnabled);
        return out;
    }

    @Override
    public float metersToEquatorPixels(final float meters) {
        return metersToPixels(meters, 0, mZoomLevelProjection);
    }

    /**
     * Converts a distance in meters to one in (horizontal) pixels at the current zoomlevel and at
     * the current latitude at the center of the screen.
     *
     * @param meters the distance in meters
     * @return The number of pixels corresponding to the distance, if measured at the center of the
     * screen, at the current zoom level. The return value may only be approximate.
     */
    public float metersToPixels(final float meters) {
        return metersToPixels(meters, getBoundingBox().getCenterWithDateLine().getLatitude(), mZoomLevelProjection);
    }

    /**
     * @since 6.0
     */
    public float metersToPixels(final float meters, final double latitude, final double zoomLevel) {
        return (float) (meters / TileSystem.GroundResolution(latitude, zoomLevel));
    }

    @Override
    public IGeoPoint getNorthEast() {
        return fromPixels(mIntrinsicScreenRectProjection.right, mIntrinsicScreenRectProjection.top, null, true);
    }

    @Override
    public IGeoPoint getSouthWest() {
        return fromPixels(mIntrinsicScreenRectProjection.left, mIntrinsicScreenRectProjection.bottom, null, true);
    }

    /**
     * This will provide a Matrix that will revert the current map's scaling and rotation. This can
     * be useful when drawing to a fixed location on the screen.
     */
    public Matrix getInvertedScaleRotateCanvasMatrix() {
        return mUnrotateAndScaleMatrix;
    }

    /**
     * This will revert the current map's scaling and rotation for a point. This can be useful when
     * drawing to a fixed location on the screen.
     */
    public Point unrotateAndScalePoint(int x, int y, Point reuse) {
        return applyMatrixToPoint(x, y, reuse, mUnrotateAndScaleMatrix, mOrientation != 0);
    }

    /**
     * This will apply the current map's scaling and rotation for a point. This can be useful when
     * converting MotionEvents to a screen point.
     */
    public Point rotateAndScalePoint(int x, int y, Point reuse) {
        return applyMatrixToPoint(x, y, reuse, mRotateAndScaleMatrix, mOrientation != 0);
    }

    /**
     * @since 6.0.0
     */
    private Point applyMatrixToPoint(final int pX, final int pY, final Point reuse, final Matrix pMatrix, final boolean pCondition) {
        final Point out = reuse != null ? reuse : new Point();
        if (pCondition) {
            mRotateScalePoints[0] = pX;
            mRotateScalePoints[1] = pY;
            pMatrix.mapPoints(mRotateScalePoints);
            out.x = (int) mRotateScalePoints[0];
            out.y = (int) mRotateScalePoints[1];
        } else {
            out.x = pX;
            out.y = pY;
        }
        return out;
    }

    /**
     * @since 5.6
     */
    public void detach() {
    }

    /**
     * @since 6.0.0
     */
    public Rect getPixelFromTile(final int pTileX, final int pTileY, final Rect pReuse) {
        final Rect out = pReuse != null ? pReuse : new Rect();
        out.left = TileSystem.truncateToInt(getLongPixelXFromMercator(getMercatorFromTile(pTileX), false));
        out.top = TileSystem.truncateToInt(getLongPixelYFromMercator(getMercatorFromTile(pTileY), false));
        out.right = TileSystem.truncateToInt(getLongPixelXFromMercator(getMercatorFromTile(pTileX + 1), false));
        out.bottom = TileSystem.truncateToInt(getLongPixelYFromMercator(getMercatorFromTile(pTileY + 1), false));
        return out;
    }

    /**
     * @since 6.0.0
     */
    public long getMercatorFromTile(final int pTile) {
        return TileSystem.getMercatorFromTile(pTile, mTileSize);
    }

    /**
     * This will provide a Matrix that will revert the current map's scaling and rotation. This can
     * be useful when drawing to a fixed location on the screen.
     */
    public Matrix getScaleRotateCanvasMatrix() {
        return mRotateAndScaleMatrix;
    }

    /**
     * @since 6.0.0
     */
    public double getProjectedPowerDifference() {
        return mProjectedMapSize / getWorldMapSize();
    }

    /**
     * @since 6.0.0
     * @deprecated Use {@link #getLongPixelsFromProjected(PointL, double, boolean, PointL)} instead
     */
    @Deprecated
    public Point getPixelsFromProjected(final PointL in, final double powerDifference, final Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        final PointL tmp = new PointL();
        getLongPixelsFromProjected(in, powerDifference, true, tmp);
        out.x = TileSystem.truncateToInt(tmp.x);
        out.y = TileSystem.truncateToInt(tmp.y);
        return out;
    }

    /**
     * @param in              Input point: a geo point projected to the map with the largest zoom level (aka "projected" map)
     * @param powerDifference Factor between the large "projected" map and the wanted projection zoom level
     * @param pCloser         "Should we move the resulting point - modulo the map size - so that it's
     *                        as close to the screen limits as possible?"
     * @since 6.0.0
     */
    public PointL getLongPixelsFromProjected(final PointL in, final double powerDifference, final boolean pCloser, final PointL reuse) {
        final PointL out = reuse != null ? reuse : new PointL();
        out.x = getLongPixelXFromMercator((long) (in.x / powerDifference), pCloser);
        out.y = getLongPixelYFromMercator((long) (in.y / powerDifference), pCloser);
        return out;
    }

    /**
     * @since 6.0.0
     * Correction of pixel value.
     * Pixel values are identical, modulo mapSize.
     * What we explicitly want is either:
     * * the visible pixel that is the closest to the left (first choice)
     * * the invisible pixel that is the closest to the screen center
     */
    private long getCloserPixel(long pPixel, final int pScreenLimitFirst, final int pScreenLimitLast, final double pMapSize) {
        final long center = (pScreenLimitFirst + pScreenLimitLast) / 2;
        long previous = 0;
        if (pPixel < pScreenLimitFirst) {
            while (pPixel < pScreenLimitFirst) {
                previous = pPixel;
                pPixel += pMapSize;
            }
            if (pPixel < pScreenLimitLast) {
                return pPixel;
            }
            if (Math.abs(center - pPixel) < Math.abs(center - previous)) {
                return pPixel;
            }
            return previous;
        }

        while (pPixel >= pScreenLimitFirst) {
            previous = pPixel;
            pPixel -= pMapSize;
        }
        if (previous < pScreenLimitLast) {
            return previous;
        }
        if (Math.abs(center - pPixel) < Math.abs(center - previous)) {
            return pPixel;
        }
        return previous;
    }

    /**
     * @since 6.0.0
     */
    private long getLongPixelXFromMercator(final long pMercatorX, final boolean pCloser) {
        return getLongPixelFromMercator(pMercatorX, pCloser, mOffsetX, mIntrinsicScreenRectProjection.left, mIntrinsicScreenRectProjection.right);
    }

    /**
     * @since 6.0.0
     */
    private long getLongPixelYFromMercator(final long pMercatorY, final boolean pCloser) {
        return getLongPixelFromMercator(pMercatorY, pCloser, mOffsetY, mIntrinsicScreenRectProjection.top, mIntrinsicScreenRectProjection.bottom);
    }

    /**
     * @since 6.0.0
     */
    private long getLongPixelFromMercator(final long pMercator, final boolean pCloser, final long pOffset, final int pScreenLimitFirst, final int pScreenLimitLast) {
        long result = pMercator + pOffset;
        if (pCloser) {
            result = getCloserPixel(result, pScreenLimitFirst, pScreenLimitLast, mMercatorMapSize);
        }
        return result;
    }

    /**
     * @since 6.0.0
     */
    public int getTileFromMercator(final long pMercator) {
        return TileSystem.getTileFromMercator(pMercator, mTileSize);
    }

    /**
     * @since 6.0.0
     */
    public RectL getMercatorViewPort(final RectL pReuse) {
        final RectL out = pReuse != null ? pReuse : new RectL();

        // in the standard case, that's all we need: the screen rect corners
        float left = mIntrinsicScreenRectProjection.left;
        float right = mIntrinsicScreenRectProjection.right;
        float top = mIntrinsicScreenRectProjection.top;
        float bottom = mIntrinsicScreenRectProjection.bottom;

        // sometimes we need to expand beyond in order to get all visible tiles
        if (mOrientation != 0) {
            final float scaleRotatePoints[] = new float[8];
            scaleRotatePoints[0] = mIntrinsicScreenRectProjection.left;
            scaleRotatePoints[1] = mIntrinsicScreenRectProjection.top;
            scaleRotatePoints[2] = mIntrinsicScreenRectProjection.right;
            scaleRotatePoints[3] = mIntrinsicScreenRectProjection.bottom;
            scaleRotatePoints[4] = mIntrinsicScreenRectProjection.left;
            scaleRotatePoints[5] = mIntrinsicScreenRectProjection.bottom;
            scaleRotatePoints[6] = mIntrinsicScreenRectProjection.right;
            scaleRotatePoints[7] = mIntrinsicScreenRectProjection.top;
            mUnrotateAndScaleMatrix.mapPoints(scaleRotatePoints);

            for (int i = 0; i < 8; i += 2) {
                if (left > scaleRotatePoints[i]) {
                    left = scaleRotatePoints[i];
                }
                if (right < scaleRotatePoints[i]) {
                    right = scaleRotatePoints[i];
                }
                if (top > scaleRotatePoints[i + 1]) {
                    top = scaleRotatePoints[i + 1];
                }
                if (bottom < scaleRotatePoints[i + 1]) {
                    bottom = scaleRotatePoints[i + 1];
                }
            }
        }

        out.left = getMercatorXFromPixel((int) left);
        out.top = getMercatorYFromPixel((int) top);
        out.right = getMercatorXFromPixel((int) right);
        out.bottom = getMercatorYFromPixel((int) bottom);
        return out;
    }

    /**
     * @since 6.0.0
     */
    public int getScreenCenterX() {
        return (mIntrinsicScreenRectProjection.right + mIntrinsicScreenRectProjection.left) / 2 + mMapCenterOffsetX;
    }

    /**
     * @since 6.0.0
     */
    public int getScreenCenterY() {
        return (mIntrinsicScreenRectProjection.bottom + mIntrinsicScreenRectProjection.top) / 2 + mMapCenterOffsetY;
    }

    /**
     * @since 6.0.0
     */
    public long getMercatorXFromPixel(final int pPixelX) {
        return pPixelX - mOffsetX;
    }

    /**
     * @since 6.0.0
     */
    public long getMercatorYFromPixel(final int pPixelY) {
        return pPixelY - mOffsetY;
    }

    /**
     * @since 6.0.0
     */
    public long getCleanMercator(final long pMercator, final boolean wrapEnabled) {
        return mTileSystem.getCleanMercator(pMercator, mMercatorMapSize, wrapEnabled);
    }

    /**
     * @since 6.0.0
     */
    public GeoPoint getCurrentCenter() {
        return mCurrentCenter;
    }

    public long getOffsetX() {
        return mOffsetX;
    }

    public long getOffsetY() {
        return mOffsetY;
    }

    /**
     * @since 6.0.0
     */
    public void save(final Canvas pCanvas, final boolean pMapRotation, final boolean pForce) {
        if (mOrientation != 0 || pForce) {
            pCanvas.save();
            pCanvas.concat(pMapRotation ? mRotateAndScaleMatrix : mUnrotateAndScaleMatrix);
        }
    }

    /**
     * @since 6.0.0
     */
    public void restore(final Canvas pCanvas, final boolean pForce) {
        if (mOrientation != 0 || pForce) {
            pCanvas.restore();
        }
    }

    /**
     * @since 6.0.0
     */
    private void refresh() {
        // of course we could write mIntrinsicScreenRectProjection.centerX() and centerY()
        // but we should keep writing it that way (cf. ProjectionTest)
        fromPixels(getScreenCenterX(), getScreenCenterY(), mCurrentCenter);

        if (mOrientation != 0 && mOrientation != 180) {
            GeometryMath.getBoundingBoxForRotatatedRectangle(
                    mIntrinsicScreenRectProjection, getScreenCenterX(), getScreenCenterY(),
                    mOrientation, mScreenRectProjection);
        } else {
            // of course we could write mScreenRectProjection.set(mIntrinsicScreenRectProjection);
            // but we should keep writing it that way (cf. ProjectionTest)
            mScreenRectProjection.left = mIntrinsicScreenRectProjection.left;
            mScreenRectProjection.top = mIntrinsicScreenRectProjection.top;
            mScreenRectProjection.right = mIntrinsicScreenRectProjection.right;
            mScreenRectProjection.bottom = mIntrinsicScreenRectProjection.bottom;
        }

        IGeoPoint neGeoPoint = fromPixels(
                mScreenRectProjection.right, mScreenRectProjection.top, null, true);
        final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
        if (neGeoPoint.getLatitude() > tileSystem.getMaxLatitude()) {
            neGeoPoint = new GeoPoint(tileSystem.getMaxLatitude(), neGeoPoint.getLongitude());
        }
        if (neGeoPoint.getLatitude() < tileSystem.getMinLatitude()) {
            neGeoPoint = new GeoPoint(tileSystem.getMinLatitude(), neGeoPoint.getLongitude());
        }
        IGeoPoint swGeoPoint = fromPixels(
                mScreenRectProjection.left, mScreenRectProjection.bottom, null, true);
        if (swGeoPoint.getLatitude() > tileSystem.getMaxLatitude()) {
            swGeoPoint = new GeoPoint(tileSystem.getMaxLatitude(), swGeoPoint.getLongitude());
        }
        if (swGeoPoint.getLatitude() < tileSystem.getMinLatitude()) {
            swGeoPoint = new GeoPoint(tileSystem.getMinLatitude(), swGeoPoint.getLongitude());
        }

        mBoundingBoxProjection.set(
                neGeoPoint.getLatitude(), neGeoPoint.getLongitude(),
                swGeoPoint.getLatitude(), swGeoPoint.getLongitude());
    }

    /**
     * Adjust the offsets so that this geo point projects into that pixel
     *
     * @since 6.0.0
     */
    public void adjustOffsets(final IGeoPoint pGeoPoint, final PointF pPixel) {
        if (pPixel == null) {
            return;
        }
        if (pGeoPoint == null) {
            return;
        }
        final Point unRotatedExpectedPixel = unrotateAndScalePoint((int) pPixel.x, (int) pPixel.y, null);
        final Point unRotatedActualPixel = toPixels(pGeoPoint, null);
        final long deltaX = unRotatedExpectedPixel.x - unRotatedActualPixel.x;
        final long deltaY = unRotatedExpectedPixel.y - unRotatedActualPixel.y;
        adjustOffsets(deltaX, deltaY);
    }

    /**
     * Adjust the offsets so that
     * either this bounding box is bigger than the screen and contains it
     * or it is smaller and it is centered
     *
     * @since 6.0.0
     * @deprecated Use {@link #adjustOffsets(double, double, boolean, int)} instead
     */
    @Deprecated
    public void adjustOffsets(final BoundingBox pBoundingBox) {
        if (pBoundingBox == null) {
            return;
        }
        adjustOffsets(pBoundingBox.getLonWest(), pBoundingBox.getLonEast(), false, 0);
        adjustOffsets(pBoundingBox.getActualNorth(), pBoundingBox.getActualSouth(), true, 0);
    }

    /**
     * Adjust offsets so that north and south (if latitude, west and east if longitude)
     * actually "fit" into the screen, with a tolerance of extraSize pixels.
     * Used in order to ensure scroll limits.
     *
     * @since 6.0.0
     */
    void adjustOffsets(final double pNorthOrWest, final double pSouthOrEast,
                       final boolean isLatitude, final int pExtraSize) {
        final long min;
        final long max;
        final long deltaX;
        final long deltaY;
        if (isLatitude) {
            min = getLongPixelYFromLatitude(pNorthOrWest);
            max = getLongPixelYFromLatitude(pSouthOrEast);
            deltaX = 0;
            deltaY = getScrollableOffset(min, max, mMercatorMapSize, mIntrinsicScreenRectProjection.height(), pExtraSize);
        } else {
            min = getLongPixelXFromLongitude(pNorthOrWest);
            max = getLongPixelXFromLongitude(pSouthOrEast);
            deltaX = getScrollableOffset(min, max, mMercatorMapSize, mIntrinsicScreenRectProjection.width(), pExtraSize);
            deltaY = 0;
        }
        adjustOffsets(deltaX, deltaY);
    }

    /**
     * @since 6.0.0
     */
    void adjustOffsets(final long pDeltaX, final long pDeltaY) {
        if (pDeltaX == 0 && pDeltaY == 0) {
            return;
        }
        mOffsetX += pDeltaX;
        mOffsetY += pDeltaY;
        mScrollX -= pDeltaX;
        mScrollY -= pDeltaY;
        refresh();
    }

    /**
     * @param pPixelMin   Pixel position of the limit (left)
     * @param pPixelMax   Pixel position of the limit (right)
     * @param pWorldSize  World map size - for modulo adjustments
     * @param pScreenSize Screen size
     * @param pExtraSize  Extra size to consider at each side of the screen
     * @return the offset to apply so that the limits are within the screen
     * @since 6.0.0
     */
    public static long getScrollableOffset(final long pPixelMin, long pPixelMax,
                                           final double pWorldSize,
                                           final int pScreenSize, final int pExtraSize) {
        while (pPixelMax - pPixelMin < 0) { // date line + several worlds fix
            pPixelMax += pWorldSize;
        }

        long delta;
        if (pPixelMax - pPixelMin < pScreenSize - 2 * pExtraSize) {
            final long half = (pPixelMax - pPixelMin) / 2;
            if ((delta = pScreenSize / 2 - half - pPixelMin) > 0) {
                return delta;
            }
            if ((delta = pScreenSize / 2 + half - pPixelMax) < 0) {
                return delta;
            }
            return 0;
        }
        if ((delta = pExtraSize - pPixelMin) < 0) {
            return delta;
        }
        if ((delta = pScreenSize - pExtraSize - pPixelMax) > 0) {
            return delta;
        }
        return 0;
    }

    /**
     * @since 6.0.0
     */
    boolean setMapScroll(final MapView pMapView) {
        if (pMapView.getMapScrollX() == mScrollX && pMapView.getMapScrollY() == mScrollY) {
            return false;
        }
        pMapView.setMapScroll(mScrollX, mScrollY);
        return true;
    }

    /**
     * @since 6.1.0
     */
    public boolean isHorizontalWrapEnabled() {
        return horizontalWrapEnabled;
    }

    /**
     * @since 6.1.0
     */
    public boolean isVerticalWrapEnabled() {
        return verticalWrapEnabled;
    }

    /**
     * @since 6.1.0
     */
    public float getOrientation() {
        return mOrientation;
    }

    /**
     * @since 6.1.0
     */
    public int getWidth() {
        return mIntrinsicScreenRectProjection.width();
    }

    /**
     * @since 6.1.0
     */
    public int getHeight() {
        return mIntrinsicScreenRectProjection.height();
    }

    /**
     * @since 6.2.0
     */
    public double getWorldMapSize() {
        return mMercatorMapSize;
    }
}

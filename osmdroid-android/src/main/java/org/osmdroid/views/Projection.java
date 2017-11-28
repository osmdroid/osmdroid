package org.osmdroid.views;


import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.GeometryMath;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.TileSystem;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

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

	private final double mProjectedMapSize = TileSystem.MapSize((double)microsoft.mappoint.TileSystem.projectionZoomLevel);
	private final long mOffsetX;
	private final long mOffsetY;
	private final float mMultiTouchScale;
	private final PointF mMultiTouchScalePoint;

	private final Matrix mRotateAndScaleMatrix = new Matrix();
	private final Matrix mUnrotateAndScaleMatrix = new Matrix();
	private final Matrix mScaleMatrix = new Matrix();
	private final float[] mRotateScalePoints = new float[2];

	private final BoundingBox mBoundingBoxProjection;
	private final double mZoomLevelProjection;
	private final Rect mScreenRectProjection;
	private final Rect mIntrinsicScreenRectProjection;

	private boolean wrapEnabled;

	private final double mMercatorMapSize;
	private final double mTileSize;
	private final float mOrientation;
	private final GeoPoint mCurrentCenter;

	Projection(MapView mapView) {
		this(
				mapView.getZoomLevel(false), mapView.getIntrinsicScreenRect(null),
				mapView.getCenter(),
				mapView.getMapScrollX(), mapView.getMapScrollY(),
				mapView.getMapOrientation(), mapView.getMapScale(),
				mapView.mMultiTouchScalePoint, mapView.isMapRepetitionEnabled());
	}

	/**
	 * @since 5.6.6
	 */
	public Projection(
			final double pZoomLevel, final Rect pScreenRect,
			final GeoPoint pCenter,
			final long pScrollX, final long pScrollY,
			final float pOrientation, final float pScale, final PointF pMultiTouchScalePoint, boolean wrapEnabled) {
		mZoomLevelProjection = pZoomLevel;
		this.wrapEnabled = wrapEnabled;
		mMercatorMapSize = TileSystem.MapSize(mZoomLevelProjection);
		mTileSize = TileSystem.getTileSize(mZoomLevelProjection);
		mIntrinsicScreenRectProjection = pScreenRect;
		final GeoPoint center = pCenter != null ? pCenter : new GeoPoint(0., 0);
		mOffsetX = getScreenCenterX() - pScrollX - TileSystem.getMercatorXFromLongitude(center.getLongitude(), mMercatorMapSize, this.wrapEnabled);
		mOffsetY = getScreenCenterY() - pScrollY - TileSystem.getMercatorYFromLatitude(center.getLatitude(), mMercatorMapSize, this.wrapEnabled);
		final IGeoPoint neGeoPoint = fromPixels(pScreenRect.right, pScreenRect.top, null, true);
		final IGeoPoint swGeoPoint = fromPixels(pScreenRect.left, pScreenRect.bottom, null, true);
		mBoundingBoxProjection = new BoundingBox(
				neGeoPoint.getLatitude(), neGeoPoint.getLongitude(),
				swGeoPoint.getLatitude(), swGeoPoint.getLongitude());
		mOrientation = pOrientation;
		mMultiTouchScale = pScale;
		mMultiTouchScalePoint = pMultiTouchScalePoint;
		mRotateAndScaleMatrix.preScale(mMultiTouchScale, mMultiTouchScale, mMultiTouchScalePoint.x, mMultiTouchScalePoint.y);
		mRotateAndScaleMatrix.preRotate(mOrientation, getScreenCenterX(), getScreenCenterY());
		mRotateAndScaleMatrix.invert(mUnrotateAndScaleMatrix);
		mScaleMatrix.preScale(mMultiTouchScale, mMultiTouchScale, mMultiTouchScalePoint.x, mMultiTouchScalePoint.y);
		mScaleMatrix.preRotate(0, getScreenCenterX(), getScreenCenterY());
		mScreenRectProjection = new Rect();
		mScreenRectProjection.left = mIntrinsicScreenRectProjection.left;
		mScreenRectProjection.top = mIntrinsicScreenRectProjection.top;
		mScreenRectProjection.right = mIntrinsicScreenRectProjection.right;
		mScreenRectProjection.bottom = mIntrinsicScreenRectProjection.bottom;
		if (mOrientation != 0 && mOrientation != 180) {
			GeometryMath.getBoundingBoxForRotatatedRectangle(mScreenRectProjection, getScreenCenterX(), getScreenCenterY(),
					mOrientation, mScreenRectProjection);
		}
		final Point newCenter = unrotateAndScalePoint(
				mIntrinsicScreenRectProjection.centerX(),
				mIntrinsicScreenRectProjection.centerY(), null);
		mCurrentCenter = (GeoPoint)fromPixels(newCenter.x, newCenter.y, null);
	}

	/**
	 * @since 5.6.6
	 */
	public Projection getOffspring(final double pZoomLevel, final Rect pScreenRect) {
		return new Projection(
				pZoomLevel, pScreenRect,
				(GeoPoint)fromPixels(getScreenCenterX(), getScreenCenterY()), 0, 0,
				mOrientation, mMultiTouchScale, mMultiTouchScalePoint, wrapEnabled);
	}

	public double getZoomLevel() {
		return mZoomLevelProjection;
	}

	public BoundingBox getBoundingBox() {
		return mBoundingBoxProjection;
	}
     
     @Deprecated
     public BoundingBoxE6 getBoundingBoxE6() {
          BoundingBoxE6 x = new BoundingBoxE6(mBoundingBoxProjection.getLatNorth(),
               mBoundingBoxProjection.getLonEast(), mBoundingBoxProjection.getLatSouth(),
               mBoundingBoxProjection.getLonWest());
		return x;
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
	 * note: if {@link MapView#setMapRepetitionEnabled} is false, then this
	 * can return values that beyond the max extents of the world. This may or may not be
	 * desired. <a href="https://github.com/osmdroid/osmdroid/pull/722">https://github.com/osmdroid/osmdroid/pull/722</a>
	 * for more information and the discussion associated with this.
	 * @param pPixelX
	 * @param pPixelY
	 * @param pReuse
	 * @return
	 */
	public IGeoPoint fromPixels(final int pPixelX, final int pPixelY, final GeoPoint pReuse) {
		return this.fromPixels(pPixelX, pPixelY, pReuse, false);
	}

	/**
	 *  * note: if {@link MapView#setMapRepetitionEnabled} is false, then this
	 * can return values that beyond the max extents of the world. This may or may not be
	 * desired. <a href="https://github.com/osmdroid/osmdroid/pull/722">https://github.com/osmdroid/osmdroid/pull/722</a>
	 * for more information and the discussion associated with this.
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
		return TileSystem.getGeoFromMercator(getCleanMercator(getMercatorXFromPixel(pPixelX)),
						getCleanMercator(getMercatorYFromPixel(pPixelY)), mMercatorMapSize, pReuse, wrapEnabled || forceWrap);
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
	 */
	public long getLongPixelXFromLongitude(final double pLongitude, boolean forceWrap) {
		return getLongPixelXFromMercator(TileSystem.getMercatorXFromLongitude(pLongitude, mMercatorMapSize, wrapEnabled || forceWrap), wrapEnabled);
	}

	/**
	 * @since 6.0.0
	 */
	public long getLongPixelYFromLatitude(final double pLatitude, boolean forceWrap) {
		return getLongPixelYFromMercator(TileSystem.getMercatorYFromLatitude(pLatitude, mMercatorMapSize, wrapEnabled || forceWrap), wrapEnabled);
	}

	/**
	 * A wrapper for {@link #toProjectedPixels(long, long, PointL)}
	 */
	public PointL toProjectedPixels(final GeoPoint geoPoint, final PointL reuse) {
		return toProjectedPixels(geoPoint.getLatitude(), geoPoint.getLongitude(), reuse);
	}

	/**
	 * Performs only the first computationally heavy part of the projection. Call
	 * {@link #toPixelsFromProjected(PointL, Point)} to get the final position.
	 * 
	 * @param latituteE6
	 *            the latitute of the point
	 * @param longitudeE6
	 *            the longitude of the point
	 * @param reuse
	 *            just pass null if you do not have a PointL to be 'recycled'.
	 * @return intermediate value to be stored and passed to toMapPixelsTranslated.
	 */
	public PointL toProjectedPixels(final long latituteE6, final long longitudeE6, final PointL reuse) {
		return toProjectedPixels(latituteE6 * 1E-6, longitudeE6 * 1E-6, reuse);
	}

    /**
     * Performs only the first computationally heavy part of the projection. Call
     * {@link #toPixelsFromProjected(PointL, Point)} to get the final position.
     *
     * @param latitude
     *            the latitute of the point
     * @param longitude
     *            the longitude of the point
     * @param reuse
     *            just pass null if you do not have a PointL to be 'recycled'.
     * @return intermediate value to be stored and passed to toMapPixelsTranslated.
     */
    public PointL toProjectedPixels(final double latitude, final double longitude, final PointL reuse) {
        return TileSystem.getMercatorFromGeo(latitude, longitude, mProjectedMapSize, reuse, true);
    }

	/**
	 * Performs the second computationally light part of the projection.
	 * 
	 * @param in
	 *            the Point calculated by the {@link #toProjectedPixels(long, long, PointL)}
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 * @return the Point containing the coordinates of the initial GeoPoint passed to the
	 *         {@link #toProjectedPixels(long, long, PointL)}.
	 */
	@Deprecated
	public Point toPixelsFromProjected(final PointL in, final Point reuse) {
		final Point out = reuse != null ? reuse : new Point();
		final double power = getProjectedPowerDifference();
		out.x = getPixelXFromMercator((long) (in.x / power), true);
		out.y = getPixelYFromMercator((long) (in.y / power), true);
		return out;
	}

	public Point toPixelsFromMercator(final long pMercatorX, final long pMercatorY, final Point reuse) {
		final Point out = reuse != null ? reuse : new Point();
		out.x = getPixelXFromMercator(pMercatorX, true);
		out.y = getPixelYFromMercator(pMercatorY, true);
		return out;
	}

	public PointL toMercatorPixels(final int pPixelX, final int pPixelY, final PointL reuse) {
		final PointL out = reuse != null ? reuse : new PointL();
		out.x = getCleanMercator(getMercatorXFromPixel(pPixelX));
		out.y = getCleanMercator(getMercatorYFromPixel(pPixelY));
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
	 * @param meters
	 *            the distance in meters
	 * @return The number of pixels corresponding to the distance, if measured at the center of the
	 *         screen, at the current zoom level. The return value may only be approximate.
	 */
	public float metersToPixels(final float meters) {
		return metersToPixels(meters, getBoundingBox().getCenterWithDateLine().getLatitude(), mZoomLevelProjection);
	}

	/**
	 * @since 6.0
	 */
	public static float metersToPixels(final float meters, final double latitude, final double zoomLevel) {
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
		return applyMatrixToPoint(x, y, reuse,
				mUnrotateAndScaleMatrix, mOrientation != 0 || mMultiTouchScale != 1.0f);
	}

	/**
	 * This will apply the current map's scaling and rotation for a point. This can be useful when
	 * converting MotionEvents to a screen point.
	 */
	public Point rotateAndScalePoint(int x, int y, Point reuse) {
		return applyMatrixToPoint(x, y, reuse,
				mRotateAndScaleMatrix, mOrientation != 0 || mMultiTouchScale != 1.0f);
	}

	/**
	 * @since 6.0.0
	 */
	public Point scalePoint(int x, int y, Point reuse) {
		return applyMatrixToPoint(x, y, reuse,
				mScaleMatrix, mMultiTouchScale != 1.0f);
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
	public void detach(){
	}

	/**
	 * @since 5.6.6
	 */
	public Rect getPixelFromTile(final int pTileX, final int pTileY, final Rect pReuse) {
		final Rect out = pReuse != null ? pReuse : new Rect();
		out.left = getPixelXFromMercator(getMercatorFromTile(pTileX), false);
		out.top = getPixelYFromMercator(getMercatorFromTile(pTileY), false);
		out.right = getPixelXFromMercator(getMercatorFromTile(pTileX + 1), false);
		out.bottom = getPixelYFromMercator(getMercatorFromTile(pTileY + 1), false);
		return out;
	}

	/**
	 * @since 5.6.6
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
	 * @since 5.6.6
	 */
	public double getProjectedPowerDifference() {
		final double zoomDifference = microsoft.mappoint.TileSystem.projectionZoomLevel - getZoomLevel();
		return TileSystem.getFactor(zoomDifference);
	}

	/**
	 * @since 5.6.6
	 */
	public Point getPixelsFromProjected(final PointL in, final double powerDifference, final Point reuse) {
		final Point out = reuse != null ? reuse : new Point();
		out.x = getPixelXFromMercator((long) (in.x / powerDifference), true);
		out.y = getPixelYFromMercator((long) (in.y / powerDifference), true);
		return out;
	}

	/**
     * @since 6.0.0
     */
    public PointL getLongPixelsFromProjected(final PointL in, final double powerDifference, final boolean pCloser, final PointL reuse) {
        final PointL out = reuse != null ? reuse : new PointL();
        out.x = getLongPixelXFromMercator((long) (in.x / powerDifference), pCloser);
        out.y = getLongPixelYFromMercator((long) (in.y / powerDifference), pCloser);
        return out;
    }

	/**
	 * @since 5.6.6
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
	 * @since 5.6.6
	 */
	private int getPixelXFromMercator(final long pMercatorX, final boolean pCloser) {
		return getPixelFromMercator(pMercatorX, pCloser, mOffsetX, mIntrinsicScreenRectProjection.left, mIntrinsicScreenRectProjection.right);
	}

	/**
	 * @since 5.6.6
	 */
	private int getPixelYFromMercator(final long pMercatorY, final boolean pCloser) {
		return getPixelFromMercator(pMercatorY, pCloser, mOffsetY, mIntrinsicScreenRectProjection.top, mIntrinsicScreenRectProjection.bottom);
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
	 * @since 5.6.6
	 */
	private int getPixelFromMercator(final long pMercator, final boolean pCloser, final long pOffset, final int pScreenLimitFirst, final int pScreenLimitLast) {
		return TileSystem.truncateToInt(getLongPixelFromMercator(pMercator, pCloser, pOffset, pScreenLimitFirst, pScreenLimitLast));
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
	 * @since 5.6.6
	 */
	public int getTileFromMercator(final long pMercator) {
		return TileSystem.getTileFromMercator(pMercator, mTileSize);
	}

	/**
	 * @since 5.6.6
	 */
	public RectL getMercatorViewPort(final RectL pReuse) {
		final RectL out = pReuse != null ? pReuse : new RectL();

		// in the standard case, that's all we need: the screen rect corners
		float left = mIntrinsicScreenRectProjection.left;
		float right = mIntrinsicScreenRectProjection.right;
		float top = mIntrinsicScreenRectProjection.top;
		float bottom = mIntrinsicScreenRectProjection.bottom;

		// sometimes we need to expand beyond in order to get all visible tiles
		if (mOrientation != 0 || mMultiTouchScale != 1) {
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

		out.left = getMercatorXFromPixel((int)left);
		out.top = getMercatorYFromPixel((int)top);
		out.right = getMercatorXFromPixel((int)right);
		out.bottom = getMercatorYFromPixel((int)bottom);
		return out;
	}

	/**
	 * @since 5.6.6
	 */
	private int getScreenCenterX() {
		return (mIntrinsicScreenRectProjection.right + mIntrinsicScreenRectProjection.left) / 2;
	}

	/**
	 * @since 5.6.6
	 */
	private int getScreenCenterY() {
		return (mIntrinsicScreenRectProjection.bottom + mIntrinsicScreenRectProjection.top) / 2;
	}

	/**
	 * @since 5.6.6
	 */
	private long getMercatorXFromPixel(final int pPixelX) {
		return pPixelX - mOffsetX;
	}

	/**
	 * @since 5.6.6
	 */
	private long getMercatorYFromPixel(final int pPixelY) {
		return pPixelY - mOffsetY;
	}

	/**
	 * @since 5.6.6
	 */
	private long getCleanMercator(final long pMercator) {
		return TileSystem.getCleanMercator(pMercator, mMercatorMapSize, wrapEnabled);
	}

	/**
	 * @since 6.0.0
	 */
	public GeoPoint getCurrentCenter() {
		return mCurrentCenter;
	}
}

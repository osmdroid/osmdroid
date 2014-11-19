package org.osmdroid.views;


import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * A Projection serves to translate between the coordinate system of x/y on-screen pixel coordinates
 * and that of latitude/longitude points on the surface of the earth. You obtain a Projection from
 * MapView.getProjection(). You should not hold on to this object for more than one draw, since the
 * projection of the map could change. <br />
 * <br />
 * <b>Note:</b> This class will "wrap" all pixel and lat/long values that overflow their bounds
 * (rather than clamping to their bounds).
 * 
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 */
public class Projection implements IProjection, MapViewConstants {

	private final int mMapViewWidth;
	private final int mMapViewHeight;
	// The offsets will take us from the MapView's current coordinate system
	// to a 0,0 coordinate system
	protected final int mOffsetX;
	protected final int mOffsetY;
	protected final float mMultiTouchScale;

	private final Matrix mRotateAndScaleMatrix = new Matrix();
	private final Matrix mUnrotateAndScaleMatrix = new Matrix();
	private final float[] mRotateScalePoints = new float[2];

	private final BoundingBox mBoundingBoxProjection;
	private final int mZoomLevelProjection;
	private final Rect mScreenRectProjection;
	private final Rect mIntrinsicScreenRectProjection;
	private final float mMapOrientation;

	Projection(MapView mapView) {

		mZoomLevelProjection = mapView.getZoomLevel(false);
		mScreenRectProjection = mapView.getScreenRect(null);
		mIntrinsicScreenRectProjection = mapView.getIntrinsicScreenRect(null);
		mMapOrientation = mapView.getMapOrientation();

		mMapViewWidth = mapView.getWidth();
		mMapViewHeight = mapView.getHeight();
		mOffsetX = -mapView.getScrollX();
		mOffsetY = -mapView.getScrollY();

		mRotateAndScaleMatrix.set(mapView.mRotateScaleMatrix);
		mRotateAndScaleMatrix.invert(mUnrotateAndScaleMatrix);
		mMultiTouchScale = mapView.mMultiTouchScale;

		final IGeoPoint neGeoPoint = fromPixels(mMapViewWidth, 0, null);
		final IGeoPoint swGeoPoint = fromPixels(0, mMapViewHeight, null);

		mBoundingBoxProjection = new BoundingBox(neGeoPoint.getLatitude(),
				neGeoPoint.getLongitude(), swGeoPoint.getLatitude(),
				swGeoPoint.getLongitude());
	}

	public int getZoomLevel() {
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

	public float getMapOrientation() {
		return mMapOrientation;
	}

	@Override
	public IGeoPoint fromPixels(int x, int y) {
		return fromPixels(x, y, null);
	}

	public IGeoPoint fromPixels(int x, int y, GeoPoint reuse) {
		return TileSystem.PixelXYToLatLong(x - mOffsetX, y - mOffsetY, mZoomLevelProjection, reuse);
	}

	@Override
	public Point toPixels(final IGeoPoint in, final Point reuse) {
		Point out = TileSystem.LatLongToPixelXY(in.getLatitude(), in.getLongitude(),
				getZoomLevel(), reuse);

		out = toPixelsFromMercator(out.x, out.y, out);
		out = adjustForDateLine(out.x, out.y, out);
		return out;
	}

	protected Point adjustForDateLine(int x, int y, Point reuse) {
		final Point out = reuse != null ? reuse : new Point();
		out.set(x, y);
		out.offset(-mMapViewWidth / 2, -mMapViewHeight / 2);
		final int mapSize = TileSystem.MapSize(getZoomLevel());
		final int absX = Math.abs(out.x);
		final int absY = Math.abs(out.y);
		if (absX > Math.abs(out.x - mapSize)) {
			out.x -= mapSize;
		}
		if (absX > Math.abs(out.x + mapSize)) {
			out.x += mapSize;
		}
		if (absY > Math.abs(out.y - mapSize)) {
			out.y -= mapSize;
		}
		if (absY > Math.abs(out.y + mapSize)) {
			out.y += mapSize;
		}
		out.offset(mMapViewWidth / 2, mMapViewHeight / 2);
		return out;
	}
	
	/**
	 * A wrapper for {@link #toProjectedPixels(double, double, Point)}
	 */
	public Point toProjectedPixels(final GeoPoint geoPoint, final Point reuse) {
		return toProjectedPixels(geoPoint.getLatitude(), geoPoint.getLongitude(), reuse);
	}

	/**
	 * Performs only the first computationally heavy part of the projection. Call
	 * {@link #toPixelsFromProjected(Point, Point)} to get the final position.
	 * 
	 * @param latitude
	 *            the latitute of the point
	 * @param longitude
	 *            the longitude of the point
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 * @return intermediate value to be stored and passed to toMapPixelsTranslated.
	 */
	public Point toProjectedPixels(final double latitude, final double longitude, final Point reuse) {
		return TileSystem.LatLongToPixelXY(latitude, longitude,
				MAXIMUM_ZOOMLEVEL, reuse);
	}

	/**
	 * Performs the second computationally light part of the projection.
	 * 
	 * @param in
	 *            the Point calculated by the {@link #toProjectedPixels(double, double, Point)}
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 * @return the Point containing the coordinates of the initial GeoPoint passed to the
	 *         {@link #toProjectedPixels(double, double, Point)}.
	 */
	public Point toPixelsFromProjected(final Point in, final Point reuse) {
		Point out = reuse != null ? reuse : new Point();

		final int zoomDifference = MAXIMUM_ZOOMLEVEL - getZoomLevel();
		out.set(in.x >> zoomDifference, in.y >> zoomDifference);

		out = toPixelsFromMercator(out.x, out.y, out);
		out = adjustForDateLine(out.x, out.y, out);

		return out;
	}

	public Point toPixelsFromMercator(int x, int y, Point reuse) {
		final Point out = reuse != null ? reuse : new Point();
		out.set(x, y);
		out.offset(mOffsetX, mOffsetY);
		return out;
	}

	public Point toMercatorPixels(int x, int y, Point reuse) {
		final Point out = reuse != null ? reuse : new Point();
		out.set(x, y);
		out.offset(-mOffsetX, -mOffsetY);
		return out;
	}

	@Override
	public float metersToEquatorPixels(final float meters) {
		return meters / (float) TileSystem.GroundResolution(0, mZoomLevelProjection);
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
		return meters
				/ (float) TileSystem.GroundResolution(getBoundingBox().getCenter().getLatitude(),
						mZoomLevelProjection);
	}

	@Override
	public IGeoPoint getNorthEast() {
		return fromPixels(mMapViewWidth, 0, null);
	}

	@Override
	public IGeoPoint getSouthWest() {
		return fromPixels(0, mMapViewHeight, null);
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
		if (reuse == null)
			reuse = new Point();

		if (getMapOrientation() != 0 || mMultiTouchScale != 1.0f) {
			mRotateScalePoints[0] = x;
			mRotateScalePoints[1] = y;
			mUnrotateAndScaleMatrix.mapPoints(mRotateScalePoints);
			reuse.set((int) mRotateScalePoints[0], (int) mRotateScalePoints[1]);
		} else
			reuse.set(x, y);
		return reuse;
	}

	/**
	 * This will apply the current map's scaling and rotation for a point. This can be useful when
	 * converting MotionEvents to a screen point.
	 */
	public Point rotateAndScalePoint(int x, int y, Point reuse) {
		if (reuse == null)
			reuse = new Point();

		if (getMapOrientation() != 0 || mMultiTouchScale != 1.0f) {
			mRotateScalePoints[0] = x;
			mRotateScalePoints[1] = y;
			mRotateAndScaleMatrix.mapPoints(mRotateScalePoints);
			reuse.set((int) mRotateScalePoints[0], (int) mRotateScalePoints[1]);
		} else
			reuse.set(x, y);
		return reuse;
	}
}

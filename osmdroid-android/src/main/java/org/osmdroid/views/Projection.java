package org.osmdroid.views;


import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.BoundingBoxE6;
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

	private final BoundingBoxE6 mBoundingBoxProjection;
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

		mBoundingBoxProjection = new BoundingBoxE6(neGeoPoint.getLatitudeE6(),
				neGeoPoint.getLongitudeE6(), swGeoPoint.getLatitudeE6(),
				swGeoPoint.getLongitudeE6());
	}

	public int getZoomLevel() {
		return mZoomLevelProjection;
	}

	public BoundingBoxE6 getBoundingBox() {
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
	public IGeoPoint fromPixels(int x, int y, GeoPoint reuse) {
		return TileSystem.PixelXYToLatLong(x - mOffsetX, y - mOffsetY, mZoomLevelProjection, reuse);
	}

	@Override
	public Point toPixels(final IGeoPoint in, final Point reuse) {
		Point out = TileSystem.LatLongToPixelXY(in.getLatitude(), in.getLongitude(),
				getZoomLevel(), reuse);

		out = fromMercatorPixels(out.x, out.y, out);
		out = adjustForDateLine(out.x, out.y, out);
		return out;
	}

	protected Point adjustForDateLine(int x, int y, Point reuse) {
		final Point out = reuse != null ? reuse : new Point();
		final int worldsize_2 = TileSystem.MapSize(getZoomLevel() - 1);
		out.set(x, y);
		out.offset(-worldsize_2, -worldsize_2);
		if (Math.abs(out.x) > Math.abs(out.x - TileSystem.MapSize(getZoomLevel()))) {
			out.x -= TileSystem.MapSize(getZoomLevel());
		}
		if (Math.abs(out.x) > Math.abs(out.x + TileSystem.MapSize(getZoomLevel()))) {
			out.x += TileSystem.MapSize(getZoomLevel());
		}
		if (Math.abs(out.y) > Math.abs(out.y - TileSystem.MapSize(getZoomLevel()))) {
			out.y -= TileSystem.MapSize(getZoomLevel());
		}
		if (Math.abs(out.y) > Math.abs(out.y + TileSystem.MapSize(getZoomLevel()))) {
			out.y += TileSystem.MapSize(getZoomLevel());
		}
		out.offset(worldsize_2, worldsize_2);
		return out;
	}
	
	/**
	 * Performs only the first computationally heavy part of the projection. Call
	 * {@link #toPixelsTranslated(Point, Point)} to get the final position.
	 * 
	 * @param latituteE6
	 *            the latitute of the point
	 * @param longitudeE6
	 *            the longitude of the point
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 * @return intermediate value to be stored and passed to toMapPixelsTranslated.
	 */
	public Point toPixelsProjected(final int latituteE6, final int longitudeE6, final Point reuse) {
		return TileSystem.LatLongToPixelXY(latituteE6 * 1E-6, longitudeE6 * 1E-6,
				MAXIMUM_ZOOMLEVEL, reuse);
	}

	/**
	 * Performs the second computationally light part of the projection.
	 * 
	 * @param in
	 *            the Point calculated by the {@link #toPixelsProjected(int, int, Point)}
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 * @return the Point containing the coordinates of the initial GeoPoint passed to the
	 *         {@link #toPixelsProjected(int, int, Point)}.
	 */
	public Point toPixelsTranslated(final Point in, final Point reuse) {
		Point out = reuse != null ? reuse : new Point();

		final int zoomDifference = MAXIMUM_ZOOMLEVEL - getZoomLevel();
		out.set(in.x >> zoomDifference, in.y >> zoomDifference);

		out = fromMercatorPixels(out.x, out.y, out);
		out = adjustForDateLine(out.x, out.y, out);

		return out;
	}

	public Point fromMercatorPixels(int x, int y, Point reuse) {
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

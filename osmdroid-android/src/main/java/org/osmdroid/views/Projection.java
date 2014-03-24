package org.osmdroid.views;


import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.util.constants.MapViewConstants;

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
		final Point out = reuse != null ? reuse : new Point();

		final int zoomDifference = MAXIMUM_ZOOMLEVEL - getZoomLevel();
		out.set(in.x >> zoomDifference, in.y >> zoomDifference);
		return fromMercatorPixels(out.x, out.y, out);
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
}

package org.osmdroid.views.util;

import java.util.List;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.Projection;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class PathProjection {

	public static Path toPixels(Projection projection, final List<? extends GeoPoint> in,
			final Path reuse) {
		return toPixels(projection, in, reuse, true);
	}

	public static Path toPixels(Projection projection, final List<? extends GeoPoint> in,
			final Path reuse, final boolean doGudermann) throws IllegalArgumentException {
		if (in.size() < 2) {
			throw new IllegalArgumentException("List of GeoPoints needs to be at least 2.");
		}

		final Path out = (reuse != null) ? reuse : new Path();
		out.incReserve(in.size());

		boolean first = true;
		for (final GeoPoint gp : in) {
			final Point underGeopointTileCoords = TileSystem.LatLongToPixelXY(
					gp.getLatitudeE6() / 1E6, gp.getLongitudeE6() / 1E6, projection.getZoomLevel(),
					null);
			TileSystem.PixelXYToTileXY(underGeopointTileCoords.x, underGeopointTileCoords.y,
					underGeopointTileCoords);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords of the MapTile.
			 */
			final Point upperRight = TileSystem.TileXYToPixelXY(underGeopointTileCoords.x,
					underGeopointTileCoords.y, null);
			final Point lowerLeft = TileSystem.TileXYToPixelXY(underGeopointTileCoords.x
					+ TileSystem.getTileSize(),
					underGeopointTileCoords.y + TileSystem.getTileSize(), null);
			final GeoPoint neGeoPoint = TileSystem.PixelXYToLatLong(upperRight.x, upperRight.y,
					projection.getZoomLevel(), null);
			final GeoPoint swGeoPoint = TileSystem.PixelXYToLatLong(lowerLeft.x, lowerLeft.y,
					projection.getZoomLevel(), null);
			final BoundingBoxE6 bb = new BoundingBoxE6(neGeoPoint.getLatitudeE6(),
					neGeoPoint.getLongitudeE6(), swGeoPoint.getLatitudeE6(),
					swGeoPoint.getLongitudeE6());

			final PointF relativePositionInCenterMapTile;
			if (doGudermann && (projection.getZoomLevel() < 7)) {
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
								gp.getLatitudeE6(), gp.getLongitudeE6(), null);
			} else {
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
								gp.getLatitudeE6(), gp.getLongitudeE6(), null);
			}

			final Rect screenRect = projection.getScreenRect();
			Point centerMapTileCoords = TileSystem.PixelXYToTileXY(screenRect.centerX(),
					screenRect.centerY(), null);
			final Point upperLeftCornerOfCenterMapTile = TileSystem.TileXYToPixelXY(
					centerMapTileCoords.x, centerMapTileCoords.y, null);
			final int tileDiffX = centerMapTileCoords.x - underGeopointTileCoords.x;
			final int tileDiffY = centerMapTileCoords.y - underGeopointTileCoords.y;
			final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
					- (TileSystem.getTileSize() * tileDiffX);
			final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
					- (TileSystem.getTileSize() * tileDiffY);

			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile.x * TileSystem.getTileSize());
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile.y * TileSystem.getTileSize());

			/* Add up the offset caused by touch. */
			if (first) {
				out.moveTo(x, y);
				// out.moveTo(x + MapView.this.mTouchMapOffsetX, y +
				// MapView.this.mTouchMapOffsetY);
			} else {
				out.lineTo(x, y);
			}
			first = false;
		}

		return out;
	}
}

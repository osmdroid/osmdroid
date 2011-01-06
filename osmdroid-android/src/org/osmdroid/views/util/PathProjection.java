package org.osmdroid.views.util;

import java.util.List;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView.Projection;

import android.graphics.Path;
import android.graphics.PointF;

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
			final GeoPoint underGeopointTileCoords = Mercator.projectGeoPoint(gp.getLatitudeE6(),
					gp.getLongitudeE6(), projection.getZoomLevel(), null);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords of the MapTile.
			 */
			final BoundingBoxE6 bb = Mercator.getBoundingBoxFromPointInMapTile(
					underGeopointTileCoords, projection.getZoomLevel());

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

			final int tileDiffX = projection.getCenterMapTileCoords().x
					- underGeopointTileCoords.getLongitudeE6();
			final int tileDiffY = projection.getCenterMapTileCoords().y
					- underGeopointTileCoords.getLatitudeE6();
			final int underGeopointTileScreenLeft = projection.getUpperLeftCornerOfCenterMapTile().x
					- (projection.getTileSizePixels() * tileDiffX);
			final int underGeopointTileScreenTop = projection.getUpperLeftCornerOfCenterMapTile().y
					- (projection.getTileSizePixels() * tileDiffY);

			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile.x * projection.getTileSizePixels());
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile.y * projection.getTileSizePixels());

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

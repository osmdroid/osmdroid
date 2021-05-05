package org.osmdroid.views.util;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

/**
 * @deprecated Use {@link Polyline} or {@link Polygon} instead
 */
@Deprecated
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

        final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
        boolean first = true;
        for (final GeoPoint gp : in) {
            final Point underGeopointTileCoords = new Point();
            final double mapSize = TileSystem.MapSize(projection.getZoomLevel());
            final PointL mercator = tileSystem.getMercatorFromGeo(
                    gp.getLatitude(), gp.getLongitude(), mapSize,
                    null, true);
            underGeopointTileCoords.x = projection.getTileFromMercator(mercator.x);
            underGeopointTileCoords.y = projection.getTileFromMercator(mercator.y);

            /*
             * Calculate the Latitude/Longitude on the left-upper ScreenCoords of the MapTile.
             */
            final PointL upperRight = new PointL(
                    projection.getMercatorFromTile(underGeopointTileCoords.x),
                    projection.getMercatorFromTile(underGeopointTileCoords.y));
            final PointL lowerLeft = new PointL(
                    projection.getMercatorFromTile(underGeopointTileCoords.x + TileSystem.getTileSize()),
                    projection.getMercatorFromTile(underGeopointTileCoords.y + TileSystem.getTileSize()));
            final GeoPoint neGeoPoint = tileSystem.getGeoFromMercator(upperRight.x, upperRight.y, mapSize, null, true, true);
            final GeoPoint swGeoPoint = tileSystem.getGeoFromMercator(lowerLeft.x, lowerLeft.y, mapSize, null, true, true);
            final BoundingBox bb = new BoundingBox(neGeoPoint.getLatitude(),
                    neGeoPoint.getLongitude(), swGeoPoint.getLatitude(),
                    swGeoPoint.getLongitude());

            final PointF relativePositionInCenterMapTile;
            if (doGudermann && (projection.getZoomLevel() < 7)) {
                relativePositionInCenterMapTile = bb
                        .getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
                                gp.getLatitude(), gp.getLongitude(), null);
            } else {
                relativePositionInCenterMapTile = bb
                        .getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
                                gp.getLatitude(), gp.getLongitude(), null);
            }

            final Rect screenRect = projection.getScreenRect();
            final Point centerMapTileCoords = new Point(
                    projection.getTileFromMercator(screenRect.centerX()),
                    projection.getTileFromMercator(screenRect.centerY()));
            final PointL upperLeftCornerOfCenterMapTile = new PointL(
                    projection.getMercatorFromTile(centerMapTileCoords.x),
                    projection.getMercatorFromTile(centerMapTileCoords.y));
            final int tileDiffX = centerMapTileCoords.x - underGeopointTileCoords.x;
            final int tileDiffY = centerMapTileCoords.y - underGeopointTileCoords.y;
            final long underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
                    - (TileSystem.getTileSize() * tileDiffX);
            final long underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
                    - (TileSystem.getTileSize() * tileDiffY);

            final long x = underGeopointTileScreenLeft
                    + (long) (relativePositionInCenterMapTile.x * TileSystem.getTileSize());
            final long y = underGeopointTileScreenTop
                    + (long) (relativePositionInCenterMapTile.y * TileSystem.getTileSize());

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

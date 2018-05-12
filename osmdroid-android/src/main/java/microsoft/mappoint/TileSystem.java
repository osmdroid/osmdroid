package microsoft.mappoint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystemWebMercator;

import android.graphics.Point;

/**
 * This class provides methods to handle the Mercator projection that is used for the osmdroid tile
 * system.
 */
@Deprecated
public final class TileSystem {

	private static final TileSystemWebMercator tileSystem = new TileSystemWebMercator();

	@Deprecated
	public static final int primaryKeyMaxZoomLevel = org.osmdroid.util.TileSystem.primaryKeyMaxZoomLevel;

	@Deprecated
	public static final int projectionZoomLevel = org.osmdroid.util.TileSystem.projectionZoomLevel;

	@Deprecated
	public static void setTileSize(final int tileSize) {
		org.osmdroid.util.TileSystem.setTileSize(tileSize);
	}

	@Deprecated
	public static int getTileSize() {
		return org.osmdroid.util.TileSystem.getTileSize();
	}

	@Deprecated
	public static int getMaximumZoomLevel() {
		return org.osmdroid.util.TileSystem.getMaximumZoomLevel();
	}

	@Deprecated
	public static int MapSize(final int levelOfDetail) {
		return org.osmdroid.util.TileSystem.MapSize(levelOfDetail);
	}

	@Deprecated
	public static double GroundResolution(double latitude, final int levelOfDetail) {
		return org.osmdroid.util.TileSystem.GroundResolution(
				latitude, org.osmdroid.util.TileSystem.MapSize(levelOfDetail));
	}

	@Deprecated
	public static double MapScale(final double latitude, final int levelOfDetail,
			final int screenDpi) {
		return GroundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
	}

	@Deprecated
	public static Point LatLongToPixelXY(double latitude, double longitude,
			final int levelOfDetail, final Point reuse) {
		return tileSystem.LatLongToPixelXY(latitude, longitude, levelOfDetail, reuse);
	}

	@Deprecated
	public static GeoPoint PixelXYToLatLong(final int pixelX, final int pixelY,
			final int levelOfDetail, final GeoPoint reuse) {
		return tileSystem.PixelXYToLatLong(pixelX, pixelY, levelOfDetail, reuse);
	}

	@Deprecated
	public static Point PixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
		return tileSystem.PixelXYToTileXY(pixelX, pixelY, reuse);
	}

	@Deprecated
	public static Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
		return tileSystem.TileXYToPixelXY(tileX, tileY, reuse);
	}

	@Deprecated
	public static String TileXYToQuadKey(final int tileX, final int tileY, final int levelOfDetail) {
		return org.osmdroid.util.TileSystem.TileXYToQuadKey(tileX, tileY, levelOfDetail);
	}

	@Deprecated
	public static Point QuadKeyToTileXY(final String quadKey, final Point reuse) {
		return org.osmdroid.util.TileSystem.QuadKeyToTileXY(quadKey, reuse);
	}
}

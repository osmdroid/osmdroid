package org.osmdroid.util;

import android.graphics.Point;

/**
 * Proxy class for TileSystem. For coordinate conversions (tile to lat/lon and
 * reverse) TileSystem only accepts input parameters within certain ranges and
 * crops any values outside of it. For lat/lon the range is ~(-85,+85) /
 * (-180,+180) and for tile coordinates (0,mapsize-1). Under certain conditions
 * osmdroid creates values outside of these ranges, for example when zooming out
 * and displaying the earth more that once side by side or when scrolling across
 * the 180 degree longitude (international date line). This class fixes this by
 * wrapping input coordinates into a valid range by adding/subtracting the valid
 * span. Example: longitude +185 =&gt; -175
 *
 * @author Oliver Seiler
 */
public class TileSystem {

     /* BEGIN the following code snippet was previously in microsoft.mappoint.TileSystem, however it's not part of the microsoft code base
      and thus was graphed into the osmdroid implementation
      */
     /**
      * Maximum Zoom Level - we use Integers to store zoom levels so overflow
      * happens at 2^32 - 1, but we also have a tile size that is typically 2^8,
      * so (32-1)-8-1 = 22
      */
     private static int mMaxZoomLevel = 22;

     public static void setTileSize(final int tileSize) {
          int pow2 = (int) (Math.log(tileSize) / Math.log(2));
          mMaxZoomLevel = (32 - 1) - pow2 - 1;

          mTileSize = tileSize;
     }

     public static int getTileSize() {
          return mTileSize;
     }

     public static int getMaximumZoomLevel() {
          return mMaxZoomLevel;
     }
     protected static int mTileSize = 256;

     /* END the following code snippet was previously in microsoft.mappoint.TileSystem, however it's not part of the microsoft code base
      and thus was graphed into the osmdroid implementation
      */
     /**
      * Determines the map width and height (in pixels) at a specified level of
      * detail.
      *
      * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23
      * (highest detail)
      * @return The map width and height in pixels
      */
     public static int MapSize(final int zoom) {
          return (int) (Math.pow(2.0, zoom) * mTileSize);
     }

     /**
      * Determines the ground resolution (in meters per pixel) at a specified
      * latitude and level of detail.
      *
      * @param latitude Latitude (in degrees) at which to measure the ground
      * resolution
      * @param zoom Level of detail, from 1 (lowest detail) to 23 (highest
      * detail)
      * @return The ground resolution, in meters per pixel
      */
     public static double GroundResolution(final double latitude, final int zoom) {
          return MapScale(latitude, zoom, 0);
     }

     /**
      * Determines the ground resolution (in meters per pixel) at a specified
      * latitude and level of detail.
      *
      * @param latitude Latitude (in degrees) at which to measure the ground
      * resolution
      * @param zoom Level of detail, from 1 (lowest detail) to 23 (highest
      * detail)
      * @return The ground resolution, in meters per pixel
      */
     public static double MapScale(final double latitude, final int zoom, final int screenDpi) {
          //resolution = 156543.03 meters/pixel * cos(latitude) / (2 ^ zoomlevel)
          return 156543.03 * Math.cos(latitude) / (2 ^ zoom);
     }

     /**
      * Converts a point from latitude/longitude WGS-84 coordinates (in degrees)
      * into pixel XY coordinates at a specified level of detail.
      *
      * @param latitude Latitude of the point, in degrees
      * @param longitude Longitude of the point, in degrees
      * @param zoom Level of detail, from 1 (lowest detail) to 23 (highest
      * detail)
      * @param reuse An optional Point to be recycled, or null to create a new
      * one automatically
      * @return Output parameter receiving the X and Y coordinates in pixels
      */
     public static Point LatLongToPixelXY(
          final double latitude, final double longitude, final int zoom, final Point reuse) {
          final Point out = (reuse == null ? new Point() : reuse);

          int xtile = (int) Math.floor((longitude + 180) / 360 * (1 << zoom));
          int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(latitude)) + 1 / Math.cos(Math.toRadians(latitude))) / Math.PI) / 2 * (1 << zoom));
          if (xtile < 0) {
               xtile = 0;
          }
          if (xtile >= (1 << zoom)) {
               xtile = ((1 << zoom) - 1);
          }
          if (ytile < 0) {
               ytile = 0;
          }
          if (ytile >= (1 << zoom)) {
               ytile = ((1 << zoom) - 1);
          }

          //right now we have tile x/y, need pixel xy
          return TileXYToPixelXY(xtile, ytile, out);

     }

     /**
      * Converts a pixel from pixel XY coordinates at a specified level of
      * detail into latitude/longitude WGS-84 coordinates (in degrees).
      *
      * @param pixelX X coordinate of the point, in pixels
      * @param pixelY Y coordinate of the point, in pixels
      * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23
      * (highest detail)
      * @param reuse An optional GeoPoint to be recycled, or null to create a
      * new one automatically
      * @return Output parameter receiving the latitude and longitude in
      * degrees.
      */
     public static GeoPoint PixelXYToLatLong(
          final int pixelX, final int pixelY, final int zoom, final GeoPoint reuse) {
          //convert pixel xy to tile xy
          Point pt = PixelXYToTileXY(pixelX, pixelY, null);
          return tileXYtoLatLon(pt.x, pt.y, zoom, reuse);

     }

     /**
      * Converts pixel XY coordinates into tile XY coordinates of the tile
      * containing the specified pixel. based on code from
      * https://msdn.microsoft.com/en-us/library/bb259689.aspx but altered to
      * meet Android uses
      *
      * @param pixelX Pixel X coordinate
      * @param pixelY Pixel Y coordinate
      * @param reuse An optional Point to be recycled, or null to create a new
      * one automatically
      * @return Output parameter receiving the tile X and Y coordinates
      */
     public static Point PixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
          final Point out = (reuse == null ? new Point() : reuse);
          out.x = pixelX / mTileSize;
          out.y = pixelY / mTileSize;
          return out;
     }

     /**
      * Converts tile XY coordinates into pixel XY coordinates of the upper-left
      * pixel of the specified tile. based on code from
      * https://msdn.microsoft.com/en-us/library/bb259689.aspx but altered to
      * meet Android uses
      *
      * @param tileX Tile X coordinate
      * @param tileY Tile X coordinate
      * @param reuse An optional Point to be recycled, or null to create a new
      * one automatically
      * @return Output parameter receiving the pixel X and Y coordinates
      */
     public static Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
          final Point out = (reuse == null ? new Point() : reuse);

          out.x = tileX * mTileSize;
          out.y = tileY * mTileSize;
          return out;
     }

     public static GeoPoint tileXYtoLatLon(final int tileX, final int tileY, final int zoom, final GeoPoint reuse) {
          final GeoPoint out = reuse!=null ? reuse : new GeoPoint(0, 0);
          double lon = tile2lon(tileX, zoom);
          double lat = tile2lat(tileY, zoom);
          out.setLatitudeE6((int) (lat * 1E6));
          out.setLongitudeE6((int) (lon * 1E6));
          return out;
     }

     public static BoundingBoxE6 tile2boundingBox(final int x, final int y, final int zoom) {
          return new BoundingBoxE6(tile2lat(y, zoom), tile2lon(x + 1, zoom), tile2lat(y + 1, zoom), tile2lon(x, zoom));

     }

     static double tile2lon(int x, int z) {
          return x / Math.pow(2.0, z) * 360.0 - 180;
     }

     static double tile2lat(int y, int z) {
          double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
          return Math.toDegrees(Math.atan(Math.sinh(n)));
     }

}

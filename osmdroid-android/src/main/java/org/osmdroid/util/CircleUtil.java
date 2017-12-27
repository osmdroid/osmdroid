package org.osmdroid.util;

import org.osmdroid.api.IGeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities related to 2D circles
 * created on 12/27/2017.
 * @since 6.0.0
 * @author Alex O'Ree
 */

public class CircleUtil {

    private static final double EARTH_RADIUS_NM = 3437.670013352;

    public static double convertToRadians(double angle) {
        return (angle * Math.PI) / 180d;
    }


    public static GeoPoint convertToLatLonDegrees(double lat, double lon) {


        // first of all get everthing into the range -2pi to 2pi
        double rad = lat % (Math.PI * 2);

        // convert negatives to equivalent positive angle
        if (rad < 0)
            rad = 2 * Math.PI + rad;

        // restict to 0 - 180
        double rad180 = rad % (Math.PI);

        // anything above 90 is subtracted from 180
        if (rad180 > Math.PI / 2)
            rad180 = Math.PI - rad180;
        // if it is greater than 180 then make negative
        if (rad > Math.PI)
            rad = -rad180;
        else
            rad = rad180;

        double latitude = (rad / Math.PI * 180);


        // first of all get everthing into the range -2pi to 2pi
        rad = lon % (Math.PI * 2);
        if (rad < 0)
            rad = 2 * Math.PI + rad;

        // convert negatives to equivalent positive angle
        double rad360 = rad % (Math.PI * 2);

        // anything above 90 is subtracted from 360
        if (rad360 > Math.PI)
            rad360 = Math.PI * 2 - rad360;

        // if it is greater than 180 then make negative
        if (rad > Math.PI)
            rad = -rad360;
        else
            rad = rad360;

        double longitude = (rad / Math.PI * 180);
        return new GeoPoint(latitude, longitude);
    }


    /**
     * generates a point set based on the center point, radius in kilometers. Produces 'pointCount' quantity
     * of points. For lower resolution maps (numerically lower zoom levels), use a smaller point count for better
     * performance.
     *
     * Note: the radius of the circle is dependent on the latitude of the center point. Due to distortions in the
     * web mercator projection, circles will appear to have different radius with the same inputs with varying latitude.
     * @param centerPoint
     * @param radiusKm
     * @param pointCount
     * @return
     */
    public static List<GeoPoint> getCircle(IGeoPoint centerPoint, float radiusKm, int pointCount) {
        List<GeoPoint> pts = new ArrayList<>(360);
        double lat = convertToRadians(centerPoint.getLatitude());
        double lon = convertToRadians(centerPoint.getLongitude());
        double d = radiusKm / EARTH_RADIUS_NM;

        double multiplier = 360/pointCount;
        for (int x = 0; x < pointCount; x++) {
            //for 2 degrees per point we need a multiplier of 2

            double brng = convertToRadians(x * multiplier);
            double latRadians = Math.asin(Math.sin(lat) * Math.cos(d) + Math.cos(lat) * Math.sin(d) * Math.cos(brng));
            double lngRadians = lon + Math.atan2(Math.sin(brng) * Math.sin(d) * Math.cos(lat), Math.cos(d) - Math.sin(lat) * Math.sin(latRadians));

            pts.add(convertToLatLonDegrees(latRadians, lngRadians));
        }

        return pts;
    }

    /**
     * generates a point set using 360 points
     * @param centerPoint
     * @param radiusKm
     * @return
     */
    public static List<GeoPoint> getCircle(IGeoPoint centerPoint, float radiusKm) {
        return getCircle(centerPoint, radiusKm, 360);
    }
}

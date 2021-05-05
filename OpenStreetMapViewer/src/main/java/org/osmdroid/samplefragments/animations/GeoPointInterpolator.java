/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */

package org.osmdroid.samplefragments.animations;

import org.osmdroid.util.GeoPoint;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public interface GeoPointInterpolator {
    public GeoPoint interpolate(float fraction, GeoPoint a, GeoPoint b);

    public class Linear implements GeoPointInterpolator {
        @Override
        public GeoPoint interpolate(float fraction, GeoPoint a, GeoPoint b) {
            double lat = (b.getLatitude() - a.getLatitude()) * fraction + a.getLatitude();
            double lng = (b.getLongitude() - a.getLongitude()) * fraction + a.getLongitude();
            return new GeoPoint(lat, lng);
        }
    }

    public class LinearFixed implements GeoPointInterpolator {
        @Override
        public GeoPoint interpolate(float fraction, GeoPoint a, GeoPoint b) {
            double lat = (b.getLatitude() - a.getLatitude()) * fraction + a.getLatitude();
            double lngDelta = b.getLongitude() - a.getLongitude();

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + a.getLongitude();
            return new GeoPoint(lat, lng);
        }
    }

    public class Spherical implements GeoPointInterpolator {

        /* From github.com/googlemaps/android-maps-utils */
        @Override
        public GeoPoint interpolate(float fraction, GeoPoint from, GeoPoint to) {
            // http://en.wikipedia.org/wiki/Slerp
            double fromLat = toRadians(from.getLatitude());
            double fromLng = toRadians(from.getLongitude());
            double toLat = toRadians(to.getLatitude());
            double toLng = toRadians(to.getLongitude());
            double cosFromLat = cos(fromLat);
            double cosToLat = cos(toLat);

            // Computes Spherical interpolation coefficients.
            double angle = computeAngleBetween(fromLat, fromLng, toLat, toLng);
            double sinAngle = sin(angle);
            if (sinAngle < 1E-6) {
                return from;
            }
            double a = sin((1 - fraction) * angle) / sinAngle;
            double b = sin(fraction * angle) / sinAngle;

            // Converts from polar to vector and interpolate.
            double x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng);
            double y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng);
            double z = a * sin(fromLat) + b * sin(toLat);

            // Converts interpolated vector back to polar.
            double lat = atan2(z, sqrt(x * x + y * y));
            double lng = atan2(y, x);
            return new GeoPoint(toDegrees(lat), toDegrees(lng));
        }

        private double computeAngleBetween(double fromLat, double fromLng, double toLat, double toLng) {
            // Haversine's formula
            double dLat = fromLat - toLat;
            double dLng = fromLng - toLng;
            return 2 * asin(sqrt(pow(sin(dLat / 2), 2) +
                    cos(fromLat) * cos(toLat) * pow(sin(dLng / 2), 2)));
        }
    }
}

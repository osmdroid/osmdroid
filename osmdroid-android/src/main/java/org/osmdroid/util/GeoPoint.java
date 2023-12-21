// Created by plusminus on 21:28:12 - 25.09.2008
package org.osmdroid.util;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.util.constants.MathConstants;

import java.io.Serializable;

/**
 * @author Nicolas Gramlich
 * @author Theodore Hong
 */
public class GeoPoint implements IGeoPoint, MathConstants, GeoConstants, Parcelable, Serializable, Cloneable {

    // ===========================================================
    // Constants
    // ===========================================================



    // ===========================================================
    // Fields
    // ===========================================================

    private double mLongitude;
    private double mLatitude;
    private double mAltitude;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Deprecated
    public GeoPoint(final int aLatitudeE6, final int aLongitudeE6) {
        this.mLatitude = aLatitudeE6 / 1E6;
        this.mLongitude = aLongitudeE6 / 1E6;
    }

    @Deprecated
    public GeoPoint(final int aLatitudeE6, final int aLongitudeE6, final int aAltitude) {
        this.mLatitude = aLatitudeE6 / 1E6;
        this.mLongitude = aLongitudeE6 / 1E6;
        this.mAltitude = aAltitude;
    }

    public GeoPoint(final double aLatitude, final double aLongitude) {
        this.mLatitude = aLatitude;
        this.mLongitude = aLongitude;
    }

    public GeoPoint(final double aLatitude, final double aLongitude, final double aAltitude) {
        this.mLatitude = aLatitude;
        this.mLongitude = aLongitude;
        this.mAltitude = aAltitude;
    }

    public GeoPoint(@NonNull final Location aLocation) {
        this(aLocation.getLatitude(), aLocation.getLongitude(), aLocation.getAltitude());
    }

    public GeoPoint(@NonNull final GeoPoint aGeopoint) {
        this.mLatitude = aGeopoint.mLatitude;
        this.mLongitude = aGeopoint.mLongitude;
        this.mAltitude = aGeopoint.mAltitude;
    }

    /**
     * @since 6.0.3
     */
    public GeoPoint(@NonNull final IGeoPoint pGeopoint) {
        this.mLatitude = pGeopoint.getLatitude();
        this.mLongitude = pGeopoint.getLongitude();
    }

    public static GeoPoint fromDoubleString(@NonNull final String s, @NonNull final char spacer) {
        final int spacerPos1 = s.indexOf(spacer);
        final int spacerPos2 = s.indexOf(spacer, spacerPos1 + 1);

        if (spacerPos2 == -1) {
            return new GeoPoint(
                    (Double.parseDouble(s.substring(0, spacerPos1))),
                    (Double.parseDouble(s.substring(spacerPos1 + 1))));
        } else {
            return new GeoPoint(
                    (Double.parseDouble(s.substring(0, spacerPos1))),
                    (Double.parseDouble(s.substring(spacerPos1 + 1, spacerPos2))),
                    Double.parseDouble(s.substring(spacerPos2 + 1)));
        }
    }

    public static GeoPoint fromInvertedDoubleString(@NonNull final String s, @NonNull final char spacer) {
        final int spacerPos1 = s.indexOf(spacer);
        final int spacerPos2 = s.indexOf(spacer, spacerPos1 + 1);

        if (spacerPos2 == -1) {
            return new GeoPoint(
                    Double.parseDouble(s.substring(spacerPos1 + 1)),
                    Double.parseDouble(s.substring(0, spacerPos1)));
        } else {
            return new GeoPoint(
                    Double.parseDouble(s.substring(spacerPos1 + 1, spacerPos2)),
                    Double.parseDouble(s.substring(0, spacerPos1)),
                    Double.parseDouble(s.substring(spacerPos2 + 1)));

        }
    }

    @Deprecated
    public static GeoPoint fromIntString(@NonNull final String s) {
        final int commaPos1 = s.indexOf(',');
        final int commaPos2 = s.indexOf(',', commaPos1 + 1);

        if (commaPos2 == -1) {
            return new GeoPoint(
                    Integer.parseInt(s.substring(0, commaPos1)),
                    Integer.parseInt(s.substring(commaPos1 + 1)));
        } else {
            return new GeoPoint(
                    Integer.parseInt(s.substring(0, commaPos1)),
                    Integer.parseInt(s.substring(commaPos1 + 1, commaPos2)),
                    Integer.parseInt(s.substring(commaPos2 + 1))
            );
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    @Override
    public double getLongitude() {
        return this.mLongitude;
    }

    @Override
    public double getLatitude() {
        return this.mLatitude;
    }

    public double getAltitude() {
        return this.mAltitude;
    }

    public void setLatitude(final double aLatitude) {
        this.mLatitude = aLatitude;
    }

    public void setLongitude(final double aLongitude) {
        this.mLongitude = aLongitude;
    }

    public void setAltitude(final double aAltitude) {
        this.mAltitude = aAltitude;
    }

    public void setCoords(final double aLatitude, final double aLongitude) {
        this.mLatitude = aLatitude;
        this.mLongitude = aLongitude;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    /** @noinspection MethodDoesntCallSuperMethod*/
    @NonNull
    @Override
    public GeoPoint clone() {
        return new GeoPoint(this.mLatitude, this.mLongitude, this.mAltitude);
    }

    public String toIntString() {
        return ((int) (this.mLatitude * 1E6)) +
                "," +
                ((int) (this.mLongitude * 1E6)) +
                "," +
                (int) (this.mAltitude);
    }

    @NonNull
    @Override
    public String toString() {
        return this.mLatitude + "," + this.mLongitude + "," + this.mAltitude;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final GeoPoint rhs = (GeoPoint) obj;
        return rhs.mLatitude == this.mLatitude && rhs.mLongitude == this.mLongitude && rhs.mAltitude == this.mAltitude;
    }

    @Override
    public int hashCode() {
        return 37 * (17 * (int) (mLatitude * 1E-6) + (int) (mLongitude * 1E-6)) + (int) mAltitude;
    }

    // ===========================================================
    // Parcelable
    // ===========================================================
    private GeoPoint(final Parcel in) {
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mAltitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeDouble(mLatitude);
        out.writeDouble(mLongitude);
        out.writeDouble(mAltitude);
    }

    public static final Parcelable.Creator<GeoPoint> CREATOR = new Parcelable.Creator<GeoPoint>() {
        @Override
        public GeoPoint createFromParcel(final Parcel in) {
            return new GeoPoint(in);
        }

        @Override
        public GeoPoint[] newArray(final int size) {
            return new GeoPoint[size];
        }
    };

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * @return distance in meters
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
     * @see <a href="http://www.movable-type.co.uk/scripts/gis-faq-5.1.html">GIS FAQ</a>
     * @since 6.0.0
     */
    public double distanceToAsDouble(@NonNull final IGeoPoint other) {
        return distanceToAsDouble(other.getLatitude(), other.getLongitude());
    }
    /**
     * @return distance in meters
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
     * @see <a href="http://www.movable-type.co.uk/scripts/gis-faq-5.1.html">GIS FAQ</a>
     * @since 6.0.0
     */
    public double distanceToAsDouble(final double lat, final double lon) {
        return distanceToAsDouble(getLatitude(), getLongitude(), lat, lon);
    }

    public static double distanceToAsDouble(double lat1, double lon1, double lat2, double lon2) {
        lat1 = DEG2RAD * lat1;
        lat2 = DEG2RAD * lat2;
        lon1 = DEG2RAD * lon1;
        lon2 = DEG2RAD * lon2;
        return RADIUS_EARTH_METERS * 2 * Math.asin(Math.min(1, Math.sqrt(
                Math.pow(Math.sin((lat2 - lat1) / 2), 2)
                        + Math.cos(lat1) * Math.cos(lat2)
                        * Math.pow(Math.sin((lon2 - lon1) / 2), 2)
        )));
    }

    /**
     * @return bearing in degrees
     * @see <a href="http://groups.google.com/group/osmdroid/browse_thread/thread/d22c4efeb9188fe9/bc7f9b3111158dd">discussion</a>
     */
    public double bearingTo(@NonNull final IGeoPoint other) {
        final double lat1 = Math.toRadians(this.mLatitude);
        final double long1 = Math.toRadians(this.mLongitude);
        final double lat2 = Math.toRadians(other.getLatitude());
        final double long2 = Math.toRadians(other.getLongitude());
        final double delta_long = long2 - long1;
        final double a = Math.sin(delta_long) * Math.cos(lat2);
        final double b = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(delta_long);
        final double bearing = Math.toDegrees(Math.atan2(a, b));
        final double bearing_normalized = (bearing + 360) % 360;
        return bearing_normalized;
    }

    /**
     * Calculate a point that is the specified distance and bearing away from this point.
     *
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html">latlong.html</a>
     * @see <a href="http://www.movable-type.co.uk/scripts/latlon.js">latlon.js</a>
     */
    public GeoPoint destinationPoint(final double aDistanceInMeters, final double aBearingInDegrees) {

        // convert distance to angular distance
        final double dist = aDistanceInMeters / RADIUS_EARTH_METERS;

        // convert bearing to radians
        final double brng = DEG2RAD * aBearingInDegrees;

        // get current location in radians
        final double lat1 = DEG2RAD * getLatitude();
        final double lon1 = DEG2RAD * getLongitude();

        final double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
                * Math.sin(dist) * Math.cos(brng));
        final double lon2 = lon1
                + Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist)
                - Math.sin(lat1) * Math.sin(lat2));

        final double lat2deg = lat2 / DEG2RAD;
        final double lon2deg = lon2 / DEG2RAD;

        return new GeoPoint(lat2deg, lon2deg);
    }

    public static GeoPoint fromCenterBetween(@NonNull final GeoPoint geoPointA, @NonNull final GeoPoint geoPointB) {
        return new GeoPoint((geoPointA.getLatitude() + geoPointB.getLatitude()) / 2,
                (geoPointA.getLongitude() + geoPointB.getLongitude()) / 2);
    }

    public String toDoubleString() {
        return this.mLatitude + "," +
                this.mLongitude + "," + this.mAltitude;
    }

    public String toInvertedDoubleString() {
        return this.mLongitude + "," +
                this.mLatitude + "," + this.mAltitude;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    @Deprecated
    @Override
    public int getLatitudeE6() {
        return (int) (this.getLatitude() * 1E6);
    }

    @Deprecated
    @Override
    public int getLongitudeE6() {
        return (int) (this.getLongitude() * 1E6);
    }
}

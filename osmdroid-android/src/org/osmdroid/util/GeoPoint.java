// Created by plusminus on 21:28:12 - 25.09.2008
package org.osmdroid.util;

import org.andnav.osm.util.constants.GeoConstants;
import org.andnav.osm.views.util.constants.MathConstants;

import java.io.Serializable;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * @author Nicolas Gramlich
 * @author Theodore Hong
 *
 */
public class GeoPoint implements MathConstants, GeoConstants, Parcelable, Serializable, Cloneable {

	// ===========================================================
	// Constants
	// ===========================================================

	static final long serialVersionUID = 1L;

	// ===========================================================
	// Fields
	// ===========================================================

	private int mLongitudeE6;
	private int mLatitudeE6;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeoPoint(final int aLatitudeE6, final int aLongitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;
	}

	public GeoPoint(final double aLatitude, final double aLongitude) {
		this.mLatitudeE6 = (int) (aLatitude * 1E6);
		this.mLongitudeE6 = (int) (aLongitude * 1E6);
	}

	public GeoPoint(final Location aLocation) {
		this(aLocation.getLatitude(), aLocation.getLongitude());
	}

	public GeoPoint(final GeoPoint aGeopoint) {
		this.mLatitudeE6 = aGeopoint.mLatitudeE6;
		this.mLongitudeE6 = aGeopoint.mLongitudeE6;
	}

	public static GeoPoint fromDoubleString(final String s, final char spacer) {
		final int spacerPos = s.indexOf(spacer);
		return new GeoPoint((int) (Double.parseDouble(s.substring(0,
				spacerPos - 1)) * 1E6), (int) (Double.parseDouble(s.substring(
				spacerPos + 1, s.length())) * 1E6));
	}

	public static GeoPoint fromInvertedDoubleString(final String s, final char spacer) {
		final int spacerPos = s.indexOf(spacer);
		return new GeoPoint((int) (Double.parseDouble(s.substring(
				spacerPos + 1, s.length())) * 1E6), (int) (Double.parseDouble(s.substring(
				0, spacerPos)) * 1E6));
	}

	public static GeoPoint fromIntString(final String s){
		final int commaPos = s.indexOf(',');
		return new GeoPoint(Integer.parseInt(s.substring(0,commaPos-1)),
				Integer.parseInt(s.substring(commaPos+1,s.length())));
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getLongitudeE6() {
		return this.mLongitudeE6;
	}

	public int getLatitudeE6() {
		return this.mLatitudeE6;
	}

	public void setLongitudeE6(final int aLongitudeE6) {
		this.mLongitudeE6 = aLongitudeE6;
	}

	public void setLatitudeE6(final int aLatitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
	}

	public void setCoordsE6(final int aLatitudeE6, final int aLongitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public Object clone() {
		return new GeoPoint(this.mLatitudeE6, this.mLongitudeE6);
	}

	@Override
	public String toString(){
		return new StringBuilder().append(this.mLatitudeE6).append(",").append(this.mLongitudeE6).toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != getClass()) return false;
		final GeoPoint rhs = (GeoPoint)obj;
		return rhs.mLatitudeE6 == this.mLatitudeE6 && rhs.mLongitudeE6 == this.mLongitudeE6;
	}

	// ===========================================================
	// Parcelable
	// ===========================================================
	private GeoPoint(final Parcel in) {
		this.mLatitudeE6 = in.readInt();
		this.mLongitudeE6 = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mLatitudeE6);
		out.writeInt(mLongitudeE6);
	}

	public static final Parcelable.Creator<GeoPoint> CREATOR = new Parcelable.Creator<GeoPoint>() {
		@Override
		public GeoPoint createFromParcel(Parcel in) {
			return new GeoPoint(in);
		}

		@Override
		public GeoPoint[] newArray(int size) {
			return new GeoPoint[size];
		}
	};

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * @see Source@ http://www.geocities.com/DrChengalva/GPSDistance.html
	 * @param gpA
	 * @param gpB
	 * @return distance in meters
	 */
	public int distanceTo(final GeoPoint other) {

		final double a1 = DEG2RAD * (this.mLatitudeE6 / 1E6);
		final double a2 = DEG2RAD * (this.mLongitudeE6 / 1E6);
		final double b1 = DEG2RAD * (other.mLatitudeE6 / 1E6);
		final double b2 = DEG2RAD * (other.mLongitudeE6 / 1E6);

		final double cosa1 = Math.cos(a1);
		final double cosb1 = Math.cos(b1);

		final double t1 = cosa1*Math.cos(a2)*cosb1*Math.cos(b2);

		final double t2 = cosa1*Math.sin(a2)*cosb1*Math.sin(b2);

		final double t3 = Math.sin(a1)*Math.sin(b1);

		final double tt = Math.acos( t1 + t2 + t3 );

		return (int)(RADIUS_EARTH_METERS*tt);
	}

	/**
	 * @see Source@ http://groups.google.com/group/osmdroid/browse_thread/thread/d22c4efeb9188fe9/bc7f9b3111158dd
	 * @param other
	 * @return bearing in degrees
	 */
	public double bearingTo(final GeoPoint other) {
		final double lat1 = Math.toRadians(this.mLatitudeE6 / 1E6);
		final double long1 = Math.toRadians(this.mLongitudeE6 / 1E6);
		final double lat2 = Math.toRadians(other.mLatitudeE6 / 1E6);
		final double long2 = Math.toRadians(other.mLongitudeE6 / 1E6);
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
	 * @see Source@ http://www.movable-type.co.uk/scripts/latlong.html
	 * @see Source@ http://www.movable-type.co.uk/scripts/latlon.js
	 */
	public GeoPoint destinationPoint(final double aDistanceInMeters, float aBearingInDegrees) {

		// convert distance to angular distance
		final double dist = aDistanceInMeters / RADIUS_EARTH_METERS;

		// convert bearing to radians
		float brng = DEG2RAD * aBearingInDegrees;

		// get current location in radians
		final double lat1 = DEG2RAD * getLatitudeE6() / 1E6;
		final double lon1 = DEG2RAD * getLongitudeE6() / 1E6;

		final double lat2 = Math.asin( Math.sin(lat1) * Math.cos(dist) +
				Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
		final double lon2 = lon1 + Math.atan2( Math.sin(brng) * Math.sin(dist) * Math.cos(lat1),
				Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));

		final double lat2deg = lat2 / DEG2RAD;
		final double lon2deg = lon2 / DEG2RAD;

		return new GeoPoint(lat2deg, lon2deg);
	}

	public static GeoPoint fromCenterBetween(final GeoPoint geoPointA, final GeoPoint geoPointB) {
		return new GeoPoint((geoPointA.getLatitudeE6() + geoPointB.getLatitudeE6()) / 2,
							(geoPointA.getLongitudeE6() + geoPointB.getLongitudeE6()) / 2);
	}

	public String toDoubleString() {
		return new StringBuilder().append(this.mLatitudeE6 / 1E6).append(",").append(this.mLongitudeE6  / 1E6).toString();
	}

	public String toInvertedDoubleString() {
		return new StringBuilder().append(this.mLongitudeE6 / 1E6).append(",").append(this.mLatitudeE6 / 1E6).toString();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

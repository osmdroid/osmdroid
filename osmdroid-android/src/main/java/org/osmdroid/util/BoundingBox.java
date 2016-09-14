// Created by plusminus on 19:06:38 - 25.09.2008
package org.osmdroid.util;

import static org.osmdroid.util.MyMath.gudermann;
import static org.osmdroid.util.MyMath.gudermannInverse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class BoundingBox implements Parcelable, Serializable, MapViewConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	static final long serialVersionUID = 2L;

	// ===========================================================
	// Fields
	// ===========================================================

	protected final double mLatNorth;
	protected final double mLatSouth;
	protected final double mLonEast;
	protected final double mLonWest;

	// ===========================================================
	// Constructors
	// ===========================================================

	public BoundingBox(final double north, final double east, final double south, final double west) {
		this.mLatNorth = north;
		this.mLonEast = east;
		this.mLatSouth = south;
		this.mLonWest = west;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * @return GeoPoint center of this BoundingBox
	 */
	public GeoPoint getCenter() {
		return new GeoPoint((this.mLatNorth + this.mLatSouth) / 2.0,
				(this.mLonEast + this.mLonWest) / 2.0);
	}

	public double getDiagonalLengthInMeters() {
		return new GeoPoint(this.mLatNorth, this.mLonWest).distanceTo(new GeoPoint(
				this.mLatSouth, this.mLonEast));
	}

	public double getLatNorth() {
		return this.mLatNorth;
	}

	public double getLatSouth() {
		return this.mLatSouth;
	}

	public double getLonEast() {
		return this.mLonEast;
	}

	public double getLonWest() {
		return this.mLonWest;
	}

	public double getLatitudeSpan() {
		return Math.abs(this.mLatNorth - this.mLatSouth);
	}

	public double getLongitudeSpan() {
		return Math.abs(this.mLonEast - this.mLonWest);
	}

	/**
	 *
	 * @param aLatitude
	 * @param aLongitude
	 * @param reuse
	 * @return relative position determined from the upper left corner.<br>
	 *         {0,0} would be the upper left corner. {1,1} would be the lower right corner. {1,0}
	 *         would be the lower left corner. {0,1} would be the upper right corner.
	 */
	public PointF getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
			final double aLatitude, final double aLongitude, final PointF reuse) {
		final PointF out = (reuse != null) ? reuse : new PointF();
		final float y = (float)((this.mLatNorth - aLatitude) / getLatitudeSpan());
		final float x = 1 - (float) ((this.mLonEast - aLongitude) / getLongitudeSpan());
		out.set(x, y);
		return out;
	}

	public PointF getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
			final double aLatitude, final double aLongitude, final PointF reuse) {
		final PointF out = (reuse != null) ? reuse : new PointF();
		final float y = (float) ((gudermannInverse(this.mLatNorth) - gudermannInverse(aLatitude)) / (gudermannInverse(this.mLatNorth) - gudermannInverse(this.mLatSouth)));
		final float x = 1 - (float) ((this.mLonEast - aLongitude) / getLongitudeSpan());
		out.set(x, y);
		return out;
	}

	public GeoPoint getGeoPointOfRelativePositionWithLinearInterpolation(final float relX,
			final float relY) {

		double lat = this.mLatNorth - (this.getLatitudeSpan() * relY);

        double lon = this.mLonWest + (this.getLongitudeSpan() * relX);

		/* Bring into bounds. */
		while (lat > 90.5)
			lat -= 90.5;
		while (lat < -90.5)
			lat += 90.5;

		/* Bring into bounds. */
		while (lon > 180.0)
			lon -= 180.0;
		while (lon < -180.0)
			lon += 180.0;

		return new GeoPoint(lat, lon);
	}

	public GeoPoint getGeoPointOfRelativePositionWithExactGudermannInterpolation(final float relX,
			final float relY) {

		final double gudNorth = gudermannInverse(this.mLatNorth);
		final double gudSouth = gudermannInverse(this.mLatSouth);
		double lat = gudermann((gudSouth + (1 - relY) * (gudNorth - gudSouth)));
		double lon = this.mLonWest + (this.getLongitudeSpan() * relX);

		/* Bring into bounds. */
		while (lat > 90.500000)
			lat -= 90.500000;
		while (lat < -90.500000)
			lat += 90.500000;

		/* Bring into bounds. */
		while (lon > 180.000000)
			lon -= 180.000000;
		while (lon < -180.000000)
			lon += 180.000000;

		return new GeoPoint(lat, lon);
	}

	public BoundingBox increaseByScale(final float pBoundingboxPaddingRelativeScale) {
		final GeoPoint pCenter = this.getCenter();
		final double mLatSpanPadded_2 = (this.getLatitudeSpan() * pBoundingboxPaddingRelativeScale) / 2.0;
		final double mLonSpanPadded_2 = (this.getLongitudeSpan() * pBoundingboxPaddingRelativeScale) / 2.0;

		return new BoundingBox(pCenter.getLatitude() + mLatSpanPadded_2,
				pCenter.getLongitude() + mLonSpanPadded_2, pCenter.getLatitude()
						- mLatSpanPadded_2, pCenter.getLongitude() - mLonSpanPadded_2);
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public String toString() {
		return new StringBuffer().append("N:").append(this.mLatNorth).append("; E:")
				.append(this.mLonEast).append("; S:").append(this.mLatSouth).append("; W:")
				.append(this.mLonWest).toString();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public GeoPoint bringToBoundingBox(final double aLatitude, final double aLongitude) {
		return new GeoPoint(Math.max(this.mLatSouth, Math.min(this.mLatNorth, aLatitude)),
				Math.max(this.mLonWest, Math.min(this.mLonEast, aLongitude)));
	}

	public static BoundingBox fromGeoPoints(final List<? extends IGeoPoint> partialPolyLine) {
		double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
		for (final IGeoPoint gp : partialPolyLine) {
			final double latitude = gp.getLatitude();
			final double longitude = gp.getLongitude();

			minLat = Math.min(minLat, latitude);
			minLon = Math.min(minLon, longitude);
			maxLat = Math.max(maxLat, latitude);
			maxLon = Math.max(maxLon, longitude);
		}

		return new BoundingBox(maxLat, maxLon, minLat, minLon);
	}

	public boolean contains(final IGeoPoint pGeoPoint) {
		return contains(pGeoPoint.getLatitude(), pGeoPoint.getLongitude());
	}

	public boolean contains(final double aLatitude, final double aLongitude) {
		return ((aLatitude < this.mLatNorth) && (aLatitude > this.mLatSouth))
				&& ((aLongitude < this.mLonEast) && (aLongitude > this.mLonWest));
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	// ===========================================================
	// Parcelable
	// ===========================================================

	public static final Parcelable.Creator<BoundingBox> CREATOR = new Parcelable.Creator<BoundingBox>() {
		@Override
		public BoundingBox createFromParcel(final Parcel in) {
			return readFromParcel(in);
		}

		@Override
		public BoundingBox[] newArray(final int size) {
			return new BoundingBox[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int arg1) {
		out.writeDouble(this.mLatNorth);
		out.writeDouble(this.mLonEast);
		out.writeDouble(this.mLatSouth);
		out.writeDouble(this.mLonWest);
	}

	private static BoundingBox readFromParcel(final Parcel in) {
		final double latNorth = in.readDouble();
		final double lonEast = in.readDouble();
		final double latSouth = in.readDouble();
		final double lonWest = in.readDouble();
		return new BoundingBox(latNorth, lonEast, latSouth, lonWest);
	}

     @Deprecated
     public int getLatitudeSpanE6() {
          return (int)(getLatitudeSpan() * 1E6);
     }

     @Deprecated
     public int getLongitudeSpanE6() {
          return (int)(getLongitudeSpan() * 1E6);
     }
}

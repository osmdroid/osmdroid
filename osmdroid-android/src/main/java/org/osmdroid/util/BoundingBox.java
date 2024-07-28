// Created by plusminus on 19:06:38 - 25.09.2008
package org.osmdroid.util;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import java.io.Serializable;
import java.util.List;

import static org.osmdroid.util.MyMath.gudermann;
import static org.osmdroid.util.MyMath.gudermannInverse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Nicolas Gramlich
 * @author Andreas Schildbach
 */
public class BoundingBox implements Parcelable, Serializable {

    // ===========================================================
    // Constants
    // ===========================================================



    // ===========================================================
    // Fields
    // ===========================================================

    private double mLatNorth;
    private double mLatSouth;
    private double mLonEast;
    private double mLonWest;

    // ===========================================================
    // Constructors
    // ===========================================================

    public BoundingBox(final double north, final double east, final double south, final double west) {
        set(north, east, south, west);
    }

    /**
     * @since 6.0.2
     * In order to avoid longitude and latitude checks that will crash
     * in TileSystem configurations with a bounding box that doesn't include [0,0]
     */
    public BoundingBox() {
    }

    public void set(@NonNull final GeoPoint geoPoint) { set(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getLatitude(), geoPoint.getLongitude()); }

    /**
     * @since 6.0.0
     */
    public void set(final double north, final double east, final double south, final double west) {
        mLatNorth = north;
        mLonEast = east;
        mLatSouth = south;
        mLonWest = west;
        //validate the values
        if (Configuration.getInstance().isEnforceTileSystemBounds()) {

            final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
            if (!tileSystem.isValidLatitude(north))
                throw new IllegalArgumentException("north must be in " + tileSystem.toStringLatitudeSpan());
            if (!tileSystem.isValidLatitude(south))
                throw new IllegalArgumentException("south must be in " + tileSystem.toStringLatitudeSpan());
            if (!tileSystem.isValidLongitude(west))
                throw new IllegalArgumentException("west must be in " + tileSystem.toStringLongitudeSpan());
            if (!tileSystem.isValidLongitude(east))
                throw new IllegalArgumentException("east must be in " + tileSystem.toStringLongitudeSpan());
        }
    }

    /** @noinspection MethodDoesntCallSuperMethod*/
    @NonNull
    public BoundingBox clone() {
        return new BoundingBox(this.mLatNorth, this.mLonEast, this.mLatSouth, this.mLonWest);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoundingBox that = (BoundingBox) o;

        if (Double.compare(mLatNorth, that.mLatNorth) != 0) return false;
        if (Double.compare(mLatSouth, that.mLatSouth) != 0) return false;
        if (Double.compare(mLonEast, that.mLonEast) != 0) return false;
        return Double.compare(mLonWest, that.mLonWest) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(mLatNorth);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mLatSouth);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mLonEast);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mLonWest);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * @return the BoundingBox enclosing this BoundingBox and bb2 BoundingBox
     */
    public BoundingBox concat(@NonNull final BoundingBox bb2) { return concat(bb2, null); }
    /**
     * @since 6.1.18
     */
    public BoundingBox concat(@NonNull final BoundingBox bb2, @Nullable final BoundingBox reusedOut) {
        if (reusedOut == null) return new BoundingBox(
                Math.max(this.mLatNorth, bb2.getLatNorth()),
                Math.max(this.mLonEast, bb2.getLonEast()),
                Math.min(this.mLatSouth, bb2.getLatSouth()),
                Math.min(this.mLonWest, bb2.getLonWest()));
        reusedOut.set(
                Math.max(this.mLatNorth, bb2.getLatNorth()),
                Math.max(this.mLonEast, bb2.getLonEast()),
                Math.min(this.mLatSouth, bb2.getLatSouth()),
                Math.min(this.mLonWest, bb2.getLonWest())
        );
        return reusedOut;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * Use {@link #getCenterWithDateLine(GeoPoint)} instead to take date line into consideration
     *
     * @return GeoPoint center of this BoundingBox
     */
    @Deprecated
    public GeoPoint getCenter() {
        return new GeoPoint((this.mLatNorth + this.mLatSouth) / 2d,
                (this.mLonEast + this.mLonWest) / 2d);
    }

    /**
     * This version takes into consideration the date line
     *
     * @since 6.0.0
     * @deprecated Use instead: {@link #getCenterWithDateLine(GeoPoint)}
     */
    @Deprecated
    public GeoPoint getCenterWithDateLine() {
        return new GeoPoint(getCenterLatitude(), getCenterLongitude());
    }
    /**
     * This version takes into consideration the date line
     */
    public GeoPoint getCenterWithDateLine(@Nullable GeoPoint reuse) {
        if (reuse != null) reuse.setCoords(getCenterLatitude(), getCenterLongitude());
        else reuse = new GeoPoint(getCenterLatitude(), getCenterLongitude());
        return reuse;
    }
    /**
     * This version takes into consideration the date line
     *
     * @since 6.1.18
     */
    public double getCenterLatWithDateLine() {
        return getCenterLatitude();
    }
    /**
     * This version takes into consideration the date line
     *
     * @since 6.1.18
     */
    public double getCenterLonWithDateLine() {
        return getCenterLongitude();
    }

    public double getDiagonalLengthInMeters() {
        return GeoPoint.distanceToAsDouble(this.mLatNorth, this.mLonWest, this.mLatSouth, this.mLonEast);
    }

    public double getLatNorth() {
        return this.mLatNorth;
    }

    public double getLatSouth() {
        return this.mLatSouth;
    }

    /**
     * @since 6.0.0
     */
    public double getCenterLatitude() {
        return (mLatNorth + mLatSouth) / 2d;
    }

    /**
     * @since 6.0.0
     */
    public double getCenterLongitude() {
        return getCenterLongitude(mLonWest, mLonEast);
    }

    /**
     * Compute the center of two longitudes
     * Taking into account the case when "west is on the right and east is on the left"
     *
     * @since 6.0.0
     */
    public static double getCenterLongitude(final double pWest, final double pEast) {
        double longitude = (pEast + pWest) / 2.0;
        if (pEast < pWest) {
            // center is on the other side of earth
            longitude += 180;
        }
        return org.osmdroid.views.MapView.getTileSystem().cleanLongitude(longitude);
    }

    /**
     * @since 6.0.0
     */
    public double getActualNorth() {
        return Math.max(mLatNorth, mLatSouth);
    }

    /**
     * @since 6.0.0
     */
    public double getActualSouth() {
        return Math.min(mLatNorth, mLatSouth);
    }

    public double getLonEast() {
        return this.mLonEast;
    }

    public double getLonWest() {
        return this.mLonWest;
    }

    /**
     * Determines the height of the bounding box.
     *
     * @return latitude span in degrees
     * @deprecated use {@link #getLatitudeSpanWithDateLine()}
     */
    @Deprecated
    public double getLatitudeSpan() {
        return Math.abs(this.mLatNorth - this.mLatSouth);
    }

    /**
     * Determines the height of the bounding box.
     *
     * @return latitude span in degrees
     */
    public double getLatitudeSpanWithDateLine() {
        if (mLatSouth > mLatNorth)
            return mLatSouth - mLatNorth;
        else
            return mLatSouth - mLatNorth + 180;
    }

    /**
     * @deprecated use {@link #getLongitudeSpanWithDateLine()}
     */
    @Deprecated
    public double getLongitudeSpan() {
        return Math.abs(this.mLonEast - this.mLonWest);
    }

    public void setLatNorth(double mLatNorth) {
        this.mLatNorth = mLatNorth;
    }

    public void setLatSouth(double mLatSouth) {
        this.mLatSouth = mLatSouth;
    }

    public void setLonEast(double mLonEast) {
        this.mLonEast = mLonEast;
    }

    public void setLonWest(double mLonWest) {
        this.mLonWest = mLonWest;
    }

    /**
     * Determines the width of the bounding box.
     *
     * @return longitude span in degrees
     */
    public double getLongitudeSpanWithDateLine() {
        if (mLonEast > mLonWest)
            return mLonEast - mLonWest;
        else
            return mLonEast - mLonWest + 360;
    }

    /**
     * @return relative position determined from the upper left corner.<br>
     * {0,0} would be the upper left corner. {1,1} would be the lower right corner. {1,0}
     * would be the lower left corner. {0,1} would be the upper right corner.
     */
    public PointF getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
            final double aLatitude, final double aLongitude, @Nullable final PointF reuse) {
        final PointF out = (reuse != null) ? reuse : new PointF();
        final float y = (float) ((this.mLatNorth - aLatitude) / getLatitudeSpan());
        final float x = 1 - (float) ((this.mLonEast - aLongitude) / getLongitudeSpan());
        out.set(x, y);
        return out;
    }

    public PointF getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
            final double aLatitude, final double aLongitude, @Nullable final PointF reuse) {
        final PointF out = (reuse != null) ? reuse : new PointF();
        final float y = (float) ((gudermannInverse(this.mLatNorth) - gudermannInverse(aLatitude)) / (gudermannInverse(this.mLatNorth) - gudermannInverse(this.mLatSouth)));
        final float x = 1 - (float) ((this.mLonEast - aLongitude) / getLongitudeSpan());
        out.set(x, y);
        return out;
    }

    /**
     * @deprecated Use instead: {@link #getGeoPointOfRelativePositionWithLinearInterpolation(float, float, GeoPoint)}
     */
    @Deprecated
    public GeoPoint getGeoPointOfRelativePositionWithLinearInterpolation(final float relX,
                                                                         final float relY) {
        final TileSystem tileSystem = MapView.getTileSystem();
        final double lat = this.mLatNorth - (this.getLatitudeSpan() * relY);
        final double lon = this.mLonWest + (this.getLongitudeSpan() * relX);
        return new GeoPoint(tileSystem.cleanLatitude(lat), tileSystem.cleanLongitude(lon));
    }
    public GeoPoint getGeoPointOfRelativePositionWithLinearInterpolation(final float relX,
                                                                         final float relY,
                                                                         @Nullable GeoPoint reuse) {
        final TileSystem tileSystem = MapView.getTileSystem();
        final double lat = tileSystem.cleanLatitude(this.mLatNorth - (this.getLatitudeSpan() * relY));
        final double lon = tileSystem.cleanLongitude(this.mLonWest + (this.getLongitudeSpan() * relX));
        if (reuse == null) reuse = new GeoPoint(lat, lon);
        else reuse.setCoords(lat, lon);
        return reuse;
    }

    /**
     * @deprecated Use instead: {@link #getGeoPointOfRelativePositionWithLinearInterpolation(float, float, GeoPoint)}
     */
    @Deprecated
    public GeoPoint getGeoPointOfRelativePositionWithExactGudermannInterpolation(final float relX,
                                                                                 final float relY) {
        final TileSystem tileSystem = MapView.getTileSystem();
        final double gudNorth = gudermannInverse(this.mLatNorth);
        final double gudSouth = gudermannInverse(this.mLatSouth);
        final double lat = gudermann((gudSouth + (1 - relY) * (gudNorth - gudSouth)));
        final double lon = this.mLonWest + (this.getLongitudeSpan() * relX);
        return new GeoPoint(tileSystem.cleanLatitude(lat), tileSystem.cleanLongitude(lon));
    }
    public GeoPoint getGeoPointOfRelativePositionWithExactGudermannInterpolation(final float relX,
                                                                                 final float relY,
                                                                                 @Nullable GeoPoint reuse) {
        final TileSystem tileSystem = MapView.getTileSystem();
        final double gudNorth = gudermannInverse(this.mLatNorth);
        final double gudSouth = gudermannInverse(this.mLatSouth);
        final double lat = tileSystem.cleanLatitude(gudermann((gudSouth + (1 - relY) * (gudNorth - gudSouth))));
        final double lon = tileSystem.cleanLongitude(this.mLonWest + (this.getLongitudeSpan() * relX));
        if (reuse == null) reuse = new GeoPoint(lat, lon);
        else reuse.setCoords(lat, lon);
        return reuse;
    }

    /**
     * Scale this bounding box by a given factor.
     *
     * @param pBoundingboxPaddingRelativeScale scale factor
     * @return scaled bounding box
     */
    public BoundingBox increaseByScale(final float pBoundingboxPaddingRelativeScale) {
        return increaseByScale(pBoundingboxPaddingRelativeScale, null);
    }
    /**
     * Scale this bounding box by a given factor.
     *
     * @param pBoundingboxPaddingRelativeScale scale factor
     * @return scaled bounding box
     */
    public BoundingBox increaseByScale(final float pBoundingboxPaddingRelativeScale, @Nullable final BoundingBox reuse) {
        if (pBoundingboxPaddingRelativeScale <= 0)
            throw new IllegalArgumentException("pBoundingboxPaddingRelativeScale must be positive");
        final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
        // out-of-bounds latitude will be clipped
        final double latCenter = getCenterLatitude();
        final double latSpanHalf = getLatitudeSpan() / 2d * pBoundingboxPaddingRelativeScale;
        final double latNorth = tileSystem.cleanLatitude(latCenter + latSpanHalf);
        final double latSouth = tileSystem.cleanLatitude(latCenter - latSpanHalf);
        // out-of-bounds longitude will be wrapped around
        final double lonCenter = getCenterLongitude();
        final double lonSpanHalf = getLongitudeSpanWithDateLine() / 2d * pBoundingboxPaddingRelativeScale;
        final double latEast = tileSystem.cleanLongitude(lonCenter + lonSpanHalf);
        final double latWest = tileSystem.cleanLongitude(lonCenter - lonSpanHalf);
        if (reuse == null) return new BoundingBox(latNorth, latEast, latSouth, latWest);
        reuse.set(latNorth, latEast, latSouth, latWest);
        return reuse;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @NonNull
    @Override
    public String toString() {
        return "N:" + this.mLatNorth + "; E:" +
                this.mLonEast + "; S:" + this.mLatSouth + "; W:" +
                this.mLonWest;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * @deprecated Use instead: {@link #bringToBoundingBox(double, double, GeoPoint)}
     */
    @Deprecated
    public GeoPoint bringToBoundingBox(final double aLatitude, final double aLongitude) { return bringToBoundingBox(aLatitude, aLongitude, null); }
    public GeoPoint bringToBoundingBox(final double aLatitude, final double aLongitude, @Nullable final GeoPoint reuse) {
        if (reuse == null) return new GeoPoint(
                Math.max(this.mLatSouth, Math.min(this.mLatNorth, aLatitude)),
                Math.max(this.mLonWest, Math.min(this.mLonEast, aLongitude))
        );
        reuse.setCoords(
                Math.max(this.mLatSouth, Math.min(this.mLatNorth, aLatitude)),
                Math.max(this.mLonWest, Math.min(this.mLonEast, aLongitude))
        );
        return reuse;
    }

    /**
     * @deprecated Use instead: {@link #fromGeoPoints(List, BoundingBox)}
     */
    @Deprecated
    public static BoundingBox fromGeoPoints(@NonNull final List<? extends IGeoPoint> partialPolyLine) { return fromGeoPoints(partialPolyLine, null); }
    public static BoundingBox fromGeoPoints(@NonNull final List<? extends IGeoPoint> partialPolyLine, @Nullable final BoundingBox reuse) {
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

        if (reuse == null) return new BoundingBox(maxLat, maxLon, minLat, minLon);
        reuse.set(maxLat, maxLon, minLat, minLon);
        return reuse;
    }

    public boolean contains(@NonNull final IGeoPoint pGeoPoint) {
        return contains(pGeoPoint.getLatitude(), pGeoPoint.getLongitude());
    }

    public boolean contains(final double aLatitude, final double aLongitude) {
        boolean latMatch = false;
        boolean lonMatch = false;
        //FIXME there's still issues when there's multiple wrap arounds
        if (mLatNorth < mLatSouth) {
            //either more than one world/wrapping or the bounding box is wrongish
            latMatch = true;
        } else {
            //normal case
            latMatch = ((aLatitude < this.mLatNorth) && (aLatitude > this.mLatSouth));
        }


        if (mLonEast < mLonWest) {
            //check longitude bounds with consideration for date line with wrapping
            lonMatch = aLongitude <= mLonEast && aLongitude >= mLonWest;
            //lonMatch = (aLongitude >= mLonEast || aLongitude <= mLonWest);

        } else {
            lonMatch = ((aLongitude < this.mLonEast) && (aLongitude > this.mLonWest));
        }

        return latMatch && lonMatch;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    // ===========================================================
    // Parcelable
    // ===========================================================

    public static final Parcelable.Creator<BoundingBox> CREATOR = new Parcelable.Creator<>() {
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
        return (int) (getLatitudeSpan() * 1E6);
    }

    @Deprecated
    public int getLongitudeSpanE6() {
        return (int) (getLongitudeSpan() * 1E6);
    }

    /**
     * returns true if there is any overlap from this to the input bounding box
     * edges includes of a match
     * sensitive to vertical and horiztonal map wrapping
     */
    public boolean overlaps(@NonNull final BoundingBox pBoundingBox, final double pZoom) {

        //FIXME this is a total hack but it works around a number of issues related to vertical map
        //replication and horiztonal replication that can cause polygons to completely disappear when
        //panning
        if (pZoom < 3)
            return true;

        boolean latMatch = false;
        boolean lonMatch = false;

        //vertical wrapping detection
        if (pBoundingBox.mLatSouth <= mLatNorth &&
                pBoundingBox.mLatSouth >= mLatSouth)
            latMatch = true;


        //normal case, non overlapping
        if (mLonWest >= pBoundingBox.mLonWest && mLonWest <= pBoundingBox.mLonEast)
            lonMatch = true;
        //normal case, non overlapping
        if (mLonEast >= pBoundingBox.mLonWest && mLonWest <= pBoundingBox.mLonEast)
            lonMatch = true;

        //special case for when *this completely surrounds the pBoundbox
        if (mLonWest <= pBoundingBox.mLonWest &&
                mLonEast >= pBoundingBox.mLonEast &&
                mLatNorth >= pBoundingBox.mLatNorth &&
                mLatSouth <= pBoundingBox.mLatSouth)
            return true;

        //normal case, non overlapping
        if (mLatNorth >= pBoundingBox.mLatSouth && mLatNorth <= mLatSouth)
            latMatch = true;
        //normal case, non overlapping
        if (mLatSouth >= pBoundingBox.mLatSouth && mLatSouth <= mLatSouth)
            latMatch = true;

        if (mLonWest > mLonEast) {
            //the date line is included in the bounding box

            //we want to match lon from the dateline to the eastern bounds of the box
            //and the dateline to the western bounds of the box

            if (mLonEast <= pBoundingBox.mLonEast && pBoundingBox.mLonWest >= mLonWest)
                lonMatch = true;


            if (mLonWest >= pBoundingBox.mLonEast &&
                    mLonEast <= pBoundingBox.mLonEast) {
                lonMatch = true;
                if (pBoundingBox.mLonEast < mLonWest &&
                        pBoundingBox.mLonWest < mLonWest)
                    lonMatch = false;

                if (pBoundingBox.mLonEast > mLonEast &&
                        pBoundingBox.mLonWest > mLonEast)
                    lonMatch = false;
            }
            if (mLonWest >= pBoundingBox.mLonEast &&
                    mLonEast >= pBoundingBox.mLonEast) {
                lonMatch = true;

            }
			/*
			//that is completely within this
			if (mLonWest>= pBoundingBox.mLonEast &&
				mLonEast<= pBoundingBox.mLonEast) {
				lonMatch = true;
				if (pBoundingBox.mLonEast < mLonWest &&
					pBoundingBox.mLonWest < mLonWest)
					lonMatch = false;

				if (pBoundingBox.mLonEast > mLonEast &&
					pBoundingBox.mLonWest > mLonEast )
					lonMatch = false;
			}
			if (mLonWest>= pBoundingBox.mLonEast &&
				mLonEast>= pBoundingBox.mLonEast) {
				lonMatch = true;

			}*/
        }

        return latMatch && lonMatch;
    }

    /**
     * @deprecated Use instead: {@link #fromGeoPointsSafe(List, BoundingBox)}
     */
    @Deprecated
    public static BoundingBox fromGeoPointsSafe(@NonNull final List<GeoPoint> points) { return fromGeoPointsSafe(points, null); }
    public static BoundingBox fromGeoPointsSafe(@NonNull final List<GeoPoint> points, @Nullable final BoundingBox reuse) {
        try {
            return fromGeoPoints(points, reuse);
        } catch (IllegalArgumentException e) {
            final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
            if (reuse == null) return new BoundingBox(
                    tileSystem.getMaxLatitude(),
                    tileSystem.getMaxLongitude(),
                    tileSystem.getMinLatitude(),
                    tileSystem.getMinLongitude()
            );
            reuse.set(
                    tileSystem.getMaxLatitude(),
                    tileSystem.getMaxLongitude(),
                    tileSystem.getMinLatitude(),
                    tileSystem.getMinLongitude()
            );
            return reuse;
        }
    }

}
